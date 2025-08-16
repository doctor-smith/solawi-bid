package org.evoleq.exposedx.migrations.structural

import org.jetbrains.exposed.sql.Table

fun Table.addColumnsIfMissing(vararg columnDefs: ColumnDef.Missing<Any?>): AddMissingColumns = AddMissingColumns(
    this,
    listOf(
        *columnDefs
    )
)

fun Table.modifyColumnNames(vararg columnDefs: ColumnDef.ModifyName): ModifyColumnNames = ModifyColumnNames (
    this,
    listOf(
        *columnDefs
    )
)
