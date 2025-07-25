package org.solyton.solawi.bid.application.data.db

import org.jetbrains.exposed.sql.Table
import org.solyton.solawi.bid.module.banking.schema.BankAccounts
import org.solyton.solawi.bid.module.banking.schema.FiscalYears
import org.solyton.solawi.bid.module.bid.schema.AcceptedRounds
import org.solyton.solawi.bid.module.bid.schema.AuctionBiddersTable
import org.solyton.solawi.bid.module.bid.schema.AuctionDetailsSolawiTuebingenTable
import org.solyton.solawi.bid.module.bid.schema.AuctionTypes
import org.solyton.solawi.bid.module.bid.schema.AuctionsTable
import org.solyton.solawi.bid.module.bid.schema.BidRounds
import org.solyton.solawi.bid.module.bid.schema.BidderDetailsSolawiTuebingenTable
import org.solyton.solawi.bid.module.bid.schema.Bidders
import org.solyton.solawi.bid.module.bid.schema.DistributionPointsTable
import org.solyton.solawi.bid.module.bid.schema.RoundsTable
import org.solyton.solawi.bid.module.bid.schema.SearchBiddersTable
import org.solyton.solawi.bid.module.bid.schema.ShareTypesTable
import org.solyton.solawi.bid.module.bid.schema.SharesTable
import org.solyton.solawi.bid.module.permission.schema.Contexts
import org.solyton.solawi.bid.module.permission.schema.Resources
import org.solyton.solawi.bid.module.permission.schema.Rights
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import org.solyton.solawi.bid.module.permission.schema.Roles
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.user.schema.Addresses
import org.solyton.solawi.bid.module.user.schema.Organisations
import org.solyton.solawi.bid.module.user.schema.Sessions
import org.solyton.solawi.bid.module.user.schema.Tokens
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import org.solyton.solawi.bid.module.user.schema.UserProfiles
import org.solyton.solawi.bid.module.user.schema.Users
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
