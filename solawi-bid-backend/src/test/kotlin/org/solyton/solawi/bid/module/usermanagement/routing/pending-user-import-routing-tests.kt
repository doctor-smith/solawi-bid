package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserProfileEntity
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.usermanagement.routing.util.createOrganization
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Imported users without chosen credentials must remain non-login-capable.
 * Never replace missing credentials with a shared or predictable placeholder.
 */
class PendingUserImportRoutingTests {
    private fun config(url: String) = HoconApplicationConfig(
        ConfigFactory.parseString("database.url = \"$url\"")
            .withFallback(ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf")))
            .resolve()
    )

    @Api @Test
    fun importMembersCreatesPendingAccountWithoutPassword() = runBlocking {
        val url = "jdbc:h2:mem:pending_members_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment { config = config(url) }
            val token = client.getTestToken("developer@alpha-structure.com")
            val rootContextId = client.getTestContextIdByName("APPLICATION")
            val created = client.createOrganization(
                CreateOrganization("pending-members-${UUID.randomUUID()}"),
                token,
                rootContextId
            ) { assertEquals(HttpStatusCode.OK, status) }
            val organization = assertIs<Result.Success<ApiOrganization>>(created).data
            val username = "new-member-${UUID.randomUUID()}@solyton.test"

            val response = client.post("organizations/members/import") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, organization.contextId)
                setBody(Json.encodeToString(
                    ImportMembers.serializer(),
                    ImportMembers(organization.id, listOf(username))
                ))
            }
            assertEquals(HttpStatusCode.OK, response.status)

            val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
            transaction(database) {
                val user = UserEntity.find { UsersTable.username eq username }.single()
                assertEquals(UserStatus.PENDING, user.status)
                assertNull(user.password)
                val savedOrganization = OrganizationEntity.find {
                    OrganizationsTable.id eq UUID.fromString(organization.id)
                }.single()
                assertTrue(savedOrganization.members.any { it.id == user.id })
            }
        }
    }

    @Api @Test
    fun importProfilesCreatesPendingAccountWithoutPassword() = runBlocking {
        val url = "jdbc:h2:mem:pending_profiles_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment { config = config(url) }
            val token = client.getTestToken("developer@alpha-structure.com")
            val rootContextId = client.getTestContextIdByName("APPLICATION")
            val username = "new-profile-${UUID.randomUUID()}@solyton.test"
            val profile = UserProfileToImport(
                username = username,
                firstName = "New",
                lastName = "Member",
                title = null,
                phoneNumber = null,
                phoneNumber1 = null,
                address = CreateAddress(
                    recipientName = "New Member",
                    organizationName = null,
                    addressLine1 = "Teststraße 1",
                    addressLine2 = "",
                    city = "Reutlingen",
                    stateOrProvince = "BW",
                    postalCode = "72764",
                    countryCode = "DE"
                )
            )

            val response = client.post("users/profiles/import") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, rootContextId)
                setBody(Json.encodeToString(
                    ImportUserProfiles.serializer(),
                    ImportUserProfiles(listOf(profile))
                ))
            }
            assertEquals(HttpStatusCode.OK, response.status)

            val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
            transaction(database) {
                val user = UserEntity.find { UsersTable.username eq username }.single()
                assertEquals(UserStatus.PENDING, user.status)
                assertNull(user.password)
                val savedProfile = UserProfileEntity.find {
                    UserProfilesTable.userId eq user.id
                }.single()
                assertEquals("New", savedProfile.firstName)
                assertEquals("Member", savedProfile.lastName)
            }
        }
    }
}
