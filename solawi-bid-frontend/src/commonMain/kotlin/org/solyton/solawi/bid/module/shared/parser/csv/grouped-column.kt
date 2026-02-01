package org.solyton.solawi.bid.module.shared.parser.csv


data class ColumnType(val name: String, val type: String? = null, val key: String? = null)

fun String.toColumnType(): ColumnType {
    val name = substringBefore(":").substringBefore(".")
    if (name.isEmpty()) throw IllegalArgumentException("Name cannot be empty")

    val type = if (contains(":")) {
        substringAfter(":").substringBefore(".").takeIf { it.isNotEmpty() }
    } else null

    val key = if (contains(".")) {
        substringAfterLast(".").takeIf { it.isNotEmpty() }
    } else null

    return ColumnType(name, type, key)
}
