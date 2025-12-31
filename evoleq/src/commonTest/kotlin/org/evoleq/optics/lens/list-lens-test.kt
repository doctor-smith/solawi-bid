package org.evoleq.optics.lens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListLensTest {

    @Test
    fun filterBy_shouldReturnSublistMatchingPredicate() {
        // Arrange
        val list = listOf(1, 2, 3, 4, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val lens = FilterBy(predicate)

        // Act
        val filteredList = lens.get(list)

        // Assert
        assertEquals(listOf(2, 4), filteredList)
    }

    @Test
    fun filterBy_shouldReplaceMatchingSublist() {
        // Arrange
        val list = listOf(1, 2, 3, 4, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val lens = FilterBy(predicate)
        val replacement = listOf(6, 8)

        // Act
        val newList = lens.set(replacement)(list)

        // Assert
        assertEquals(listOf(1, 3, 5, 6, 8), newList)
    }

    @Test
    fun filterBy_shouldHandleEmptyInputList() {
        // Arrange
        val list = emptyList<Int>()
        val predicate: (Int) -> Boolean = { it > 10 }
        val lens = FilterBy(predicate)

        // Act
        val filteredList = lens.get(list)
        val newList = lens.set(listOf(20, 30))(list)

        // Assert
        assertTrue(filteredList.isEmpty())
        assertEquals(listOf(20, 30), newList)
    }

    @Test
    fun filterBy_shouldHandleNoMatchingElementsInGet() {
        // Arrange
        val list = listOf(1, 3, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val lens = FilterBy(predicate)

        // Act
        val filteredList = lens.get(list)

        // Assert
        assertTrue(filteredList.isEmpty())
    }

    @Test
    fun filterBy_shouldHandleNoMatchingElementsInSet() {
        // Arrange
        val list = listOf(1, 3, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val lens = FilterBy(predicate)
        val replacement = listOf(6, 8)

        // Act
        val newList = lens.set(replacement)(list)

        // Assert
        assertEquals(listOf(1, 3, 5, 6, 8), newList)
    }

    @Test
    fun filterBy_shouldHandleAllMatchingElements() {
        // Arrange
        val list = listOf(2, 4, 6, 8)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val lens = FilterBy(predicate)
        val replacement = listOf(10, 12)

        // Act
        val filteredList = lens.get(list)
        val newList = lens.set(replacement)(list)

        // Assert
        assertEquals(list, filteredList)
        assertEquals(listOf(10, 12), newList)
    }

    @Test
    fun filterBy_shouldPreserveOrderInSetOperation() {
        // Arrange
        val list = listOf(1, 2, 3, 4, 5)
        val predicate: (Int) -> Boolean = { it < 3 }
        val lens = FilterBy(predicate)
        val replacement = listOf(6, 7)

        // Act
        val newList = lens.set(replacement)(list)

        // Assert
        assertEquals(listOf(3, 4, 5, 6, 7), newList)
    }
}
