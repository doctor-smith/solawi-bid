package org.evoleq.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ReadersDeepReadTest {

    // Mock implementation of Children interface for testing
    data class TestNode(
        private val children: List<TestNode>
    ) : Children<TestNode> {
        override val getChildren: () -> List<TestNode> = { children }
        override val setChildren: (List<TestNode>) -> TestNode = { TestNode(it) }
    }

    @Test
    fun `test DeepRead returns null for empty list`() {
        // Arrange
        val predicate: (TestNode) -> Boolean = { true }

        // Act
        val result = DeepRead(predicate)(emptyList())

        // Assert
        assertNull(result)
    }

    @Test
    fun `test DeepRead returns matching element from top-level list`() {
        // Arrange
        val predicate: (TestNode) -> Boolean = { it.getChildren().isEmpty() }
        val list = listOf(
            TestNode(listOf(TestNode(emptyList()))),
            TestNode(emptyList()) // Matching node
        )

        // Act
        val result = DeepRead(predicate)(list)

        // Assert
        assertEquals(list[1], result)
    }

    @Test
    fun `test DeepRead returns matching element from child nodes`() {
        // Arrange
        val predicate: (TestNode) -> Boolean = { it.getChildren().isEmpty() }
        val child = TestNode(emptyList()) // Matching node
        val parent = TestNode(listOf(child))
        val list = listOf(parent)

        // Act
        val result = DeepRead(predicate)(list)

        // Assert
        assertEquals(child, result)
    }

    @Test
    fun `test DeepRead returns null when no matching element exists`() {
        // Arrange
        val predicate: (TestNode) -> Boolean = { false }
        val list = listOf(
            TestNode(emptyList()),
            TestNode(listOf(TestNode(emptyList())))
        )

        // Act
        val result = DeepRead(predicate)(list)

        // Assert
        assertNull(result)
    }

    @Test
    fun `test DeepRead processes deeply nested children correctly`() {
        // Arrange
        val predicate: (TestNode) -> Boolean = { it.getChildren().isEmpty() }
        val deepChild = TestNode(emptyList()) // Matching node
        val nestedParent = TestNode(listOf(deepChild))
        val topParent = TestNode(listOf(nestedParent))
        val list = listOf(topParent)

        // Act
        val result = DeepRead(predicate)(list)

        // Assert
        assertEquals(deepChild, result)
    }
}
