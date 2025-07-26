package org.solyton.solawi.bid.module.bid.action.db

import io.ktor.util.*
import kotlinx.datetime.LocalDate
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.ktorx.result.map
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.db.schema.*
import java.util.*
import org.solyton.solawi.bid.module.bid.data.api.Auctions as ApiAuctions

@MathDsl
val CreateAuction = KlAction<Result<CreateAuction>, Result<ApiAuction>> {
    auction: Result<CreateAuction> -> DbAction {
        database -> auction bindSuspend  { data -> resultTransaction(database) {
            println("Create auction: ${data.name}")
            createAuction(data.name, data.date).toApiType()
        } }  x database
    }
}

fun Transaction.createAuction(name: String, date: LocalDate, type: String = "SOLAWI_TUEBINGEN"): AuctionEntity {
    val auctionType = AuctionType.find { AuctionTypes.type eq type.toUpperCasePreservingASCIIRules() }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuctionType(type)

    return AuctionEntity.new {
        this.name = name
        this.date = DateTime().withDate(date.year, date.monthNumber, date.dayOfMonth)
        this.type = auctionType
    }
}

@MathDsl
val ReadAuctions = KlAction<Result<GetAuctions>, Result<ApiAuctions>> {
    _ -> DbAction { database ->  resultTransaction(database){
        val auctions = readAuctions().map {
            it.toApiType().copy(
                bidderInfo = getBidderDetails(it).map {det  -> det.toBidderInfo()},
                auctionDetails = getAuctionDetails(it)
            )
        }
        ApiAuctions(auctions)


    // TODO(use identifier to return all auction which are accessible as identified person)
    } x database }
}

fun Transaction.getAuctionDetails(auction: AuctionEntity): AuctionDetails {
    val tue = AuctionDetailsSolawiTuebingen.find { AuctionDetailsSolawiTuebingenTable.auctionId eq auction.id.value }.firstOrNull()
    return if(tue != null) {
        AuctionDetails.SolawiTuebingen(
            tue.minimalBid,
            tue.benchmark,
            tue.targetAmount,
            tue.solidarityContribution
        )
    } else {
        AuctionDetails.Empty
    }
}

fun Transaction.readAuctions(): List<AuctionEntity> = with(AuctionEntity.all().map{it
}) {
   try {
       toList()
   } catch(exception: Exception)
   {
       listOf()
   }
}

@MathDsl
val ReadAuction = KlAction<Result<UUID>, Result<ApiAuction>> {
    auction -> DbAction {
        database ->  auction bindSuspend { resultTransaction(database) {
                readAuction(it).toApiType()

                // TODO(use identifier to return all auction which are accessible as identified person)
            }
        }  x database
    }}



fun Transaction.readAuction(auctionId: UUID): AuctionEntity {
    val auction = AuctionEntity.find { Auctions.id eq auctionId }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction

    return auction
}



@MathDsl
val DeleteAuctions = KlAction<Result<DeleteAuctions>, Result<GetAuctions>> {
    auctions -> DbAction {
        database -> auctions bindSuspend {
            data -> resultTransaction(database){
                deleteAuctions(data.auctionIds.map { UUID.fromString(it) })
        } } map { GetAuctions } x database
    }
}

fun Transaction.deleteAuctions(auctionIds: List<UUID>) {
    // todo:dev validation: There could be accepted auctions in the list -> What to do?
    Auctions.deleteWhere { Auctions.id inList auctionIds }
}

@MathDsl
val UpdateAuctions = KlAction<Result<UpdateAuctions>, Result<GetAuctions>> {
    auctions -> DbAction {
        database -> auctions bindSuspend {
            data -> resultTransaction(database) {
                 updateAuctions(data.list )
            }
        } map { GetAuctions } x database
    }
}


@Suppress("UNUSED_PARAMETER")
fun Transaction.updateAuctions(auctions: List<ApiAuction>) {
    TODO("Function updateAuctions not implemented yet! Do not forget validation! Auctions could be accepted")
}

@MathDsl
val ConfigureAuction = KlAction<Result<ConfigureAuction>, Result<ApiAuction>> {
    auction -> DbAction {
        database -> auction bindSuspend {
            data -> resultTransaction(database) {
                configureAuction(data)
            }
        }  x database
    }
}

fun Transaction.configureAuction(auction: ConfigureAuction): ApiAuction {
    val auctionId = UUID.fromString(auction.id)
    val auctionEntity = AuctionEntity.findById(auctionId)?:
        throw BidRoundException.NoSuchAuction

    validateAuctionNotAccepted(auctionId)

    val auctionDetails = setAuctionDetails(auctionEntity, auction.auctionDetails)

    return with(auctionEntity) {
        name = auction.name
        date = DateTime().withDate(auction.date.year, auction.date.monthNumber, auction.date.dayOfMonth)


        this
    }.toApiType().copy(
        bidderInfo = getBidderDetails(auctionEntity).map{det -> det.toBidderInfo()},
        auctionDetails = auctionDetails
    )
}

fun Transaction.setAuctionDetails(auction: AuctionEntity, auctionDetails: AuctionDetails): AuctionDetails =
    when(auctionDetails) {
        is AuctionDetails.Empty -> auctionDetails
        is AuctionDetails.SolawiTuebingen -> {
            val detailsEntity = AuctionDetailsSolawiTuebingen.find {
                AuctionDetailsSolawiTuebingenTable.auctionId eq auction.id.value
            }.firstOrNull()
            if(detailsEntity == null) {
                AuctionDetailsSolawiTuebingen.new {
                    this.auction = auction
                    benchmark = auctionDetails.benchmark
                    targetAmount = auctionDetails.targetAmount
                    solidarityContribution = auctionDetails.solidarityContribution
                    minimalBid = auctionDetails.minimalBid
                }
            } else {
                detailsEntity.benchmark = auctionDetails.benchmark
                detailsEntity.targetAmount = auctionDetails.targetAmount
                detailsEntity.solidarityContribution = auctionDetails.solidarityContribution
                detailsEntity.minimalBid = auctionDetails.minimalBid
            }
            auctionDetails
        }
    }

fun BidderDetails.toBidderInfo(): BidderInfo = when(this) {
    is BidderDetails.SolawiTuebingen -> BidderInfo(bidder.id.value.toString(), numberOfShares)
    // else -> throw Exception("No such BidderDetails")
}
