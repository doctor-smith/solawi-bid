package org.solyton.solawi.bid.application

import org.evoleq.compose.Markup
import org.evoleq.compose.storage.Store
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Style
import org.jetbrains.compose.web.renderComposable
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.env.set
import org.solyton.solawi.bid.application.data.environment
import org.solyton.solawi.bid.application.serialization.installSerializers
import org.solyton.solawi.bid.application.storage.Storage
import org.solyton.solawi.bid.application.storage.event.langLoaded
import org.solyton.solawi.bid.application.storage.event.loadLanguage
import org.solyton.solawi.bid.application.ui.UI
import org.solyton.solawi.bid.application.ui.effect.LaunchReadEnvironmentEffect
import org.solyton.solawi.bid.application.ui.effect.LaunchSetDeviceData
import org.solyton.solawi.bid.application.ui.page.login.effect.LaunchIsLoggedInEffect
import org.solyton.solawi.bid.application.ui.style.GlobalStyles
import org.solyton.solawi.bid.module.loading.component.Loading

@Markup
@Suppress("FunctionName")
fun Application() = renderComposable("root") {
    installSerializers()
    Style(GlobalStyles)
    Store({ Storage() }) {

        val environmentSet = (this * environment * set).read()
        when(environmentSet){
            true -> {
                LaunchSetDeviceData(this@Store * deviceData)
                loadLanguage()
                LaunchIsLoggedInEffect(this)
            }
            false -> LaunchReadEnvironmentEffect(this)
        }
        when( langLoaded() && environmentSet ) {
            true -> UI(this)
            false -> Loading()
        }
    }
}