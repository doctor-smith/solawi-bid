package org.solyton.solawi.bid.module.permissions.service

import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.Permissions

fun Permissions.isGranted(rightId: String, contextId: String): Boolean {
    val context = contexts().firstOrNull() { it.contextId == contextId }
    if(context == null) return false

    return context.isGranted(rightId)
}

fun Context.isGranted(rightId: String): Boolean = roles.flatMap { it.rights }.any{ right -> right.rightId == rightId }

fun Permissions.isNotGranted(rightId: String, contextId: String): Boolean =
    !isGranted(rightId, contextId)

fun Permissions.contextByPath(path: String): Context = TODO()

fun Permissions.contexts(): List<Context> =
    Pair(listOf<Context>(), contexts)
        .collectContexts().first
        .distinctBy{context: Context -> context.contextId}

tailrec fun Pair<List<Context>,List<Context>>.collectContexts(): Pair<List<Context>,List<Context>> = when(second.isEmpty()) {
    true -> this
    false -> {
        val context = second.first()
        Pair(
            listOf(*first.toTypedArray(),context),
            listOf(
                *context.children.toTypedArray(),
                *second.drop(1).toTypedArray()
            )
        ).collectContexts()
    }
}