package org.solyton.solawi.bid.module.application.action

import org.solyton.solawi.bid.module.application.data.ApiApplications
import org.solyton.solawi.bid.module.application.data.toApiFromPairs
import org.solyton.solawi.bid.module.application.schema.UserApplicationEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserModuleEntity
import org.solyton.solawi.bid.module.application.schema.UserModulesTable
import java.util.UUID

fun readUserApplications(userId: UUID): ApiApplications {
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

    return applicationModules.toApiFromPairs()
}

fun <K, V> Map<K, V>.getNullSave(key: K): V = this[key]?: throw IndexOutOfBoundsException("No value for key: $key")
