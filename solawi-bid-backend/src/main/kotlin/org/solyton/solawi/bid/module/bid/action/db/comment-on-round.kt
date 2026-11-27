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
import org.solyton.solawi.bid.module.bid.repository.addComment

@MathDsl
val CommentOnRound = KlAction<Result< Contextual< CommentOnRound>>, Result<RoundComments>> {
    result -> DbAction {
        database -> result bindSuspend  { contextual -> resultTransaction(database){
            val data = contextual.data
            val round = readRound(data.roundId)

            round.addComment(
                data.comment,
                contextual.userId,

            )
            ApiRoundComments(round.comments.toList().map { it.toApiType() })
        } } x database
    }
}
