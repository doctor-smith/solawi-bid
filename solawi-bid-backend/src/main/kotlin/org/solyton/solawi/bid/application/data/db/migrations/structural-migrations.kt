package org.solyton.solawi.bid.application.data.db.migrations

import org.evoleq.exposedx.migrations.structural.*
import org.evoleq.uuid.UUID_ZERO
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserModulesTable
import org.solyton.solawi.bid.module.banking.schema.AccountType
import org.solyton.solawi.bid.module.banking.schema.BankAccountsTable
import org.solyton.solawi.bid.module.banking.schema.FiscalYears
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.distribution.schema.DistributionPointsTable
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.Resources
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.shares.schema.ShareStatusTable
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionsTable
import org.solyton.solawi.bid.module.shares.schema.ShareTypesTable
import org.solyton.solawi.bid.module.user.schema.*
import java.util.*


val structuralMigrations by lazy {
    StructuralMigrations(
        addMissingColumns = columnsToAdd,
        modifyColumnNames = columnNamesToModify,
        modifyColumnProperties = columnPropertiesToModify,
        modifyTableChecks = tableChecks,
        modifyTableUniques = uniqueIndexes
    )
}

val tableChecks: List<TableDef.CheckConstraint> by lazy {
    listOf(
        TableDef.CheckConstraint.Update(
            UsersTable,
            "password_vs_status",
            """
                (status IN ('PENDING', 'INVITED') AND password IS NULL)
                OR
                (status IN ('ACTIVE', 'DISABLED', 'REGISTERED') AND password IS NOT NULL)
            """.trimIndent()
        )
    )
}

val uniqueIndexes: List<TableDef.UniqueIndex> by lazy {
    listOf(
        /*
        TableDef.UniqueIndex.Update(
            "provider_key",
            ShareTypesTable,
            listOf("share_key", "provider_id")
        )

         */
    )
}

val columnsToAdd: List<AddMissingColumns> by lazy {
    listOf(
        UsersTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
            ColumnDef.Missing<UserStatus>("status", default = UserStatus.ACTIVE)
        ),
        UserProfilesTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
            ColumnDef.Missing<String>("first_name", ""),
            ColumnDef.Missing<String>("last_name", ""),
            ColumnDef.Missing<String?>("title", null),
            ColumnDef.Missing<String?>("phone_number_1", null)
        ),
        AuctionsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
            ColumnDef.Missing<UUID>("context_id", UUID_ZERO)
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
        ShareSubscriptionsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
            ColumnDef.Missing<UUID?>("sepa_mandate_id", null),
        ),
        ShareTypesTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
            ColumnDef.Missing<String>("share_key", ""),
            ColumnDef.Missing<UUID>("provider_id", UUID_ZERO),
        ),
        BankAccountsTable.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
            ColumnDef.Missing<String>("account_holder", ""),
            ColumnDef.Missing<Boolean>("is_active", true),
            ColumnDef.Missing<String>("account_type", AccountType.DEBTOR.name ),
        ),
        FiscalYears.addColumnsIfMissing(
            ColumnDef.Missing<UUID>("created_by", UUID_ZERO),
            ColumnDef.Missing<DateTime>("created_at", DateTime.now()),
            ColumnDef.Missing<UUID?>("modified_by",null),
            ColumnDef.Missing<DateTime?>("modified_at", null),
            ColumnDef.Missing<UUID>("legal_entity_id", UUID_ZERO)
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
        RoundsTable.addColumnsIfMissing(
            ColumnDef.Missing("number", 0)
        )
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

val columnPropertiesToModify by lazy {
    listOf(
        ShareStatusTable.modifyColumnProperties(
            ColumnDef.ModifyProperties.Varchar(
                "name",
                newLength = 50,
            )
        ),
        UsersTable.modifyColumnProperties(
            ColumnDef.ModifyProperties.Varchar(
                "password",
                nullable = true
            ),
            ColumnDef.ModifyProperties.Varchar(
                "username",
                newLength = 100,
            )
        )
    )
}
