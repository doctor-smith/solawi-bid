package org.solyton.solawi.bid.module.bid.service

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.UserApplication
import org.solyton.solawi.bid.module.application.schema.UserApplicationEntity
import org.solyton.solawi.bid.module.application.schema.UserApplicationsTable
import org.solyton.solawi.bid.module.bid.schema.Auction
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.permission.schema.repository.RightRoleContextIds
import org.solyton.solawi.bid.module.permission.schema.repository.cloneRightRoleContextWrtRoles
import org.solyton.solawi.bid.module.permission.schema.repository.createChild
import java.util.UUID

/**
 * Creates a new standalone context under the user’s auctionApplication and updates auction.context if it already exists
 */
fun Transaction.createStandaloneAuctionContext(userId: UUID, auction: Auction): List<RightRoleContextIds> {
    val userAuctionApplication = getAuctionUserApplication(userId)
    val auctionContext = userAuctionApplication.context.createChild("AUCTION.${auction.id}")
    // set auction context
    auction.context = auctionContext
    // clone default context
    val rrc = cloneDefaultAuctionContext(
        userId = userId,
        targetContextId = auctionContext.id.value,
        targetAuctionUserApplication = userAuctionApplication
    )
    // add user as owner
    addUserAsOwnerToContext(userId, auctionContext.id.value)
    // return rrc data
    return rrc
}

/**
 * Attaches an auction context to a target context and updates auction.context to accordingly
 */
fun Transaction.attachAuctionContext(userId: UUID, auction: Auction, targetContext: ContextEntity): List<RightRoleContextIds>{
    val userAuctionApplication = getAuctionUserApplication(userId)
    // set auctionContext
    auction.context = targetContext
    // clone default context
    val rrc = cloneDefaultAuctionContext(
        userId = userId,
        targetContextId = targetContext.id.value,
        targetAuctionUserApplication = userAuctionApplication
    )
    // add user as owner
    addUserAsOwnerToContext(userId, targetContext.id.value)
    // return rrc data
    return rrc
}

fun Transaction.getAuctionUserApplication(userId: UUID): UserApplication {
    val auctionApplication = ApplicationEntity.find {
        ApplicationsTable.name eq "AUCTIONS"
    }.firstOrNull() ?: throw ApplicationException.NoSuchApplication("AUCTIONS")
    val userAuctionApplication = UserApplicationEntity.find {
        (UserApplicationsTable.userId eq userId) and
                (UserApplicationsTable.applicationId eq auctionApplication.id)
    }.firstOrNull()?: throw ApplicationException.NoSuchApplication( "User owns no Auction Application")
    return userAuctionApplication
}

fun Transaction.cloneDefaultAuctionContext(userId: UUID, targetContextId: UUID, targetAuctionUserApplication: UserApplication? = null): List<RightRoleContextIds> {
    val userAuctionApplication = targetAuctionUserApplication?: getAuctionUserApplication(userId)
    // Necessary Roles:
    // USER
    // BIDDER
    // OWNER
    // AUCTION_TEAMMATE
    // AUCTION_MANAGER
    // AUCTION_TEAM_MANAGER
    // AUCTION_MODERATOR
    val defaultAuctionContext = cloneRightRoleContextWrtRoles(
        userAuctionApplication.context.id.value,
        targetContextId,
        "USER",
        "BIDDER",
        "OWNER",
        "AUCTION_TEAMMATE",
        "AUCTION_MANAGER",
        "AUCTION_TEAM_MANAGER",
        "AUCTION_MODERATOR",
    )
    return defaultAuctionContext
}

/**
 * Adds a user to the context as the owner.
 * The user will also retain any other auction roles, except the bidder role
 */
fun Transaction.addUserAsOwnerToContext(userId: UUID, contextId: UUID) : List<UUID>  {
    return assignAuctionRoles(
        userId,
        contextId, "USER",
        "OWNER",
        "AUCTION_TEAMMATE",
        "AUCTION_MANAGER",
        "AUCTION_TEAM_MANAGER",
        "AUCTION_MODERATOR",
    )
}

@Suppress("UnusedParameter")
fun Transaction.assignAuctionRoles(uuid: UUID, contextId: UUID, vararg roleNames: String) : List<UUID> {
    val roles = RoleEntity.find { RolesTable.name inList listOf(*roleNames) }.toList()
    return roles.map{role ->
        UserRoleContext.insertAndGetId {
            it[UserRoleContext.userId] = uuid
            it[roleId] = role.id
            it[UserRoleContext.contextId] = contextId
        }.value
    }
}
