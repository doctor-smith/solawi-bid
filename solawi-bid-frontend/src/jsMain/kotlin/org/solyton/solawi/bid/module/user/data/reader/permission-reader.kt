package org.solyton.solawi.bid.module.user.data.reader

import org.evoleq.math.Reader
import org.evoleq.math.times
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.user.data.Application
import org.solyton.solawi.bid.shared.ValueWithDescription



fun isGranted(right: ValueWithDescription, contextId: String): Reader<Application, Boolean> = Reader{
    application ->
        val context: Context? = application.user.permissions.contexts.firstOrNull { it.contextId == contextId }
        if(context == null) false
        require(context != null)
        context.roles.map { it.rights }.flatten().distinctBy { it.rightName }.map { it.rightName }.contains(right.value)
}

fun isNotGranted(right: ValueWithDescription, contextId: String): Reader<Application, Boolean> =
    isGranted(right,contextId) * Reader<Boolean, Boolean>{bool -> !bool}

fun hasRole(role: ValueWithDescription, contextId: String): Reader<Application, Boolean> = Reader{
    application ->
    val context: Context? = application.user.permissions.contexts.firstOrNull { it.contextId == contextId }
    if(context == null) false
    require(context != null)
    context.roles.map { it.roleName }.distinct().contains(role.value)
}
