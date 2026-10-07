package org.solyton.solawi.bid.module.control.tooltip

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

enum class TooltipPosition(val className: String) {
    Top("tooltip-top"),
    Bottom("tooltip-bottom"),
    Left("tooltip-left"),
    Right("tooltip-right")
}

@Markup
@Composable
fun Tooltip(
    text: Source<String?>,
    position: TooltipPosition = TooltipPosition.Top,
    wrapperStyle: StyleScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    val tooltipText = text.emit()
    if (tooltipText.isNullOrBlank()) {
        content()
        return
    }

    Span(attrs = {
        classes("tooltip-wrapper")
        style {
            wrapperStyle()
        }
    }) {
        content()
        Span(attrs = {
            classes("tooltip-content", position.className)
            attr("role", "tooltip")
            attr("aria-hidden", "true")
        }) {
            Text(tooltipText)
        }
    }
}

@Markup
@Composable
fun Tooltip(
    text: String?,
    position: TooltipPosition = TooltipPosition.Top,
    wrapperStyle: StyleScope.() -> Unit = {},
    content: @Composable () -> Unit
) = Tooltip({ text }, position, wrapperStyle, content)
