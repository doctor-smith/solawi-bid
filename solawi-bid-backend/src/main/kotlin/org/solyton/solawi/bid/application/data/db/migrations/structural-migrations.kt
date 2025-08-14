package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.AddMissingColumns
import org.evoleq.exposedx.migrations.ColumnDef
import org.evoleq.exposedx.migrations.StructuralMigrations
import org.solyton.solawi.bid.module.bid.schema.AuctionsTable
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.*


val structuralMigrations by lazy {
    StructuralMigrations(
        addMissingColumns = listOf(
            AddMissingColumns(
                UsersTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID(0L, 0L))
                )
            ),
            AddMissingColumns(
                AuctionsTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID(0L, 0L))
                )
            )
        )
    )
}
