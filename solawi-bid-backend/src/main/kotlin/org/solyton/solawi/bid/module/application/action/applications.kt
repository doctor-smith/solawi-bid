package org.solyton.solawi.bid.module.application.action

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.ktorx.result.map
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.jetbrains.exposed.sql.and
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.repository.moveLifecycleStage
import org.solyton.solawi.bid.module.application.repository.registerForApplication
import org.solyton.solawi.bid.module.application.repository.registerForModule
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.UserApplicationEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserModuleEntity
import org.solyton.solawi.bid.module.application.schema.UserModulesTable
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun ReadAllApplications(): KlAction<Result<ReadApplications>, Result<Applications>> = KlAction<Result<ReadApplications>, Result<Applications>> {
    result: Result<ReadApplications> -> DbAction {
        database -> result bindSuspend {data: ReadApplications -> resultTransaction(database){
            ApplicationEntity.all().map { it.toApi() } } map { ApiApplications(it) }
        } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun ReadPersonalUserApplications(): KlAction<Result<Contextual<ReadPersonalUserApplications>>, Result<Applications>> = KlAction<Result< Contextual<ReadPersonalUserApplications>>, Result<Applications>> {
    result: Result<Contextual<ReadPersonalUserApplications>> -> DbAction {
        database -> result bindSuspend {contextual: Contextual<ReadPersonalUserApplications> -> resultTransaction(database){
            readUserApplications(contextual.userId)
        } } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun ReadPersonalApplicationContextRelations(): KlAction<
        Result<Contextual<ReadPersonalApplicationContextRelations>>,
        Result<ApplicationContextRelations>
        > = KlAction<Result< Contextual<ReadPersonalApplicationContextRelations>>, Result<ApplicationContextRelations>> {
    result: Result<Contextual<ReadPersonalApplicationContextRelations>> -> DbAction {
        database -> result bindSuspend {contextual: Contextual<ReadPersonalApplicationContextRelations> -> resultTransaction(database){
            val userId = contextual.userId
            val applicationContexts = UserApplicationsTable.select(
                UserApplicationsTable.applicationId,
                UserApplicationsTable.contextId
            ).where{
                UserApplicationsTable.userId eq userId
            }.toList()
            ApplicationContextRelations(applicationContexts.map{
                ApplicationContextRelation(
                    it[UserApplicationsTable.applicationId].value.toString(),
                    it[UserApplicationsTable.contextId].value.toString()
                )
            })
        } } x database
    }
}

/**
 * Just returns entries nonempty values.
 */
@MathDsl
@Suppress("FunctionName")
fun ReadApplicationsOfUsers(): KlAction<Result<Contextual<ReadUserApplications>>, Result<UserApplications>> = KlAction<Result< Contextual<ReadUserApplications>>, Result<UserApplications>> {
    result: Result<Contextual<ReadUserApplications>> -> DbAction {
        database -> result bindSuspend {contextual: Contextual<ReadUserApplications> -> resultTransaction(database){
            val userIds = contextual.data.userIds.map { UUID.fromString(it) }
            val applications = UserApplicationEntity.find{ UserApplicationsTable.userId inList userIds }.toList()
            val modules = UserModuleEntity.find { UserModulesTable.userId inList userIds }.toList()

            val applicationModules = applications.map{ application ->
                Pair(
                    application,
                    modules.filter{ module ->
                        module.module in application.application.modules
                    }
                )
            }

            applicationModules.toApiUserApplications()
        } } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun RegisterForApplications(): KlAction<Result<Contextual<RegisterForApplications>>, Result<Applications>> = KlAction {
    result -> DbAction {
        database -> result bindSuspend {contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val applicationIds = contextual.data.applicationIds.map{ uuid ->UUID.fromString(uuid) }

            applicationIds.forEach{ applicationId ->
                val application = registerForApplication(userId, applicationId)
                val modules = ModuleEntity.find { ModulesTable.applicationId eq applicationId and (ModulesTable.isMandatory eq true) }.toList()
                modules.forEach { module ->
                    registerForModule(userId, module.id.value)
                }
                application
            }

            readUserApplications(userId)
        } } x database
    }
}

@MathDsl
@Suppress("FunctionName")
fun StartTrialsOfApplications(): KlAction<Result<Contextual<StartTrialsOfApplications>>, Result<Applications>> = KlAction {
    result -> DbAction {
        database -> result bindSuspend {contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val applicationIds = contextual.data.applicationIds.map{ uuid ->UUID.fromString(uuid) }
            val lifecycleStages: Map<String, UUID> = LifecycleStageEntity.all().associate {
                    lifecycleStage -> lifecycleStage.name x lifecycleStage.id.value
            }
            val applications = applicationIds.map { applicationId ->
                UserApplicationEntity
                    .find { (UserApplicationsTable.userId eq userId) and (UserApplicationsTable.applicationId eq applicationId)  }
                    .firstOrNull()?: throw ApplicationException.NoSuchApplication("")

            }
            // Check if all apps can be subscribed ? Is this done by the state machine?

            val allowedModuleStageIds = listOf(
                lifecycleStages[LifecycleStage.Registered.name()]!!
            )
            applications.forEach{ application ->
                moveLifecycleStage(application, lifecycleStages[LifecycleStage.Trialing.name()]!!, userId)
                val moduleIds = application.application.modules.map {
                        module -> module.id.value
                }

                val modules = UserModuleEntity.find {
                    (UserModulesTable.userId eq userId) and
                    (UserModulesTable.moduleId inList moduleIds) and
                    (UserModulesTable.lifecycleStageId inList allowedModuleStageIds)
                }

                modules.forEach { module ->
                    moveLifecycleStage(module, lifecycleStages[LifecycleStage.Trialing.name()]!!, userId)
                }
            }

            readUserApplications(userId)
        } } x database
    }
}


@MathDsl
@Suppress("FunctionName")
fun SubscribeApplications(): KlAction<Result<Contextual<SubscribeApplications>>, Result<Applications>> = KlAction {
    result -> DbAction {
        database -> result bindSuspend {contextual -> resultTransaction(database) {
            val userId = contextual.userId
            val applicationIds = contextual.data.applicationIds.map{ uuid ->UUID.fromString(uuid) }
            val lifecycleStages: Map<String, UUID> = LifecycleStageEntity.all().associate {
                lifecycleStage -> lifecycleStage.name x lifecycleStage.id.value
            }
            val applications = applicationIds.map { applicationId ->
                UserApplicationEntity.find { (UserApplicationsTable.userId eq userId) and (UserApplicationsTable.applicationId eq applicationId)  }
                    .firstOrNull()?: throw ApplicationException.NoSuchApplication("")

            }
            // Check if all apps can be subscribed ? Is this done by the state machine?
            val allowedModuleStageIds = listOf(
                lifecycleStages[LifecycleStage.Registered.name()]!!,
                lifecycleStages[LifecycleStage.Trialing.name()]!!,
                lifecycleStages[LifecycleStage.Paused.name()]!!
            )
            applications.forEach{ application ->
                moveLifecycleStage(application, lifecycleStages[LifecycleStage.Active.name()]!!, userId)
                val moduleIds = application.application.modules.map {
                    module -> module.id.value
                }

                val modules = UserModuleEntity.find {
                    (UserModulesTable.userId eq userId) and
                    (UserModulesTable.moduleId inList moduleIds) and
                    (UserModulesTable.lifecycleStageId inList allowedModuleStageIds)
                }

                modules.forEach { module ->
                    moveLifecycleStage(module, lifecycleStages[LifecycleStage.Active.name()]!!, userId)
                }
            }


            readUserApplications(userId)
        } } x database
    }
}
