package org.solyton.solawi.bid.application.service

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.evoleq.value.StringValueWithDescription
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement
import org.solyton.solawi.bid.module.context.data.current
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.contexts
import org.solyton.solawi.bid.module.user.data.user.permissions
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

fun organizationApplicationContextId(applicationName: String, organizationId: String): Reader<ApplicationManagement, String?> = Reader{applicationManagement ->
    // find application by name
    val application = applicationManagement.availableApplications.firstOrNull{
            application -> application.name == applicationName
    }
    if(application == null) {  null }
    requireNotNull(application) { "Application with name $applicationName not found" }
    // find corresponding application-context-relations
    val contextId = applicationManagement.applicationOrganizationRelations.firstOrNull {
            (applicationId, orgId, _, _) -> applicationId == application.id && orgId == organizationId
    }?.contextId

    contextId
}
