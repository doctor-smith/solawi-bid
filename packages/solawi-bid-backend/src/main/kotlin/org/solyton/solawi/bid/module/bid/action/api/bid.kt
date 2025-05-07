package org.solyton.solawi.bid.module.bid.action.api

import io.ktor.server.request.*
import org.evoleq.math.x
import org.evoleq.util.Action
import org.evoleq.util.ApiAction
import org.solyton.solawi.bid.module.bid.data.api.Bid

val ReceiveBid: Action<Bid> = ApiAction {
    call -> call.receive<Bid>() x call
}