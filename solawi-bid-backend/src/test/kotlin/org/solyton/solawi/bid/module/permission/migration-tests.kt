package org.solyton.solawi.bid.module.permission

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.evoleq.exposedx.migrations.Migration
import org.evoleq.exposedx.migrations.runOn
import org.evoleq.exposedx.test.Config
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.permission.Context
import org.solyton.solawi.bid.application.permission.Role
import org.solyton.solawi.bid.module.bid.permission.Value
import org.solyton.solawi.bid.application.permission.Right
import org.solyton.solawi.bid.application.data.db.migrations.Migration1730143239225
import org.solyton.solawi.bid.application.data.db.migrations.Migration1743235367945
import org.solyton.solawi.bid.application.data.db.migrations.Migration1743786680319
import org.solyton.solawi.bid.module.application.permission.AppRight
import org.solyton.solawi.bid.module.user.permission.OrganizationRight
import org.solyton.solawi.bid.module.application.permission.ApplicationContext
import org.solyton.solawi.bid.module.bid.permission.AuctionContext
import org.solyton.solawi.bid.module.permission.schema.repository.parent
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.permission.schema.Contexts
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.Rights
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import org.solyton.solawi.bid.module.permission.schema.Roles
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.schema.UsersTable
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.user.permission.OrganizationContext
import org.solyton.solawi.bid.module.user.schema.UserEntity
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MigrationTests {
    val neededTables = arrayOf (
        Roles,
        Rights,
        Contexts,
        RoleRightContexts
    )

    @DbFunctional@Test
    fun contextMigrationTest() = runBlocking {
        val database = Database.connect(
            url = Config.H2NoClose.url,
            driver = Config.H2.driver,
            user = Config.H2.user,
            password = Config.H2.password
        )

        val migrations = arrayListOf<Database.()-> Migration>(
            { Migration1730143239225(database) },
            { Migration1743235367945(database) },
            { Migration1743786680319(database) }
        )

        // val r =
        suspendedTransactionAsync {
            SchemaUtils.drop(*neededTables)
            migrations.runOn(database)
         }.await()
        delay(1000)
        // val s =
        suspendedTransactionAsync {
            // Application Context
            // Exists:  APPLICATION, ORGANIZATION
            // Deleted: APPLICATION/ORGANIZATION
            val applicationContext =
                ContextEntity.find { ContextsTable.name eq Context.Application.value }.firstOrNull()
            assertNotNull(applicationContext)

            val oldApplicationOrganizationContext =
                ContextEntity.find { ContextsTable.name eq ApplicationContext.Organization.value }.firstOrNull()
            assertNull(oldApplicationOrganizationContext)

            val applicationOrganizationContext = ContextEntity.find {
                ContextsTable.name eq Value.ORGANIZATION
                ContextsTable.rootId eq applicationContext.id
            }.firstOrNull()
            assertNotNull(applicationOrganizationContext)

            assertEquals(applicationContext, applicationOrganizationContext.parent())

            // Organization Context
            // Exists:  ORGANIZATION, MANAGEMENT
            // Deleted: ORGANIZATION/MANAGEMENT
            val oldOrganizationManagementContext = ContextEntity.find{
                ContextsTable.name eq OrganizationContext.Management.value
            }.firstOrNull()
            assertNull(oldOrganizationManagementContext)
            val organizationContext = ContextEntity.find{
                ContextsTable.name eq OrganizationContext.value and
                (ContextsTable.rootId eq null)
            }.firstOrNull()
            assertNotNull(organizationContext)
            val managementContext = ContextEntity.find{
                ContextsTable.name eq Value.MANAGEMENT and
                (ContextsTable.rootId eq organizationContext.id)
            }.firstOrNull()
            assertNotNull(managementContext)

            assertEquals(organizationContext, managementContext.parent())

            // Auction Context
            // Exists:  AUCTION, MANAGEMENT
            // Deleted: AUCTION/MANAGEMENT
            val oldAuctionManagementContext = ContextEntity.find { ContextsTable.name eq AuctionContext.Management.value }.firstOrNull()
            assertNull(oldAuctionManagementContext)
            val auctionContext = ContextEntity.find { ContextsTable.name eq Context.Auction.value }.firstOrNull()
            assertNotNull(auctionContext)
            val auctionManagementContext = ContextEntity.find {
                ContextsTable.name eq Value.MANAGEMENT and
                (ContextsTable.rootId eq auctionContext.id)

            }.firstOrNull()
            assertNotNull(auctionManagementContext)

            assertEquals(auctionContext, auctionManagementContext.parent())

            // Overall Assertion
            assertEquals(6, ContextEntity.all().count())


            // inject app owner
            SchemaUtils.create(UsersTable, UserRoleContext)
            val applicationOwnerId = injectApplicationOwner("appOwner","password")

            // Assert owner Rights in context APPLICATION
            // General
            val create = RightEntity.find { RightsTable.name eq Right.create.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, create.id.value))

            val read = RightEntity.find { RightsTable.name eq Right.read.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, read.id.value))

            val update = RightEntity.find { RightsTable.name eq Right.update.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, update.id.value))

            val delete = RightEntity.find { RightsTable.name eq Right.delete.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, delete.id.value))

            // Organizations
            val createOrganization = RightEntity.find { RightsTable.name eq OrganizationRight.Organization.create.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, createOrganization.id.value))

            val readOrganization = RightEntity.find { RightsTable.name eq OrganizationRight.Organization.read.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, readOrganization.id.value))

            val updateOrganization = RightEntity.find { RightsTable.name eq OrganizationRight.Organization.update.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, updateOrganization.id.value))

            val deleteOrganization = RightEntity.find { RightsTable.name eq OrganizationRight.Organization.delete.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, deleteOrganization.id.value))

            val readRightRoleContextsOfUser = RightEntity.find { RightsTable.name eq Right.ReadRightRoleContexts.value }.first()
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, readRightRoleContextsOfUser.id.value))


            // Users
            Assertions.assertTrue(isGranted(applicationOwnerId, applicationContext.id.value, AppRight.Application.Users.manage))
        }.await()
    }
}

fun injectApplicationOwner(ownerUsername: String, ownerPassword: String):UUID {
    val applicationOwner = UserEntity.new {
        username = ownerUsername
        password = ownerPassword
    }

    val applicationContextId = ContextEntity.find {
        Contexts.name eq Context.Application.value
    }.first().id.value

    val ownerRoleId = RoleEntity.find {
        Roles.name eq Role.owner.value
    }.first().id

    val userRoleId = RoleEntity.find{
        Roles.name eq Role.user.value
    }.first().id

    UserRoleContext.insert {
        it[userId] = applicationOwner.id.value
        it[contextId] = applicationContextId
        it[roleId] = ownerRoleId
    }

    UserRoleContext.insert {
        it[userId] = applicationOwner.id.value
        it[contextId] = applicationContextId
        it[roleId] = userRoleId
    }

    return applicationOwner.id.value
}
