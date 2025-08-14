package org.solyton.solawi.bid.application.data.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.DateColumnType

@Suppress("CognitiveComplexMethod")
fun generateMermaidER(tables: List<Table>): String {
    val sb = StringBuilder()
    sb.appendLine("erDiagram")

    val relations = mutableSetOf<String>()

    // Sort tables for consistent output
    val sortedTables = tables.sortedBy { it.tableName }

    for (table in sortedTables) {
        val tableName = table.tableName
        sb.appendLine("    $tableName {")

        val sortedColumns = table.columns.sortedBy { it.name }

        for (column in sortedColumns) {
            val columnName = column.name
            val typeHint = mapColumnType(column.columnType)
            val isPrimary = isPrimaryKeyColumn(table, column)
            val isFK = column.referee != null
            // val nullableMarker = if (column.columnType.nullable) "?" else ""

            // val nullableInfo = if (column.columnType.nullable) "NULL" else "NOT_NULL"

            val label = when {
                isPrimary -> "PK"
                isFK -> "FK"
                else -> "" //nullableInfo
            }

            val parts = listOf(typeHint, columnName) + if (label.isNotBlank()) listOf(label) else emptyList()
            sb.appendLine("        " + parts.joinToString(" "))
        }

        sb.appendLine("    }")

        // Add FK relationships
        for (column in sortedColumns) {
            val ref = column.referee
            if (ref != null) {
                val sourceTable = table.tableName
                val targetTable = ref.table.tableName
                val relation = """    $sourceTable ||--o{ $targetTable : "FK ${column.name}""""
                relations.add(relation)
            }
        }
    }

    sb.appendLine()
    relations.sorted().forEach { sb.appendLine(it) }

    return sb.toString()
}

fun mapColumnType(columnType: IColumnType): String = when (columnType) {
    is IntegerColumnType -> "INT"
    is LongColumnType -> "LONG"
    is VarCharColumnType -> "STRING"
    is TextColumnType -> "TEXT"
    is BooleanColumnType -> "BOOLEAN"
    is DateColumnType -> "DATE"
    // , is DateTimeColumnType
    is UUIDColumnType -> "STRING"//"UUID" <-- not supported by mermaid
    is DoubleColumnType -> "DOUBLE"
    is FloatColumnType -> "FLOAT"
    is DecimalColumnType -> "DECIMAL"
    is BlobColumnType -> "BLOB"
    // Add more mappings as needed
    else -> "STRING" // fallback
}


fun isPrimaryKeyColumn(table: Table, column: Column<*>): Boolean {
    return table.primaryKey?.columns?.contains(column) == true
}
