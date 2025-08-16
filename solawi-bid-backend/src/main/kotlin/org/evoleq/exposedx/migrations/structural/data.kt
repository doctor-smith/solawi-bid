package org.evoleq.exposedx.migrations.structural

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table


data class AddMissingColumns(
    val table: Table,
    val columnDefs: List<ColumnDef.Missing<Any?>>
)

sealed class ColumnDef(open val name: String) {

    data class Missing<out T : Any?>(
        override val name: String,
        val default: T
    ) : ColumnDef(name)

    data class ModifyName(
        override val name: String,
        val newName: String
    ) : ColumnDef(name)
}

data class StructuralMigrations(
    val addMissingColumns: List<AddMissingColumns>
) {
    fun runOn(database: Database) {
        database.addMissingColumns(*addMissingColumns.toTypedArray())
    }
}
