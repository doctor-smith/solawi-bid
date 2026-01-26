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

data class StructuralMigrations(
    val addMissingColumns: List<AddMissingColumns> = emptyList(),
    val modifyColumnNames: List<ModifyColumnNames> = emptyList(),
    val modifyColumnProperties: List<ModifyColumnProperties<*>> = emptyList()
) {
    fun runOn(database: Database) {
        database.addMissingColumns(*addMissingColumns.toTypedArray())
        database.modifyColumnNames(*modifyColumnNames.toTypedArray())
        database.modifyColumnProperties(*modifyColumnProperties.toTypedArray())
    }
}
