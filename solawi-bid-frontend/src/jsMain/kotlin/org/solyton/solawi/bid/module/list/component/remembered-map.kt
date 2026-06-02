package org.solyton.solawi.bid.module.list.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap

/**
 * Creates a [SnapshotStateMap] and remembers it.
 */
@Composable
fun <K, V> rememberMutableStateMapOf(vararg pairs: Pair<K, V>): SnapshotStateMap<K, V> = remember {
    mutableStateMapOf(*pairs)
}
