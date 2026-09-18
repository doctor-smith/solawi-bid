package org.solyton.solawi.bid.module.application.service

import org.evoleq.exposedx.test.runSimpleH2Test
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.application.repository.createApplication
import org.solyton.solawi.bid.module.application.repository.createLifecycleStage
import org.solyton.solawi.bid.module.application.repository.registerForApplication
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.permission.repository.*
import org.solyton.solawi.bid.module.permission.schema.*
import kotlin.test.assertEquals

class FixContextsTests {

    val tables by lazy {
        arrayOf<Table>(
            RightsTable,
            RolesTable,
            ContextsTable,
            RoleRightContexts,
            UserRoleContext,
            OrganizationApplicationContextsTable,
            OrganizationModuleContextsTable,
            UserApplicationsTable,
            UserModulesTable,
            ApplicationsTable,
            ModulesTable,
            LifecycleStagesTable,

        )
    }

    fun Transaction.setup() {
        // Roles and rights
        createRole("OWNER", "owner", UUID_ZERO)

        // lifecycle stages
        createLifecycleStage(
            "REGISTERED",
            "D",
            UUID_ZERO
        )
    }


    @DbFunctional@Test
    fun `should fix contexts for given application id`() = runSimpleH2Test(*tables) {
        setup()
        val userId = UUID_ZERO
        val context = createRootContext("DEFAULT_APP_CONTEXT")
        val application = createApplication(
            "APPLICATION",
            "DESCRIPTION",
            userId,
            true,
            context.id.value
        )

        registerForApplication(userId, application.id.value)

        val newRole = createRole("NEW_ROLE", "D", UUID_ZERO)
        val newRight = createRight("NEW_RIGHT", "D", UUID_ZERO)

        (newRole of context).grant(newRight)

        fixContexts(application.id.value)

        val userApplication = UserApplicationEntity.find{
            UserApplicationsTable.applicationId eq application.id.value
        }.first()

        val newRoleInTargetContext = !RoleRightContexts.selectAll().where{
            RoleRightContexts.roleId eq newRole.id.value and
            (RoleRightContexts.contextId eq userApplication.context.id.value) and
            (RoleRightContexts.rightId eq newRight.id.value)
        }.empty()


        assertEquals(true, newRoleInTargetContext)
    }

    @DbFunctional@Test
    fun `should fix target context using source context`() = runSimpleH2Test(*tables) {
        val sourceContext = createRootContext("DEFAULT_APP_CONTEXT")
        val targetContext = createRootContext("TARGET_APP_CONTEXT")

        val role1 = createRole("ROLE_1", "D", UUID_ZERO)
        val right1 = createRight("RIGHT_1", "D", UUID_ZERO)

        val role2 = createRole("ROLE_2", "D", UUID_ZERO)
        val right2 = createRight("RIGHT_2", "D", UUID_ZERO)

        val newRole = createRole("NEW_ROLE", "D", UUID_ZERO)
        val newRight = createRight("NEW_RIGHT", "D", UUID_ZERO)

        (role1 of sourceContext).grant(right1)
        (role2 of sourceContext).grant(right2)

        (role1 of targetContext).grant(right1)
        (role2 of targetContext).grant(right2)

        // Action
        // delete role1 from source context
        RoleRightContexts.deleteWhere {
            (RoleRightContexts.roleId eq role1.id.value) and
            (RoleRightContexts.contextId eq sourceContext.id.value)
        }
        // add new role to source context
        (newRole of sourceContext).grant(newRight)
        fixContext(
            sourceContext.id.value,
            targetContext.id.value
        )

        // Check
        // 1. new role is in target context
        // 2. role2 is in target context
        // 3. role1 is not in target context

        // 1. new role is in target context
        val newRoleInTargetContext = !RoleRightContexts.selectAll().where{
            RoleRightContexts.roleId eq newRole.id.value and
            (RoleRightContexts.contextId eq targetContext.id.value) and
            (RoleRightContexts.rightId eq newRight.id.value)
        }.empty()

        assertEquals(true, newRoleInTargetContext)

        // 2. role2 is in target context
        val role2InTargetContext = !RoleRightContexts.selectAll().where{
            RoleRightContexts.roleId eq role2.id.value and
            (RoleRightContexts.contextId eq targetContext.id.value) and
            (RoleRightContexts.rightId eq right2.id.value)
        }.empty()

        assertEquals(true, role2InTargetContext)

        // 3. role1 is not in target context
        val role1InTargetContext = !RoleRightContexts.selectAll().where{
            RoleRightContexts.roleId eq role1.id.value and
            (RoleRightContexts.contextId eq targetContext.id.value) and
            (RoleRightContexts.rightId eq right1.id.value)
        }.empty()

        assertEquals(false, role1InTargetContext)
    }
}
