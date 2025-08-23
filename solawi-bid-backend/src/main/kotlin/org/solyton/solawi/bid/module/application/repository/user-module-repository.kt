package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleStages
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.UserModule
import org.solyton.solawi.bid.module.application.schema.UserModules
import java.util.UUID

fun Transaction.registerForModule(userId: UUID, moduleId: UUID): UserModule {
    val module = ModuleEntity.find{ ModulesTable.id eq moduleId }.firstOrNull()
        ?: throw ApplicationException.NoSuchModule(moduleId.toString())

    val registrationPossible = UserModule.find {
        (UserModules.moduleId eq moduleId) and
        (UserModules.userId eq userId)
    }.empty()

    if(!registrationPossible) throw ApplicationException.ModuleRegistrationImpossible(userId.toString(), moduleId.toString())

    val registeredStage = LifecycleStageEntity.find { LifecycleStages.name eq "REGISTERED" }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage("REGISTERED")

    return UserModule.new{
        this.userId = userId
        this.module = module
        lifecycleStage = registeredStage
        createdBy = userId
    }
}

fun Transaction.moveLifecycleStage(module: UserModule, toId: UUID, modifierId: UUID): UserModule {
    val toStage = transitionTargetOf(module.lifecycleStage.id.value, toId)

    module.lifecycleStage = toStage
    module.modifiedBy = modifierId
    module.modifiedAt = DateTime.now()

    return module
}
