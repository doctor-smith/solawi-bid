package org.evoleq.exposedx.migrations.structural.mysql

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.vendors.MysqlDialect


fun <T : Any?> MysqlDialect.alterDefault(table: String, column: Column<*>, default: T?): String {
    // MySQL requires full column redefinition
    val type = column.columnType.sqlType()
    val nullability = if (column.columnType.nullable) "NULL" else "NOT NULL"
    val defaultClause = if (default != null) "DEFAULT $default" else ""


    return """
        |ALTER TABLE $table
        |MODIFY COLUMN ${column.name} $type $nullability $defaultClause;
    """.trimMargin()

}


fun MysqlDialect.alterNullability(table: String, column: Column<*>, nullable: Boolean): String {
    // MySQL requires full column redefinition
    val type = column.columnType.sqlType()
    val nullability = if (nullable) "NULL" else "NOT NULL"
    return """
        |ALTER TABLE $table
        |MODIFY COLUMN ${column.name} $type $nullability;
    """.trimMargin()
}


fun MysqlDialect.alterVarcharLength(table: String, column: String, length: Int) = """
    |ALTER TABLE $table
    |MODIFY COLUMN $column VARCHAR($length);
""".trimMargin()
