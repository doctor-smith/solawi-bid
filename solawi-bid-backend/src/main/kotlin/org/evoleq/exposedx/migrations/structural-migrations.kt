package org.evoleq.exposedx.migrations

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

data class AddMissingColumns(
    val table: Table,
    val columnDefs: List<ColumnDef<Any?>>
)

data class ColumnDef<out T : Any?>(
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
    // Here, one has to take into account that names in dbs are case-insensitive
    // while strings on the client side are case-sensitive
    // We are better off forcing lowercase in every comparison.
    val database = this
    val existingTables = transaction(database) {
        SchemaUtils.listTables().map{it.substringAfterLast(".")}
    }
    val relevantDataSets = dataSets.filter { it.table.tableName in existingTables }
    transaction(database) {
        // Enable SQL logging
        addLogger(StdOutSqlLogger)

        relevantDataSets.forEach { (table, columnDefs) ->
            val columnNames = columnDefs.map { it.name.lowercase() }
            table.columns.filter { column -> column.name.lowercase() in columnNames }.forEach { column ->
                val columnName = column.name
                val columnExists = columnExists(table.tableName, columnName)

                if (!columnExists) {
                    when {
                        column.defaultValueFun != null ||
                        column.columnType.nullable ->
                            exec("ALTER TABLE ${table.tableName} ADD COLUMN  $columnName ${column.columnType.sqlType()};")
                        else -> {
                            val columnDef = columnDefs.find { it.name.lowercase() == column.name.lowercase() }
                                ?: throw MigrationException.NoSuchColumnDef(column.name)

                            exec("ALTER TABLE ${table.tableName} ADD COLUMN $columnName ${column.columnType.sqlType()} NULL;")

                            exec(
                            """
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
