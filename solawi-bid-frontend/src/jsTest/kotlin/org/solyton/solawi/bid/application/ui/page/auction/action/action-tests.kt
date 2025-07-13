package org.solyton.solawi.bid.application.ui.page.auction.action

import kotlinx.datetime.LocalDate
import org.evoleq.device.data.Device
import org.evoleq.ktorx.result.on
import org.evoleq.math.write
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.transform.bid.bidApplicationIso
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.bid.component.form.DEFAULT_AUCTION_ID
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.auction.Auction
import org.solyton.solawi.bid.module.bid.data.bidenv.Environment
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import org.solyton.solawi.bid.module.bid.data.api.Auction as ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.Auctions as ApiAuctions

class AuctionTests {

    @Test fun createAuctionTest() {
        val name = "name"
        installSerializers()
        val auctionLens = Lens<BidApplication, Auction>(
            get = {Auction(DEFAULT_AUCTION_ID,name, LocalDate(0,0,0))},
            set = {{it}}
        )

        createAuction(auctionLens)
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun readAuctionsTest() = runTest{
        val name = "name"
        installSerializers()
        val action = readAuctions()

        val apiAuction = ApiAuction("id", name, LocalDate(1,1,1))
        val apiAuctions = ApiAuctions(listOf(apiAuction))

        val application = BidApplication(Environment(),ActionDispatcher{}, )
        val domainAuctions = (action.writer.write(apiAuctions) on application).auctions
        assertEquals(1, domainAuctions.size)

        composition {
            val storage = TestStorage()

            (storage * bidApplicationIso * action.writer).write(apiAuctions) on Unit

            // assertEquals(1,(storage * auctions).read().size)
        }

    }
}
