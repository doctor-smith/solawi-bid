package org.solyton.solawi.bid.module.bid.action.db

import io.ktor.util.*
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.db.schema.*
import org.solyton.solawi.bid.module.db.schema.Auction
import org.solyton.solawi.bid.module.db.schema.Auctions
import org.solyton.solawi.bid.module.db.schema.Bidder
import java.util.*

@MathDsl
val ImportBidders = KlAction{bidders: Result<ImportBidders> -> DbAction {
    database: Database -> bidders bindSuspend  {
        resultTransaction(database) {
            importBidders(auctionId = UUID.fromString(it.auctionId), it.bidders).toApiType().copy(
                bidderInfo = getBidderDetails(it.bidders).map{det -> det.toBidderInfo()}
            )
        }
    } x database
}}

fun Transaction.importBidders(auctionId: UUID, newBidders: List<NewBidder>): AuctionEntity {
    val auction = AuctionEntity.find { Auctions.id eq auctionId }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction

    validateAuctionNotAccepted(auction)

    // There are four kinds of bidders to consider
    // 1. Bidders to add
    // 2. Bidders to keep
    // 3. Bidders to be deleted from the auction only (belong to other auctions)
    // 4. Bidders to be deleted completely

    // Bidders to keep:
    // newBidders that are already listed in the auction.
    val biddersToKeep = auction.bidders.filter{ bidder -> newBidders.map { it.username }.contains(bidder.username)}

    // Bidders to be deleted from auction:
    // All bidders that are part of the auction, but not listed in newBidders
    val biddersToBeDeletedFromAuction = auction.bidders.filter { !biddersToKeep.contains(it)  }

    // Bidders to be deleted completely:
    // All bidders that
    // - are part of the auction
    // - not part of any other auction
    // - not listed in newBidders
    val biddersToBeDeletedCompletely = biddersToBeDeletedFromAuction.filter {
        it.auctions.count() == 1L
    }

    // Bidders to add:
    // All newBidders that are not listed in the auction
    // -> The rest is done by the function addBidders!
    val biddersToAdd = newBidders.filter {
        newBidder -> !biddersToKeep.map { it.username }.contains(newBidder.username)
    }

    AuctionBidders.deleteWhere { bidderId inList biddersToBeDeletedFromAuction.map { it.id } }

    BiddersTable.deleteWhere { BiddersTable.id inList biddersToBeDeletedCompletely.map { it.id } }
    BidderDetailsSolawiTuebingenTable.deleteWhere { bidderId inList biddersToBeDeletedCompletely.map { it.id }  }

    return addBidders(auction,biddersToAdd)
}

fun Transaction.getBidderDetails(bidder: Bidder): BidderDetailsEntity =
    BidderDetailsSolawiTuebingenEntity.find {
        BidderDetailsSolawiTuebingenTable.bidderId eq bidder.id.value
    }.firstOrNull()
    ?: throw BidRoundException.MissingBidderDetails

fun Transaction.getBidderDetails(auction: Auction): SizedIterable<BidderDetailsEntity> {
    val bidderIds = auction.bidders.map { it.id.value }
    val details = BidderDetailsSolawiTuebingenEntity.find {
        BidderDetailsSolawiTuebingenTable.bidderId inList bidderIds
    }
    return details
}

fun Transaction.getBidderDetails(bidders: List<NewBidder>): SizedIterable<BidderDetailsEntity> {
    val usernames = bidders.map { it.username }
    val bidderIds = BidderEntity.find{ BiddersTable.username inList usernames }.map { it.id }.toList()
    val details = BidderDetailsSolawiTuebingenEntity.find {
        BidderDetailsSolawiTuebingenTable.bidderId inList bidderIds
    }
    return details
}

