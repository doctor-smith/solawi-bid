package org.solyton.solawi.bid.module.usermanagement.routing

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.evoleq.ktorx.headers.Header
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mindrot.jbcrypt.BCrypt
import org.solyton.solawi.bid.Api
import org.solyton.solawi.bid.module.permission.repository.createChild
import org.solyton.solawi.bid.module.permission.repository.grant
import org.solyton.solawi.bid.module.permission.repository.of
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.testFramework.getTestContextIdByName
import org.solyton.solawi.bid.module.testFramework.getTestToken
import org.solyton.solawi.bid.module.user.data.api.ChangePassword
import org.solyton.solawi.bid.module.user.data.api.UpdateUsername
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.values.Username
import java.io.File
import java.util.UUID

class PlatformCredentialSecurityTests {
    @Api @Test
    fun platformRightsMustMatchTheExactContextForBothCredentialOperations() = runBlocking {
        val url = "jdbc:h2:mem:credential_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        testApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseString("database.url = \"$url\"").withFallback(
                        ConfigFactory.parseFile(File("src/test/resources/usermanagement.api.test.conf"))
                    ).resolve()
                )
            }
            val rootId = client.getTestContextIdByName("APPLICATION")
            val database = Database.connect(url, driver = "org.h2.Driver", user = "root", password = "")
            val (platformId, foreignId) = transaction(database) {
                val root = ContextEntity.find { ContextsTable.name eq "APPLICATION" }.first()
                val platform = root.createChild("MANAGEMENT").createChild("ORGANIZATION_MANAGEMENT")
                val foreign = root.createChild("OTHER_ORGANIZATION")
                val update = RightEntity.new { name = "UPDATE_USER"; description = "Update users"; createdBy = UUID_ZERO }
                val read = RightEntity.new { name = "READ_USER"; description = "Read users"; createdBy = UUID_ZERO }
                val admin = RoleEntity.new { name = "APPLICATION_ORGANIZATION_MANAGER"; description = "Platform admin"; createdBy = UUID_ZERO }
                val mixed = RoleEntity.new { name = "MIXED_ROLE"; description = "Other context only"; createdBy = UUID_ZERO }
                (admin of platform).grant(update)
                (mixed of platform).grant(read)
                (mixed of foreign).grant(update)
                val adminUser = UserEntity.find { UsersTable.username eq "developer@alpha-structure.com" }.first()
                val attacker = UserEntity.find { UsersTable.username eq "unautorized@solyton.org" }.first()
                UserRoleContext.insert { it[userId] = adminUser.id.value; it[roleId] = admin.id; it[contextId] = platform.id }
                UserRoleContext.insert { it[userId] = attacker.id.value; it[roleId] = mixed.id; it[contextId] = platform.id }
                UserRoleContext.insert { it[userId] = attacker.id.value; it[roleId] = mixed.id; it[contextId] = foreign.id }
                platform.id.value.toString() to foreign.id.value.toString()
            }
            val attacker = client.getTestToken("unautorized@solyton.org")
            val admin = client.getTestToken("developer@alpha-structure.com")
            val target = "unautorized@solyton.org"
            val renamed = "renamed-test-user@solyton.org"
            val adminName = "developer@alpha-structure.com"
            val originalPassword = transaction(database) {
                UserEntity.find { UsersTable.username eq adminName }.first().password
            }
            val originalTargetPassword = transaction(database) {
                UserEntity.find { UsersTable.username eq target }.first().password
            }

            for (context in listOf(platformId, foreignId, rootId)) {
                val reset = client.patch("users/change-password") {
                    header(HttpHeaders.Authorization, "Bearer $attacker")
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(Header.CONTEXT, context)
                    setBody(Json.encodeToString(ChangePassword.serializer(), ChangePassword(adminName, "blocked-password")))
                }
                assertEquals(HttpStatusCode.Forbidden, reset.status)
                val rename = client.patch("users/update-username") {
                    header(HttpHeaders.Authorization, "Bearer $attacker")
                    header(HttpHeaders.ContentType, ContentType.Application.Json)
                    header(Header.CONTEXT, context)
                    setBody(Json.encodeToString(UpdateUsername.serializer(), UpdateUsername(Username(adminName), Username(renamed))))
                }
                assertEquals(HttpStatusCode.Forbidden, rename.status)
            }
            transaction(database) {
                assertEquals(originalPassword, UserEntity.find { UsersTable.username eq adminName }.first().password)
                assertTrue(UserEntity.find { UsersTable.username eq renamed }.empty())
            }

            val weakPasswordReset = client.patch("users/change-password") {
                header(HttpHeaders.Authorization, "Bearer $admin")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, foreignId)
                setBody(Json.encodeToString(ChangePassword.serializer(), ChangePassword(target, "too-short")))
            }
            assertEquals(HttpStatusCode.BadRequest, weakPasswordReset.status)
            transaction(database) {
                assertEquals(originalTargetPassword, UserEntity.find { UsersTable.username eq target }.first().password)
            }

            val reset = client.patch("users/change-password") {
                header(HttpHeaders.Authorization, "Bearer $admin")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, foreignId)
                setBody(Json.encodeToString(ChangePassword.serializer(), ChangePassword(target, "new-test-password")))
            }
            assertEquals(HttpStatusCode.OK, reset.status)
            val rename = client.patch("users/update-username") {
                header(HttpHeaders.Authorization, "Bearer $admin")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(Header.CONTEXT, foreignId)
                setBody(Json.encodeToString(UpdateUsername.serializer(), UpdateUsername(Username(target), Username(renamed))))
            }
            assertEquals(HttpStatusCode.OK, rename.status)
            transaction(database) {
                val user = UserEntity.find { UsersTable.username eq renamed }.first()
                assertTrue(BCrypt.checkpw("new-test-password", user.password))
                assertTrue(UserEntity.find { UsersTable.username eq target }.empty())
            }
        }
    }
}
