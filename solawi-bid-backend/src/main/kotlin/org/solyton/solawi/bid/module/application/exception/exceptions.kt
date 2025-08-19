package org.solyton.solawi.bid.module.application.exception

sealed class ApplicationException(override val message: String): Exception(message) {
    data class DuplicateApplicationName(val name: String) : ApplicationException("Application with name '$name' already exists")
    data class NoSuchApplication(val id: String): ApplicationException("No such application id = $id")

    data class DuplicateModuleName(val name: String, val appName: String) : ApplicationException("module with name '$name' already exists in application $appName")
    data class NoSuchModule(val id: String): ApplicationException("No such module id = $id")

}
