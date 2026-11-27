package org.solyton.solawi.bid.module.permission

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
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.repository.createLifecycleStage
import org.solyton.solawi.bid.module.application.repository.createUserRoleContext
import org.solyton.solawi.bid.module.application.routing.findUserId
import org.solyton.solawi.bid.module.application.schema.OrganizationApplicationContextEntity
import org.solyton.solawi.bid.module.permission.data.api.PutUserRoleContext
import org.solyton.solawi.bid.module.permission.repository.*
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.usermanagement.routing.util.createOrganization
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

class OrganizationApplicationDelegationRoutingTests {
    private data class Booking(val context: UUID, val owner: UUID, val member: UUID, val outsider: UUID, val ownerRole: UUID, val managerRole: UUID)

    @Api @Test
    fun applicationOwnerDelegatesOnlyApprovedRolesToMembers() = runBlocking {
        val url = "jdbc:h2:mem:delegation_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseString("database.url = \"$url\"").withFallback(
                        ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                    ).resolve()
                )
            }
            val ownerToken = client.getTestToken("developer@alpha-structure.com")
            val memberToken = client.getTestToken("unautorized@solyton.org")
            val root = client.getTestContextIdByName("APPLICATION")
            val organization = assertIs<Result.Success<ApiOrganization>>(
                client.createOrganization(CreateOrganization("Application delegation fixture"), ownerToken, root)
            ).data
            val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
            val booking = transaction(database) {
                createBooking(UUID.fromString(organization.id))
            }

            suspend fun update(token: String, target: UUID, vararg roles: UUID) =
                client.put("permissions/user/user-role-context") {
                    bearerAuth(token)
                    header(Header.CONTEXT, root)
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(PutUserRoleContext(target.toString(), booking.context.toString(), roles.map(UUID::toString))))
                }

            fun assignments() = transaction(database) {
                UserRoleContext.selectAll().where { UserRoleContext.contextId eq booking.context }
                    .map { it[UserRoleContext.id].value to (it[UserRoleContext.userId] to it[UserRoleContext.roleId].value) }.toMap()
            }

            val initial = assignments()
            assertEquals(HttpStatusCode.Forbidden, update(memberToken, booking.member, booking.managerRole).status)
            assertEquals(HttpStatusCode.Forbidden, update(ownerToken, booking.outsider, booking.managerRole).status)
            assertEquals(HttpStatusCode.Forbidden, update(ownerToken, booking.member, booking.managerRole, booking.ownerRole).status)
            assertEquals(initial, assignments())

            assertEquals(HttpStatusCode.OK, update(ownerToken, booking.member, booking.managerRole).status)
            val granted = assignments()
            assertEquals(initial.values.toSet() + (booking.member to booking.managerRole), granted.values.toSet())
            assertEquals(HttpStatusCode.OK, update(ownerToken, booking.member, booking.managerRole).status)
            assertEquals(granted, assignments())

            assertEquals(HttpStatusCode.OK, update(ownerToken, booking.owner).status)
            assertEquals(granted, assignments())
            assertEquals(HttpStatusCode.OK, update(ownerToken, booking.member).status)
            assertEquals(initial, assignments())
        }
    }

    private fun org.jetbrains.exposed.sql.Transaction.createBooking(organization: UUID): Booking {
        val owner = findUserId("developer@alpha-structure.com")
        val member = findUserId("unautorized@solyton.org")
        val outsider = UserEntity.new {
            username = "delegation-fixture-outsider@example.test"
            status = UserStatus.PENDING
            createdBy = UUID_ZERO
        }.id.value
        UserOrganization.insert {
            it[userId] = member
            it[organizationId] = organization
        }
        val context = createRootContext("AUCTION_DELEGATION_FIXTURE")
        val application = createApplication("AUCTIONS", "Fixture", owner, defaultContextId = context.id.value)
        val stage = createLifecycleStage("ACTIVE", "Active", owner)
        OrganizationApplicationContextEntity.new {
            organizationId = organization
            this.context = context
            this.application = application
            lifecycleStage = stage
            createdBy = owner
        }
        val ownerRole = RoleEntity.find { RolesTable.name eq "OWNER" }.firstOrNull() ?: createRole("OWNER", "Fixture owner", owner)
        val managerRole = createRole("AUCTION_MANAGER", "Fixture role", owner)
        val right = createRight("CREATE_AUCTION", "Fixture right", owner)
        (managerRole of context).grant(right)
        createUserRoleContext(owner, ownerRole.name, context.id.value)
        return Booking(context.id.value, owner, member, outsider, ownerRole.id.value, managerRole.id.value)
    }
}
