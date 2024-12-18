package org.solyton.solawi.bid.module.bid.action.db

import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.ktorx.result.map
import org.evoleq.ktorx.result.mapSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.crypto.generateSecureLink
import org.evoleq.math.x
import org.evoleq.util.DbAction
import org.evoleq.util.KlAction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.db.BidRoundException
import org.solyton.solawi.bid.module.db.schema.AuctionBidders
import org.solyton.solawi.bid.module.db.schema.Auctions
import org.solyton.solawi.bid.module.db.schema.Rounds
import java.util.*
import org.solyton.solawi.bid.module.bid.data.api.Auctions as ApiAuctions
import org.solyton.solawi.bid.module.db.schema.Auction as AuctionEntity
import org.solyton.solawi.bid.module.db.schema.Bidder as BidderEntity
import org.solyton.solawi.bid.module.db.schema.Round as RoundEntity

@MathDsl
val CreateAuction = KlAction<Result<CreateAuction>, Result<Auction>> {
    auction: Result<CreateAuction> -> DbAction {
        database -> auction bindSuspend  { data -> resultTransaction(database) {
            println("Create auction: ${data.name}")
            createAuction(data.name, data.date)
        } } mapSuspend  {
            it.toApiType()
        } x database
    }
}

fun Transaction.createAuction(name: String, date: LocalDate): AuctionEntity = AuctionEntity.new {
    this.name = name
    this.date = DateTime().withDate(date.year, date.monthNumber, date.dayOfMonth)
}

@MathDsl
val ReadAuctions = KlAction<Result<GetAuctions>, Result<ApiAuctions>> {
    auctions -> DbAction { database ->  resultTransaction(database){
      readAuctions()//

    // TODO(use identifier to return all auction which are accessible as identified person)
    }  mapSuspend  {
        ApiAuctions(it.toApiType())
    } x database }
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
val DeleteAuctions = KlAction<Result<DeleteAuctions>, Result<GetAuctions>> {
    auctions -> DbAction {
        database -> auctions bindSuspend {
            data -> resultTransaction(database){
                deleteAuctions(data.auctionIds.map { UUID.fromString(it) })
        } } map { GetAuctions } x database
    }
}

fun Transaction.deleteAuctions(auctionIds: List<UUID>) {
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

fun Transaction.updateAuctions(auctions: List<ApiAuction>) {
    TODO("Function updateAuctions not implemented yet!")
}

val AddRound = KlAction<Result<PreRound>, Result<Round>> {
    round -> DbAction {
        database -> round bindSuspend  { data -> resultTransaction(database){
            addRound(data)
        } } map {it.toApiType()}  x database
    }
}

fun Transaction.addRound(round: PreRound): RoundEntity {
    val auctionEntity = AuctionEntity.find { Auctions.id eq UUID.fromString(round.roundId) }.first()
    val roundEntity = RoundEntity.new {
        auction = auctionEntity
    }
    roundEntity.link = generateSecureLink(round.roundId.toString(), roundEntity.id.value.toString(), UUID.randomUUID().toString())
    return roundEntity
}

/*
val AddBidders = KlAction<List<Bidder>, Result<AuctionEntity>> = KlAction{
    list -> DbAction {
        database -> resultTransaction(database) {

        } x database
    }
}

 */

fun Transaction.addBidders(auction: AuctionEntity, bidders: List<NewBidder>): AuctionEntity {

    bidders.forEach { bidder ->
        val newBidder = BidderEntity.new {
            username = bidder.username
            weblingId = bidder.weblingId
            this.numberOfParts = bidder.numberOfParts
        }
        AuctionBidders.insert {
            it[AuctionBidders.auctionId] = auction.id.value
            it[AuctionBidders.bidderId] = newBidder.id.value
        }
    }
    return auction
}

fun Transaction.addBidders(auctionId: UUID, bidders: List<NewBidder>): AuctionEntity {
    val auction = AuctionEntity.find { Auctions.id eq auctionId }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction

    return addBidders(auction, bidders)
}

val ChangeRoundState = KlAction<ChangeRoundState, Result<Round>> {
    roundState -> DbAction {
        database -> coroutineScope {  resultTransaction(database) {
            changeRoundState(roundState)
        } } mapSuspend  { it.toApiType() } x database
    }
}

fun Transaction.changeRoundState(newState: ChangeRoundState): RoundEntity {
    val round = RoundEntity.find { Rounds.id eq UUID.fromString(newState.roundId) }.firstOrNull()
        ?: throw BidRoundException.NoSuchRound

    round.state = newState.state

    return round
}