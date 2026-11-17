package org.solyton.solawi.bid.module.permission

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.permission.action.db.isGranted
import org.solyton.solawi.bid.module.permission.action.db.isGrantedOneOf
import org.solyton.solawi.bid.module.permission.repository.cloneRightRoleContext
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class PermissionTests {
    @DbFunctional@Test
    fun `isGranted requires user role and right policy in the same context`() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,
    ) {
        val ts = System.currentTimeMillis()
        val user = UserEntity.new {
            username = "exact-context-$ts"
            password = "y"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val right = RightEntity.new {
            name = "EXACT_CONTEXT-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val role = RoleEntity.new {
            name = "EXACT_CONTEXT_ROLE-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val assignedContext = ContextEntity.new {
            name = "ASSIGNED_CONTEXT-$ts"
            createdBy = UUID_ZERO
        }
        val policyContext = ContextEntity.new {
            name = "POLICY_CONTEXT-$ts"
            createdBy = UUID_ZERO
        }
        UserRoleContext.insert {
            it[contextId] = assignedContext.id
            it[userId] = user.id.value
            it[roleId] = role.id
        }
        RoleRightContexts.insert {
            it[contextId] = policyContext.id
            it[rightId] = right.id
            it[roleId] = role.id
        }

        assertFalse(isGranted(user.id.value, policyContext.id.value, right.id.value))
        assertFalse(isGranted(user.id.value, policyContext.id.value, right.name))
    }

    @DbFunctional@Test
    fun `isGrantedOneOf does not grant a role from another context`() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,
    ) {
        val ts = System.currentTimeMillis()
        val user = UserEntity.new {
            username = "cross-context-single-$ts"
            password = "y"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val right = RightEntity.new {
            name = "CROSS_CONTEXT_SINGLE-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val role = RoleEntity.new {
            name = "CROSS_CONTEXT_ROLE_SINGLE-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val authorizedContext = ContextEntity.new {
            name = "AUTHORIZED_SINGLE-$ts"
            createdBy = UUID_ZERO
        }
        val unauthorizedContext = ContextEntity.new {
            name = "UNAUTHORIZED_SINGLE-$ts"
            createdBy = UUID_ZERO
        }
        UserRoleContext.insert {
            it[contextId] = authorizedContext.id
            it[userId] = user.id.value
            it[roleId] = role.id
        }
        RoleRightContexts.insert {
            it[contextId] = unauthorizedContext.id
            it[rightId] = right.id
            it[roleId] = role.id
        }

        assertFalse(isGrantedOneOf(user.id.value, unauthorizedContext.id.value, listOf(right.name)))
    }

    @DbFunctional@Test
    fun `isGrantedOneOf context list requires role assignment in the matching context`() = runSimpleH2Test(
        RoleRightContexts,
        UserRoleContext,
        UsersTable,
        ContextsTable,
        RightsTable,
        RolesTable,
    ) {
        val ts = System.currentTimeMillis()
        val user = UserEntity.new {
            username = "cross-context-list-$ts"
            password = "y"
            status = UserStatus.ACTIVE
            createdBy = UUID_ZERO
        }
        val right = RightEntity.new {
            name = "CROSS_CONTEXT_LIST-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val role = RoleEntity.new {
            name = "CROSS_CONTEXT_ROLE_LIST-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val authorizedContext = ContextEntity.new {
            name = "AUTHORIZED_LIST-$ts"
            createdBy = UUID_ZERO
        }
        val unauthorizedContext = ContextEntity.new {
            name = "UNAUTHORIZED_LIST-$ts"
            createdBy = UUID_ZERO
        }
        UserRoleContext.insert {
            it[contextId] = authorizedContext.id
            it[userId] = user.id.value
            it[roleId] = role.id
        }
        RoleRightContexts.insert {
            it[contextId] = authorizedContext.id
            it[rightId] = right.id
            it[roleId] = role.id
        }
        RoleRightContexts.insert {
            it[contextId] = unauthorizedContext.id
            it[rightId] = right.id
            it[roleId] = role.id
        }

        assertTrue(isGrantedOneOf(user.id.value, listOf(authorizedContext.id.value), listOf(right.name)))
        assertFalse(isGrantedOneOf(user.id.value, listOf(unauthorizedContext.id.value), listOf(right.name)))
    }

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
            status = UserStatus.ACTIVE
            createdBy = UUID(0L,0L)
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val context = ContextEntity.new {
            name = "APP-$ts"
            createdBy = UUID_ZERO

        }
        RoleRightContexts.insert {
            it[roleId] = role.id
            it[contextId] = context.id
            it[rightId] = readRight.id
        }
        UserRoleContext.insert {
            it[contextId] = context.id
            it[userId] = user.id.value
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
            status = UserStatus.ACTIVE
            createdBy = UUID(0L,0L)
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val context = ContextEntity.new {
            name = "APP-$ts"
            createdBy = UUID_ZERO
        }
        RoleRightContexts.insert {
            it[roleId] = role.id
            it[contextId] = context.id
            it[rightId] = readRight.id
        }
        UserRoleContext.insert {
            it[contextId] = context.id
            it[userId] = user.id.value
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
            status = UserStatus.ACTIVE
            createdBy = UUID(0L,0L)
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val context = ContextEntity.new {
            name = "APP-$ts"
            createdBy = UUID_ZERO
        }
        RoleRightContexts.insert {
            it[roleId] = role.id
            it[contextId] = context.id
            it[rightId] = readRight.id
        }
        UserRoleContext.insert {
            it[contextId] = context.id
            it[userId] = user.id.value
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
        UserEntity.new {
            username = "x-$ts"
            password = "y"
            status = UserStatus.ACTIVE
            createdBy = UUID(0L,0L)
        }
        val readRight = RightEntity.new {
            name = "READ-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val updateRight = RightEntity.new {
            name = "UPDATE-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val role = RoleEntity.new {
            name = "READER-$ts"
            description = ""
            createdBy = UUID_ZERO
        }
        val context = ContextEntity.new {
            name = "APP-$ts"
            createdBy = UUID_ZERO

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
            createdBy = UUID_ZERO
        }

        val cloned = cloneRightRoleContext(context.id.value, context1.id.value)
        assertTrue { cloned.distinctBy { it.rightId }.size == 2 }
    }
}
