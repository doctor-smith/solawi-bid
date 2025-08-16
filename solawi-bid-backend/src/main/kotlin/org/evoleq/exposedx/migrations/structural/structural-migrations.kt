package org.evoleq.exposedx.migrations.structural

import org.evoleq.exposedx.migrations.MigrationException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


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

fun Database.modifyColumnNames(vararg dataSets: ModifyColumnNames) {
    // Here, one has to take into account that names in dbs are case-insensitive
    // while strings on the client side are case-sensitive
    // We are better off forcing lowercase in every comparison.
    val database = this
    val existingTables = transaction(database) {
        SchemaUtils.listTables().map{it.substringAfterLast(".")}
    }
    val relevantDataSets = dataSets.filter { it.table.tableName in existingTables }

    transaction(database) {
        relevantDataSets.forEach { (table, columnDefs) ->
            val newColumnNames = columnDefs.map { it.newName.lowercase() }

            table.columns.filter { column -> column.name.lowercase() in newColumnNames }.forEach { column ->
                val newColumnName = column.name
                val oldColumnName = columnDefs.firstOrNull { it.newName == newColumnName }?.oldName
                    ?:throw MigrationException.NoSuchColumnDef(newColumnName)

                // check if column still carries old name in database
                val columnExists = columnExists(table.tableName, oldColumnName)

                // check if old name is a reserved word

                if(columnExists) {
                    exec("ALTER TABLE ${table.tableName} CHANGE COLUMN ${oldColumnName.fixColumnName()} $newColumnName ${column.columnType.sqlType()};")
                }
            }
        }
    }
}

fun String.fixColumnName(): String = when{
    listOf("varchar").contains(this) -> "`$this`"
    else -> this
}

