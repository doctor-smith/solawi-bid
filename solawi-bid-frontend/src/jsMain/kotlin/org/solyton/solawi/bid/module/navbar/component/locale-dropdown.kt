package org.solyton.solawi.bid.module.navbar.component

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
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
    // Container für das Dropdown
    Div(attrs = {
        style {
            position(Position.Relative)
            // width(200.px)
            // border(1.px, LineStyle.Solid, Color.black)
            // borderRadius(4.px)
            cursor("pointer")
        }
        onClick { open = !open }
    }) {
        // Angezeigte aktuelle Auswahl
        // val displayText = ((i18n * language).read() as Lang.Block).component("solyton.locales")[currentLocale]
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
                    style { width(40.px); auto; marginRight(8.px) }
                }
            )
            // Text(displayText)
            Span {
                Text(if (open) "▲" else "▼")
            }
        }

        // Dropdown-Liste
        if (open) {
            Div(attrs = {
                style {
                    position(Position.Absolute)
                    top(100.percent)
                    left(0.px)
                    width(100.percent)
                    backgroundColor(Color.white)
                    border(1.px, LineStyle.Solid, Color.black)
                    borderRadius(4.px)
                    //boxShadow(Color.gray, offsetX = 2.px, offsetY = 2.px, blurRadius = 4.px)
                    //zIndex(5000)
                    property("z-index", 500)
                }
            }) {
                locales.forEach { s ->
                    // val text = ((i18n * language).read() as Lang.Block).component("solyton.locales")[s]
                    Div(attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            alignItems(AlignItems.Center)
                            padding(4.px)
                            /*
                            hover {
                                backgroundColor(Color.lightGray)
                            }

                             */
                        }
                        onClick {
                            // open = false;
                            scope.launch {  (i18n * locale).write(s) }
                        }
                    }) {
                        // Text(text)
                        Img(
                            src = "/assets/flags/4x3/$s.svg",
                            alt = s,
                            attrs = {
                                style { width(40.px); auto; marginRight(8.px) }
                            }
                        )
                    }
                }
            }
        }
    }
}
