package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import org.evoleq.compose.Markup
import org.evoleq.compose.routing.navigate
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.Source
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar
import org.solyton.solawi.bid.module.navbar.data.navbar.i18n
import org.solyton.solawi.bid.module.navbar.effect.TriggerLogoutEffect

@Markup
@Composable
@Suppress("FunctionName")
fun NavBar(
    navBar: Storage<NavBar>,
    device: Source<DeviceType>,
    logoutAction: Action<NavBar, Logout, Unit>
) = Div({
    style {
        paddingTop(10.px)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.FlexEnd)
    }
}) {

    val i18n = navBar * i18n
    // val currentLocale = (i18n * locale).read()
    val scope = rememberCoroutineScope()

    // todo:i18n
    StdButton(
        {"Home"},
        device
    ) {
        navigate("/")
    }

    // todo:i18n
    StdButton(
        {"Dashboard"},
        device
    ) {
        navigate("/app/dashboard")
    }

    Div({style { width(50.px) }}) {  }

    LocaleDropdown(
        i18n,
        scope
    )
    // todo:dev: extract
    Div({classes("select")}) {
        Select {
                Option("MyData", {

                    onClick {
                        navigate("/app/private/data")
                    }
                }) {
                    Text("Meine Daten")
                }
                Option("Logout", {

                    onClick {
                        TriggerLogoutEffect(navBar, logoutAction )
                    }
                }) {
                    Text("Logout")
                }
            }
        }
}
