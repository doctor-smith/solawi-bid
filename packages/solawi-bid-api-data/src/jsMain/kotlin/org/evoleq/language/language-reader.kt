package org.evoleq.language

import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times


fun Texts(path: String): Reader<Lang.Block, Lang.Block> = {
    block -> block.component(path)
}

fun <T> Source<T>.store(): Storage<Unit> = Storage(
    read = {emit()},
    write = {}
)

operator fun <T> Lens<T, Lang.Block>.times(component: String): Reader<T, Lang.Block> = this * Texts(component)