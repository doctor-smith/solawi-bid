package org.evoleq.exposedx.migrations

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

data class AddMissingColumns(
    val table: Table,
    val columnDefs: List<ColumnDef<Any>>
)

data class ColumnDef<out T : Any>(
    val name: String,
    val default: T
)

data class StructuralMigrations(
    val addMissingColumns: List<AddMissingColumns>
) {
    fun runOn(database: Database) {
        database.addMissingColumns(*addMissingColumns.toTypedArray())
    }
}


fun Database.addMissingColumns(vararg dataSets: AddMissingColumns){
    val database = this
    val existingTables = transaction(database) {
        SchemaUtils.listTables()
    }
    val relevantDataSets = dataSets.filter { it.table.tableName in existingTables }
    relevantDataSets.forEach { (table, columnDefs) ->
        val columnNames = columnDefs.map { it.name }
        table.columns.filter { column -> column.name in columnNames }.forEach { column ->
            val columnName = column.name
            when{
                column.defaultValueFun != null ||
                column.columnType.nullable -> transaction(database) {
                    if (!columnExists(table.tableName, columnName)) {
                        exec("ALTER TABLE ${table.tableName} MODIFY $columnName ${column.columnType.sqlType()};")
                    }
                }
                else -> transaction(database) {
                    val columnDef = columnDefs.find { it.name == column.name }
                        ?:throw MigrationException.NoSuchColumnDef(column.name)

                    if (!columnExists(table.tableName, columnName)) {
                        exec("ALTER TABLE ${table.tableName} ADD COLUMN $columnName ${column.columnType.sqlType()} NULL;")

                        exec("""
                        |UPDATE ${table.tableName}
                        |   SET $columnName = ${columnDef.default}
                        |   WHERE $columnName IS NULL;
                    """.trimMargin()
                        )

                        exec("ALTER TABLE ${table.tableName} MODIFY $columnName ${column.columnType.sqlType()};")
                    }
                }
            }
        }
    }

}

fun columnExists(tableName: String, columnName: String): Boolean {
    return transaction {
        exec("""
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = '$tableName'
              AND column_name = '$columnName'
        """) { rs ->
            if (rs.next()) rs.getInt(1) > 0 else false
        } ?: false
    }
}

