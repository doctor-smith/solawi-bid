package org.solyton.solawi.bid.application.data.db

import org.jetbrains.exposed.sql.Table
import org.solyton.solawi.bid.module.banking.schema.BankAccounts
import org.solyton.solawi.bid.module.banking.schema.FiscalYears
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.permission.schema.*
import org.solyton.solawi.bid.module.user.schema.*
import java.io.File


fun main() {
    val fullDiagram = generateMermaidER(
        listOf(
            // Users
            Users,
            UserProfiles,
            Addresses,
            Organisations,
            UserOrganization,
            UserRoleContext,
            Tokens,
            Sessions,

            // Banking
            BankAccounts,
            FiscalYears,

            // Bid

            // Permission
            Contexts,
            Resources,
            Rights,
            Roles,
            RoleRightContexts,
            UserRoleContext
        )
    )

    val bidERM = generateMermaidER(
        listOf(
            AcceptedRounds,
            AuctionsTable,
            AuctionBiddersTable,
            AuctionDetailsSolawiTuebingenTable,
            AuctionTypes,

            Bidders,
            BidderDetailsSolawiTuebingenTable,
            DistributionPointsTable,
            RoundsTable,
            BidRounds,
            SharesTable,
            ShareTypesTable,
            SearchBiddersTable
        )

    )

    val folder = "/data/sync/projects/IdeaProjects/solyton/solawi-bid/solawi-bid-backend/src/main/kotlin/org/solyton/solawi/bid/application/data/db"
    val file = File(folder, "erm.md")
    file.writeText("""# ERM Complete
        |```mermaid
        |$fullDiagram
        |```
        |
        |## Bid Schema
        |```mermaid
        |$bidERM
        |```
        |
        |## Permission ERM
        |```mermaid
        |${generateMermaidER(permissionErmTables)}
        |```
    """.trimMargin())
    // println(diagram)
}

val permissionErmTables = listOf<Table>(
    Contexts,
    Roles,
    Rights,
    Resources,
    // Users,
    RoleRightContexts,
    //UserRoleContext

)
