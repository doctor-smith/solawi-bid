package org.evoleq.compose.icons.svg

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Span

/**
 * A composable function that renders an SVG icon.
 *
 * @param name The name of the SVG file (without the file extension) to be used as the icon.
 * @param path The path to the directory where the SVG files are stored (default is "icons").
 * @param color The color of the icon, specified as a hex color code (default is "#888888").
 */

@Composable
fun SvgIcon(
    name: String,
    path: String = "assets/svg",
    color: String = "#888888"
) {
    Span({
        style {
            display(DisplayStyle.InlineBlock)
            width(16.px)
            height(16.px)

            backgroundColor(Color(color))

            property(
                "mask",
                "url('/$path/$name.svg') center / contain no-repeat"
            )
            property(
                "-webkit-mask",
                "url('/$path/$name.svg') center / contain no-repeat"
            )

            property("flex-shrink", "0")
        }
    })
}
