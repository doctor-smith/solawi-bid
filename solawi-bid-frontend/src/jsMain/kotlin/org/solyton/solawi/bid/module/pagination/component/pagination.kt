package org.solyton.solawi.bid.module.pagination.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.disabled
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.pagination.data.Pagination
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import kotlin.math.min

data class PaginationStyles(
    val containerStyle: StyleScope.()->Unit = {
        display(DisplayStyle.Flex)
        gap(5.px)
        alignItems("center")
    },
    val buttonStyle: StyleScope.() -> Unit = {
        backgroundColor(Color.white)
        cursor(Cursor.Pointer)
        width(30.px)
        height(30.px) // Ensures the button is square and matches typical text field height
        padding(0.px) // Removes inner padding to center text within the button
        border {
            borderRadius(3.px)
        }
    }
)

@Markup
@Composable
@Suppress("FunctionName")
fun Pagination(
    data: Pagination,
    styles: PaginationStyles = PaginationStyles(),
    setNumberOfItemsPerPage: (Int)->Unit = {},
    next: (nextPage: Int)->Unit
)  {
    val (containerStyle, buttonStyle) = styles
    Div(attrs = { style { containerStyle() } }) {
    Button(
        attrs = {
            onClick { if (data.page > 1) next(1) }
            disabled(data.page <= 1)
            style { buttonStyle() }
        }
    ) {
        Text("<<")
    }
    Button(
        attrs = {
        onClick { if (data.page > 1) next(data.page - 1) }
            disabled(data.page <= 1)
            style { buttonStyle() }
        }
    ) {
        Text("<")
    }
    if (data.totalPages > 5) {
        repeat(min(5, data.totalPages)) { index ->
            val pageIndex = data.page + index - 2
            if (pageIndex in 1..data.totalPages) {
                Button(
                    attrs = {
                        onClick { if (pageIndex != data.page) next(pageIndex) }
                        if (pageIndex == data.page) attr("aria-current", "page")
                    }
                ) {
                    Text("$pageIndex")
                }
            }
        }
    } else {
        Text("${data.page} | ${data.totalPages}")
    }
    Div {
        TextInput(
            attrs = {
                value(data.itemsPerPage.toString())
                onInput { event ->
                    event.value.toIntOrNull()?.let {
                        setNumberOfItemsPerPage(it )
                    }
                }
                attr("type", "number")
                attr("min", "${data.minimalNumberOfItemsPerPage}")
                attr("step", "${data.incrementBy}")
                style {
                    width(100.px)
                    marginLeft(5.px)
                }
            }
        )
    }
    Button(
        attrs = {
            onClick { if (data.page < data.totalPages) next(data.page + 1) }
            disabled(data.page >= data.totalPages)
            style { buttonStyle() }
        }
    ) {
        Text(">")
    }
    Button(
        attrs = {
            onClick { if (data.page < data.totalPages) next(data.totalPages) }
            disabled(data.page >= data.totalPages)
            style { buttonStyle() }
        }
        ) {
            Text(">>")
        }
    }
}
