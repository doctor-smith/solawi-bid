package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiRoundComments
import org.solyton.solawi.bid.module.bid.data.api.CommentOnRound
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.bidround.comments
import org.solyton.solawi.bid.module.bid.data.toDomainType

fun commentOnRound(comment: String, round: Lens<BidApplication, Round>) = Action<BidApplication, CommentOnRound, ApiRoundComments>(
    name = "CommentOnRound",
    reader = round * Reader { r:Round -> CommentOnRound(comment, r.roundId) },
    endPoint = CommentOnRound::class,
    writer = (round * comments).set contraMap { apiRoundComments: ApiRoundComments -> apiRoundComments.toDomainType() }
)
