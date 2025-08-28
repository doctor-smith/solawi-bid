package org.solyton.solawi.bid.module.application.data

import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.application.data.LifecycleStage as ApiLifecycleStage


fun List<ApplicationEntity>.toApi(): ApiApplications =
    ApiApplications(map { application -> application.toApi() } )

fun List<Pair<UserApplicationEntity, List<UserModuleEntity>>>.toApiFromPairs(): ApiApplications =
    ApiApplications( map { pair ->  pair.toApi() } )

fun Pair<UserApplicationEntity, List<UserModuleEntity>>.toApi(): ApiApplication {
    val (userApplication, userModules) = this
    val application = userApplication.application

    val resultApplication = with(application.toApi()) {
        copy(
            lifecycleStage = userApplication.lifecycleStage.toApi(),
            modules = modules.map { module ->
                module.copy(
                    lifecycleStage = userModules.find { userModule -> userModule.module.id.value.toString() == module.id }
                        ?.lifecycleStage?.toApi()?: ApiLifecycleStage.Empty
                )
            }
        )
    }
    return resultApplication
}

fun ApplicationEntity.toApi(): ApiApplication = ApiApplication(
    id = id.value.toString(),
    name = name,
    description = description,
    lifecycleStage = ApiLifecycleStage.Empty,
    modules = modules.map{ module -> module.toApi() }
)

fun ModuleEntity.toApi(): ApiModule = ApiModule(
    id = id.value.toString(),
    name = name,
    description = description,
    lifecycleStage = ApiLifecycleStage.Empty
)

fun LifecycleStageEntity.toApi() = when(name) {
    "REGISTERED" -> ApiLifecycleStage.Registered
    "TRIALING" -> ApiLifecycleStage.Trialing
    "ACTIVE" -> ApiLifecycleStage.Active
    "PAUSED" -> ApiLifecycleStage.Paused
    "PAYMENT_FAILED_GRACE_PERIOD" -> ApiLifecycleStage.PaymentFailedGracePeriod
    "CANCELLED" -> ApiLifecycleStage.Cancelled
    "CHURNED" -> ApiLifecycleStage.Churned
    else -> ApiLifecycleStage.Empty
}
