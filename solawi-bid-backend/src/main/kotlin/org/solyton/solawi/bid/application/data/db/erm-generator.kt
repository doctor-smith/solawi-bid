package org.solyton.solawi.bid.application.data.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Table
import org.solyton.solawi.bid.module.user.schema.Sessions
import org.solyton.solawi.bid.module.user.schema.Tokens
import org.solyton.solawi.bid.module.user.schema.Users

fun main() {
    val diagram = generateMermaidER(
        listOf(
            Users,
            // UserProfiles,
            // Addresses,
            // Organisations,
            // UserOrganization,
            // UserRoleContext,
            Tokens,
            Sessions
        )
    )

    println(diagram)
}

fun generateMermaidER(tables: List<Table>): String {
    val sb = StringBuilder()
    sb.appendLine("erDiagram")

    val relations = mutableSetOf<String>()

    for (table in tables) {
        val tableName = table.tableName
        sb.appendLine("    $tableName {")

        for (column in table.columns) {
            val columnName = column.name
            val columnType = column.columnType.javaClass.simpleName
            val typeHint = when {
                columnType.contains("Int") -> "INT"
                columnType.contains("VarChar", true) -> "STRING"
                columnType.contains("Date") -> "DATE"
                columnType.contains("Boolean") -> "BOOLEAN"
                else -> "STRING"
            }

            val isPrimary = isPrimaryKeyColumn(table, column)
            val isFK = column.referee != null

            val label = when {
                isPrimary -> "PK"
                isFK -> "FK"
                else -> ""
            }

            sb.appendLine("        $typeHint $columnName $label")
        }

        sb.appendLine("    }")

        // Add FK relationships
        for (column in table.columns) {
            val ref = column.referee
            if (ref != null) {
                val sourceTable = table.tableName
                val targetTable = ref.table.tableName
                relations.add("    $sourceTable ||--o{ $targetTable : \"FK ${column.name}\"")
            }
        }
    }

    sb.appendLine()
    relations.forEach { sb.appendLine(it) }

    return sb.toString()
}



fun isPrimaryKeyColumn(table: Table, column: Column<*>): Boolean {
    return table.primaryKey?.columns?.contains(column) == true
}
