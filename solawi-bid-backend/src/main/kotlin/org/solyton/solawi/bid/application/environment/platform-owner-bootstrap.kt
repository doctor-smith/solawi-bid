package org.solyton.solawi.bid.application.environment

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.application.repository.registerForApplication
import org.solyton.solawi.bid.module.application.repository.registerForModule
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleStagesTable
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.UserApplicationEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserModuleEntity
import org.solyton.solawi.bid.module.application.schema.UserModulesTable
import org.solyton.solawi.bid.module.permission.repository.path
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.schema.UserEntity

private const val PLATFORM_ROOT = "APPLICATION"
private const val PLATFORM_MANAGEMENT = "APPLICATION/MANAGEMENT"
private const val MANAGEMENT_APP = "APPLICATION_MANAGEMENT"

private val platformModules = setOf(
    "TECHNICAL_APPLICATION_MANAGEMENT",
    "ORGANIZATION_MANAGEMENT",
    "APPLICATION_USER_MANAGEMENT",
    "FINANCIAL_TOOLING"
)

private fun Transaction.platformContext(path: String): ContextEntity {
    val contextName = path.substringAfterLast('/')
    return ContextEntity.find { ContextsTable.name eq contextName }
        .firstOrNull { it.path() == path }
        ?: error("Missing or mismatched platform management context: $path")
}

private fun Transaction.hasRole(user: UserEntity, role: RoleEntity, context: ContextEntity): Boolean =
    !UserRoleContext.selectAll().where {
        (UserRoleContext.userId eq user.id.value) and
            (UserRoleContext.roleId eq role.id) and
            (UserRoleContext.contextId eq context.id)
    }.empty()

private fun Transaction.ensureRole(user: UserEntity, role: RoleEntity, context: ContextEntity) {
    if (!hasRole(user, role, context)) {
        UserRoleContext.insert {
            it[userId] = user.id.value
            it[roleId] = role.id
            it[contextId] = context.id
        }
    }
}

/**
 * Called AFTER application migrations and user creation.
 * Provision only the configured platform owner in platform-management contexts.
 * In particular, never assign OWNER to a customer's organization or tenant.
 *
 * A pre-existing account requires an existing OWNER assignment in the APPLICATION root.
 * Otherwise a user who registered the configured username could become a platform admin.
 */
internal fun Transaction.provisionPlatformOwner(owner: UserEntity, alreadyExisted: Boolean) {
    val root = platformContext(PLATFORM_ROOT)
    val ownerRole = RoleEntity.find { RolesTable.name eq "OWNER" }.first()
    val userRole = RoleEntity.find { RolesTable.name eq "USER" }.first()

    check(!alreadyExisted || hasRole(owner, ownerRole, root)) {
        "Configured platform owner exists without an established APPLICATION/OWNER assignment; refusing automatic privilege elevation"
    }

    ensureRole(owner, ownerRole, root)
    ensureRole(owner, userRole, root)

    val management = platformContext(PLATFORM_MANAGEMENT)
    val application = ApplicationEntity.find { ApplicationsTable.name eq MANAGEMENT_APP }.firstOrNull()
        ?: error("Missing internal application $MANAGEMENT_APP")
    check(application.defaultContext.id == management.id) {
        "$MANAGEMENT_APP has an unexpected default context"
    }

    val modules = ModuleEntity.find { ModulesTable.applicationId eq application.id }.toList()
    check(modules.map { it.name }.containsAll(platformModules)) {
        "$MANAGEMENT_APP is missing one of its required modules"
    }

    // Existing role/right grants come from migrations; only the user's explicit
    // role assignments are provisioned here. No rights are inherited into customer tenants.
    ensureRole(owner, ownerRole, management)
    modules.forEach { module ->
        val moduleContext = platformContext("$PLATFORM_MANAGEMENT/${module.name}")
        check(module.defaultContext.id == moduleContext.id) {
            "Module ${module.name} has an unexpected default context"
        }
        ensureRole(owner, ownerRole, moduleContext)
    }

    // The management application is internal, but the owner must be able to reach its UI.
    // Provisioning here (after migrations) also works on fresh installations and is repeatable.
    val active = LifecycleStageEntity.find { LifecycleStagesTable.name eq "ACTIVE" }.first()
    val userApplication = UserApplicationEntity.find {
        (UserApplicationsTable.userId eq owner.id.value) and
            (UserApplicationsTable.applicationId eq application.id)
    }.firstOrNull() ?: registerForApplication(owner.id.value, application.id.value)
    userApplication.lifecycleStage = active

    modules.forEach { module ->
        val userModule = UserModuleEntity.find {
            (UserModulesTable.userId eq owner.id.value) and
                (UserModulesTable.moduleId eq module.id)
        }.firstOrNull() ?: registerForModule(owner.id.value, module.id.value)
        userModule.lifecycleStage = active
    }
}
