package org.evoleq.ktorx.result

import kotlinx.serialization.builtins.serializer
import org.solyton.solawi.bid.module.authentication.data.api.LoggedIn
import org.solyton.solawi.bid.module.authentication.data.api.Login
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.authentication.data.api.RefreshToken
import org.solyton.solawi.bid.module.bid.data.api.Bid
import org.solyton.solawi.bid.module.bid.data.api.BidRound
import org.solyton.solawi.bid.module.bid.data.api.Bidder
import kotlin.test.Test
import kotlin.test.assertTrue

class SerializersTest {

    @Test
    fun serializers() {
        serializers {
            // Standard
            add<Int>(Int.serializer())
            add<Long>(Long.serializer())
            add<String>(String.serializer())
            add<Boolean>(Boolean.serializer())
            add<Double>(Double.serializer())
            //...

            // Result
            add<Result<*>>(ResultSerializer)
            add<Result.Failure>(Result.Failure.serializer())
            add<Result.Failure.Message>(Result.Failure.Message.serializer())
            add(Result.Success::class, ResultSerializer)
            // Authorization
            add<Login>(Login.serializer())
            add<LoggedIn>(LoggedIn.serializer())
            add<RefreshToken>(RefreshToken.serializer())
            add<Logout>(Logout.serializer())

            // Bid
            add<Bid>(Bid.serializer())
            add<Bidder>(Bidder.serializer())
            add<BidRound>(BidRound.serializer())
        }

        assertTrue (serializers.isNotEmpty())

    }

}