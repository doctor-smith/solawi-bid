package org.evoleq.exposedx.migrations.structural

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table


data class AddMissingColumns(
    val table: Table,
    val columnDefs: List<ColumnDef.Missing<Any?>>
)

data class ModifyColumnNames(
    val table: Table,
    val columnDefs: List<ColumnDef.ModifyName>
)

sealed class ColumnDef(open val name: String) {

    data class Missing<out T : Any?>(
        override val name: String,
        val default: T
    ) : ColumnDef(name)

    data class ModifyName(
        val oldName: String,
        val newName: String
    ) : ColumnDef(oldName)
}

data class StructuralMigrations(
    val addMissingColumns: List<AddMissingColumns> = emptyList(),
    val modifyColumnNames: List<ModifyColumnNames> = emptyList()
) {
    fun runOn(database: Database) {
        database.addMissingColumns(*addMissingColumns.toTypedArray())
        database.modifyColumnNames(*modifyColumnNames.toTypedArray())
    }
}
