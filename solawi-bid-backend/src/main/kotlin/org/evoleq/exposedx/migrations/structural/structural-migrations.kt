package org.evoleq.exposedx.migrations.structural

import org.evoleq.exposedx.migrations.MigrationException
import org.evoleq.exposedx.migrations.structural.h2.alterDefault
import org.evoleq.exposedx.migrations.structural.h2.alterNullability
import org.evoleq.exposedx.migrations.structural.h2.alterVarcharLength
import org.evoleq.exposedx.migrations.structural.mysql.alterDefault
import org.evoleq.exposedx.migrations.structural.mysql.alterNullability
import org.evoleq.exposedx.migrations.structural.mysql.alterVarcharLength
import org.evoleq.exposedx.migrations.structural.postresql.alterDefault
import org.evoleq.exposedx.migrations.structural.postresql.alterNullability
import org.evoleq.exposedx.migrations.structural.postresql.alterVarcharLength
import org.evoleq.ktorx.result.Result
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.H2Dialect
import org.jetbrains.exposed.sql.vendors.MysqlDialect
import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect


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

@Suppress("UnusedParameter")
fun Database.modifyColumnProperties(vararg dataSets: ModifyColumnProperties<*>) {

    val database = this
    val existingTables = transaction(database) {
        SchemaUtils.listTables().map{it.substringAfterLast(".")}
    }
    val relevantDataSets = dataSets.filter { it.table.tableName in existingTables }

    transaction(this) {
        relevantDataSets.forEach { (table, columnDefs) ->
            val columnNames = columnDefs.map { it.name.lowercase() }
            val columns = table.columns.filter { column -> column.name.lowercase() in columnNames }.associate {
                it.name.lowercase() to it
            }
            // common stuff
            columnDefs.forEach { colDef ->
                @Suppress("MapGetWithNotNullAssertionOperator", "UnsafeCallOnNullableType")
                val column = columns[colDef.name.lowercase()]!!
                colDef.nullable?.let { nullable ->
                    modifyNullability(database, table, column, nullable)
                }
                colDef.newDefault?.let { default ->
                    modifyDefault(database, table, column, default)
                }
            }

            // varchars:
            val varcharDefs = columnDefs.filterIsInstance<ColumnDef.ModifyProperties.Varchar>()
            varcharDefs.forEach { colDef ->
                modifyVarchar(table, colDef, columns, this)
            }
        }
    }
}

fun Transaction.modifyNullability(database: Database, table: Table, column: Column<*>, nullable: Boolean) {
    when (val dialect = database.dialect) {
        is MysqlDialect -> exec(
            dialect.alterNullability(table.tableName, column, nullable)
        )
        is PostgreSQLDialect -> exec(
            dialect.alterNullability(table.tableName, column.name, nullable)
        )
        is H2Dialect -> exec(
            dialect.alterNullability(table.tableName, column.name, nullable)
        )
    }
}

fun <T> Transaction.modifyDefault(database: Database,table: Table, column: Column<*>, default: T?) {
    when (val dialect = database.dialect) {
        is MysqlDialect -> exec(
            dialect.alterDefault(table.tableName, column, default)
        )
        is PostgreSQLDialect -> exec(
            dialect.alterDefault(table.tableName, column.name, default)
        )
        is H2Dialect -> exec(
            dialect.alterDefault(table.tableName, column.name, default)
        )
    }
}

fun Database.modifyVarchar(table: Table, colDef: ColumnDef.ModifyProperties.Varchar, columns: Map<String, Column<*>>, transaction: Transaction) {
    val database = this
    @Suppress("MapGetWithNotNullAssertionOperator", "UnsafeCallOnNullableType")
    val column = columns[colDef.name.lowercase()]!!
    // Be careful: only varchars can be handled by now
    colDef.newLength?.let { length ->

        when(val dialect = database.dialect) {
            is MysqlDialect -> with(transaction){exec(
                dialect.alterVarcharLength(table.tableName, column.name, length)
            )}
            is PostgreSQLDialect -> with(transaction){ exec(
                dialect.alterVarcharLength(table.tableName, column.name, length)
            )}
            is H2Dialect-> with(transaction){exec(
                dialect.alterVarcharLength(table.tableName, column.name, length)
            )}
            else -> error("Unsupported dialect: ${database.dialect.name}")
        }
    }
}

fun Database.modifyCheckConstraints(vararg dataSets: TableDef.CheckConstraint) {
    val database = this
    val existingTables = transaction(database) {
        SchemaUtils.listTables().map { it.substringAfterLast(".") }
    }
    val relevantDataSets = dataSets.filter { it.table.tableName in existingTables }
    transaction(database) {
        // Enable SQL logging
        addLogger(StdOutSqlLogger)

        relevantDataSets.filterIsInstance<TableDef.CheckConstraint.Update>().forEach { def ->
            val tableName = def.table.tableName
            val check = def.check
            val sql = def.sql
            val dropStatement = """
                |ALTER TABLE $tableName DROP CHECK chk_$check;
            """.trimMargin()


            val createStatement = """
                |ALTER TABLE $tableName
                |ADD CONSTRAINT chk_$check
                |CHECK ($sql);
            """.trimMargin()

            try{exec(dropStatement)}catch(_: Exception){}
            exec(createStatement)
        }
        relevantDataSets.filterIsInstance<TableDef.CheckConstraint.Remove>().forEach { def ->
            val tableName = def.table.tableName
            val check = def.check
            val dropStatement = """
                |ALTER TABLE $tableName
                |DROP CHECK chk_$check;
            """.trimMargin()

            exec(dropStatement)
        }
    }
}

fun String.fixColumnName(): String = when{
    listOf("varchar").contains(this) -> "`$this`"
    else -> this
}

