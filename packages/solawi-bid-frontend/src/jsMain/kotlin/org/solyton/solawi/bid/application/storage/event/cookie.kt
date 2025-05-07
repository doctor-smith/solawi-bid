package org.solyton.solawi.bid.application.storage.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.cookieDisclaimer
import org.solyton.solawi.bid.module.cookie.api.readCookie
import org.solyton.solawi.bid.module.cookie.api.writeCookie
import org.solyton.solawi.bid.module.cookie.data.isConfirmed

fun Storage<Application>.onCookieDisclaimerConfirmed(oldApplication: Application, newApplication: Application) {
    if (newApplication.cookieDisclaimer.isConfirmed != oldApplication.cookieDisclaimer.isConfirmed) {
        if (newApplication.cookieDisclaimer.isConfirmed) {
            CoroutineScope(Job()).launch {
                writeCookie()
            }
        }
    }
}


fun Storage<Application>.checkCookie(){
    val storage = this
    val cookie = readCookie()
    if(cookie != null){
        (storage * cookieDisclaimer * isConfirmed).write(true)
    }
}
