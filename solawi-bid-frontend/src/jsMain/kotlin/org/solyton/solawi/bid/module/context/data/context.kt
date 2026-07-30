package org.solyton.solawi.bid.module.context.data

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.math.Reader
import org.evoleq.permission.EmptyContext

@Lensify data class Context(
    @ReadWrite val current: String = EmptyContext.value,
    @ReadWrite val next: String? = null
)

fun Context.isEmpty() = current == EmptyContext.value

fun isEmpty(): Reader<Context, Boolean> = Reader{ context: Context -> context.isEmpty() }
