package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import org.evoleq.compose.Markup
import org.evoleq.compose.routing.navigate
import org.evoleq.compose.routing.openUrlInNewTab
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
import org.solyton.solawi.bid.module.control.button.SupportButton
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
        paddingBottom(10.px)
        paddingRight(16.px)
        display(DisplayStyle.Flex)
        alignItems(AlignItems.Center)
        justifyContent(JustifyContent.FlexEnd)
        gap(8.px)
    }
}) {
    val i18n = navBar * i18n
    val scope = rememberCoroutineScope()
    val navIconColor = Color("#334155")

    // todo:i18n
    HomeButton(
        navIconColor,
        Color.transparent,
        {"Home"},
        device,
        isDisabled = true
    ){
        navigate("/home")
    }

    // todo:i18n
    AppsButton(
        navIconColor,
        Color.transparent,
        {"Dashboard"},
        device,
        ){
        navigate("/app/dashboard")
    }
    // todo:i18n
    HelpButton(
        navIconColor,
        Color.transparent,
        {"Help"},
        device
    ) {
        navigate("/manual")
    }

    SupportButton(
        navIconColor,
        Color.transparent,
        {"Support"},
        device
    ) {
        // todo:dev move to env
        openUrlInNewTab("https://solawi-management.atlassian.net/servicedesk/customer/portal/1")
    }

    Div({style { width(20.px) }}) {  }

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
}
