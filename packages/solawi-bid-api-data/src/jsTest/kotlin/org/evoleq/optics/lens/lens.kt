package org.evoleq.optics.lens

import org.evoleq.ktorx.result.on
import org.evoleq.math.Reader
import org.evoleq.math.Writer
import org.evoleq.math.readFrom
import org.evoleq.math.write
import org.evoleq.optics.P
import org.evoleq.optics.W
import org.evoleq.optics.transform.times
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


class LensTest {
    @Test
    fun lensComposition() {

        val lens = Lens(
            {w: W -> w.p},
            {p: P -> { w : W -> w.copy(p = p)}}
        )
        val lens2 = Lens(
            {p: P ->p.name},
            {name: String -> {p: P -> p.copy(name = name) }}
        )

        val data = W(
            1, P("heinz")
        )

        val name = lens * lens2

        val result = name.set("joe") (data)
        val expected = W(1, P("joe"))

        assertEquals(expected, result)
    }


    @Test fun composeLensWithReader() {
        val lens = Lens(
            {w: W -> w.p},
            {p: P -> { w : W -> w.copy(p = p)}}
        )
        val reader: Reader<P, String> = {p -> p.name}

        val whole = W(3, P("name"))

        val newReader = lens * reader

        assertIs<Reader<W, String>>(newReader)
        assertEquals(whole.p.name, newReader.readFrom(whole))
    }

    @Test fun composeLensWithWriter() {
        val lens = Lens(
            {w: W -> w.p},
            {p: P -> { w : W -> w.copy(p = p)}}
        )
        val writer: Writer<P, String> = {s -> {p -> p.copy(name = s)}}

        val whole = W(3, P("name"))

        val newWriter = lens * writer

        val newName = "newName"
        val newWhole = W(3, P(newName))

        assertIs<Writer<W, String>>(newWriter)
        assertEquals(newWhole, newWriter write newName on whole)
    }
}