package org.solyton.solawi.bid.module.permissions.service

import org.evoleq.math.Reader
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.Permissions

/**
 * Compute context from path
 * pattern of paths: <path/to/resource>
 *
 * Background:
 *  - The root contexts are uniquely determined by their names
 *  - Within one context, subcontexts are uniquely determined by their names
 */
val contextFromPath: (String)-> Reader<Permissions, Context?> = {path -> Reader {
    permissions: Permissions ->
        val pathAsList = path.split("/")
        permissions.contexts.fromPath(pathAsList)

}}

fun Context.fromPath(path: List<String>): Context? = when {
    path.isEmpty() -> this
    else -> {
        val name = path.first()
        val rest = path.drop(1)

        when {
            contextName == name -> when{
                rest.isEmpty() -> this
                else -> children.fromPath(rest)
            }
            else -> null
        }
    }
}

fun List<Context>.fromPath(path: List<String>): Context? {
    if(isEmpty()) return null
    val context = first()
    val rest = drop(1)

    val result = context.fromPath(path)
    return result ?: rest.fromPath(path)
}

fun Context.readableName(): String = contextName.readableName()

fun String.readableName(): String = when{
    contains(".") -> split(".")[0]
    else -> this
}
