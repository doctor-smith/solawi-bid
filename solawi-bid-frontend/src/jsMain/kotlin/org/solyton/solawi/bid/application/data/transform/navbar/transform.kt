package org.solyton.solawi.bid.application.data.transform.navbar

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.navbar.data.environment.Environment
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar
import org.solyton.solawi.bid.module.navbar.data.user.User as NavBarUser

val navBarIso: Lens<Application, NavBar> by lazy {
    Lens(
        get = {whole: Application -> navbarPreIso.get(whole).copy(actions = whole.actions * navbarPreIso)},
        set = navbarPreIso.set
    )
}

val navbarPreIso: Lens<Application, NavBar> by lazy {
    Lens<Application, NavBar>(
        get = { whole -> NavBar(
            actions = ActionDispatcher {  },
            i18n = whole.i18N,
            environment = Environment,
            modals = whole.modals,
            deviceData = whole.deviceData,
            user = NavBarUser(
                whole.userData.refreshToken,
                whole.userData.accessToken,
                whole.userData.username,
                whole.userData.password
            )
        )},
        set = { part -> {whole ->
            whole.copy(
                i18N = part.i18n,
                userData = whole.userData.copy(
                    username = part.user.username,
                    password = part.user.password,
                    accessToken = part.user.accessToken,
                    refreshToken =part.user.refreshToken
                )
            )
        } }
    )
}
