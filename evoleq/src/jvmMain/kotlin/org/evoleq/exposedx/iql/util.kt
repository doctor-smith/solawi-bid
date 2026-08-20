package org.evoleq.exposedx.iql

internal fun String.toSnakeCase(): String =
    replace(
        Regex("([a-z0-9])([A-Z])"),
        "$1_$2"
    )
        .lowercase()
