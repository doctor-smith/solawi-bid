package org.solyton.solawi.bid.module.bid.action.db

import io.ktor.util.reflect.*
import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.api.CreateRound
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.bid.setupBidProcess
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class BidBests {

    @DbFunctional@Test
    fun bidProcessTest() = runSimpleH2Test(*tables) {
        val (_,round,bidder) = setupBidProcess()
        val link = round.link
        // set round state to "STARTED"
        changeRoundState(org.solyton.solawi.bid.module.bid.data.api.ChangeRoundState(
            round.id.value.toString(),
            RoundState.Started.toString()
        ))
        //round.state = RoundState.Started.toString()

        // perform action under consideration
        // Create a bid,
        // note: the link has to be provided by the auctioneer
        val bid = Bid(
            username = bidder.username,
            link = link,
            amount = 1.0
        )
        val storedBid = storeBid(bid).toApiType()

        // Assert
        val bidRounds = BidRound.all()

        assertEquals(1, bidRounds.count())
        assertEquals(storedBid, bidRounds.first().toApiType())

        val nextBid = bid.copy(amount = 2.0)
        val nextStoredBid = storeBid(nextBid).toApiType()

        val nextBidRounds = BidRound.all()
        assertEquals(1, nextBidRounds.count())
        assertEquals(nextStoredBid, nextBidRounds.first().toApiType())
    }
    @DbFunctional@Test fun validateLinkIsResent() = runSimpleH2Test(*tables) {
        val (_,round,bidder) = setupBidProcess()
        // perform action under consideration
        // Create a bid,
        // note: the link has to be provided by the auctioneer
        val bid = Bid(
            username = bidder.username,
            link = round.link + "_mess-it-up",
            amount = 1.0
        )
        val storedBid= try {
             storeBid(bid)
        } catch (e: Exception) {
            e
        }

        assertTrue { storedBid.instanceOf(BidRoundException.LinkNotPresent::class) }
    }
    @DbFunctional@Test fun validateBidderExists() = runSimpleH2Test(*tables) {
        val (_,round,bidder) = setupBidProcess()
        // perform action under consideration
        // Create a bid,
        // note: the link has to be provided by the auctioneer
        val bid = Bid(
            username = bidder.username+  "_mess-it-up",
            link = round.link ,
            amount = 1.0
        )
        val storedBid= try {
            storeBid(bid)
        } catch (e: Exception) {
            e
        }

        assertTrue { storedBid.instanceOf(BidRoundException.UnregisteredBidder::class) }
    }
    @DbFunctional@Test fun validateBidderIsPartOfTheAuction() = runSimpleH2Test(*tables) {
        val (_,round,bidder) = setupBidProcess()
        AuctionBidders.deleteAll()
        // perform action under consideration
        // Create a bid,
        // note: the link has to be provided by the auctioneer
        val bid = Bid(
            username = bidder.username,
            link = round.link ,
            amount = 1.0
        )
        val storedBid= try {
            storeBid(bid)
        } catch (e: Exception) {
            e
        }

        assertTrue { storedBid.instanceOf(BidRoundException.RegisteredBidderNotPartOfTheAuction::class) }
    }
    @DbFunctional@Test fun validateRoundIsStarted() = runSimpleH2Test(*tables) {
        val (_,round,bidder) = setupBidProcess()
        // perform action under consideration
        // Create a bid,
        // note: the link has to be provided by the auctioneer
        val bid = Bid(
            username = bidder.username,
            link = round.link ,
            amount = 1.0
        )
        val storedBid= try {
            storeBid(bid)
        } catch (e: Exception) {
            e
        }

        assertTrue { storedBid.instanceOf(BidRoundException.RoundNotStarted::class) }
    }

    @DbFunctional@Test fun validateRoundNumbers() = runSimpleH2Test(*tables){
        val (auction,round,_) = setupBidProcess()

        assertEquals(1, auction.rounds.count())
        assertEquals(1,round.number)
        // start with 2
        (2..10).forEach { number ->
            val newRound = addRound(CreateRound(auction.id.value.toString()))
            assertEquals(number,newRound.number)
        }

        assertEquals(10L,auction.rounds.count())
    }

    // Setup
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private val tables = arrayOf(
        AuctionBidders,
        AcceptedRoundsTable,
        BidRounds,
        Auctions,
        Bidders,
        Rounds,
        AuctionTypes,
        AuctionDetailsSolawiTuebingenTable,
        BidderDetailsSolawiTuebingenTable,
        RoundCommentsTable,
        ContextsTable
    )

    data class BidProcessSetup(
        val auction: Auction,
        val round: Round,
        val bidder: Bidder
    )


}
