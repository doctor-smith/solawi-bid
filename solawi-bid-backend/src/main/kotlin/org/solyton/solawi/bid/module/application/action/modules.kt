package org.solyton.solawi.bid.module.application.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.repository.moveLifecycleStage
import org.solyton.solawi.bid.module.application.repository.registerForModule
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.ModuleContexts
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.UserModuleEntity
import org.solyton.solawi.bid.module.application.schema.UserModulesTable
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun ReadPersonalModuleContextRelations(): KlAction<
        Result<Contextual<ReadPersonalModuleContextRelations>>,
        Result<ModuleContextRelations>
        > = KlAction<Result< Contextual<ReadPersonalModuleContextRelations>>, Result<ModuleContextRelations>> {
   result: Result<Contextual<ReadPersonalModuleContextRelations>> -> DbAction {
        database -> result bindSuspend {contextual: Contextual<ReadPersonalModuleContextRelations> -> resultTransaction(database){
            val userId = contextual.userId
            val modules = UserModuleEntity.find { UserModulesTable.userId eq  userId}.toList().map{it.module.id.value}
            val moduleContexts = ModuleContexts.select(ModuleContexts.moduleId, ModuleContexts.contextId)
                .where{ ModuleContexts.moduleId inList modules }.toList()
            ModuleContextRelations(moduleContexts.map{
                ModuleContextRelation(
                    it[ModuleContexts.moduleId].value.toString(),
                    it[ModuleContexts.contextId].value.toString()
                )
            })
        } } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun RegisterForModules(): KlAction<Result<Contextual<RegisterForModules>>, Result<Applications>> = KlAction {
    result -> DbAction {
        database -> result bindSuspend {contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val moduleIds = contextual.data.moduleIds.map{ uuid ->UUID.fromString(uuid) }

            moduleIds.forEach { moduleId ->
                registerForModule(moduleId, userId)
            }

            readUserApplications(userId)
        } } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun StartTrialsOfModules(): KlAction<Result<Contextual<StartTrialsOfModules>>, Result<Applications>> = KlAction {
    result -> DbAction {
        database -> result bindSuspend {contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val moduleIds = contextual.data.moduleIds.map{ uuid -> UUID.fromString(uuid) }

            val lifecycleStages: Map<String, UUID> = LifecycleStageEntity.all().associate {
                lifecycleStage -> lifecycleStage.name x lifecycleStage.id.value
            }

            val modules = UserModuleEntity.find {
                ModulesTable.id inList moduleIds
            }.toList()

            modules.forEach { module ->
                moveLifecycleStage(module, lifecycleStages.getNullSave(LifecycleStage.Trialing.name()), userId)
            }

            readUserApplications(userId)
        } } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun SubscribeModules(): KlAction<Result<Contextual<SubscribeModules>>, Result<Applications>> = KlAction {
    result -> DbAction {
        database -> result bindSuspend {contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val moduleIds = contextual.data.moduleIds.map{ uuid ->UUID.fromString(uuid) }

            val lifecycleStages: Map<String, UUID> = LifecycleStageEntity.all().associate {
                    lifecycleStage -> lifecycleStage.name x lifecycleStage.id.value
            }

            val modules = UserModuleEntity.find {
                ModulesTable.id inList moduleIds
            }.toList()

            modules.forEach { module ->
                moveLifecycleStage(module, lifecycleStages.getNullSave(LifecycleStage.Active.name()), userId)
            }

            readUserApplications(userId)
        } } x database
    }
}
