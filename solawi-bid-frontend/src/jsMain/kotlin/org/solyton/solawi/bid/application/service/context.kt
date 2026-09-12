package org.solyton.solawi.bid.application.service

import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.evoleq.permission.EmptyContext
import org.evoleq.value.StringValueWithDescription
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.application.data.ApplicationName
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.context.data.current
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.user.data.api.OrganizationId
import org.solyton.solawi.bid.module.user.data.user.permissions
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun Application.getContextById(id: String): Context? {
    val contexts = userData.permissions.contexts
    return contexts.firstOrNull{ it.contextId == id }
}

@OptIn(ExperimentalUuidApi::class)
fun Storage<Application>.setContext(contextIdentifier: String) {
    try {
        val context: Uuid = Uuid.parse(contextIdentifier)
        setContext(context)
    } catch (_: Exception) {
        setContextByName(contextIdentifier)
    }
}

fun Storage<Application>.setContext(context: StringValueWithDescription) {
    setContextByName(context.value)
}

fun Storage<Application>.setContextIfEmpty(contextIdentifier: String) {
    if((this * context * current).read() == EmptyContext.value) {
        setContextByName(contextIdentifier)
    }
}

@OptIn(ExperimentalUuidApi::class)
fun Storage<Application>.setContext(contextId: Uuid) {
    val stringifiedId = contextId.toString()
    if(stringifiedId == (this * context * current).read()) return
    (this * context * current).write(stringifiedId)
}

fun Storage<Application>.getContextByName(contextName: String): Context? {
    val contexts = (this * userData * permissions * contexts).read()
    return contexts.firstOrNull { it.contextName == contextName }
}

fun Storage<Application>.setContextByName(contextName: String) {
    val contexts = (this * userData * permissions * contexts).read()
    val contextId = contexts.firstOrNull { it.contextName == contextName }?.contextId

    if(contextId == null || contextId == (this * context * current).read()) return
    (this * context * current).write(contextId)
}

/**
 * Retrieves the context ID associated with a specified application and organization.
 *
 * @param applicationName The name of the application to locate.
 * @param organizationId The ID of the organization for which the application context ID is being retrieved.
 * @return A Reader that, when executed with an ApplicationManagement instance, provides the context ID
 *         associated with the specified application and organization, or null if no matching context ID is found.
 */
fun organizationApplicationContextId(
    applicationName: String,
    organizationId: String
): Reader<ApplicationManagement, String?> = Reader{ applicationManagement ->
    // find application by name
    val application = applicationManagement.availableApplications.firstOrNull{
            application -> application.name.equals(applicationName, ignoreCase = true)
    }
    if(application == null) return@Reader null

    // find corresponding application-context-relations
    val contextId = applicationManagement.applicationOrganizationRelations.firstOrNull {
            (applicationId, orgId, _, _) -> applicationId == application.id && orgId == organizationId
    }?.contextId

    contextId
}

/**
 * Dispatches the context for a specific application and organization in the storage.
 *
 * This method retrieves the context identifier associated with the combination of the
 * provided application name and organization ID. It performs a look-up using the application's
 * management module and ensures that a valid context identifier is set for the storage.
 * Throws an exception if the context ID cannot be found for the given inputs.
 *
 * @param applicationName The name of the application for which to find the context.
 * @param organizationId The ID of the organization for which the application context is being requested.
 */
fun Storage<Application>.dispatchContextOf(applicationName: ApplicationName, organizationId: OrganizationId) {
    val contextId = (this * applicationManagementModule * organizationApplicationContextId(applicationName.value, organizationId.value)).emit()
    if(contextId == null) {
        console.warn("Context id not found for application $applicationName and organization $organizationId")
        return
    }

    setContext(contextId)
}
