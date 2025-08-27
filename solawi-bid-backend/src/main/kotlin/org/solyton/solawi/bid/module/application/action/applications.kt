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
import org.solyton.solawi.bid.module.application.data.*
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserModuleEntity
import org.solyton.solawi.bid.module.application.schema.UserModulesTable

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
            val userId = contextual.userId
            val applications = UserApplicationEntity.find{ UserApplicationsTable.userId eq userId }.toList()
            val modules = UserModuleEntity.find { UserModulesTable.userId eq userId }.toList()

            val applicationModules = applications.map{ application ->
                Pair(
                    application,
                    modules.filter{ module ->
                        module.module in application.application.modules
                    }
                )
            }

            applicationModules.toApiFromPairs()
        } } x database
    }
}
