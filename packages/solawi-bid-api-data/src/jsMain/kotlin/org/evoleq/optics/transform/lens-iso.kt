package org.evoleq.optics.transform

import org.evoleq.math.Maths
import org.evoleq.math.o
import org.evoleq.optics.iso.Iso
import org.evoleq.optics.lens.Lens


@Maths
operator fun <W, P, Q> Lens<W, P>.times(iso: Iso<P, Q>): Lens<W, Q> = Lens(
    iso.forth o get,
    set o iso.back
)

@Maths
operator fun <V, W, P,> Iso<V, W>.times(lens: Lens<W, P>): Lens<V, P> = Lens(
    lens.get o forth
) {
        p -> back o lens.set(p) o forth
}