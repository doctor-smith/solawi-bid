package org.evoleq.optics.transform

import org.evoleq.optics.storage.Storage
import org.evoleq.math.Reader

operator fun <W, P> Storage<W>.times(reader: Reader<W, P>): Reader<Unit, P> = (lens() * reader)