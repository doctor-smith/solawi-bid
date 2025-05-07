package org.solyton.solawi.bid.application.storage.event

import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.module.localstorage.api.read
import org.solyton.solawi.bid.module.user.data.accessToken
import org.solyton.solawi.bid.module.user.data.refreshToken


fun Storage<Application>.checkUserData() {
    val storage = this
    val (aToken, rToken ) = Pair(read("accessToken"), read("refreshToken"))
    if (aToken != null) {
        (storage * userData * accessToken).write(aToken)
    }
    if (rToken != null) {
        (storage * userData * refreshToken).write(rToken)
    }
}
