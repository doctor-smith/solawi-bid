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
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.control.button.AppsButton
import org.solyton.solawi.bid.module.control.button.HelpButton
import org.solyton.solawi.bid.module.control.button.HomeButton
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar
import org.solyton.solawi.bid.module.navbar.data.navbar.i18n

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
    HomeButton(
        Color.black,
        Color.transparent,
        {"Home"},
        device,
    ){
        navigate("/home")
    }

    // todo:i18n
    AppsButton(
        Color.black,
        Color.transparent,
        {"Dashboard"},
        device,
        ){
        navigate("/app/dashboard")
    }

    Div({style { width(50.px) }}) {  }

    LocaleDropdown(
        i18n,
        scope
    )

    PersonalDropdown(
        navBar,
        i18n,
        logoutAction,
        scope
    )

    HelpButton(
        Color.black,
        Color.transparent,
        {"Help"},
        device
    ) {
        navigate("/manual")
    }
}
