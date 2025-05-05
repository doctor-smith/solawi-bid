package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.routing.navigate
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Option
import org.jetbrains.compose.web.dom.Select
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.page.login.effect.TriggerLogoutEffect
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.data.locale
import org.solyton.solawi.bid.module.i18n.data.locales
import org.solyton.solawi.bid.module.navbar.data.NavBar
import org.solyton.solawi.bid.module.navbar.data.i18n

@Markup
@Composable
@Suppress("FunctionName")
fun NavBar(
    storage: Storage<Application>,
    navBar: Storage<NavBar>,
    device: Source<DeviceType>
) = Div({
    style {
        paddingTop(10.px)
        display(DisplayStyle.Flex)
        justifyContent(JustifyContent.FlexEnd)
    }
}) {

    val i18n = navBar * i18n
    val currentLocale = (i18n * locale).read()
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
        navigate("/solyton/dashboard")
    }

    Div({style { width(50.px) }}) {  }

    // todo:dev: extract
    Div({classes("select")}) {
        Select {
            (i18n * locales).read().forEach { s ->
                Option(s, {
                    if (s == currentLocale) {
                        selected()
                    }
                    onClick {
                        scope.launch {
                            (i18n * locale).write(s)
                        }
                    }
                }) {
                     Text(((i18n * language).read() as Lang.Block).component("solyton.locales")[s])
                }
            }
        }
    }
    // todo:dev: extract
    Div({classes("select")}) {
        Select {
                Option("MyData", {

                    onClick {
                        navigate("/solyton/private/data")
                    }
                }) {
                    Text("Meine Daten")
                }
                Option("Logout", {

                    onClick {
                        TriggerLogoutEffect(storage )
                    }
                }) {
                    Text("Logout")
                }
            }
        }

}