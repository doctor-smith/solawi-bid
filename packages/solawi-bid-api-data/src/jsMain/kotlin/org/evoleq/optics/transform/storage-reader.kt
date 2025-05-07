package org.evoleq.optics.transform

import org.evoleq.math.Reader
import org.evoleq.optics.storage.Storage

operator fun <W, P> Storage<W>.times(reader: Reader<W, P>): Reader<Unit, P> = (lens() * reader)