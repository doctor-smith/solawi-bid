package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.structural.AddMissingColumns
import org.evoleq.exposedx.migrations.structural.ColumnDef
import org.evoleq.exposedx.migrations.structural.StructuralMigrations
import org.evoleq.exposedx.migrations.structural.addColumnsIfMissing
import org.evoleq.exposedx.migrations.structural.modifyColumnNames
import org.evoleq.uuid.UUID_ZERO
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserModulesTable
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
        addMissingColumns = columnsToAdd,
        modifyColumnNames = columnNamesToModify
    )
}

val columnsToAdd: List<AddMissingColumns> by lazy {
    listOf(
        UsersTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        UserProfilesTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        AuctionsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        AuctionDetailsSolawiTuebingenTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        RoundsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        OrganizationsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        DistributionPointsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        SharesTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        ShareTypesTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        BankAccountsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        FiscalYears.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        AddressesTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        BiddersTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        BidderDetailsSolawiTuebingenTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        ContextsTable.addColumnsIfMissing(
  
          ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        Resources.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        RightsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        RolesTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
        ),
        ApplicationsTable.addColumnsIfMissing(
            ColumnDef.Missing<Boolean>("is_mandatory", false),
            ColumnDef.Missing("default_context_id", UUID_ZERO)
        ),
        ModulesTable.addColumnsIfMissing(
            ColumnDef.Missing<Boolean>("is_mandatory", false),
            ColumnDef.Missing("default_context_id", UUID_ZERO)
        ),
        UserApplicationsTable.addColumnsIfMissing(
            ColumnDef.Missing("lifecycle_stage_id", false),
            ColumnDef.Missing("context_id", UUID_ZERO)
        ),
        UserModulesTable.addColumnsIfMissing(
        ColumnDef.Missing("lifecycle_stage_id", false),
        ColumnDef.Missing("context_id", UUID_ZERO)
        ),
    )
}

val columnNamesToModify by lazy {
    listOf(
        UsersTable.modifyColumnNames(
            ColumnDef.ModifyName(
            "varchar",
            "password"
            ),
        )
    )
}
