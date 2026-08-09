package org.evoleq.compose.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage

@Composable fun LaunchedEffectOnSource(
    key: Source<Any?>,
    block: suspend CoroutineScope.()->Unit
) = LaunchedEffect(key.emit(), block)

@Composable fun LaunchedEffectOnSource(
    key1: Source<Any?>,
    key2: Source<Any?>,
    key3: Source<Any?>,
    block: suspend CoroutineScope.()->Unit
) = LaunchedEffect(
    key1.emit(),
    key2.emit(),
    key3.emit(),
    block
)

@Composable fun LaunchedEffectOnSource(
    key1: Source<Any?>,
    key2: Source<Any?>,
    block: suspend CoroutineScope.()->Unit
) = LaunchedEffect(key1.emit(), key2.emit(), block)

@Composable fun LaunchedEffectOnStorage(
    key: Storage<Any?>,
    block: suspend CoroutineScope.()->Unit
) =
    LaunchedEffectOnSource(Read(key), block)