package org.solyton.solawi.bid.module.permission.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.or
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContext
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permission.data.api.ReadParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import java.util.*


/**
 * Reads the parent-child relationships of contexts based on the provided context identifiers.
 * The function:
 * - Extracts context IDs from the input data and fetches the corresponding context entities
 *    from the database.
 *  - Identifies root contexts of all provided contexts and collects their
 *    descendants -> The minimal tree containing the provided contexts
 *  - Constructs a flattened structure of all contexts, ensuring uniqueness and maintaining relationships.
 *  - Maps the collected data into `ParentChildRelationsOfContext` objects, capturing each context's
 *    ID, name, root ID (if available), and the IDs of its children.
 *
 * @param parentChildRelationsOfContexts The data class containing a list of context IDs for which the parent-child relationships should be determined.
 * @return A `ParentChildRelationsOfContexts` object containing a list of parent-child relationships for each context.
 */
fun Transaction.readParentChildRelationsOfContexts(parentChildRelationsOfContexts: ReadParentChildRelationsOfContexts): ParentChildRelationsOfContexts {
    val contextUuids = parentChildRelationsOfContexts.contextIds.map{ UUID.fromString(it) }
    val contextEntities = ContextEntity.find {
        ContextsTable.id inList contextUuids
    }.toList()
    val roots = contextEntities.filter { it.root == null }
    @Suppress("UnsafeCallOnNullableType")
    val otherRoots = contextEntities.filter { it.root != null }.map { it.root!! }
    val contextRoots = listOf(
        *roots.toTypedArray(),
        *otherRoots.toTypedArray()
    ).distinctBy { root -> root.id.value }

    val contextRootIds = contextRoots.map { it.id.value }
    val allContexts = ContextEntity.find {
        (ContextsTable.rootId inList contextRootIds) or
                (ContextsTable.id inList contextRootIds)
    }.toList()

    return ParentChildRelationsOfContexts(
        list = allContexts.map { contextEntity ->
            ParentChildRelationsOfContext(
                contextId = contextEntity.id.value.toString(),
                name = contextEntity.name,
                rootId = contextEntity.root?.id?.value?.toString(),
                children = allContexts.filter {
                        context ->
                    (context.root?.id == (contextEntity.root?.id ?: contextEntity.id)) &&
                            (context.level == contextEntity.level + 1)
                }.map { context -> context.id.value.toString() }
            )
        }
    )
}
