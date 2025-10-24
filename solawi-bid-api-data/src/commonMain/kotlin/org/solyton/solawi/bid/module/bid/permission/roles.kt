package org.solyton.solawi.bid.module.bid.permission

import org.evoleq.permission.Owner
import org.evoleq.permission.User
import org.evoleq.value.StringValueWithDescription

object Role {
    val user = User
    val owner = Owner
    val bidder = Bidder
    val auctionManager = AuctionManager
    val auctionTeamManager = AuctionTeamManager
    val auctionTeammate = AuctionTeammate
    val auctionModerator = AuctionModerator
}

object Bidder : StringValueWithDescription {
    override val value  = "BIDDER"
    override val description: String = "Participant in a bid-round, context: AUCTION"
}

object AuctionManager : StringValueWithDescription {
    override val value  = "AUCTION_MANAGER"
    override val description: String = ""
}

object AuctionTeammate : StringValueWithDescription {
    override val value  = "AUCTION_TEAMMATE"
    override val description: String = ""
}
object AuctionTeamManager : StringValueWithDescription {
    override val value  = "AUCTION_TEAM_MANAGER"
    override val description: String = ""
}

object AuctionModerator : StringValueWithDescription {
    override val value  = "AUCTION_MODERATOR"
    override val description: String = ""
}
