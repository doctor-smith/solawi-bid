package org.evoleq.exposedx.migrations.structural.h2

import org.jetbrains.exposed.sql.vendors.H2Dialect


fun <T : Any?> H2Dialect.alterDefault(table: String, column: String, default: T?): String {
    return """
        |ALTER TABLE $table
        |ALTER COLUMN $column SET DEFAULT $default;
    """.trimMargin()
}

fun H2Dialect.alterNullability(table: String, column: String, nullable: Boolean): String {
    val nullability = if (nullable) "DROP NOT NULL" else "SET NOT NULL"

    return """
        |ALTER TABLE $table
        |ALTER COLUMN $column $nullability;
    """.trimMargin()

}

fun H2Dialect.alterVarcharLength(table: String, column: String, length: Int) = """
    |ALTER TABLE $table
    |ALTER COLUMN $column SET DATA TYPE VARCHAR($length);
""".trimMargin()
