package org.solyton.solawi.bid.module.application.service

import org.evoleq.uuid.toUuid
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.application.data.ApplicationId
import org.solyton.solawi.bid.module.application.data.ModuleId
import org.solyton.solawi.bid.module.application.repository.validatedApplication
import org.solyton.solawi.bid.module.application.repository.validatedModule
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.Role
import org.solyton.solawi.bid.module.permission.repository.getRightRoleContexts
import org.solyton.solawi.bid.module.permission.repository.putRoleRightContext

fun Transaction.updateStandardApplicationContext(applicationId: ApplicationId, roles: List<Role>): Context {

    val application = validatedApplication(applicationId.value.toUuid())
    val contextId = application.defaultContext.id.value
    val roleToRightsMap = roles.associateBy({
        role -> role.id.toUuid()
    }) { role -> role.rights.map { right -> right.id.toUuid() } }

    roleToRightsMap.forEach { (roleId, rightIds) ->
        putRoleRightContext(roleId, rightIds, contextId)
    }

    flushCache()

    fixContexts(application.id.value)

    val context = getRightRoleContexts(listOf(contextId)).first()

    return context
}

fun Transaction.updateStandardModuleContext(moduleId: ModuleId, roles: List<Role>): Context {
    val module = validatedModule(moduleId.value.toUuid())
    val contextId = module.defaultContext.id.value

    val roleToRightsMap = roles.associateBy({
            role -> role.id.toUuid()
    }) { role -> role.rights.map { right -> right.id.toUuid() } }

    roleToRightsMap.forEach { (roleId, rightIds) ->
        putRoleRightContext(roleId, rightIds, contextId)
    }

    flushCache()

    fixContexts(module.application.id.value)

    val context = getRightRoleContexts(listOf(contextId)).first()

    return context
}
