package org.solyton.solawi.bid.module.permission

import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.db.repository.cloneRightRoleContext
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


class PermissionTests {
    @DbFunctional@Test
    fun testIsGranted() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,

        ) {
        val ts = System.currentTimeMillis()
        // Setup database entries
        val user = UserEntity.new {
            username = "x-$ts"
            password = "y"
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
        }
        val context = ContextEntity.new {
            name = "APP-$ts"

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


    @DbFunctional@Test
    fun testIsGranted1() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,

        ) {
        val ts = System.currentTimeMillis()
        // Setup database entries
        val user = UserEntity.new {
            username = "x-$ts"
            password = "y"
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
        }
        val context = ContextEntity.new {
            name = "APP-$ts"

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
        val success = isGranted(user.id.value, context.id.value, readRight.name)
        assertTrue { success }

        val failure = !isGranted(user.id.value, context.id.value, updateRight.name)
        assertTrue { failure }

        try {
            isGranted(user.id.value, context.id.value, "A")
        } catch (exception : Exception) {
            assertEquals(PermissionException.NoSuchRight("A"), exception)
        }


    }

    @DbFunctional@Test
    fun testIsGranted2() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,

        ) {
        val ts = System.currentTimeMillis()
        // Setup database entries
        val user = UserEntity.new {
            username = "x-$ts"
            password = "y"
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
        }
        val context = ContextEntity.new {
            name = "APP-$ts"

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
        val success = isGranted(user.id.value, context.id.value, readRight.id.value)
        assertTrue { success }

        val failure = !isGranted(user.id.value, context.id.value, updateRight.id.value)
        assertTrue { failure }

        try {
            isGranted(user.id.value, updateRight.id.value, readRight.id.value)
        } catch (exception : Exception) {
            assertEquals(PermissionException.NoSuchContext(updateRight.id.value.toString()), exception)
        }
    }

    @DbFunctional@Test
    fun cloneRightRoleContextTest() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,
    ) {
        val ts = System.currentTimeMillis()
        // Setup database entries
        val user = UserEntity.new {
            username = "x-$ts"
            password = "y"
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
        }
        val context = ContextEntity.new {
            name = "APP-$ts"

        }
        RoleRightContexts.insert {
            it[roleId] = role.id
            it[contextId] = context.id
            it[rightId] = readRight.id
        }
        RoleRightContexts.insert {
            it[roleId] = role.id
            it[contextId] = context.id
            it[rightId] = updateRight.id
        }
        val context1 = ContextEntity.new {
            name = "APP1-$ts"

        }

        val cloned = cloneRightRoleContext(context.id.value, context1.id.value)
        assertTrue { cloned.distinctBy { it.rightId }.size == 2 }
    }
}
