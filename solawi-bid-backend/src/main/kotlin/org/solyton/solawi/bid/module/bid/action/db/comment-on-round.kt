package org.solyton.solawi.bid.module.bid.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.ApiRoundComments
import org.solyton.solawi.bid.module.bid.data.api.CommentOnRound
import org.solyton.solawi.bid.module.bid.data.api.RoundComments
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.bid.repository.addComment
import org.solyton.solawi.bid.module.bid.schema.RoundEntity
import org.solyton.solawi.bid.module.bid.schema.RoundsTable
import java.util.*

@MathDsl
val CommentOnRound = KlAction<Result< Contextual< CommentOnRound>>, Result<RoundComments>> {
    result -> DbAction {
        database -> result bindSuspend  { contextual -> resultTransaction(database){
            val data = contextual.data
            val round = RoundEntity.find { RoundsTable.id eq UUID.fromString(data.roundId) }.firstOrNull()
                ?: throw BidRoundException.NoSuchRound

            round.addComment(
                data.comment,
                contextual.userId,

            )
            ApiRoundComments(round.comments.toList().map { it.toApiType() })
        } } x database
    }
}
