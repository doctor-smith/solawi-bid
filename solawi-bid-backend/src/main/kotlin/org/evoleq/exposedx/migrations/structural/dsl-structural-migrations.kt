package org.evoleq.exposedx.migrations.structural

import org.jetbrains.exposed.sql.Table

fun Table.addColumns(vararg columnDef: ColumnDef<Any?>): AddMissingColumns = AddMissingColumns(
    this,
    listOf(
        *columnDef
    )
)