internal fun Transaction.addBidders(auction: AuctionEntity, newBidders: List<NewBidder>, type: String = "SOLAWI_TUEBINGEN"): AuctionEntity {
    val auctionType = AuctionType.find { AuctionTypes.type eq type.toUpperCasePreservingASCIIRules() }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuctionType(type)
    val typeName = auctionType.type.toLowerCasePreservingASCIIRules()

    // There are different kinds of newBidders to consider
    // 1. known bidders listed in Bidders and AuctionBidders
    // 2. known bidders listed only in Bidders
    // 3. bidders to be created

    // Known bidders:
    // All bidders that are already listed in Bidders
    val knownBidders = Bidder.find{ BiddersTable.username inList newBidders.map { it.username } }
    val knownBiddersUsernames = knownBidders.map { it.username }

    val knownBiddersToBeAddedToAuction = knownBidders.filter { bidder -> !auction.bidders.contains(bidder)  }

    // Other bidders:
    // bidders that need to be created on the fly
    val createdBidders = mutableListOf<Bidder>()
    newBidders.filter { !knownBiddersUsernames.contains(it.username) }.forEach { bidder ->
        val newBidder = Bidder.new {
            username = bidder.username
            this.type = auctionType
            // weblingId = bidder.weblingId
            // this.numberOfParts = bidder.numberOfShares
        }
        when(typeName) {
            "solawi_tuebingen" -> {
                BidderDetailsSolawiTuebingenTable.insert {
                    it[BidderDetailsSolawiTuebingenTable.bidderId] = newBidder.id.value
                    it[weblingId] = bidder.weblingId
                    it[numberOfShares] = bidder.numberOfShares
                }
            }

        }
        createdBidders.add(newBidder)
    }
    listOf(
        *knownBiddersToBeAddedToAuction.toList().toTypedArray(),
        *createdBidders.toTypedArray()
    ).forEach {
            bidder ->
        AuctionBidders.insert {
            it[AuctionBidders.auctionId] = auction.id.value
            it[AuctionBidders.bidderId] = bidder.id.value
        }
    }
    return auction
}

fun Transaction.addBidders(auctionId: UUID, bidders: List<NewBidder>): AuctionEntity {
    val auction = AuctionEntity.find { Auctions.id eq auctionId }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction

    validateAuctionNotAccepted(auction)

    return addBidders(auction, bidders)
}


@MathDsl
@Suppress("FunctionName")
val SearchBidderMails: KlAction<Result<SearchBidderData>, Result<BidderMails>> = KlAction{bidders: Result<SearchBidderData> -> DbAction {
    database: Database -> bidders bindSuspend  {
        resultTransaction(database) {
            BidderMails(searchBidderMails(it))
        }
    } x database
}}

fun Transaction.searchBidderMails(searchBidderData: SearchBidderData): List<String> {
    val operations = listOf<Op<Boolean>?>(
        if(searchBidderData.firstname.isNotBlank()){ SearchBiddersTable.firstname eq searchBidderData.firstname } else {null},
        if(searchBidderData.lastname.isNotBlank()){ SearchBiddersTable.lastname eq searchBidderData.lastname } else {null},
        if(searchBidderData.email.isNotBlank()){ SearchBiddersTable.email eq searchBidderData.email } else {null},
        if(searchBidderData.relatedEmails.isNotEmpty()) { SearchBiddersTable.relatedEmails columnContainsAny searchBidderData.relatedEmails} else {null},
        if(searchBidderData.relatedNames.isNotEmpty()) { SearchBiddersTable.relatedNames columnContainsAny searchBidderData.relatedNames} else {null}
    )
    .filter{it != null}
    .reduceOrNull{
        acc, item ->
        acc?.and(item!!)
    } ?: Op.FALSE

    return SearchBidderEntity.find (operations).map{ it.email }
}

infix fun Column<String>.columnContainsAny( values: List<String>): Op<Boolean> {
    return values.map { this like "%$it%" }
        .reduceOrNull { acc, op -> (acc or op) as LikeEscapeOp } ?: Op.FALSE
}


fun String.containsOneOf(strings: List<String>): Boolean = when  {
        strings.isEmpty() -> false
        contains(strings.first()) ->  true
        else ->containsOneOf(strings.drop(1))
    }


@MathDsl
@Suppress("FunctionName")
val AddBidders: KlAction<Result<AddBidders>, Result<Unit>> = KlAction{ bidders: Result<AddBidders> -> DbAction {
    database: Database -> bidders bindSuspend  { data ->
        resultTransaction(database) {
            SearchBiddersTable.deleteAll()
            data.bidders.forEach {
                SearchBidderEntity.new {
                    firstname = it.firstname
                    lastname = it.lastname
                    email = it.email
                    relatedEmails = it.relatedEmails.joinToString(",") { it }
                    relatedNames = it.relatedNames.joinToString(",") { it }
                }
            }
        }
    } x database
}}

