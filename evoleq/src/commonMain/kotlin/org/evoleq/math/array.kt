package org.evoleq.math

inline fun <reified T> arrayOf(vararg elements: T?): Array<T> = elements.filterNotNull().toTypedArray()
