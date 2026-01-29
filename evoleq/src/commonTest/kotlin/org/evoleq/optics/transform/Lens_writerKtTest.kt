package org.evoleq.optics.transform

import org.evoleq.optics.lens.Lens
import kotlin.test.Test
import kotlin.test.assertEquals

class LensWriterPlusOperatorTest {

    /**
     * Tests for the `plus` function in the `Lens_writerKt` class.
     *
     * This function allows adding a new element (`item`) to a list
     * encapsulated by the `Lens` and returns a `Writer` that performs
     * this addition operation.
     */
    @Test
    fun `test plus function - add item to an empty list`() {
        val lens = Lens(
            get = { w: MutableMap<String, List<String>> -> w["list"] ?: emptyList() },
            set = { p: List<String> -> { w: MutableMap<String, List<String>> -> w.apply { this["list"] = p } } }
        )
        val writer = lens.add()
        val world = mutableMapOf<String, List<String>>()

        val result = writer("Item1")(world)

        assertEquals(listOf("Item1"), result["list"])
    }

    @Test
    fun `test plus function - add item to a non-empty list`() {
        val lens = Lens(
            get = { w: MutableMap<String, List<String>> -> w["list"] ?: emptyList() },
            set = { p: List<String> -> { w: MutableMap<String, List<String>> -> w.apply { this["list"] = p } } }
        )
        val writer = lens.add()
        val world = mutableMapOf("list" to listOf("Item1"))

        val result = writer("Item2")(world)

        assertEquals(listOf("Item1", "Item2"), result["list"])
    }

    @Test
    fun `test plus function - item added only once`() {
        val lens = Lens(
            get = { w: MutableMap<String, List<String>> -> w["list"] ?: emptyList() },
            set = { p: List<String> -> { w: MutableMap<String, List<String>> -> w.apply { this["list"] = p } } }
        )
        val writer = lens.add()
        val world = mutableMapOf("list" to listOf("Item1", "Item2"))

        val result = writer("Item3")(world)

        assertEquals(listOf("Item1", "Item2", "Item3"), result["list"])
    }
}

class LensWriterUpdateOperatorTest {
    /**
     * Tests for the `update` function in the `Lens_writerKt` class.
     *
     * Description:
     * The `update` function provides a way to update an element in a list managed by a `Lens`.
     * The update is driven by a predicate function `identifiedBy`, which locates the element in the list.
     * The updated element is inserted back into the list at the location where it was found.
     */

    @Test
    fun `should update an element in list when predicate identifies the correct element`() {
        // Arrange
        val lens = Lens(
            get = { w: Map<String, List<Int>> -> w["list"] ?: emptyList() },
            set = { list: List<Int> -> { w: Map<String, List<Int>> -> w + ("list" to list) } }
        )
        val writer = lens.update { p,q -> q == 2 }
        val world = mapOf("list" to listOf(1, 2, 3))

        // Act
        val updatedWorld = writer(4)(world)

        // Assert
        assertEquals(mapOf("list" to listOf(1, 4, 3)), updatedWorld)
    }

    @Test
    fun `should not update anything if predicate does not match any element`() {
        // Arrange
        val lens = Lens(
            get = { w: Map<String, List<Int>> -> w["list"] ?: emptyList() },
            set = { list: List<Int> -> { w: Map<String, List<Int>> -> w + ("list" to list) } }
        )
        val writer = lens.update {p,q -> p == q }
        val world = mapOf("list" to listOf(1, 2, 3))

        // Act
        val updatedWorld = writer(4)(world)

        // Assert
        assertEquals(mapOf("list" to listOf(1, 2, 3)), updatedWorld)
    }

    @Test
    fun `should handle empty list gracefully`() {
        // Arrange
        val lens = Lens(
            get = { w: Map<String, List<Int>> -> w["list"] ?: emptyList() },
            set = { list: List<Int> -> { w: Map<String, List<Int>> -> w + ("list" to list) } }
        )
        val writer = lens.update { p,q -> p == q }
        val world = mapOf("list" to emptyList<Int>())

        // Act
        val updatedWorld = writer(4)(world)

        // Assert
        assertEquals(mapOf("list" to emptyList<Int>()), updatedWorld)
    }

    @Test
    fun `should update first matching element identified by predicate`() {
        // Arrange
        val lens = Lens(
            get = { w: Map<String, List<Int>> -> w["list"] ?: emptyList() },
            set = { list: List<Int> -> { w: Map<String, List<Int>> -> w + ("list" to list) } }
        )
        val writer = lens.update { p,q -> q % 2 == 0 } // Identifies even numbers
        val world = mapOf("list" to listOf(1, 2, 4, 6))

        // Act
        val updatedWorld = writer(8)(world)

        // Assert
        assertEquals(mapOf("list" to listOf(1, 8, 4, 6)), updatedWorld)
    }

    @Test
    fun `should update an element when the list contains duplicates`() {
        // Arrange
        val lens = Lens(
            get = { w: Map<String, List<Int>> -> w["list"] ?: emptyList() },
            set = { list: List<Int> -> { w: Map<String, List<Int>> -> w + ("list" to list) } }
        )
        val writer = lens.update { p,q -> q == 3 }
        val world = mapOf("list" to listOf(1, 3, 3, 5))

        // Act
        val updatedWorld = writer(9)(world)

        // Assert
        assertEquals(mapOf("list" to listOf(1, 9, 3, 5)), updatedWorld)
    }
}
