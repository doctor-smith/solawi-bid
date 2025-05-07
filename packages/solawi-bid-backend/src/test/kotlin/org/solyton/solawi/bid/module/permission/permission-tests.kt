package org.solyton.solawi.bid.module.permission

import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


class PermissionTests {
    @DbFunctional@Test
    fun testIsGranted() = runSimpleH2Test(
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,
        RoleRightContexts,
        UserRoleContext
    ) {
        // Setup database entries
        val user = UserEntity.new {
            username = "x"
            password = "y"
        }
        val readRight = RightEntity.new {
            name = "READ"
            description = ""
        }
        val updateRight = RightEntity.new {
            name = "UPDATE"
            description = ""
        }
        val role = RoleEntity.new {
            name = "READER"
            description = ""
        }
        val context = ContextEntity.new {
            name = "APP"

        }
        RoleRightContexts.insert {
            it[roleId] = role.id
            it[contextId] = context.id
            it[rightId] = readRight.id
        }
        UserRoleContext.insert {
            it[contextId] = context.id
            it[userId] = user.id
            it[roleId] = role.id
        }

        // Test
        val success = isGranted(user.id.value, context.name, readRight.name)
        assertTrue { success }

        val failure = !isGranted(user.id.value, context.name, updateRight.name)
        assertTrue { failure }

        try {
            isGranted(user.id.value, context.name, "A")
        } catch (exception : Exception) {
            assertEquals(PermissionException.NoSuchRight("A"), exception)
        }

        try {
            isGranted(user.id.value, "C", readRight.name)
        } catch (exception : Exception) {
            assertEquals(PermissionException.NoSuchContext("C"), exception)
        }
    }
}
