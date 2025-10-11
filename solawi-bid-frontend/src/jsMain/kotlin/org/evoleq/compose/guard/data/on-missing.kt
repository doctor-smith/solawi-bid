package org.evoleq.compose.guard.data

import androidx.compose.runtime.Composable
import org.evoleq.math.Source
import org.evoleq.math.emit

@Composable fun <T> onMissing(
    source: Source<List<T>>,
    predicate: (T)-> Boolean,
    effect: @Composable ()->Unit
): Boolean {
    val missing = source.emit().none { predicate(it) }
    return if(missing){ effect(); true } else { false }
}

@Composable fun <T> onEmpty(
    source: Source<List<T>>,
    effect: @Composable ()->Unit
): Boolean {
    val empty = source.emit().isEmpty()
    return if(empty){ effect(); true } else { false }
}

@Composable fun isLoading(vararg booleans: Boolean): Boolean = booleans.any{ it }
