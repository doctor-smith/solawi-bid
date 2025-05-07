package org.evoleq.optics.transform

import org.evoleq.math.Writer
import org.evoleq.optics.storage.Storage

operator fun <W, P> Storage<W>.times(writer: Writer<W, P>): Writer<Unit, P> = lens() * writer