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
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.Resources
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RolesTable
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
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        UserProfilesTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        AuctionsTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        AuctionDetailsSolawiTuebingenTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        RoundsTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        OrganizationsTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        DistributionPointsTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        SharesTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        ShareTypesTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        BankAccountsTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        FiscalYears.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        AddressesTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        BiddersTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        BidderDetailsSolawiTuebingenTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        ContextsTable.addColumnsIfMissing(
  
          ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        Resources.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        RightsTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        ),
        RolesTable.addColumnsIfMissing(
            ColumnDef<UUID>("created_by", UUID_ZERO),
            ColumnDef<DateTime>("created_at", DateTime.now()),
            ColumnDef<UUID?>("modified_by",null),
            ColumnDef<DateTime?>("modified_at", null),
        )
    )
}
