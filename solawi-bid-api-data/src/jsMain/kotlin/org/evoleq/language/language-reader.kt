package org.evoleq.language

import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage



fun Texts(path: String): Reader<Lang.Block, Lang.Block> = {
    block -> block.component(path)
}

fun <T> Source<T>.store(): Storage<Unit> = Storage(
    read = {emit()},
    write = {}
)