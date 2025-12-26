package org.evoleq.optics.lens

import org.evoleq.identity.Identity
import org.evoleq.math.Children
import kotlin.test.Test
import kotlin.test.assertEquals

class DeepSearchTest {

    data class Node(
        val id: String,
        private var children: List<Node> = emptyList()
    ) : Children<Node>, Identity<String> {
        override val getChildren: () -> List<Node> = { children }
        override val setChildren: (List<Node>) -> Node = { newChildren ->
            this.copy(children = newChildren)
        }
        override val getIdentity: () -> String = { id }
    }
    @Test fun findTopLevelNode() {
        val node1 = Node("node1")
        val node2 = Node("node2")
        val node3 = Node("node3")
        val node4 = Node("node4")

        val nodes = listOf(node1, node2, node3, node4)

        val lens = DeepSearch<Node, String> { it.id == node3.id }

        val result = lens.get(nodes)

        assertEquals(node3, result)

        val node5 = Node("node5")
        val newNodes = lens.set(node5)(nodes)
        assertEquals(listOf(node1, node2, node5, node4), newNodes)
    }

    @Test fun findNodeDeepInside() {
        val node1 = Node("node1")
        val node2 = Node("node2",
            listOf(
                Node("node2-1"),
                Node("node2-2"),
                Node("node2-3",listOf(
                    Node("node-2-3-1"),
                    Node("node-2-3-2")
                )),
            ))
        val node3 = Node("node3")
        val node4 = Node("node4")

        val nodes = listOf(node1, node2, node3, node4)

        val lens = DeepSearch<Node, String> { it.id == "node-2-3-1" }

        val result = lens.get(nodes)

        assertEquals(Node("node-2-3-1"), result)

        val newNode = Node("new-node-2-3-1")
        val newNodes = lens.set(newNode)(nodes)

        val expected = listOf(node1,Node("node2",
            listOf(
                Node("node2-1"),
                Node("node2-2"),
                Node("node2-3",listOf(
                    Node("new-node-2-3-1"),
                    Node("node-2-3-2")
                )),
            )) , node3, node4)
        assertEquals(expected, newNodes)
    }
}
