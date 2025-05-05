package org.solyton.solawi.bid.module.bid.action

import kotlinx.datetime.LocalDate
import org.evoleq.ktorx.result.on
import org.evoleq.math.emit
import org.evoleq.math.write
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.times
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.auctions
import org.solyton.solawi.bid.application.data.bidRounds
import org.solyton.solawi.bid.application.data.bidderMailAddresses
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.application.ui.page.auction.action.*
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.bidder.BidderInfo
import org.solyton.solawi.bid.module.bid.data.rawResults
import org.solyton.solawi.bid.module.bid.data.rounds
import org.solyton.solawi.bid.module.bid.data.toDomainType
import org.solyton.solawi.bid.test.storage.TestStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.solyton.solawi.bid.module.bid.data.Round as DomainRound
import org.solyton.solawi.bid.module.bid.data.evaluation.WeightedBid as DomainWeightedBid

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
                null,
                null
            )

            (storage * action.writer).write(apiBidRound) on Unit

            val storedBidRound = (storage * bidRounds).read().first()

            assertEquals(apiBidRound.toDomainType(true), storedBidRound )
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test
    fun importBiddersTest() = runTest{
        val auction = Auction("id", "name", LocalDate(1,1,1))
        val newBidders = listOf(NewBidder("un", 0, 1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }
        val action = importBidders(newBidders, auctionLens)

        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)
            assertEquals(auction, (storage * auctionLens).read())

            val importBidders = (storage * action.reader).emit()
            assertEquals(importBidders.auctionId, auction.auctionId)
            assertEquals(importBidders.bidders, newBidders)

            val apiAuction = ApiAuction(
                id ="id",
                name= "name",
                date = LocalDate(1,1,1),
                rounds = listOf(),
                bidderInfo = listOf(ApiBidderInfo("1",1))
            )

            (storage * action.writer).write(apiAuction) on Unit

            val nextAuction = (storage * auctionLens).read()
            assertEquals( listOf(BidderInfo("1", 1)), nextAuction.bidderInfo,)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun createRoundTest() = runTest{
        val auction = Auction("id", "name", LocalDate(1,1,1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }

        val round = Round(
            "id",
            "link",
            RoundState.Started.toString()
        )

        val createAuction = createAuction(auctionLens)

        val action = createRound(auctionLens)

        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)

            // create an auction
            val apiAuction = ApiAuction(
                id ="id",
                name= "name",
                date = LocalDate(1,1,1),
                rounds = listOf(),
                bidderInfo = listOf(ApiBidderInfo("1",1)),
                auctionDetails = AuctionDetails.SolawiTuebingen(
                    2.0,2.0,2.0,2.0
                )
            )
            (storage * createAuction.writer).write(apiAuction) on Unit

            // create round and put it into auction
            (storage * action.writer).write(round) on Unit

            // Check results
            val storedAuction = (storage * auctionLens).read()

            assertEquals(1, storedAuction.rounds.size)
            assertTrue { storedAuction.rounds.contains(round.toDomainType()) }
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun configureAuctionTest() = runTest {
        val auction = Auction("id", "name", LocalDate(1,1,1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }

        val action = configureAuction(auctionLens)


        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)


            val apiAuction = ApiAuction(
                id ="id",
                name= "name",
                date = LocalDate(1,1,1),
                rounds = listOf(),
                bidderInfo = listOf(ApiBidderInfo("1",1)),
                auctionDetails = AuctionDetails.SolawiTuebingen(
                    2.0,2.0,2.0,2.0
                )
            )

            (storage * action.writer).write(apiAuction) on Unit

            val storedAuction = (storage * auctionLens).read()
            assertEquals(2.0, storedAuction.auctionDetails.benchmark)
            assertEquals(2.0, storedAuction.auctionDetails.minimalBid)
            assertEquals(2.0, storedAuction.auctionDetails.targetAmount)
            assertEquals(2.0, storedAuction.auctionDetails.solidarityContribution)

        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun changeRoundStateTest() = runTest {
        val auction = Auction("id", "name", LocalDate(1,1,1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }
        val roundLens = auctionLens * rounds * FirstBy { r:DomainRound -> r.roundId == "id" }

        val round = Round(
            "id",
            "link",
            RoundState.Opened.toString()
        )

        val createRound = createRound(auctionLens)
        val changeRoundState = changeRoundState(RoundState.Started, roundLens)

        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)

            (storage * createRound.writer).write(round) on Unit
            val initStoredRound = (storage * roundLens).read()
            assertEquals(round.toDomainType() , initStoredRound)

            val nextRound = Round(
                "id",
                "link",
                RoundState.Started.toString()
            )

            (storage * changeRoundState.writer).write(nextRound) on Unit

            val storedRound = (storage * roundLens).read()
            assertEquals(nextRound.toDomainType() , storedRound)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun exportBidRoundTest() = runTest {
        val auction = Auction("id", "name", LocalDate(1, 1, 1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }
        val roundLens = auctionLens * rounds * FirstBy { it.roundId == "id" }
        val round = Round(
            "id",
            "link",
            RoundState.Opened.toString()
        )

        val createRound = createRound(auctionLens)
        val export = exportBidRoundResults("id",roundLens)

        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)
            (storage * createRound.writer).write(round) on Unit

            // Check Reader of action
            val dto = (storage * export.reader).emit()
            assertEquals(ExportBidRound("id", "id"), dto)

            // Check Writer of action
            val results = ApiBidRoundResults(
                "id",
                listOf(
                    ApiBidResult(
                        "username",
                        2,
                        100.0,
                        true
                    )
                )
            )
            (storage * export.writer).write(results) on Unit
            val storedResults = (storage * roundLens * rawResults).read()
            val domainResults = results.toDomainType(true)
            assertEquals(domainResults, storedResults)
            assertEquals(domainResults.bidRoundResults,storedResults.bidRoundResults)
            assertEquals(domainResults.startDownloadOfBidRoundResults, storedResults.startDownloadOfBidRoundResults)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun evaluateBidRoundTest() = runTest {
        val auction = Auction("id", "name", LocalDate(1,1,1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }
        val roundLens = auctionLens * rounds * FirstBy { r:DomainRound -> r.roundId == "id" }

        val round = Round(
            "id",
            "link",
            RoundState.Opened.toString()
        )

        val createRound = createRound(auctionLens)
        val evaluateBidRound = evaluateBidRound("id",roundLens)

        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)
            (storage * createRound.writer).write(round) on Unit

            val evaluation = ApiBidRoundEvaluation(
                auctionDetails = AuctionDetails.SolawiTuebingen(
                    20.0,
                    80.0,
                    100_000.0,
                    5.0
                ),
                totalSumOfWeightedBids = 300.0,
                totalNumberOfShares = 3,
                weightedBids = listOf(ApiWeightedBid(
                    3, 100.0
                ))
            )

            // Check reader of action
            // read evaluation
            val dto: EvaluateBidRound = (storage * evaluateBidRound.reader).emit()
            // assert
            assertEquals(EvaluateBidRound("id", "id"), dto)

            // Check writer of action
            // write evaluation
            (storage * evaluateBidRound.writer).write(evaluation) on Unit

            // assertions
            val storedEvaluation = (storage * roundLens).read().bidRoundEvaluation
            // details
            val details = storedEvaluation.auctionDetails
            assertEquals(20.0, details.minimalBid)
            assertEquals(80.0, details.benchmark)
            assertEquals(100_000.0, details.targetAmount)
            assertEquals(5.0, details.solidarityContribution)
            // statistics
            assertEquals (300.0, storedEvaluation.totalSumOfWeightedBids )
            assertEquals(300.0, storedEvaluation.totalSumOfWeightedBids)
            assertEquals(3, storedEvaluation.totalNumberOfShares)
            assertEquals<List<DomainWeightedBid>>(listOf(DomainWeightedBid(
                3, 100.0
            )), storedEvaluation.weightedBids)

        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun preEvaluateBidRoundTest() = runTest {
        val auction = Auction("id", "name", LocalDate(1, 1, 1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }
        val roundLens = auctionLens * rounds * FirstBy { r: DomainRound -> r.roundId == "id" }

        val round = Round(
            "id",
            "link",
            RoundState.Opened.toString()
        )

        val createRound = createRound(auctionLens)
        val preEvaluateBidRound = preEvaluateBidRound("id",roundLens)

        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)
            (storage * createRound.writer).write(round) on Unit

            val evaluation = ApiBidRoundPreEvaluation(
                auctionDetails = AuctionDetails.SolawiTuebingen(
                    20.0,
                    80.0,
                    100_000.0,
                    5.0
                ),
                totalNumberOfShares = 3,
            )


            // Check reader of action
            // read evaluation
            val dto: PreEvaluateBidRound = (storage * preEvaluateBidRound.reader).emit()
            // assert
            assertEquals(PreEvaluateBidRound("id", "id"), dto)

            // Check writer of action
            // write evaluation
            (storage * preEvaluateBidRound.writer).write(evaluation) on Unit

            // assertions
            val storedEvaluation = (storage * roundLens).read().preEvaluation
            // details
            val details = storedEvaluation.auctionDetailsPreEval
            assertEquals(20.0, details.minimalBid)
            assertEquals(80.0, details.benchmark)
            assertEquals(100_000.0, details.targetAmount)
            assertEquals(5.0, details.solidarityContribution)
            // statistics
            assertEquals(3, storedEvaluation.totalNumberOfSharesPreEval)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun acceptRoundTest() = runTest {
        val auction = Auction("id", "name", LocalDate(1, 1, 1))
        val auctionLens = auctions * FirstBy<Auction> { auc -> auc.auctionId == auction.auctionId }

        val round = Round(
            "id",
            "link",
            RoundState.Opened.toString()
        )

        val createRound = createRound(auctionLens)
        val acceptRound = acceptRound(auctionLens, "id")

        composition {
            val storage = TestStorage()
            (storage * auctionLens).write(auction)
            (storage * createRound.writer).write(round) on Unit

            val acceptedRound = AcceptedRound("id")

            // Check reader of action
            // read evaluation
            val dto: AcceptRound = (storage * acceptRound.reader).emit()
            // assert
            assertEquals(AcceptRound("id", "id"), dto)

            // Check writer of action
            // write evaluation
            (storage * acceptRound.writer).write(acceptedRound) on Unit

            // assertions
            val storedAuction = (storage * auctionLens).read()

            assertEquals("id", storedAuction.acceptedRoundId)
        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun addBidders() = runTest {

        val bidders = AddBidders()
        val addBidders = addBidders(bidders)
        composition {
            val storage = TestStorage()

            // Read
            val biddersData = (storage * addBidders.reader).emit()
            assertEquals(bidders, biddersData)

        }
    }

    @OptIn(ComposeWebExperimentalTestsApi::class)
    @Test fun searchBidders() = runTest {

        val bidders = SearchBidderData(
            "",
            "",
            "",
        )
        val addBidders = searchUsernameOfBidder(bidders)
        composition {
            val storage = TestStorage()

            // Read
            val biddersData = (storage * addBidders.reader).emit()
            assertEquals(bidders, biddersData)

            // Write
            val mails = listOf("dev")
            (storage * addBidders.writer).write(BidderMails(mails)) on Unit

            val storedNails = (storage * bidderMailAddresses.get).emit()
            assertEquals(mails, storedNails.emails)

        }
    }

}
