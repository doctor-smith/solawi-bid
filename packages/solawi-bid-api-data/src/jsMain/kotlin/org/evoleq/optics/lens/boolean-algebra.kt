package org.evoleq.optics.lens

import org.evoleq.math.Reader

fun <V, W> Lens<V, Boolean>.or(other: Lens<W, Boolean>): Reader<Pair<V, W>, Boolean> = {pair: Pair<V, W> ->
    this.get(pair.first) || other.get(pair.second)
}

fun <V> not(reader: Reader<V, Boolean>): Reader<V, Boolean> = {v:V -> !reader(v)}