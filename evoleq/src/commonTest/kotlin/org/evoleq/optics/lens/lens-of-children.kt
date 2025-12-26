package org.evoleq.optics.lens

import org.evoleq.identity.Identity
import org.evoleq.math.Children
import org.evoleq.optics.exception.OpticsException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LensOfChildrenTests {

    private data class TestNode(
        private val id: String = ('A'..'Z').random() + ('0'..'9').joinToString("") {
            (('A'..'Z') + ('0'..'9')).random().toString()
        },
        private val children: List<TestNode> = emptyList(),
        private val value: String = ""
    ) : Children<TestNode>, Identity<String> {
        override val getChildren: () -> List<TestNode> = { children }
        override val setChildren: (List<TestNode>) -> TestNode = { newChildren -> TestNode(id = id, children = newChildren, value = value) }
        val getValue: () -> String = { value }
        override val getIdentity : () -> String = {id}
    }

    @Test
    fun lensOfChildren() {
        val matchingNode = TestNode(value = "match")
        val root = TestNode(children = listOf(matchingNode))

        val children = Children<TestNode>().get(root)
        assertEquals(listOf(matchingNode), children)

        val nextNode = TestNode(value = "nextNode")
        val nextRoot = Children<TestNode>().set(listOf(matchingNode, nextNode) )(root)

        assertEquals(listOf(matchingNode, nextNode), Children<TestNode>().get(nextRoot))
    }

    @Test
    fun lensOfChildrenAccessChild() {

        val matchingNode = TestNode(value = "match")
        val otherNode = TestNode(value = "otherNode")
        val root = TestNode(children = listOf(matchingNode, otherNode))

        val match = Children<TestNode>() * FirstBy { it.getValue() == "match" }

        assertEquals(matchingNode, match.get(root))

        val nextNode = TestNode(value = "nextNode")
        val nextRoot = match.set(nextNode)(root)

        assertEquals(listOf(nextNode, otherNode), Children<TestNode>().get(nextRoot))
    }

    @Test
    fun step_returnsPathWhenPredicateMatches() {
        val matchingNode = TestNode(value = "match")
        val root = TestNode(children = listOf(matchingNode))

        val predicate: (TestNode) -> Boolean = { it.getValue() == "match" }
        val path = root.Step(predicate, emptyList())

        // expect (Focus on match,IdLens)
        assertEquals(2, path.size)
    }

    @Test
    fun step_returnsEmptyListWhenNoMatchingNode() {
        val root = TestNode(children = listOf(TestNode(value = "notMatch")))

        val predicate: (TestNode) -> Boolean = { it.getValue() == "match" }
        val path = root.Step(predicate, emptyList())

        assertTrue(path.isEmpty())
    }

    @Test
    fun step_handlesMultipleChildrenAndFindsPath() {
        val matchingNode = TestNode(value = "match")
        val root = TestNode(
            children =
                listOf(TestNode(),
                    TestNode(children = listOf(matchingNode))
                )
        )

        val predicate: (TestNode) -> Boolean = { it.getValue() == "match" }
        val path = root.Step(predicate, emptyList<LensType<TestNode, TestNode>>())

        assertEquals(3, path.size)
    }

    @Test
    fun step_handlesEmptyChildren() {
        val root = TestNode()

        val predicate: (TestNode) -> Boolean = { it.getValue() == "match" }
        val path = root.Step(predicate, emptyList())

        assertTrue(path.isEmpty())
    }

    @Test
    fun step_returnsPathForDeepMatch() {
        val deepNode = TestNode(value = "deepMatch")
        val root = TestNode(
            children = listOf(TestNode(
                children = listOf(
                    TestNode(children = listOf( deepNode ) )
                    )
                )
            )
        )

        val predicate: (TestNode) -> Boolean = { it.getValue() == "deepMatch" }
        val path = root.Step(predicate, emptyList())

        assertEquals(4, path.size)
    }

    @Test
    fun focus_returnsFocusOnFirstMatchingNode() {
        val matchingNode = TestNode(value = "match")
        val root = TestNode(children = listOf(matchingNode))

        val path: List<LensType<TestNode, TestNode>> = root.Step({it.getValue() == matchingNode.getValue()}, emptyList())
        val focusOnMatch = Focus(path.first(), *path.drop(1).toTypedArray())
        assertIs<Lens<TestNode, TestNode>>(focusOnMatch)
        assertEquals(matchingNode, focusOnMatch.get(root))

        val newNode = TestNode(value = "newNode")
        val nextRoot = focusOnMatch.set(newNode)(root)
        assertEquals(listOf(newNode), Children<TestNode>().get(nextRoot))
    }

    @Test
    fun deepSearch_returnsLensForSingleMatch() {
        val matchingNode = TestNode(value = "match")
        val root = TestNode(children = listOf(matchingNode))

        val lens = root.DeepSearch { it.getValue() == "match" }

        assertIs<Lens<TestNode, TestNode>>(lens)
        assertEquals(matchingNode, lens.get(root))
    }

    @Test
    fun deepSearch_returnsLensForDeepMatch() {
        val deepNode = TestNode(value = "deepMatch")
        val root = TestNode(
            children = listOf(
                TestNode(
                    children = listOf(
                        TestNode(children = listOf(deepNode))
                    )
                )
            )
        )

        val lens = root.DeepSearch { it.getValue() == "deepMatch" }

        assertIs<Lens<TestNode, TestNode>>(lens)
        assertEquals(deepNode, lens.get(root))
    }

    @Test
    fun deepSearch_throwsExceptionWhenNoMatch() {
        val root = TestNode(children = listOf(TestNode(value = "noMatch")))


        val emptyLens = root.DeepSearch { it.getValue() == "match" } as Lens<TestNode, TestNode>
        val getterException = kotlin.runCatching {
            emptyLens.get(root)
        }.exceptionOrNull()
        val setterException = kotlin.runCatching {
            emptyLens.set(root)
        }.exceptionOrNull()

        assertIs<OpticsException.Lens.DeepSearch.Empty>(getterException)
        assertIs<OpticsException.Lens.DeepSearch.Empty>(setterException)
    }
}
