package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import org.evoleq.compose.Markup
import org.evoleq.compose.routing.navigate
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar
import org.solyton.solawi.bid.module.navbar.effect.TriggerLogoutEffect


@Markup
@Composable
@Suppress("FunctionName", "UnusedParameter")
fun PersonalDropdown(
    navBar: Storage<NavBar>,
    i18n: Storage<I18N>,
    logoutAction: Action<NavBar, Logout, Unit>,
    scope: CoroutineScope
) {

    val initialValue = "my-data"
    val options = mapOf(
        "my-data" to "Meine Daten",
        "logout" to "Logout"
    )

    var open by remember { mutableStateOf(false) }
    // var selected by remember { mutableStateOf(initialValue) }
    // val locales = (i18n * locales).read()
    // val currentLocale = (i18n * locale).read()
    // Container für das Dropdown
    Div(attrs = {
        style {
            position(Position.Relative)
            cursor("pointer")
        }
        onClick { open = !open }
    }) {
        // Display UserIcon
        Div(
            attrs = {
                title("User related actions")
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                justifyContent(JustifyContent.FlexEnd) // ?
                padding(4.px)
                gap(5.px)
            }
        }) {
            Div({
                style {
                    color(Color.black)
                    backgroundColor(Color.transparent)
                    overflow("visible")

                }
            }) {
                I({
                    classes("fa-solid", "fa-user-large")
                })
            }
            SimpleUpDown(open)
        }

        // Dropdown-List
        if (open) {
            Div(attrs = {
                style {
                    position(Position.Absolute)
                    top(100.percent)
                    right(0.px)
                    width(150.px)
                    backgroundColor(Color.white)
                    border(1.px, LineStyle.Solid, Color.black)
                    borderRadius(4.px)
                    // boxShadow // ?
                    property("z-index", 500)
                }
            }) {
                Option(
                    options[initialValue]!!,
                ) {
                    navigate("/app/private/data")
                }
                Option(
                    options["logout"]!!,
                ) {
                    TriggerLogoutEffect(navBar, logoutAction )
                }
            }
        }
    }
}



@Markup
@Composable
@Suppress("FunctionName")
private fun Option(
    text: String,
    action: ()->Unit
)  {
    var hovered by remember { mutableStateOf(false)}
    Div(attrs = {
        style {
            display(DisplayStyle.Flex)
            alignItems(AlignItems.Center)
            padding(4.px)
            when(hovered){
                true -> backgroundColor(Color.lightgray)
                false -> backgroundColor(Color.transparent)
            }
        }
        onMouseEnter { hovered = true }
        onMouseOut { hovered = false }
        onClick {
            action()
        }
}) {
    Text(text)
}}
