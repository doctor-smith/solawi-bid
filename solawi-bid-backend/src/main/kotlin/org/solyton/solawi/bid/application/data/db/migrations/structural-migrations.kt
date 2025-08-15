package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.structural.AddMissingColumns
import org.evoleq.exposedx.migrations.structural.ColumnDef
import org.evoleq.exposedx.migrations.structural.StructuralMigrations
import org.evoleq.exposedx.migrations.structural.addColumnsIfMissing
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
        UsersTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        UserProfilesTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        AuctionsTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        AuctionDetailsSolawiTuebingenTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        RoundsTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        OrganizationsTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        DistributionPointsTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        SharesTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        ShareTypesTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        BankAccountsTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        FiscalYears.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        AddressesTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        BiddersTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        ),
        BidderDetailsSolawiTuebingenTable.addColumnsIfMissing(
            ColumnDef<UUID>("CREATED_BY", UUID_ZERO),
            ColumnDef<DateTime>("CREATED_AT", DateTime.now()),
            ColumnDef<UUID?>("MODIFIED_BY",null),
            ColumnDef<DateTime?>("MODIFIED_AT", null),
        )
    )
}
