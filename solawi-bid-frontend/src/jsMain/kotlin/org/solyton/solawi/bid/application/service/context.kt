package org.solyton.solawi.bid.application.service

import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.evoleq.value.StringValueWithDescription
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.context.data.current
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

fun Storage<Application>.setContextByName(contextName: String) {
    val contexts = (this * userData * permissions * contexts).read()
    val contextId = contexts.firstOrNull { it.contextName == contextName }?.contextId

    if(contextId == null || contextId == (this * context * current).read()) return
    (this * context * current).write(contextId)
}
