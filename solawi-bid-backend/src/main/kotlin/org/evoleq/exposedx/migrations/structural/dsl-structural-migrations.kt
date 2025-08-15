package org.evoleq.exposedx.migrations.structural

import org.jetbrains.exposed.sql.Table

fun Table.addColumnsIfMissing(vararg columnDef: ColumnDef<Any?>): AddMissingColumns = AddMissingColumns(
    this,
    listOf(
        *columnDef
    )
)
