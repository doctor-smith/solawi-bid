package org.solyton.solawi.bid.module.application.data

import kotlinx.serialization.Serializable

typealias ApiApplications = Applications
typealias ApiApplication = Application
typealias ApiModule = Module
typealias ApiLifecycleStage = LifecycleStage
typealias ApiUserApplications = UserApplications


@Serializable
data object ReadApplications
@Serializable
data object ReadPersonalUserApplications

@Serializable
data class ReadUserApplications(
    val userIds: List<String>
)

@Serializable
data object ReadPersonalApplicationContextRelations

@Serializable
data object ReadPersonalModuleContextRelations

@Serializable
data class RegisterForApplications(
    val applicationIds: List<String>
)

@Serializable
data class StartTrialsOfApplications(
    val applicationIds: List<String>
)

@Serializable
data class SubscribeApplications(
    val applicationIds: List<String>
)

@Serializable
data class RegisterForModules(
    val moduleIds: List<String>
)

@Serializable
data class StartTrialsOfModules(
    val moduleIds: List<String>
)

@Serializable
data class SubscribeModules(
    val moduleIds: List<String>
)

@Serializable
data class PauseApplications(
    val applicationIds: List<String>
)

@Serializable
data class UserApplications(
    // maps userIds to applications
    val map: Map<String, List<Application>>
)

@Serializable
data class Applications(
    val list: List<Application>
)

@Serializable
data class Application(
    val id: String,
    val name: String,
    val description: String,
    val lifecycleStage: LifecycleStage,
    val modules: List<Module>
)

@Serializable
data class Module(
    val id: String,
    val name: String,
    val description: String,
    val lifecycleStage: LifecycleStage
)

@Serializable
sealed class LifecycleStage {
    @Serializable
    data object Empty : LifecycleStage()
    @Serializable
    data object Registered : LifecycleStage()
    @Serializable
    data object Trialing : LifecycleStage()
    @Serializable
    data object Active : LifecycleStage()
    @Serializable
    data object Paused : LifecycleStage()
    @Serializable
    data object PaymentFailedGracePeriod : LifecycleStage()
    @Serializable
    data object Cancelled : LifecycleStage()
    @Serializable
    data object Churned : LifecycleStage()
}

fun LifecycleStage.name(): String = when(this) {
    is LifecycleStage.Empty -> "EMPTY"
    is LifecycleStage.Active -> "ACTIVE"
    is LifecycleStage.Cancelled -> "CANCELLED"
    is LifecycleStage.Churned -> "CHURNED"
    is LifecycleStage.Paused -> "PAUSED"
    is LifecycleStage.PaymentFailedGracePeriod -> "PAYMENT_FAILED_GRACE_PERIOD"
    is LifecycleStage.Registered -> "REGISTERED"
    is LifecycleStage.Trialing -> "TRIALING"
}

@Serializable
data class ApplicationContextRelations(
    val all: List<ApplicationContextRelation>
)

@Serializable
data class ApplicationContextRelation(
    val applicationId: String,
    val contextId: String
)

@Serializable
data class ModuleContextRelations(
    val all: List<ModuleContextRelation>
)

@Serializable
data class ModuleContextRelation(
    val moduleId: String,
    val contextId: String
)
