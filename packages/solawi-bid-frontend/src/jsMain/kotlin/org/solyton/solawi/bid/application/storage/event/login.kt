package org.solyton.solawi.bid.application.storage.event

import org.evoleq.compose.routing.navigate
import org.evoleq.optics.storage.Storage
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.localstorage.api.write
import org.solyton.solawi.bid.module.user.data.User

fun Storage<Application>.onLogin(oldApplication: Application, newApplication: Application) {

    if(accessDataChanged( newApplication.userData, oldApplication.userData )) {
        if(newApplication.userData.accessToken != "") write("accessToken", newApplication.userData.accessToken)
        if(newApplication.userData.refreshToken != "") write("refreshToken", newApplication.userData.refreshToken)
        if(
            newApplication.userData.accessToken != "" &&
            newApplication.userData.refreshToken != ""
        ) {
            if(
                oldApplication.userData.accessToken == "" &&
                oldApplication.userData.refreshToken == ""
            ) {
                navigate("/solyton/dashboard")
            }
        } else {
            navigate("/login")
        }
    }
}

fun accessDataChanged(newUser: User, oldUser: User): Boolean =
    newUser.accessToken != oldUser.accessToken ||
    newUser.refreshToken != oldUser.refreshToken