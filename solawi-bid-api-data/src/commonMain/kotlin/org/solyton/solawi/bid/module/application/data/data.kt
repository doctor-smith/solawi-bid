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
