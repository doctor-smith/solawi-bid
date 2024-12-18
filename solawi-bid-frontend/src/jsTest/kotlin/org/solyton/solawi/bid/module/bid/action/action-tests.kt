package org.solyton.solawi.bid.module.bid.action

import kotlinx.datetime.LocalDate
import org.evoleq.ktorx.result.on
import org.evoleq.math.emit
import org.evoleq.math.write
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.bidRounds
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.toDomainType
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionTests{
    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun deleteAuctionsTest() = runTest{
        val name = "name"
        installSerializers()
        val auction: Auction = Auction("id",name, LocalDate(1,1,1))
        val action = deleteAuctionAction(auction)

        val apiAuction = ApiAuction("id",name, LocalDate(1,1,1))
        val apiAuctions = ApiAuctions(listOf(apiAuction))


        val application = Application(Environment())
        val domainAuctions = (action.writer.write(apiAuctions) on application).auctions
        assertEquals(1, domainAuctions.size)

        composition {
            val storage = TestStorage()

            (storage * action.writer).write(apiAuctions) on Unit

            assertEquals(1,(storage * auctions).read().size)

            assertEquals(auction.auctionId, (storage * action.reader).emit().auctionIds.first())

            (storage * action.writer).write(ApiAuctions()) on Unit

            assertEquals(0,(storage * auctions).read().size)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun sendBidTest() = runTest{
        val bid = Bid("name", "link",2.0)

        val action = sendBidAction(bid)


        composition {
            val storage = TestStorage()

            val apiBid = (storage * action.reader).emit()

            assertEquals(bid.username, apiBid.username)
            assertEquals(bid.amount, apiBid.amount)

            val apiBidRound = ApiBidRound(
                "",
                Round("","", ""),
                ApiAuction(
                    "",
                    "",
                    LocalDate(1,1,1),
                    listOf(),
                    listOf(),

                ),
                null
            )

            (storage * action.writer).write(apiBidRound) on Unit

            val storedBidRound = (storage * bidRounds).read().first()

            assertEquals(apiBidRound.toDomainType(), storedBidRound )
        }
    }
}