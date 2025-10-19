package org.solyton.solawi.bid.module.bid.action.db

import io.ktor.util.*
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.bid.schema.Auction
import org.solyton.solawi.bid.module.bid.schema.AuctionEntity
import org.solyton.solawi.bid.module.bid.schema.Auctions
import org.solyton.solawi.bid.module.bid.schema.Bidder
import org.solyton.solawi.bid.module.bid.schema.BidderDetailsSolawiTuebingenEntity
import org.solyton.solawi.bid.module.bid.schema.BidderEntity
import org.solyton.solawi.bid.module.bid.schema.SearchBidderEntity
import java.util.*
import kotlin.Boolean
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.contains
import kotlin.collections.drop
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.mutableListOf
import kotlin.collections.reduceOrNull
import kotlin.collections.toList
import kotlin.collections.toTypedArray
import kotlin.text.contains
import kotlin.text.isNotBlank
import kotlin.text.lowercase
import kotlin.text.trim

@MathDsl
val ImportBidders = KlAction{bidders: Result<Contextual<ImportBidders>> -> DbAction {
    database: Database -> bidders bindSuspend  {
        resultTransaction(database) {
            importBidders(auctionId = UUID.fromString(it.data.auctionId), it.data.bidders).toApiType().copy(
                bidderInfo = getBidderDetails(it.data.bidders).map{det -> det.toBidderInfo()}
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
            // todo dev:
            createdBy = UUID_ZERO
        }
        when(typeName) {
            "solawi_tuebingen" -> {
                BidderDetailsSolawiTuebingenTable.insert {
                    it[BidderDetailsSolawiTuebingenTable.bidderId] = newBidder.id.value
                    it[weblingId] = bidder.weblingId
                    it[numberOfShares] = bidder.numberOfShares
                    it[createdBy] = UUID_ZERO
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
val SearchBidderMails: KlAction<Result<Contextual<SearchBidderData>>, Result<BidderMails>> = KlAction{bidders: Result<Contextual<SearchBidderData>> -> DbAction {
    database: Database -> bidders bindSuspend  {
        resultTransaction(database) {
            BidderMails(searchBidderMails(it.data))
        }
    } x database
}}

fun Transaction.searchBidderMails(searchBidderData: SearchBidderData): List<String> {
    val operations = listOf<Op<Boolean>?>(
        if(searchBidderData.firstname.isNotBlank()){ SearchBiddersTable.firstname.lowerCase() like "%${searchBidderData.firstname.lowercase() }%"} else {null},
        if(searchBidderData.lastname.isNotBlank()){ SearchBiddersTable.lastname.lowerCase() like "%${ searchBidderData.lastname.lowercase() }%" } else {null},
        if(searchBidderData.email.isNotBlank()){ SearchBiddersTable.email.lowerCase() like "%${ searchBidderData.email.lowercase() }%" } else {null},
        if(searchBidderData.relatedEmails.isNotEmpty()) { SearchBiddersTable.relatedEmails columnContainsAny searchBidderData.relatedEmails } else {null},
        if(searchBidderData.relatedNames.isNotEmpty()) { SearchBiddersTable.relatedNames columnContainsAny searchBidderData.relatedNames } else {null}
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
val AddBidders: KlAction<Result<Contextual<AddBidders>>, Result<Unit>> = KlAction{ bidders: Result<Contextual<AddBidders>> -> DbAction {
    database: Database -> bidders bindSuspend  { contextual ->
        resultTransaction(database) {
            SearchBiddersTable.deleteAll()
            contextual.data.bidders.forEach { bidder ->
                SearchBidderEntity.new {
                    firstname = bidder.firstname.trim()
                    lastname = bidder.lastname.trim()
                    email = bidder.email.trim().toLowerCasePreservingASCIIRules()
                    relatedEmails = bidder.relatedEmails.joinToString(",") { it.trim() }
                    relatedNames = bidder.relatedNames.joinToString(",") { it.trim() }
                }
            }
        }
    } x database
}}

