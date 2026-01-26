package org.evoleq.exposedx.migrations.structural.postresql

import org.jetbrains.exposed.sql.vendors.PostgreSQLDialect


fun <T : Any?> PostgreSQLDialect.alterDefault(table: String, column: String, default: T?): String {
    return """
        |ALTER TABLE $table
        |ALTER COLUMN $column SET DEFAULT $default;
    """.trimMargin()
}

fun PostgreSQLDialect.alterNullability(table: String, column: String, nullable: Boolean): String {
    val nullability = if (nullable) "DROP NOT NULL" else "SET NOT NULL"

    return """
        |ALTER TABLE $table
        |ALTER COLUMN $column $nullability;
    """.trimMargin()
}

fun PostgreSQLDialect.alterVarcharLength(table: String, column: String, length: Int) ="""
   |ALTER TABLE $table
   |ALTER COLUMN $column TYPE VARCHAR($length);
""".trimMargin()
