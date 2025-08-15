package org.evoleq.exposedx.migrations.structural

import org.jetbrains.exposed.sql.transactions.transaction


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
