package org.solyton.solawi.bid.application.storage

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.storage.onInit
import org.evoleq.math.state.runOn
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.onChange
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.storage.event.*
import org.solyton.solawi.bid.application.storage.middleware.ProcessAction
import org.solyton.solawi.bid.module.user.data.user.User


@Markup
@Composable
fun Storage(): Storage<Application> {
    // store all data related to the application
    var application by remember{ mutableStateOf<Application>(Application(
        environment = Environment(),
        userData = User()
    ))}

    return Storage<Application>(
        read = { application },
        write = {
            newApplication -> application = newApplication
        }
    )
    .onInit {
        checkContext()
        checkCookie()
        checkUserData()
    }
    .onChange { oldApplication, newApplication ->
        onCookieDisclaimerConfirmed(oldApplication, newApplication)
        onLocaleChanged(oldApplication, newApplication)
        onLogin(oldApplication, newApplication)
    }.onDispatch {
        (this@onDispatch * actions).read().flow.collect { action : Action<Application, *, *> ->
            ProcessAction( action ) runOn this
        }
    }
}
