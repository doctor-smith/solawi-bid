package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.ResultSerializer
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import org.solyton.solawi.bid.module.user.schema.repository.createRootOrganization
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.application.routing.findUserId
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.userprofile.*
import org.solyton.solawi.bid.module.user.repository.createUserProfile
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.UserProfileId
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProfileAuthorizationRoutingTests {
    @Api @Test
    fun ordinaryUsersCanCreateAndReadTheirOwnProfile() = runBlocking {
        val databaseUrl = isolatedDatabaseUrl()
        testApplication {
            environment { config = userManagementConfig(databaseUrl) }
            val context = client.getTestContextIdByName("APPLICATION")
            val token = client.getTestToken(PROFILE_OWNER)
            val database = connect(databaseUrl)
            val (own, other) = transaction(database) { profileFor(PROFILE_OWNER) to profileFor(OTHER_USER) }

            val before = storedRows(database)
            assertEquals(HttpStatusCode.Forbidden, client.profileRequest(HttpMethod.Post, "create", token, context, other).status)
            assertEquals(before, storedRows(database))

            val create = client.profileRequest(HttpMethod.Post, "create", token, context, own)
            assertEquals(HttpStatusCode.OK, create.status)
            assertEquals(before.getValue(UserProfilesTable).size + 1, storedRows(database).getValue(UserProfilesTable).size)
            val readOwn = client.profileRequest(HttpMethod.Patch, "read-by-ids", token, context, ReadUserProfiles(listOf(own.userId.value)))
            assertEquals(HttpStatusCode.OK, readOwn.status)
            val profiles = assertIs<Result.Success<UserProfiles>>(
                Json.decodeFromString(ResultSerializer<UserProfiles>(), readOwn.bodyAsText())
            ).data
            assertEquals(listOf(own.userId.value), profiles.all.map { it.userId })

            for (ids in listOf(emptyList(), listOf(own.userId.value, other.userId.value))) {
                val read = client.profileRequest(HttpMethod.Patch, "read-by-ids", token, context, ReadUserProfiles(ids))
                assertEquals(HttpStatusCode.Forbidden, read.status)
            }
        }
    }

    @Api @Test
    fun profileUpdateRequiresMatchingOwnerAndAddresses() = runBlocking {
        val databaseUrl = isolatedDatabaseUrl()
        testApplication {
            environment { config = userManagementConfig(databaseUrl) }
            val context = client.getTestContextIdByName("APPLICATION")
            val token = client.getTestToken(PROFILE_OWNER)
            val database = connect(databaseUrl)
            val (own, other) = transaction(database) {
                seedProfile(PROFILE_OWNER) to seedProfile(OTHER_USER)
            }
            val before = storedRows(database)

            val ownUpdate = UpdateUserProfile(
                own.profile, own.owner, Firstname("Updated"), Lastname("Fixture"), null, null, null,
                listOf(newAddress(own.address))
            )
            val denied = listOf(
                ownUpdate.copy(userProfileId = other.profile),
                ownUpdate.copy(userProfileId = other.profile, userId = other.owner),
                ownUpdate.copy(addresses = listOf(newAddress(own.address), newAddress(other.address)))
            )
            for (request in denied) {
                val outcome = client.profileRequest(HttpMethod.Patch, "update", token, context, request)
                assertEquals(HttpStatusCode.Forbidden, outcome.status)
                assertEquals(before, storedRows(database))
            }

            val accepted = client.profileRequest(HttpMethod.Patch, "update", token, context, ownUpdate)
            assertEquals(HttpStatusCode.OK, accepted.status)
            transaction(database) {
                val profile = UserProfilesTable.selectAll().where { UserProfilesTable.id eq UUID.fromString(own.profile.value) }.single()
                val address = AddressesTable.selectAll().where { AddressesTable.id eq own.address }.single()
                assertEquals("Updated", profile[UserProfilesTable.firstName])
                assertEquals("New City", address[AddressesTable.city])
            }
        }
    }

    @Api @Test
    fun organizationProfileReadsUseStoredManagementScopeAndMembership() = runBlocking {
        val databaseUrl = isolatedDatabaseUrl()
        testApplication {
            environment { config = userManagementConfig(databaseUrl) }
            val headerContext = client.getTestContextIdByName("APPLICATION")
            val manager = client.getTestToken(OTHER_USER)
            val member = client.getTestToken(PROFILE_OWNER)
            val (organization, target) = transaction(connect(databaseUrl)) {
                val org = createRootOrganization("Profile read fixture", findUserId(OTHER_USER))
                val profile = seedProfile(PROFILE_OWNER)
                UserOrganization.insert {
                    it[userId] = UUID.fromString(profile.owner.value)
                    it[organizationId] = org.id
                }
                org.id.value.toString() to profile.owner.value
            }
            val request = ReadUserProfiles(listOf(target), organization)
            val accepted = client.profileRequest(HttpMethod.Patch, "read-by-ids", manager, headerContext, request)
            assertEquals(HttpStatusCode.OK, accepted.status)
            val profiles = assertIs<Result.Success<UserProfiles>>(
                Json.decodeFromString(ResultSerializer<UserProfiles>(), accepted.bodyAsText())
            ).data
            assertEquals(listOf(target), profiles.all.map { it.userId })

            val mixed = request.copy(userIds = listOf(target, UUID.randomUUID().toString()))
            assertEquals(HttpStatusCode.Forbidden, client.profileRequest(HttpMethod.Patch, "read-by-ids", manager, headerContext, mixed).status)
            assertEquals(HttpStatusCode.Forbidden, client.profileRequest(HttpMethod.Patch, "read-by-ids", member, headerContext, request).status)
        }
    }

    private fun org.jetbrains.exposed.sql.Transaction.profileFor(username: String) = CreateUserProfile(
        UserId(findUserId(username).toString()), Firstname("Test"), Lastname("Fixture"), null, null, null, CreateAddress.empty
    )

    private fun org.jetbrains.exposed.sql.Transaction.seedProfile(username: String): ProfileFixture {
        val request = profileFor(username)
        val profile = createUserProfile(request, UUID_ZERO)
        return ProfileFixture(request.userId, UserProfileId(profile.id.value.toString()), profile.addresses.single().id.value)
    }

    private fun newAddress(id: UUID) = UpdateAddress(id.toString(), "Fixture", null, "", "", "New City", "", "", "")

    private fun storedRows(database: Database) = transaction(database) {
        listOf(UserProfilesTable, AddressesTable).associateWith { table ->
            table.selectAll().map { row -> table.columns.map { row[it] } }.toSet()
        }
    }

    private fun isolatedDatabaseUrl() = "jdbc:h2:mem:profile_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"

    private fun userManagementConfig(url: String) = HoconApplicationConfig(
        ConfigFactory.parseString("database.url = \"$url\"")
            .withFallback(ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf")))
            .resolve()
    )

    private fun connect(url: String) = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")

    private suspend inline fun <reified T> HttpClient.profileRequest(
        verb: HttpMethod,
        path: String,
        token: String,
        context: String,
        data: T
    ) = request("users/profiles/$path") {
        method = verb
        bearerAuth(token)
        header(Header.CONTEXT, context)
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToString(data))
    }

    private data class ProfileFixture(val owner: UserId, val profile: UserProfileId, val address: UUID)
}

private const val PROFILE_OWNER = "unautorized@solyton.org"
private const val OTHER_USER = "developer@alpha-structure.com"
