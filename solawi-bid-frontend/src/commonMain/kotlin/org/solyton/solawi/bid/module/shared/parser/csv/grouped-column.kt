package org.solyton.solawi.bid.module.shared.parser.csv


data class ColumnType(val name: String, val type: String? = null, val key: String? = null)

fun String.toColumnType(): ColumnType {
    val nameAndType = substringBefore("?")
    val name = nameAndType.substringBefore(".")
    if (name.isEmpty()) throw IllegalArgumentException("Name cannot be empty")

    val type = if (nameAndType.contains(".")) {
        nameAndType.substringAfter(".").takeIf { it.isNotEmpty() }
    } else null

    val query = substringAfter("?")

    if (query.isEmpty() || !query.contains("=")) return ColumnType(name, type)

    val params = query.split("&").associate { item ->item.split("=").let { it[0] to it[1] } }
    val value = params["key"]

    return ColumnType(name, type, value)
}
