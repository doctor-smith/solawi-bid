package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.repository.createUserRoleContext
import org.solyton.solawi.bid.module.application.routing.findUserId
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.RemoveMember
import org.solyton.solawi.bid.module.user.data.api.organization.UpdateMember
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import org.solyton.solawi.bid.module.usermanagement.routing.util.createOrganization
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OrganizationMembershipBoundaryTests {
    @Api @Test
    fun memberOperationsLeaveIndependentRoleAssignmentsForANonmemberUnchanged() = runBlocking {
        val url = "jdbc:h2:mem:membership_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseString("database.url = \"$url\"")
                        .withFallback(ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))).resolve()
                )
            }
            val token = client.getTestToken("developer@alpha-structure.com")
            val root = client.getTestContextIdByName("APPLICATION")
            val organization = assertIs<Result.Success<ApiOrganization>>(
                client.createOrganization(CreateOrganization("Membership fixture"), token, root) {
                    assertEquals(HttpStatusCode.OK, status)
                }
            ).data
            val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
            val nonmember = transaction(database) {
                findUserId("unautorized@solyton.org").also {
                    createUserRoleContext(it, "USER", UUID.fromString(organization.contextId))
                }
            }
            fun storedRows() = transaction(database) {
                listOf(UserOrganization, UserRoleContext).associateWith { table ->
                    table.selectAll().map { row -> table.columns.map { row[it] } }.toSet()
                }
            }
            val before = storedRows()

            val update = client.patch("organizations/members/update") {
                bearerAuth(token)
                header(Header.CONTEXT, organization.contextId)
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(UpdateMember(organization.id, nonmember.toString(), emptyList())))
            }
            assertEquals(HttpStatusCode.Forbidden, update.status)
            assertEquals(before, storedRows())

            val remove = client.delete("organizations/members/remove") {
                bearerAuth(token)
                header(Header.CONTEXT, organization.contextId)
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(RemoveMember(organization.id, nonmember.toString())))
            }
            assertEquals(HttpStatusCode.OK, remove.status)
            assertEquals(before, storedRows())
        }
    }
}
