package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage


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


@Markup
@Composable
@Suppress("FunctionName")
fun <T> ListItemsIndexed(
    items: Storage<List<T>>,
    item: @Composable (Int, T)->Unit
) = ListItemsIndexed(items.read(), item)

@Markup
@Composable
@Suppress("FunctionName")
fun <T> ListItemsIndexed(
    items: List<T>,
    item: @Composable (Int, T)->Unit
) {
    items.forEachIndexed() {index, item ->
        item(index,item)
    }
}
