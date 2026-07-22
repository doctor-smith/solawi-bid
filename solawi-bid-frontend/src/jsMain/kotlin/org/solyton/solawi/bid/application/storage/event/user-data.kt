package org.solyton.solawi.bid.application.storage.event

import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.type
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.localstorage.api.read
import org.solyton.solawi.bid.module.user.data.user.accessToken
import org.solyton.solawi.bid.module.user.data.user.refreshToken

const val ACCESS_TOKEN = "access_token"
const val REFRESH_TOKEN = "refresh_token"

fun Storage<Application>.checkUserData() {
    val storage = this
    val env = (this * environment * type).read()
    val accessTokenKey = "${env.lowercase()}_$ACCESS_TOKEN"
    val refreshTokenKey = "${env.lowercase()}_$REFRESH_TOKEN"

    val (aToken, rToken ) = Pair(read(accessTokenKey), read(refreshTokenKey))
    if (aToken != null) {
        (storage * userData * accessToken).write(aToken)
    }
    if (rToken != null) {
        (storage * userData * refreshToken).write(rToken)
    }
}
