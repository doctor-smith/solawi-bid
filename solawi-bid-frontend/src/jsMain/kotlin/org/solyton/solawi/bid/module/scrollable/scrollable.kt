package org.solyton.solawi.bid.module.scrollable

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Vertical
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.keywords.auto
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.module.list.component.ListItemsIndexed
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflowX
import org.solyton.solawi.bid.module.style.overflow.overflowY
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class ScrollableStyles(
    val containerStyle: StyleScope.()->Unit = {
        display( DisplayStyle.Flex )
        width(100.percent)
        minHeight(0.px)
        flexGrow(1)
    },
    val contentStyle: StyleScope.()->Unit = {
        flexDirection(FlexDirection.Column)
        minHeight(0.px)
        flexGrow(1)
        width(100.percent)
        display( DisplayStyle.Flex )
        overflowY(Overflow.Auto)
        overflowX(Overflow.Hidden)
    }
) {
    fun modifyContainerStyle(style: StyleScope.()->Unit) = copy(
        containerStyle = {
            containerStyle()
            style()
        }
    )
    fun modifyContentStyle(style: StyleScope.()->Unit) = copy(
        contentStyle = {
            contentStyle()
            style()
        }
    )

    companion object {
        val Default = ScrollableStyles()
        val Horizontal = ScrollableStyles(
            contentStyle = {
                flexDirection(FlexDirection.Row)
                minHeight(0.px)
                flexGrow(1)
                width(auto)
                display(DisplayStyle.Flex)
                overflowX(Overflow.Auto)
                overflowY(Overflow.Hidden)
            }
        )

        fun modifyContainerStyle(style: StyleScope.()->Unit) = Default.modifyContainerStyle(style)
        fun modifyContentStyle(style: StyleScope.()->Unit) = Default.modifyContentStyle(style)
    }
}

@Markup
@Composable
@Suppress("FunctionName")
fun Scrollable(
    styles: ScrollableStyles = ScrollableStyles(),
    content: @Composable () -> Unit
) = Div(
    attrs = {style {
        with(styles){containerStyle()}
    }}
){
    Vertical(style = styles.contentStyle){
        content()
    }
}

private var virtualScrollableInstanceCounter = 0

/**
 * A virtualized variant of [Scrollable].
 *
 * Instead of composing all [items] at once, only the ones currently within (or close to) the
 * visible viewport are rendered - using [ListItemsIndexed] under the hood - while the overall
 * scroll height is preserved via top/bottom spacer elements. This keeps the DOM small and
 * scrolling smooth for long lists.
 *
 * Every item is assumed to have the same fixed [itemHeight]; [overscan] controls how many
 * additional items are rendered above/below the viewport to reduce flickering while scrolling.
 */
@Markup
@Composable
@Suppress("FunctionName", "LongParameterList")
fun <T> VirtualScrollableList(
    items: List<T>,
    itemHeight: Double,
    styles: ScrollableStyles = ScrollableStyles(),
    overscan: Int = 4,
    item: @Composable (Int, T) -> Unit
) {
    val containerId = remember { "virtual-scrollable-${virtualScrollableInstanceCounter++}" }
    var scrollTop by remember { mutableStateOf(0.0) }
    var viewportHeight by remember { mutableStateOf(0.0) }
    val itemHeightPx = itemHeight

    DisposableEffect(containerId) {
        val element = document.getElementById(containerId) as? HTMLElement

        fun measure() {
            scrollTop = element?.scrollTop ?: 0.0
            viewportHeight = element?.clientHeight?.toDouble() ?: 0.0
        }

        val scrollListener: (Event) -> Unit = { measure() }
        val resizeListener: (Event) -> Unit = { measure() }

        element?.addEventListener("scroll", scrollListener)
        window.addEventListener("resize", resizeListener)
        measure()

        onDispose {
            element?.removeEventListener("scroll", scrollListener)
            window.removeEventListener("resize", resizeListener)
        }
    }

    val totalCount = items.size
    val visibleRowCount = if (itemHeightPx > 0) ceil(viewportHeight / itemHeightPx).toInt() else 0
    val firstIndex = if (itemHeightPx > 0) min(totalCount, max(0, (scrollTop / itemHeightPx).toInt() - overscan)) else 0
    val lastIndex = min(totalCount, firstIndex + visibleRowCount + 2 * overscan)

    Div(
        attrs = {
            style {
                with(styles) { containerStyle() }
                // Prevent the browser's scroll anchoring heuristic from "correcting" the scroll
                // position whenever the virtualization spacers resize above the viewport, which
                // otherwise causes the list to drift/scroll on its own without user interaction.
                property("overflow-anchor", "none")
            }
        }
    ) {
        Div(
            attrs = {
                id(containerId)
                style {
                    with(styles) { contentStyle() }
                    property("overflow-anchor", "none")
                }
            }
        ) {
            Div(attrs = { style { height((firstIndex * itemHeightPx).px); flexShrink(0); property("overflow-anchor", "none") } }) {}

            ListItemsIndexed(items.subList(firstIndex, lastIndex)) { localIndex, value ->
                Div(attrs = { style { height(itemHeightPx.px); flexShrink(0) } }) {
                    item(firstIndex + localIndex, value)
                }
            }

            Div(attrs = { style { height(((totalCount - lastIndex) * itemHeightPx).px); flexShrink(0); property("overflow-anchor", "none") } }) {}
        }
    }
}

/**
 * A virtualized scrollable list implementation designed for composable items.
 * Instead of composing all items at once, this function utilizes virtualization to render
 * only the items close to or within the visible viewport. This approach ensures smooth scrolling
 * and reduced DOM node usage for large lists.
 *
 * @param items The source of the list of items to render, encapsulated in a [Source] data type.
 * @param itemHeight The fixed height (in pixels) for each item in the list.
 * @param styles Customizable scrollable styling settings for the container and content, represented
 *        by a [ScrollableStyles] object. Defaults to standard scrollable styles.
 * @param overscan The number of items rendered above and below the viewport boundary to reduce
 *        flickering while scrolling. Defaults to 4.
 * @param item The composable content for each list item. Accepts the index of the item and the item
 *        itself as parameters.
 */
@Markup
@Composable
@Suppress("FunctionName", "LongParameterList")
fun <T> VirtualScrollableList(
    items: Source<List<T>>,
    itemHeight: Double,
    styles: ScrollableStyles = ScrollableStyles(),
    overscan: Int = 4,
    item: @Composable (Int, T) -> Unit) = VirtualScrollableList(
    items.emit(),
    itemHeight,
    styles,
    overscan,
    item
)
