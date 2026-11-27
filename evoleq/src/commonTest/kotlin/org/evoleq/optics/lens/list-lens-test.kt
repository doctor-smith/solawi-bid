package org.evoleq.optics.lens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ListLensTest {

    @Test
    fun filterBy_shouldReturnSublistMatchingPredicate() {
        // Arrange
        val list = listOf(1, 2, 3, 4, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val selection = FilterBy(predicate)

        // Act
        val filteredList = selection.select(list)

        // Assert
        assertEquals(listOf(2, 4), filteredList)
    }

    @Test
    fun filterBy_shouldReplaceMatchingSublist() {
        // Arrange
        val list = listOf(1, 2, 3, 4, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val selection = FilterBy(predicate)
        val replacement = listOf(6, 8)

        // Act
        val newList = selection.replaceWith(list, replacement)

        // Assert
        assertEquals(listOf(1, 3, 5, 6, 8), newList)
    }

    @Test
    fun filterBy_shouldHandleEmptyInputList() {
        // Arrange
        val list = emptyList<Int>()
        val predicate: (Int) -> Boolean = { it > 10 }
        val selection = FilterBy(predicate)

        // Act
        val filteredList = selection.select(list)
        val newList = selection.replaceWith(list, listOf(20, 30))

        // Assert
        assertTrue(filteredList.isEmpty())
        assertEquals(listOf(20, 30), newList)
    }

    @Test
    fun filterBy_shouldHandleNoMatchingElementsInGet() {
        // Arrange
        val list = listOf(1, 3, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val selection = FilterBy(predicate)

        // Act
        val filteredList = selection.select(list)

        // Assert
        assertTrue(filteredList.isEmpty())
    }

    @Test
    fun filterBy_shouldHandleNoMatchingElementsInSet() {
        // Arrange
        val list = listOf(1, 3, 5)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val selection = FilterBy(predicate)
        val replacement = listOf(6, 8)

        // Act
        val newList = selection.replaceWith(list, replacement)

        // Assert
        assertEquals(listOf(1, 3, 5, 6, 8), newList)
    }

    @Test
    fun filterBy_shouldHandleAllMatchingElements() {
        // Arrange
        val list = listOf(2, 4, 6, 8)
        val predicate: (Int) -> Boolean = { it % 2 == 0 }
        val selection = FilterBy(predicate)
        val replacement = listOf(10, 12)

        // Act
        val filteredList = selection.select(list)
        val newList = selection.replaceWith(list, replacement)

        // Assert
        assertEquals(list, filteredList)
        assertEquals(listOf(10, 12), newList)
    }

    @Test
    fun filterBy_shouldAppendReplacementAfterNonMatchingElements() {
        // Arrange
        val list = listOf(1, 2, 3, 4, 5)
        val predicate: (Int) -> Boolean = { it < 3 }
        val selection = FilterBy(predicate)
        val replacement = listOf(6, 7)

        // Act
        val newList = selection.replaceWith(list, replacement)

        // Assert
        assertEquals(listOf(3, 4, 5, 6, 7), newList)
    }

    @Test
    fun firstBy_reusingSameLensDoesNotLeakPreviouslyComputedListSegments() {
        val lens = FirstBy<Int> { it == 2 }

        assertEquals(listOf(1, 20, 3), lens.set(20)(listOf(1, 2, 3)))
        assertEquals(listOf(9, 200, 8), lens.set(200)(listOf(9, 2, 8)))
        assertEquals(listOf(4, 2000, 5), lens.set(2000)(listOf(4, 2, 5)))
    }

    @Test
    fun firstBy_replacesOnlyFirstMatchAndPreservesSurroundingOrder() {
        val lens = FirstBy<Int> { it == 2 }
        assertEquals(
            listOf(1, 20, 3, 2, 4),
            lens.set(20)(listOf(1, 2, 3, 2, 4))
        )
    }

    @Test
    fun firstBy_missingMatchFailsConsistentlyForReadAndWrite() {
        val lens = FirstBy<Int> { it == 2 }
        val source = listOf(1, 3, 5)

        assertFailsWith<NoSuchElementException> { lens.get(source) }
        assertFailsWith<NoSuchElementException> { lens.set(20)(source) }
    }

    @Test
    fun firstBy_lensLawsHoldWhenReplacementPreservesPredicate() {
        val lens = FirstBy<Int> { it % 2 == 0 }
        val source = listOf(1, 2, 3, 4)
        val firstReplacement = 6
        val secondReplacement = 8

        // Get-Put
        assertEquals(source, lens.set(lens.get(source))(source))
        // Put-Get
        assertEquals(firstReplacement, lens.get(lens.set(firstReplacement)(source)))
        // Put-Put
        assertEquals(
            lens.set(secondReplacement)(source),
            lens.set(secondReplacement)(lens.set(firstReplacement)(source))
        )
    }

    @Test
    fun filterBy_selectionDoesNotClaimLensLaws() {
        val selection = FilterBy<Int> { it % 2 == 0 }
        val source = listOf(1, 2, 3)

        assertEquals(listOf(2), selection.select(source))
        // Existing replace behavior is intentional: selected elements are appended.
        // This operation is not modeled as a Lens.
        assertEquals(listOf(1, 3, 2), selection.replaceWith(source, selection.select(source)))
    }
}
