package org.evoleq.permission

fun combine(vararg contexts: String): String = contexts.joinToString("/") { it }
