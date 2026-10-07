package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import org.evoleq.compose.Markup
import org.evoleq.compose.dropdown.addDropdownCloseHandler
import org.evoleq.compose.routing.navigate
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.I
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.authentication.data.api.Logout
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.control.tooltip.Tooltip
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.navbar.data.navbar.NavBar
import org.solyton.solawi.bid.module.navbar.effect.TriggerLogoutEffect

// todo:i18n
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
    // Container für das Dropdown
    Div(attrs = {
        style {
            position(Position.Relative)
            cursor("pointer")
        }
        onClick { open = !open }
    }) {
        // Display UserIcon
        Tooltip("User related actions") {
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        alignItems(AlignItems.Center)
                        justifyContent(JustifyContent.FlexEnd)
                        padding(4.px, 8.px)
                        borderRadius(6.px)
                        gap(6.px)
                    }
                }
            ) {
                Div({
                    style {
                        color(Color("#334155"))
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
        }

        // Dropdown-List
        if (open) {

            addDropdownCloseHandler {
                open = false
            }

            Div(attrs = {
                style {
                    position(Position.Absolute)
                    top(100.percent)
                    marginTop(4.px)
                    right(0.px)
                    width(160.px)
                    backgroundColor(Color.white)
                    border(1.px, LineStyle.Solid, Color("#e2e8f0"))
                    borderRadius(8.px)
                    property("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.05)")
                    padding(4.px)
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
            padding(8.px, 12.px)
            borderRadius(4.px)
            fontSize(14.px)
            color(Color("#1e293b"))
            when(hovered){
                true -> backgroundColor(Color("#f1f5f9"))
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
