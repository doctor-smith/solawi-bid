package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.dropdown.addDropdownCloseHandler
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.i18n.data.locale
import org.solyton.solawi.bid.module.i18n.data.locales

@Markup
@Composable
@Suppress("FunctionName")
fun LocaleDropdown(
    i18n: Storage<I18N>,
    scope: CoroutineScope
) {
    var open by remember { mutableStateOf(false) }
    val locales = (i18n * locales).read()
    val currentLocale = (i18n * locale).read()



    // Dropdown Container
    Div(attrs = {
        style {
            position(Position.Relative)
            cursor("pointer")
        }
        onClick { open = !open }
    }) {
        // Display current value
        Div(attrs = {
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                padding(4.px)
            }
        }) {
            Img(
                src = "/assets/flags/4x3/$currentLocale.svg",
                alt = currentLocale,
                attrs = {
                    style { width(30.px); auto; marginRight(8.px) }
                }
            )
            SimpleUpDown(open)
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
                    left(0.px)
                    width(100.percent)
                    backgroundColor(Color.white)
                    border(1.px, LineStyle.Solid, Color("#e2e8f0"))
                    borderRadius(8.px)
                    property("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.05)")
                    property("z-index", 500)
                    padding(4.px)
                }
            }) {
                locales.forEach { s ->
                    var hovered by remember { mutableStateOf(false) }
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            padding(6.px, 8.px)
                            borderRadius(4.px)
                            when(hovered) {
                                true -> backgroundColor(Color("#f1f5f9"))
                                false -> backgroundColor(Color.transparent)
                            }
                        }
                        onMouseEnter { hovered = true }
                        onMouseOut { hovered = false }
                        onClick { event ->
                            scope.launch {  (i18n * locale).write(s) }
                        }
                    }) {
                        // Text(text)
                        Img(
                            src = "/assets/flags/4x3/$s.svg",
                            alt = s,
                            attrs = {
                                style { width(30.px); auto; marginRight(8.px) }
                            }
                        )
                    }
                }
            }
        }
    }
}
