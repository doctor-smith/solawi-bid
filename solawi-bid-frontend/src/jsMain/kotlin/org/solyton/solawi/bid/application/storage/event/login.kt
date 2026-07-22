package org.solyton.solawi.bid.application.storage.event

import org.evoleq.compose.routing.navigate
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.env.type
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.module.localstorage.api.write
import org.solyton.solawi.bid.module.user.data.user.User

fun Storage<Application>.onLogin(oldApplication: Application, newApplication: Application) {
    val env = (this@onLogin * environment * type).read()
    val accessTokenKey = "${env.lowercase()}_$ACCESS_TOKEN"
    val refreshTokenKey = "${env.lowercase()}_$REFRESH_TOKEN"
    if(accessDataChanged( newApplication.userData, oldApplication.userData )) {
        if(newApplication.userData.accessToken != "") write(accessTokenKey, newApplication.userData.accessToken)
        if(newApplication.userData.refreshToken != "") write(refreshTokenKey, newApplication.userData.refreshToken)
        if(
            newApplication.userData.accessToken != "" &&
            newApplication.userData.refreshToken != ""
        ) {
            if(
                oldApplication.userData.accessToken == "" &&
                oldApplication.userData.refreshToken == ""
            ) {
                navigate("/app/dashboard")
            }
        } else {
            navigate("/login")
        }
    }
}

fun accessDataChanged(newUser: User, oldUser: User): Boolean =
    newUser.accessToken != oldUser.accessToken ||
    newUser.refreshToken != oldUser.refreshToken
