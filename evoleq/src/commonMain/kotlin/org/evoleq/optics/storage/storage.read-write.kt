package org.evoleq.optics.storage

import org.evoleq.math.Dispatcher
import org.evoleq.math.MathDsl
import org.evoleq.math.Source

@MathDsl
@Suppress("FunctionName")
fun <P> Read(storage: Storage<P>): Source<P> = {storage.read()}


@MathDsl
@Suppress("FunctionName")
fun <P> Dispatch(storage: Storage<P>): Dispatcher<P> = {p:P -> { storage.write(p) }  }
