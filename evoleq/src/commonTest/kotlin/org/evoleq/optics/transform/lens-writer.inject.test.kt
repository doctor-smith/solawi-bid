package org.evoleq.optics.transform

import org.evoleq.math.Writer
import org.evoleq.math.on
import org.evoleq.math.write
import org.evoleq.optics.lens.Lens
import kotlin.test.Test
import kotlin.test.assertEquals

class LensWriterInjectTest {

    data class Whole(val part: List<Part>)
    data class Part(val id: Int, val name: String)


    val lens: Lens<Whole, List<Part>> = Lens(
        get = {whole -> whole.part},
        set = {p -> {whole: Whole -> whole.copy(part = p)}}
    )

    @Test
    fun testInject() {
        val whole = Whole(listOf(Part(0,"a"),Part(1,"b")))
        val map = Writer<Part, String> { q -> {p -> p.copy(name = "${p.name} $q") } }
        val injection: Writer<Whole, List<String>> = lens.inject(map) {
                part: Part, qs: List<String> -> qs[part.id]
        }
        val qs = listOf("updated first", "updated second")

        val expected = Whole(listOf(Part(0,"a updated first"),Part(1,"b updated second")))

        val result = injection write qs on whole
        assertEquals(expected, result)
    }

    @Test
    fun testInjectBy() {
        val whole = Whole(listOf(Part(0,"a"),Part(1,"b")))
        val map = Writer<Part, String> { q -> {p -> p.copy(name = "${p.name} $q") } }
        val injection: Writer<Whole, List<String>> = lens inject map by {
                part: Part, qs: List<String> -> qs[part.id]
        }
        val qs = listOf("updated first", "updated second")

        val expected = Whole(listOf(Part(0,"a updated first"),Part(1,"b updated second")))

        val result = injection write qs on whole
        assertEquals(expected, result)
    }

    @Test
    fun testInjectByMultiplication() {
        val whole = Whole(listOf(Part(0,"a"),Part(1,"b")))
        val map = Writer<Part, String> { q -> {p -> p.copy(name = "${p.name} $q") } }
        val injection = map liftBy  {
            part: Part, qs: List<String> -> qs[part.id]
        }
        val qs = listOf("updated first", "updated second")

        val expected = Whole(listOf(Part(0,"a updated first"),Part(1,"b updated second")))

        val result = lens * injection write qs on whole
        assertEquals(expected, result)
    }
}
