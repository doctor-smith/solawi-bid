package org.solyton.solawi.bid.application.ui.page.dashboard.permissions

import org.evoleq.math.Reader
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement

val canAccessApplication: (applicationName: String) -> Reader<ApplicationManagement, Boolean> = {
    applicationName -> Reader { applicationManagement ->
        // find application by name
        val application = applicationManagement.availableApplications.firstOrNull{
            application -> application.name == applicationName
        }?: return@Reader false
        // find corresponding application-context-relations
        val contextIds = applicationManagement.personalApplicationContextRelations.filter {
            (contextId, relatedId) -> relatedId == application.id
        }.map{ (contextId, _)-> contextId }

        contextIds.isNotEmpty()
}}

val canAccessModule: (moduleName: String, applicationName: String) -> Reader<ApplicationManagement, Boolean> = {
    moduleName, applicationName -> Reader { applicationManagement ->
        // find application and module by name
        val application = applicationManagement.availableApplications.firstOrNull{
            application -> application.name == applicationName
        }?.modules?.firstOrNull{
            module -> module.name == moduleName
        }?: return@Reader false
        // find corresponding application-context-relations
        val contextIds = applicationManagement.personalApplicationContextRelations.filter {
            (contextId, relatedId) -> relatedId == application.id
        }.map{ (contextId, _)-> contextId }

        contextIds.isNotEmpty()
}}
