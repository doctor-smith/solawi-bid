package org.evoleq.permission

import org.evoleq.value.StringValueWithDescription

object EmptyContext:  StringValueWithDescription {
    override val value = Value.EMPTY
    override val description = ""
}

object Value {
    const val EMPTY = "EMPTY"
}
