package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.permission.repository.createChild
import org.solyton.solawi.bid.module.permission.repository.grant
import org.solyton.solawi.bid.module.permission.repository.of
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.organization.ApiOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.CreateOrganization
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.schema.*
import org.solyton.solawi.bid.module.usermanagement.routing.util.createOrganization
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Imported users without chosen credentials must remain non-login-capable.
 * Never replace missing credentials with a shared or predictable placeholder.
 */
class PendingUserImportRoutingTests {
    private fun userManagementConfig(url: String) = HoconApplicationConfig(
        ConfigFactory.parseString("database.url = \"$url\"")
            .withFallback(ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf")))
            .resolve()
    )

    /** Complete the deliberately small user-management test migration with the
     * root-context grants required by the production import path. */
    private fun prepareImportPermissions(url: String) {
        val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
        transaction(database) {
            val application = ContextEntity.find { ContextsTable.name eq "APPLICATION" }.single()
            val developer = RoleEntity.find { RolesTable.name eq "DEVELOPER" }.single()
            val user = RoleEntity.find { RolesTable.name eq "USER" }.single()
            val manageUsers = RightEntity.find { RightsTable.name eq "MANAGE_USERS" }.single()
            val readOrganization = RightEntity.find { RightsTable.name eq "READ_ORGANIZATION" }.single()

            application.createChild("ORGANIZATION")
            (developer of application).grant(manageUsers)
            (user of application).grant(readOrganization)
        }
    }

    @Api @Test
    fun importMembersCreatesPendingAccountWithoutPassword() = runBlocking {
        val url = "jdbc:h2:mem:pending_members_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment { config = userManagementConfig(url) }
            val token = client.getTestToken("developer@alpha-structure.com")
            prepareImportPermissions(url)
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
            assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())

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

            // A pending imported account cannot authenticate, even with a guessed password.
            val loginResponse = client.post("login") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(Json.encodeToString(
                    Login.serializer(),
                    Login(username, "not-a-valid-password")
                ))
            }
            assertEquals(HttpStatusCode.Forbidden, loginResponse.status)
        }
    }

    @Api @Test
    fun importProfilesCreatesPendingAccountWithoutPassword() = runBlocking {
        val url = "jdbc:h2:mem:pending_profiles_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment { config = userManagementConfig(url) }
            val token = client.getTestToken("developer@alpha-structure.com")
            prepareImportPermissions(url)
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
            assertEquals(HttpStatusCode.OK, response.status, response.bodyAsText())

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
