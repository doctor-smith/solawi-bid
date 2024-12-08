package org.solyton.solawi.bid.module.bid.action.db

import kotlinx.datetime.LocalDate
import org.evoleq.exposedx.test.runSimpleH2Test
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.bid.data.api.NewBidder
import org.solyton.solawi.bid.module.bid.data.api.PreRound
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.db.schema.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class AuctionTests {

    @DbFunctional@Test fun createAuction() = runSimpleH2Test(
        AuctionBidders,
        Auctions,
        Bidders,
        Rounds
    ) {

    }

    @DbFunctional@Test fun prepareAuction() = runSimpleH2Test(
        AuctionBidders,
        Auctions,
        Bidders,
        Rounds
    ) {
        val name = "TestAuction"
        val link = "TestLink"

        val auction = createAuction(name,LocalDate(0,1,1)).toApiType()
        assertEquals(name, auction.name)
        val round = addRound(PreRound(
            auction.id,
        )).toApiType()
        assertNotEquals(link, round.link)

        val bidders = listOf<NewBidder>(
            NewBidder("name1",1,1),
            NewBidder("name2",3,1),
            NewBidder("name3",3,1),
            NewBidder("name4",4,1)
        )

        val auctionWithBidders = addBidders(
            auctionId = UUID.fromString( auction.id),
            bidders
        ).toApiType()
        assertEquals(bidders.size, auctionWithBidders.bidderIds.size)

    }

    @DbFunctional@Test
    fun deleteAuction() = runSimpleH2Test(
        AuctionBidders,
        Auctions,
        Bidders,
        Rounds
    ) {
        val auction = AuctionEntity.new {
            name = "TestAuction"
            date = DateTime().withDate(1,1,1)
        }

        deleteAuctions(listOf(auction.id.value))

        val a = AuctionEntity.find { Auctions.id eq auction.id }.firstOrNull()
        assertNull(a)
    }
}