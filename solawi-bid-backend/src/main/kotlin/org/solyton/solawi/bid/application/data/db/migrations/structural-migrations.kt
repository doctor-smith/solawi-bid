package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.AddMissingColumns
import org.evoleq.exposedx.migrations.ColumnDef
import org.evoleq.exposedx.migrations.StructuralMigrations
import org.evoleq.uuid.UUID_ZERO
import org.solyton.solawi.bid.module.banking.schema.BankAccountsTable
import org.solyton.solawi.bid.module.banking.schema.FiscalYears
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.user.schema.AddressesTable
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserProfilesTable
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
                UserProfilesTable,
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
            ),
            AddMissingColumns(
                DistributionPointsTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                SharesTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                ShareTypesTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                BankAccountsTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                FiscalYears,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            ),
            AddMissingColumns(
                AddressesTable,
                listOf(
                    ColumnDef<UUID>("CREATED_BY", UUID_ZERO)
                )
            )
        )
    )
}
