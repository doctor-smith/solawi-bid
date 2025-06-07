package org.evoleq.compose.storage

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage


@Markup
@Composable
@Suppress("FunctionName")
fun <D> Store(buildStorage: @Composable ()-> Storage<D>, content: @Composable Storage<D>.()->Unit) {
    buildStorage().content()
}
@Markup
@Composable
@Suppress("FunctionName")
fun <D> Store(
    initialData: D,
    buildStorage: @Composable (D)-> Storage<D>,
    content: @Composable Storage<D>.()->Unit
) {
    buildStorage(initialData).content()
}
@Markup
@Composable
fun initialize(block:@Composable  ()->Unit) {
    block()
}

@Markup
@Composable
fun <P> Storage<P>.onInit(block:  @Composable Storage<P>.()->Unit): Storage<P> {
    block()
    return this
}
