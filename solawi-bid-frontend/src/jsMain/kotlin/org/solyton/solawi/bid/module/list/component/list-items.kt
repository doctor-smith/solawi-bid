package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.w3c.dom.HTMLElement


@Markup
@Composable
@Suppress("FunctionName")
fun <T> ListItems(
    items: Storage<List<T>>,
    item: @Composable (T)->Unit
) = ListItems(items.read(), item)

@Markup
@Composable
@Suppress("FunctionName")
fun <T> ListItems(
    items: List<T>,
    item: @Composable (T)->Unit
) {
    items.forEach {
        item(it)
    }
}
