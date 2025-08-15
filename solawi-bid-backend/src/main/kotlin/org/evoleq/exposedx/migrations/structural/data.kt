package org.evoleq.exposedx.migrations.structural

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table


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
