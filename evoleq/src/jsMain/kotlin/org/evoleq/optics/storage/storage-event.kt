package org.evoleq.optics.storage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.math.MathDsl


@MathDsl
fun <P> Storage<P>.onWrite(f: suspend (P) -> Unit): Storage<P> = Storage(
    read
) {
    write(it)
    CoroutineScope(Job()).launch {
        f(it)
    }
}

@MathDsl
fun <P> Storage<P>.onChange(f: suspend Storage<P>.(P,P) -> Unit): Storage<P> = Storage(
    read
) { newP -> with(read()) {
    if (this != newP) {
        val oldP = this
        this@onChange.write(newP)
        CoroutineScope(Job()).launch {
            this@onChange.f(oldP, newP)
        }
    }
} }

