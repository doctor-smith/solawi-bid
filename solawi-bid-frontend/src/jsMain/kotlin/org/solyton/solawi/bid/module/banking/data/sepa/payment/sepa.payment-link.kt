package org.solyton.solawi.bid.module.banking.data.sepa.payment

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.solyton.solawi.bid.module.banking.data.SepaPaymentId
import org.solyton.solawi.bid.module.banking.data.sepa.SuccessorKind

@Lensify
data class SepaPaymentLink(
    @ReadOnly val successorId: SepaPaymentId,
    @ReadOnly val predecessorId: SepaPaymentId,
    @ReadOnly val kind: SuccessorKind
)

data class SepaPaymentPreNode(val id: SepaPaymentId, val links: List<SepaPaymentLink>)

sealed class SepaPaymentHistory(open val id: SepaPaymentId) {
    data class Initial(override val id: SepaPaymentId, val links: List<Successor>) : SepaPaymentHistory(id)
    sealed class Successor(override val id: SepaPaymentId, open val kind: SuccessorKind): SepaPaymentHistory(id) {
        data class Node(override val id: SepaPaymentId, override val kind: SuccessorKind, val links: List<Successor>) : Successor(id, kind)
        data class Leaf(override val id: SepaPaymentId, override val kind: SuccessorKind) : Successor(id, kind)
    }

    fun didNotFail(): Boolean {
        return when (this) {
            is SepaPaymentHistory.Initial -> true
            is SepaPaymentHistory.Successor -> kind != SuccessorKind.RETRY
        }
    }
}

data class SepaPaymentHistories(val all: List<SepaPaymentHistory.Initial>) {
    
    fun hasSuccessors(id: SepaPaymentId): Boolean {
        val candidate = get(id)
        return candidate != null && candidate is SepaPaymentHistory.Successor.Node && candidate.links.isNotEmpty()
    }

    fun didNotFail(id: SepaPaymentId): Boolean {
        return when (val candidate = get(id)) {
            null -> true
            is SepaPaymentHistory.Initial -> true
            is SepaPaymentHistory.Successor -> candidate.kind != SuccessorKind.RETRY
        }
    }

    @Suppress("CognitiveComplexMethod", "ReturnCount")
    fun predecessorOf(id: SepaPaymentId): SepaPaymentHistory? {
        // Helper function to check if successors list contains the given id
        fun hasSuccessorWithId(successors: List<SepaPaymentHistory.Successor>): Boolean {
            return successors.any { it.id == id }
        }

        // Helper function to search recursively through successors

        @Suppress("CognitiveComplexMethod")
        fun searchInSuccessors(successors: List<SepaPaymentHistory.Successor>): SepaPaymentHistory? {
            for (successor in successors) {
                when (successor) {
                    is SepaPaymentHistory.Successor.Node -> {
                        // Check if this node directly contains the id in its links
                        if (hasSuccessorWithId(successor.links)) {
                            return successor
                        }
                        // Recursively search in child nodes
                        val found = searchInSuccessors(successor.links)
                        if (found != null) return found
                    }

                    is SepaPaymentHistory.Successor.Leaf -> {
                        // Leaf nodes have no successors, continue
                    }
                }
            }
            return null
        }

        // Search through all initial histories
        for (initial in all) {
            // Check if the initial directly contains the id in its links
            if (hasSuccessorWithId(initial.links)) {
                return initial
            }
            // Recursively search in the initial's successors
            val found = searchInSuccessors(initial.links)
            if (found != null) return found
        }

        return null
    }


    @Suppress("CognitiveComplexMethod", "ReturnCount")
    operator fun get(id: SepaPaymentId): SepaPaymentHistory? {
        // Helper function to search recursively through successors
        @Suppress("ReturnCount")
        fun searchSuccessors(successors: List<SepaPaymentHistory.Successor>): SepaPaymentHistory? {
            for (successor in successors) {
                if (successor.id == id) return successor

                when (successor) {
                    is SepaPaymentHistory.Successor.Node -> {
                        val found = searchSuccessors(successor.links)
                        if (found != null) return found
                    }

                    is SepaPaymentHistory.Successor.Leaf -> {
                        // Already checked id above, continue to next
                    }
                }
            }
            return null
        }

        // Search through all initial histories
        for (initial in all) {
            if (initial.id == id) return initial

            val found = searchSuccessors(initial.links)
            if (found != null) return found
        }

        return null
    }

    companion object {
        fun build(links: List<SepaPaymentLink>): SepaPaymentHistories {
            // Group links by predecessor to quickly find successors
            val successorsByPredecessor = links.groupBy { it.predecessorId }

            // Find all payment IDs that are successors (not roots)
            val allSuccessorIds = links.map { it.successorId }.toSet()

            // Find root payments (those that are predecessors but never successors)
            val rootIds = links.map { it.predecessorId }.distinct()
                .filterNot { it in allSuccessorIds }

            // Recursive function to build successor tree
            fun buildSuccessors(paymentId: SepaPaymentId): List<SepaPaymentHistory.Successor> {
                val linksFromThis = successorsByPredecessor[paymentId] ?: return emptyList()

                return linksFromThis.map { link ->
                    val childSuccessors = buildSuccessors(link.successorId)
                    if (childSuccessors.isEmpty()) {
                        SepaPaymentHistory.Successor.Leaf(link.successorId, link.kind)
                    } else {
                        SepaPaymentHistory.Successor.Node(link.successorId, link.kind, childSuccessors)
                    }
                }
            }

            // Build initial histories for each root
            val histories = rootIds.map { rootId ->
                val successors = buildSuccessors(rootId)
                SepaPaymentHistory.Initial(rootId, successors)
            }

            return SepaPaymentHistories(histories)
        }
    }
}
