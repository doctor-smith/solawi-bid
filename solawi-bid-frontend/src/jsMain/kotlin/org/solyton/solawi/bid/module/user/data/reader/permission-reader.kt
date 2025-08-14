package org.solyton.solawi.bid.module.user.data.reader

import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.value.StringValueWithDescription
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.user.data.Application


fun isGranted(right: StringValueWithDescription, contextId: String): Reader<Application, Boolean> = Reader{
    application ->
        val context: Context? = application.user.permissions.contexts.firstOrNull { it.contextId == contextId }
        if(context == null) false
        require(context != null)
        context.roles.map { it.rights }.flatten().distinctBy { it.rightName }.map { it.rightName }.contains(right.value)
}

fun isGranted(right: StringValueWithDescription, contextId: Source<String>): Reader<Application, Boolean> =
    isGranted(right, contextId.emit())

fun isNotGranted(right: StringValueWithDescription, contextId: String): Reader<Application, Boolean> =
    isGranted(right,contextId) * Reader<Boolean, Boolean>{bool -> !bool}

fun isNotGranted(right: StringValueWithDescription, contextId: Source<String>): Reader<Application, Boolean> =
    isNotGranted(right,contextId.emit())

fun hasRole(role: StringValueWithDescription, contextId: String): Reader<Application, Boolean> = Reader{
    application ->
    val context: Context? = application.user.permissions.contexts.firstOrNull { it.contextId == contextId }
    if(context == null) false
    require(context != null)
    context.roles.map { it.roleName }.distinct().contains(role.value)
}

fun hasRole(role: StringValueWithDescription, contextId: Source<String>): Reader<Application, Boolean> =
    hasRole(role, contextId.emit())
