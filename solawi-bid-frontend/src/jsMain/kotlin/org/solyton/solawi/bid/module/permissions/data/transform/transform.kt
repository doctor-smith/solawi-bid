package org.solyton.solawi.bid.module.permissions.data.transform

import org.solyton.solawi.bid.module.permission.data.api.ApiContext
import org.solyton.solawi.bid.module.permission.data.api.ApiRight
import org.solyton.solawi.bid.module.permission.data.api.ApiRole
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.Right
import org.solyton.solawi.bid.module.permissions.data.Role

/**
 * Converts an instance of [ApiContext] to its domain representation [Context].
 *
 * @return A [Context] object containing the mapped `contextId`, `contextName`, and a list of `roles`
 *         which are also converted to their domain types.
 */
fun ApiContext.toDomainType(): Context = Context(
    contextId = id,
    contextName = name,
    roles = roles.map {
        role -> role.toDomainType()
    }
)

/**
 * Converts an instance of [ApiRole] to its domain representation `Role`.
 *
 * @return A [Role] object containing the mapped `roleId`, `roleName`, `roleDescription`,
 *         and a list of `rights` which are also converted to their domain types.
 */
fun ApiRole.toDomainType(): Role = Role(
    roleId = id,
    roleName = name,
    roleDescription = description,
    rights = rights.map {
        right -> right.toDomainType()
    }
)

/**
 * Converts an instance of [ApiRight] to its domain representation `Right`.
 *
 * @return A [Right] object containing the corresponding `rightId`, `rightName`, and `rightDescription`
 *         values mapped from the `ApiRight` instance.
 */
fun ApiRight.toDomainType(): Right = Right(
    rightId = id,
    rightName = name,
    rightDescription = description
)

/**
 * Structures a list of contexts hierarchically based on the specified parent-child relationships.
 *
 * @param parentChildRelations the parent-child relationship data specifying how contexts are related to each other
 * @return a list of contexts structured hierarchically with nested child contexts
 */
@Suppress("ReturnCount")
fun List<Context>.structureBy(parentChildRelations: ParentChildRelationsOfContexts): List<Context> {
    if (isEmpty()) return emptyList()
    if (size == 1) return this
    val contextIds = map { it.contextId }
    val rootLikeRelations =  parentChildRelations.list.filter { rel -> rel.rootId !in contextIds }
    val roots: List<Context> = filter { ctx -> parentChildRelations.list.any {
        rel -> rel.contextId == ctx.contextId && rel in rootLikeRelations
    } }
    val rootIds = roots.map { it.contextId }
    val nonRoots = filter { ctx -> ctx.contextId !in rootIds }
    val result = roots.map { it.setupChildren(parentChildRelations, nonRoots) }
    return result
}

/**
 * Recursively structures the current context by associating child contexts from the provided context pool
 * based on the specified parent-child relationships.
 *
 * @param parentChildRelations the parent-child relationship data that defines the association between contexts
 * @param contextPool a list of available contexts to filter for child association
 * @return a copy of the current context with its `children` property populated based on the child relationships
 */
fun Context.setupChildren(
    parentChildRelations: ParentChildRelationsOfContexts,
    contextPool: List<Context>
): Context {
    // Prepare lookups for O(1) access instead of O(N) filtering in every step
    val childrenIdMap = parentChildRelations.list.associate { it.contextId to it.children }
    val poolMap = (contextPool + this).associateBy { it.contextId }

    /**
     * Tail-recursive helper function.
     * @param stack The IDs of contexts currently being processed (DFS stack).
     * @param completed A map of IDs to fully structured Context objects (with their children set).
     */
    @Suppress("ReturnCount")
    tailrec fun build(
        stack: List<String>,
        completed: Map<String, Context>
    ): Map<String, Context> {
        val currentId = stack.lastOrNull() ?: return completed

        val childrenIds = childrenIdMap[currentId]

        // If no children relations exist or the list is empty, treat as leaf
        if (childrenIds == null || childrenIds.isEmpty()) {
            val node = poolMap[currentId] ?: return build(stack.dropLast(1), completed)
            return build(stack.dropLast(1), completed + (currentId to node))
        }

        // Check if all children for the current node are already processed
        val missingChildren = childrenIds.filter { it !in completed }

        return if (missingChildren.isEmpty()) {
            // All children are completed: assemble the current node
            val node = poolMap[currentId] ?: return build(stack.dropLast(1), completed)
            val assembledChildren = childrenIds.mapNotNull { completed[it] }
            val updatedNode = node.copy(children = assembledChildren)

            // Pop current from stack and add to completed
            build(stack.dropLast(1), completed + (currentId to updatedNode))
        } else {
            // Some children are not processed yet: push them onto the stack
            build(stack + missingChildren, completed)
        }
    }

    // Start the tailrec loop with the root context ID
    val resultMap = build(listOf(this.contextId), emptyMap())

    return resultMap[this.contextId] ?: this
}
