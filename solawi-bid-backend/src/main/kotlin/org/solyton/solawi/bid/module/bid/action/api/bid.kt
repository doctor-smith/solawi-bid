package org.solyton.solawi.bid.module.bid.action.api

import io.ktor.server.request.*
import org.evoleq.ktorx.Action
import org.evoleq.ktorx.ApiAction
import org.evoleq.math.x
import org.solyton.solawi.bid.module.bid.data.api.Bid

val ReceiveBid: Action<Bid> = ApiAction {
    call -> call.receive<Bid>() x call
}
