package org.evoleq.optics


data class P(val name: String)
data class W(val x: Int, val p: P, val list: List<Pair<Int, String>> = listOf())