package org.solyton.solawi.bid.application.environment

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.testApplication
import org.evoleq.exposedx.data.Database as DatabaseSettings
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.application.data.db.migrations.applicationMigrations
import org.solyton.solawi.bid.application.pipeline.installDatabase
import org.solyton.solawi.bid.application.pipeline.installUsers
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.repository.createLifecycleStage
import org.solyton.solawi.bid.module.application.repository.createModule
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.authentication.environment.JWT
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.solyton.solawi.bid.module.permission.repository.createChild
import org.solyton.solawi.bid.module.permission.repository.createRootContext
import org.solyton.solawi.bid.module.permission.repository.createRight
import org.solyton.solawi.bid.module.permission.repository.createRole
import org.solyton.solawi.bid.module.permission.repository.grant
import org.solyton.solawi.bid.module.permission.repository.of
import org.solyton.solawi.bid.module.permission.repository.path
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.Users
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformOwnerBootstrapTests {

    private fun configuredEnvironment(url: String, ownerUsername: String) = Environment(
        name = Environment.Name.TEST,
        database = DatabaseSettings(url, "org.h2.Driver", "root", ""),
        jwt = JWT("test.local", "test", "test", "test-secret"),
        applicationOwner = User(ownerUsername, "bootstrap-password-hash"),
        mailService = MailService(Smtp("localhost", 25, false, "", "", false), null)
    )

    private fun setup(database: Database) = transaction(database) {
        SchemaUtils.create(
            ContextsTable, RolesTable, RightsTable, RoleRightContexts, UserRoleContext,
            Users, LifecycleStagesTable, ApplicationsTable, ModulesTable,
            UserApplicationsTable, UserModulesTable
        )
        val root = createRootContext("APPLICATION")
        val management = root.createChild("MANAGEMENT")
        val moduleNames = listOf(
            "TECHNICAL_APPLICATION_MANAGEMENT",
            "ORGANIZATION_MANAGEMENT",
            "APPLICATION_USER_MANAGEMENT",
            "FINANCIAL_TOOLING"
        )
        val moduleContexts = moduleNames.associateWith { management.createChild(it) }
        createRootContext("CUSTOMER_ORGANIZATION")

        createRole("OWNER", "Owner", UUID_ZERO)
        createRole("USER", "User", UUID_ZERO)
        val right = createRight("UPDATE_USER", "Update platform users", UUID_ZERO)
        val ownerRole = RoleEntity.find { RolesTable.name eq "OWNER" }.first()
        val managementUsers = moduleContexts.getValue("ORGANIZATION_MANAGEMENT")
        (ownerRole of managementUsers).grant(right)

        createLifecycleStage("REGISTERED", "Registered", UUID_ZERO)
        createLifecycleStage("ACTIVE", "Active", UUID_ZERO)
        val application = createApplication(
            "APPLICATION_MANAGEMENT", "Platform management", UUID_ZERO,
            defaultContextId = management.id.value
        )
        moduleNames.forEach { name ->
            createModule(name, "Management module", application.id.value, UUID_ZERO,
                defaultContextId = moduleContexts.getValue(name).id.value)
        }
    }

    private fun db(): Pair<Database, String> {
        val url = "jdbc:h2:mem:platform_owner_${UUID.randomUUID()};DB_CLOSE_DELAY=-1"
        return Database.connect(url, driver = "org.h2.Driver", user = "root", password = "") to url
    }

    @DbFunctional @Test
    fun freshApplicationMigrationsProvisionOwnerIdempotentlyWithoutCustomerPrivileges() = testApplication {
        val (_, url) = db()
        val env = configuredEnvironment(url, "fresh-migrated-platform-owner@solyton.test")
        lateinit var database: Database

        environment {
            config = HoconApplicationConfig(ConfigFactory.parseString("ktor.application.modules = []"))
        }
        application {
            database = installDatabase(env, applicationMigrations)
            installUsers(env, database)
            transaction(database) {
                createRootContext("CUSTOMER_ORGANIZATION")
            }
            installUsers(env, database)
        }
        startApplication()

        transaction(database) {
            val owner = UserEntity.find { UsersTable.username eq env.applicationOwner.username }.single()
            val ownerRole = RoleEntity.find { RolesTable.name eq "OWNER" }.single()
            val customer = ContextEntity.find { ContextsTable.name eq "CUSTOMER_ORGANIZATION" }.single()
            val expectedPaths = listOf(
                "APPLICATION",
                "APPLICATION/MANAGEMENT",
                "APPLICATION/MANAGEMENT/TECHNICAL_APPLICATION_MANAGEMENT",
                "APPLICATION/MANAGEMENT/ORGANIZATION_MANAGEMENT",
                "APPLICATION/MANAGEMENT/APPLICATION_USER_MANAGEMENT",
                "APPLICATION/MANAGEMENT/FINANCIAL_TOOLING"
            )
            val expectedContexts = expectedPaths.map { expectedPath ->
                ContextEntity.all().single { it.path() == expectedPath }
            }

            assertTrue(expectedContexts.all { context ->
                UserRoleContext.selectAll().where {
                    (UserRoleContext.userId eq owner.id.value) and
                        (UserRoleContext.roleId eq ownerRole.id) and
                        (UserRoleContext.contextId eq context.id)
                }.any()
            })
            assertFalse(UserRoleContext.selectAll().where {
                (UserRoleContext.userId eq owner.id.value) and
                    (UserRoleContext.contextId eq customer.id)
            }.any())

            val application = ApplicationEntity.find {
                ApplicationsTable.name eq "APPLICATION_MANAGEMENT"
            }.single()
            assertEquals("MANAGEMENT", application.defaultContext.name)
            assertEquals(1, UserApplicationEntity.find {
                (UserApplicationsTable.applicationId eq application.id) and
                    (UserApplicationsTable.userId eq owner.id.value)
            }.count())
            assertEquals(4, UserModuleEntity.find { UserModulesTable.userId eq owner.id.value }.count())
        }
    }

    @DbFunctional @Test
    fun freshOwnerReceivesPlatformRolesAndApplicationEnrollmentIdempotently() {
        val (db, url) = db()
        setup(db)
        val env = configuredEnvironment(url, "configured-platform-owner@solyton.test")

        env.injectUsers(db)
        val initialCount = transaction(db) { UserRoleContext.selectAll().count() }
        env.injectUsers(db)

        transaction(db) {
            val owner = UserEntity.find { UsersTable.username eq env.applicationOwner.username }.single()
            val platformUsers = ContextEntity.find { ContextsTable.name eq "ORGANIZATION_MANAGEMENT" }.single()
            val customer = ContextEntity.find { ContextsTable.name eq "CUSTOMER_ORGANIZATION" }.single()
            val management = ContextEntity.find { ContextsTable.name eq "MANAGEMENT" }.single()
            assertTrue(isGranted(owner.id.value, platformUsers.id.value, "UPDATE_USER"))
            assertFalse(UserRoleContext.selectAll().where {
                (UserRoleContext.userId eq owner.id.value) and
                    (UserRoleContext.contextId eq customer.id)
            }.any())
            assertEquals(initialCount, UserRoleContext.selectAll().count())
            val app = ApplicationEntity.find { ApplicationsTable.name eq "APPLICATION_MANAGEMENT" }.single()
            assertEquals(management.id, app.defaultContext.id)
            val assigned = UserApplicationEntity.find {
                (UserApplicationsTable.applicationId eq app.id) and
                    (UserApplicationsTable.userId eq owner.id.value)
            }.single()
            assertEquals("ACTIVE", assigned.lifecycleStage.name)
            val modules = UserModuleEntity.find { UserModulesTable.userId eq owner.id.value }.toList()
            assertEquals(4, modules.size)
            assertTrue(modules.all { it.lifecycleStage.name == "ACTIVE" })
        }
    }

    @DbFunctional @Test
    fun repairsEstablishedOwnerWithoutResettingCredentials() {
        val (db, url) = db()
        setup(db)
        val env = configuredEnvironment(url, "existing-platform-owner@solyton.test")
        transaction(db) {
            val owner = UserEntity.new {
                username = env.applicationOwner.username
                password = "original-password-hash"
                status = UserStatus.ACTIVE
                createdBy = UUID_ZERO
            }
            val root = ContextEntity.find { ContextsTable.name eq "APPLICATION" }.single()
            val role = RoleEntity.find { RolesTable.name eq "OWNER" }.single()
            UserRoleContext.insert {
                it[userId] = owner.id.value
                it[roleId] = role.id
                it[contextId] = root.id
            }
        }

        env.injectUsers(db)
        transaction(db) {
            val owner = UserEntity.find { UsersTable.username eq env.applicationOwner.username }.single()
            assertEquals("original-password-hash", owner.password)
            val users = ContextEntity.find { ContextsTable.name eq "ORGANIZATION_MANAGEMENT" }.single()
            assertTrue(isGranted(owner.id.value, users.id.value, "UPDATE_USER"))
        }
    }

    @DbFunctional @Test
    fun refusesToElevateExistingAccountWithOnlyMatchingUsername() {
        val (db, url) = db()
        setup(db)
        val env = configuredEnvironment(url, "ordinary-user@solyton.test")
        transaction(db) {
            UserEntity.new {
                username = env.applicationOwner.username
                password = "ordinary-user-password-hash"
                status = UserStatus.ACTIVE
                createdBy = UUID_ZERO
            }
        }

        assertFailsWith<IllegalStateException> { env.injectUsers(db) }
        transaction(db) {
            val user = UserEntity.find { UsersTable.username eq env.applicationOwner.username }.single()
            val management = ContextEntity.find { ContextsTable.name eq "ORGANIZATION_MANAGEMENT" }.single()
            assertFalse(isGranted(user.id.value, management.id.value, "UPDATE_USER"))
            assertTrue(UserApplicationEntity.find {
                UserApplicationsTable.userId eq user.id.value
            }.empty())
            assertEquals("ordinary-user-password-hash", user.password)
        }
    }
}
