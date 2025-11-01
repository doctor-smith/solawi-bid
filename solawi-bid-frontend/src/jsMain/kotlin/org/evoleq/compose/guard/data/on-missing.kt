package org.evoleq.compose.guard.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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

@Composable fun <T> onNullLaunch(
    source: Source<T?>,
    effect: @Composable ()->Unit
): Boolean {
    val isNull = source.emit() == null
    return if(isNull){ effect(); true } else { false }
}

@Composable fun <T> onNullTrigger(
    source: Source<T?>,
    effect: suspend ()->Unit
): Boolean {
    val isNull = source.emit() == null
    return if(isNull){
        rememberCoroutineScope().launch { effect() }
        true
    } else {
        false
    }
}

@Composable fun <T> onEmpty(
    source: Source<List<T>>,
    effect: @Composable ()->Unit
): Boolean {
    val empty = source.emit().isEmpty()
    return if(empty){ effect(); true } else { false }
}

@Composable fun isLoading(vararg booleans: Boolean): Boolean = booleans.any{ it }
