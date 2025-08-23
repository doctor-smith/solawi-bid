package org.solyton.solawi.bid.module.application.exception

sealed class ApplicationException(override val message: String): Exception(message) {
    data class DuplicateApplicationName(val name: String) : ApplicationException("Application with name '$name' already exists")
    data class NoSuchApplication(val id: String): ApplicationException("No such application id = $id")

    data class DuplicateModuleName(val name: String, val appName: String) : ApplicationException("module with name '$name' already exists in application $appName")
    data class NoSuchModule(val id: String): ApplicationException("No such module id = $id")

    data class DuplicateLifecycleStage(val name: String): ApplicationException("LifecycleStage with name '$name' already exists")
    data class NoSuchLifecycleStage(val id: String): ApplicationException("No such LifecycleStage id = $id")

    data class ForbiddenLifecycleTransition(val from: String, val to: String): ApplicationException("Forbidden lifecycle transition: $from -> $to")
    data class DuplicateLifecycleTransition(val from: String, val to: String): ApplicationException("Duplicate lifecycle transition: $from -> $to")


    data class ApplicationRegistrationImpossible(val userId: String, val applicationId: String): ApplicationException("Registration impossible: userId = $userId; applicationId = $applicationId")
    data class ModuleRegistrationImpossible(val userId: String, val moduleId: String): ApplicationException("Registration impossible: userId = $userId; moduleId = $moduleId")

    data class ApplicationTrialImpossible(val userId: String, val applicationId: String): ApplicationException("Trial impossible: userId = $userId; applicationId = $applicationId")
    data class ModuleTrialImpossible(val userId: String, val moduleId: String): ApplicationException("Trial impossible: userId = $userId; moduleId = $moduleId")
}
