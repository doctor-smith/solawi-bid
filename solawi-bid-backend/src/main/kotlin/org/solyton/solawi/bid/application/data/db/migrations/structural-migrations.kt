package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.structural.AddMissingColumns
import org.evoleq.exposedx.migrations.structural.ColumnDef
import org.evoleq.exposedx.migrations.structural.StructuralMigrations
import org.evoleq.exposedx.migrations.structural.addColumns
import org.evoleq.uuid.UUID_ZERO
import org.joda.time.DateTime
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
        addMissingColumns = columns
    )
}

val columns: List<AddMissingColumns> by lazy {
    listOf(
        UsersTable.addColumns(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        AddMissingColumns(
            UserProfilesTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            AuctionsTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            AuctionDetailsSolawiTuebingenTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            RoundsTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            OrganizationsTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            DistributionPointsTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            SharesTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            ShareTypesTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            BankAccountsTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            FiscalYears,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        ),
        AddMissingColumns(
            AddressesTable,
            listOf(
                ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
                ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
                ColumnDef<UUID?>("MODIFIED_BY",null),
                ColumnDef<DateTime?>("MODIFIED_AT", null),
            )
        )
    )
}
