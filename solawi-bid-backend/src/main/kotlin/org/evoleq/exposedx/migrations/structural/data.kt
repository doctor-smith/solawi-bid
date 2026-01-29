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

data class ModifyColumnProperties<T : Any?>(
    val table: Table,
    val columnDefs: List<ColumnDef.ModifyProperties<T>>
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

    sealed class ModifyProperties<out T : Any?>(
        override val name: String,
        // Be careful: only varchars can be handled by now

        open val newDefault: T? = null,
        open val nullable: Boolean? = null
    ) : ColumnDef(name) {
        data class Varchar(
            override val name: String,
            override val newDefault: String? = null,
            override val nullable: Boolean? = null,
            val newLength: Int? = null,
        ): ModifyProperties<String>(
            name,
            newDefault,
            nullable
        )
    }
}

sealed class TableDef(open val table: Table) {
    sealed class CheckConstraint(
        override val table: Table,
        open val check: String,
    ) : TableDef(table) {
        data class Update(
            override val table: Table,
            override val check: String,
            val sql: String
        ): CheckConstraint(table, check)
        data class Remove(
            override val table: Table,
            override val check: String
        ): CheckConstraint(table, check)
    }
    sealed class UniqueIndex(
        table: Table,
        open val columns: List<String>
    ): TableDef(table) {
        data class Update(
            val indexName: String,
            override val table: Table,
            override val columns: List<String>
        ): UniqueIndex(
            table,
            columns
        )
    }
    sealed class ForeignKey(table: Table): TableDef(table)
}

data class StructuralMigrations(
    val addMissingColumns: List<AddMissingColumns> = emptyList(),
    val modifyColumnNames: List<ModifyColumnNames> = emptyList(),
    val modifyColumnProperties: List<ModifyColumnProperties<*>> = emptyList(),
    val modifyTableChecks: List<TableDef.CheckConstraint>,
    val modifyTableUniques: List<TableDef.UniqueIndex>,
) {
    fun runOn(database: Database) {
        database.addMissingColumns(*addMissingColumns.toTypedArray())
        database.modifyColumnNames(*modifyColumnNames.toTypedArray())
        database.modifyColumnProperties(*modifyColumnProperties.toTypedArray())
        database.modifyCheckConstraints(*modifyTableChecks.toTypedArray())
        database.modifyTableUniques(*modifyTableUniques.toTypedArray())
    }
}
