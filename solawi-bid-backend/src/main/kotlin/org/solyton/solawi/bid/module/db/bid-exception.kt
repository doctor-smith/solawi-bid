package org.solyton.solawi.bid.module.db

@Suppress("UnusedPrivateMember")
sealed class BidRoundException(override val message: String) : Exception(message)
{
    data object NoSuchRound: BidRoundException("Round not found") {
        private fun readResolve(): Any = NoSuchRound
    }

    data class NoSuchRoundState(val state: String): BidRoundException("No such RoundState: $state")
    data class LinkNotPresent(val link: String): BidRoundException("Link not present")
    data object RoundNotStarted: BidRoundException("Round not started") {
        private fun readResolve(): Any = RoundNotStarted
    }

    data class UnregisteredBidder(val username: String): BidRoundException("Unregistered bidder: $username")
    data class RegisteredBidderNotPartOfTheAuction(val username: String) : BidRoundException("bidder: $username is registered but not part of the auction")
    data object NoSuchAuction : BidRoundException("No such auction") {
        private fun readResolve(): Any = NoSuchAuction
    }
    data object AuctionAccepted : BidRoundException("Auction accepted; Editing impossible") {
        private fun readResolve(): Any = AuctionAccepted
    }

    data object MissingBidderDetails : BidRoundException("Missing bidder details") {
        private fun readResolve(): Any = MissingBidderDetails
    }
    data class NoSuchAuctionType(val type: String) : BidRoundException("No such auction type: '$type'")

    data class IllegalNumberOfParts(val value: Int): BidRoundException("Illegal number of parts: $value")

    data object NoSuchAcceptedRound: BidRoundException("AcceptedRound not found") {
        private fun readResolve(): Any = NoSuchAcceptedRound
    }
}
