package org.evoleq.math

fun or(vararg booleans: Boolean): Boolean = booleans.any()

fun and(vararg booleans: Boolean): Boolean = booleans.none()
