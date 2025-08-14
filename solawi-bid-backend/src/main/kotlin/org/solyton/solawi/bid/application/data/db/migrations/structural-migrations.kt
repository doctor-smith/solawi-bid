package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.AddMissingColumns
import org.evoleq.exposedx.migrations.ColumnDef
import org.evoleq.exposedx.migrations.StructuralMigrations
import org.evoleq.uuid.UUID_ZERO
import org.solyton.solawi.bid.module.bid.schema.AuctionDetailsSolawiTuebingenTable
import org.solyton.solawi.bid.module.bid.schema.AuctionsTable
import org.solyton.solawi.bid.module.bid.schema.RoundsTable
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UsersTable
import java.util.*


val structuralMigrations by lazy {
    StructuralMigrations(
        addMissingColumns = listOf(
            AddMissingColumns(
                UsersTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                AuctionsTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                AuctionDetailsSolawiTuebingenTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                RoundsTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                OrganizationsTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            )
        )
    )
}
