package org.evoleq.optics.storage

import org.evoleq.math.*

import org.evoleq.optics.P
import org.evoleq.optics.W
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.transform.merge
import org.evoleq.optics.transform.times
import org.evoleq.optics.transform.timesW
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs


class StorageTest {
    @Test
    fun composeStorageWithLenses() {
        var w: W = W(0, P(""))
        val storage = Storage<W>(
            {w},
            {w = it}
        )
        assertEquals(w, storage.read())
        val w1 = W(1, P(""))
        storage.write(W(1, P("")))
        assertEquals(w1, storage.read())

        val p = storage * Lens<W, P>(
            {w -> w.p},
            {p -> {w -> w.copy(p = p)}}
        )
        val name = p * Lens(
            {p -> p.name},
            {name -> {p -> p.copy(name = name)}}
        )
        name.write("flo")

        assertEquals("flo",name.read())
    }

    @Test
    fun storageDSL() {
        var x = 1
        val storage = Storage(
            {x},
            {s -> x = s}
        )

        val X = Read from storage
        assertEquals(1 ,X)
        write(5)  to storage
        assertEquals(5, read (storage))
    }

    @Test fun composeStorageWithReader() {
        val name = "name"
        var w: W = W(0, P(name))
        val storage = Storage<W>(
            {w},
            {w = it}
        )
        val pLens = Lens<W, P>(
            {w -> w.p},
            {p -> {w -> w.copy(p = p)}}
        )

        val reader: Reader<P, String> = {p -> p.name}

        val source = storage * pLens * reader
        assertIs<Reader<Unit, String>>(source)
        assertEquals(name, source.emit())
    }

    @Test fun composeStorageWithWriter() {
        val  name = "name"
        var w: W = W(0, P(name))
        val storage = Storage<W>(
            {w},
            {w = it}
        )
        val pLens = Lens<W, P>(
            {w -> w.p},
            {p -> {w -> w.copy(p = p)}}
        )

        val writer: Writer<P, String> = {s -> {p -> p.copy(name = s)}}
        val dispatcher = storage * pLens * writer

        val newName = "newName"
        val newWhole = W(0,P(newName))

        assertIs<Writer<Unit, String>>(dispatcher)
        assertIs<Dispatcher<String>>(dispatcher)
        val unit: Unit = dispatcher dispatch newName

        assertEquals(newWhole, storage.read())
    }


    @Test fun composeWithMerge() {
        val list: Lens<W, List<Pair<Int, String>>> = Lens(
            get = {w -> w.list},
            set = {list -> {w: W -> w.copy(list = list) }}
        )

        val writer: Writer<W, List<Pair<Int, String>>> = list timesW Writer{
            inc: List<Pair<Int, String>> -> {l -> l.merge(inc){g, i -> g.first == i.first} }
        }

        val w = writer(listOf(1 to "one"))(W(1,P("")))

        assertEquals(1, w.list.size)
        assertContains( w.list,  1 to "one")

        // override parts of the list
        val in1 = listOf(1 to "ONE", 2 to "two")
        val w1 = writer(listOf(1 to "ONE", 2 to "two"))(w)

        assertEquals(in1, w1.list)
        assertContains( w1.list,  1 to "ONE")
        assertContains( w1.list,  2 to "two")
        assertEquals(2, w1.list.size)

    }

    @Test fun composeWithMergeHighLevel() {
        val list: Lens<W, List<Pair<Int, String>>> = Lens(
            get = {w -> w.list},
            set = {list -> {w: W -> w.copy(list = list) }}
        )

        val writer: Writer<W, List<Pair<Int, String>>> = list merge { g, i -> g.first == i.first }


        val w = writer(listOf(1 to "one"))(W(1,P("")))

        assertEquals(1, w.list.size)
        assertContains( w.list,  1 to "one")

        // override parts of the list
        val in1 = listOf(1 to "ONE", 2 to "two")
        val w1 = writer(listOf(1 to "ONE", 2 to "two"))(w)

        assertEquals(in1, w1.list)
        assertContains( w1.list,  1 to "ONE")
        assertContains( w1.list,  2 to "two")
        assertEquals(2, w1.list.size)

    }

}