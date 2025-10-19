package org.solyton.solawi.bid.module.bid.action.db

import kotlinx.coroutines.coroutineScope
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.crypto.generateSecureLink
import org.evoleq.math.x
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.bid.schema.AcceptedRound
import org.solyton.solawi.bid.module.bid.schema.AcceptedRoundEntity
import org.solyton.solawi.bid.module.bid.schema.AcceptedRoundsTable
import org.solyton.solawi.bid.module.bid.schema.AuctionEntity
import org.solyton.solawi.bid.module.bid.schema.Auctions
import org.solyton.solawi.bid.module.bid.schema.Rounds
import java.util.*

@MathDsl
val CreateRound = KlAction<Result<Contextual<CreateRound>>, Result<Round>> {
    round -> DbAction {
        database -> round bindSuspend  { contextual -> resultTransaction(database){
            addRound(contextual.data).toApiType()
        } } x database
    }
}

fun Transaction.addRound(round: CreateRound): org.solyton.solawi.bid.module.bid.schema.Round {
    val auctionEntity = AuctionEntity.find { Auctions.id eq UUID.fromString(round.auctionId) }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction

    validateAuctionNotAccepted(auctionEntity)

    val roundNumber = auctionEntity.rounds.count().toInt() + 1

    val roundEntity = org.solyton.solawi.bid.module.bid.schema.Round.new {
        auction = auctionEntity
        number = roundNumber
        // todo:created_by add valid userId
        createdBy = UUID_ZERO
    }
    roundEntity.link = generateSecureLink(round.auctionId, roundEntity.id.value.toString(), UUID.randomUUID().toString()).signature
    return roundEntity
}


@MathDsl
val ChangeRoundState = KlAction<Result<Contextual<ChangeRoundState>>, Result<Round>> {
    roundState -> DbAction {
        database -> coroutineScope { roundState bindSuspend {contextual -> resultTransaction(database) {
            changeRoundState(contextual.data).toApiType()
        } } } x database
    }
}

fun Transaction.changeRoundState(newState: ChangeRoundState): org.solyton.solawi.bid.module.bid.schema.Round {
    val round = org.solyton.solawi.bid.module.bid.schema.Round.find { Rounds.id eq UUID.fromString(newState.roundId) }.firstOrNull()
        ?: throw BidRoundException.NoSuchRound

    validateAuctionNotAccepted(round)

    val sourceState = RoundState.fromString(round.state)
    val targetState = RoundState.fromString(newState.state)

    return when(targetState == sourceState.nextState()){
        true -> {
            round.state = newState.state
            round
        }
        false -> throw RoundStateException.IllegalTransition(sourceState, targetState)
    }
}

// AcceptRound

@MathDsl
val AcceptRound = KlAction<Result<Contextual<AcceptRound>>, Result<ApiAcceptedRound>> {
    roundState -> DbAction {
        database -> coroutineScope { roundState bindSuspend {contextual -> resultTransaction(database) {
            acceptRound(contextual.data).toApiType()
        } } } x database
    }
}

fun Transaction.acceptRound(acceptRound: AcceptRound): AcceptedRound {
    val auction = AuctionEntity.find { Auctions.id eq UUID.fromString(acceptRound.auctionId) }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction

    validateAuctionNotAccepted(auction)

    val round = org.solyton.solawi.bid.module.bid.schema.Round.find { Rounds.id eq UUID.fromString(acceptRound.roundId) }.firstOrNull()
        ?: throw BidRoundException.NoSuchRound

    validateAuctionNotAccepted(round)

    val foundAcceptedRound = AcceptedRoundEntity.find { AcceptedRoundsTable.roundId eq UUID.fromString(acceptRound.roundId) }.firstOrNull()

    if (foundAcceptedRound != null) throw BidRoundException.NoSuchAcceptedRound

    val acceptedRound = AcceptedRoundEntity.new {
        this.auction = auction
        this.round = round
    }

    return acceptedRound
}
