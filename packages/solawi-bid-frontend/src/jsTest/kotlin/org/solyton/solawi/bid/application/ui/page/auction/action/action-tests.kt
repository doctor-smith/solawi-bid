package org.solyton.solawi.bid.application.ui.page.auction.action

import kotlinx.datetime.LocalDate
import org.evoleq.ktorx.result.on
import org.evoleq.math.write
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.module.bid.component.form.DEFAULT_AUCTION_ID
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import org.solyton.solawi.bid.module.bid.data.api.Auction as ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.Auctions as ApiAuctions

class AuctionTests {

    @Test fun createAuctionTest() {
        val name = "name"
        installSerializers()
        val auctionLens = Lens<Application, Auction>(
            get = {Auction(DEFAULT_AUCTION_ID,name, LocalDate(0,0,0))},
            set = {{it}}
        )

        val action = createAuction(auctionLens)
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun readAuctionsTest() = runTest{
        val name = "name"
        installSerializers()
        val action = readAuctions()

        val apiAuction = ApiAuction("id","name", LocalDate(1,1,1))
        val apiAuctions = ApiAuctions(listOf(apiAuction))

        val application = Application(Environment())
        val domainAuctions = (action.writer.write(apiAuctions) on application).auctions
        assertEquals(1, domainAuctions.size)

        composition {
            val storage = TestStorage()

            (storage * action.writer).write(apiAuctions) on Unit

            assertEquals(1,(storage * auctions).read().size)
        }


    }
}