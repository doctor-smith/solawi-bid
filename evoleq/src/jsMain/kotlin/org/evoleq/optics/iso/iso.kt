package org.evoleq.optics.iso

data class Iso<S, T>(
    val forth: (S)->T,
    val back: (T)->S
)
