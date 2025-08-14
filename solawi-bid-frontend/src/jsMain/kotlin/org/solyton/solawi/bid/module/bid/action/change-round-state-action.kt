package org.solyton.solawi.bid.module.bid.action

import org.evoleq.compose.Markup
import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.storage.Action
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiRound
import org.solyton.solawi.bid.module.bid.data.api.ChangeRoundState
import org.solyton.solawi.bid.module.bid.data.api.RoundState
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.bidround.state

@Markup
fun changeRoundState(newState: RoundState, round: Lens<BidApplication, Round>) = Action<BidApplication, ChangeRoundState, ApiRound>(
    name = "ChangeRoundState",
    reader = round * Reader { r:Round -> ChangeRoundState(r.roundId, newState.toString()) },
    endPoint = ChangeRoundState::class,
    writer = (round * state).set contraMap {apiRound: ApiRound -> apiRound.state}
)
