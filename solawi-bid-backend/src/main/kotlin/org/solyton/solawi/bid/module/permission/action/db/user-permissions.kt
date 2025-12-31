package org.solyton.solawi.bid.module.permission.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.permission.repository.getRightRoleContexts
import org.solyton.solawi.bid.module.permission.repository.getRoleRightContexts
import org.solyton.solawi.bid.module.permission.repository.putUserRoleContext
import org.solyton.solawi.bid.module.permission.repository.readParentChildRelationsOfContexts
import java.util.*

/**
 * Represents an action used to retrieve contextualized permission contexts (rights and roles)
 * based on a set of context identifiers. This action performs database transactions to fetch
 * associated contexts, roles, and rights given a list of string context IDs, mapping them
 * to UUIDs for lookup, and wrapping the result in a `Result` type.
 *
 * It operates within the `MathDsl` DSL and leverages various utilities for functional and monadic
 * operations including `KlAction`, `DbAction`, and `resultTransaction` to handle side-effects
 * and error propagation effectively. The outcome of this action is a list of retrieved contexts
 * encapsulated in a `Contexts` object.
 */
@MathDsl
@Suppress("FunctionName")
val ReadAvailableRightRoleContexts: KlAction<Result<Contextual<ReadRightRoleContexts>>, Result<Contexts>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContexts> ->
        resultTransaction(database){
            Contexts(getRightRoleContexts(data.data.contextIds.map {  UUID.fromString(it) }))
        }
    } x database }
}

/**
 * Represents a KlAction that fetches role-right contexts associated with a user's permissions.
 *
 * This function combines database interactions, transactional context handling, and managing
 * domain-specific results to produce a list of contexts tied to the user's roles and their rights.
 * It translates a `ReadRightRoleContextsOfUser` request into a `Contexts` result, where each context
 * contains detailed role and rights information specific to the user.
 *
 * The action operates in a suspendable transactional way to ensure consistency across database
 * operations while handling potential failures using `Result` types.
 *
 * Utilized components include:
 * - Reading user role assignments and context mappings from the database.
 * - Using roles and contexts to find corresponding rights for a user.
 * - Constructing and returning a structured hierarchy of contexts containing roles and their associated rights.
 *
 * The annotation `@MathDsl` is used to indicate which domain-specific features (like monadic operations
 * and functional constructs) can be leveraged in this context.
 */
@MathDsl
@Suppress("FunctionName")
val GetRoleRightContexts: KlAction<Result<Contextual<ReadRightRoleContextsOfUser>>, Result<Contexts>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContextsOfUser> ->
        resultTransaction(database){
            Contexts(getRoleRightContexts(UUID.fromString(data.data.userId)))
    } } x database }
}

/**
 * Represents a `KlAction` which retrieves role-right-context information for a list of users.
 *
 * - Accepts a `Result` of `Contextual<ReadRightRoleContextsOfUsers>` containing user IDs as input.
 * - Produces a `Result` of `UserToContextsMap` mapping each user ID (as String) to a list of contexts.
 *
 * The action performs the following:
 * 1. Extracts user IDs from the input data.
 * 2. Converts the string representations of user IDs to UUIDs.
 * 3. Queries database transactions to fetch cumulative role, right, and context data for the specified users.
 * 4. Maps each user ID to its associated contexts.
 *
 * This action is designed to work within a database transaction and ensures error handling through `Result`.
 *
 * This is part of the role and permission management system to determine all rights and contexts assigned
 * to users based on their roles.
 */
@MathDsl
@Suppress("FunctionName")
val GetRoleRightContextsOfUsers: KlAction<Result<Contextual<ReadRightRoleContextsOfUsers>>, Result<UserToContextsMap>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContextsOfUsers> ->
        resultTransaction(database){
            UserToContextsMap(getRoleRightContexts(data.data.userIds.map { UUID.fromString(it) }).mapKeys { it.key.toString() })
    } } x database }
}

/**
 * Represents an action that updates the roles assigned to a user within a specific context.
 *
 * The `PutUserRoleContext` action processes a request to modify the roles of a user in a given context by:
 * - Deleting all existing roles for the user within that context, except for certain defined exclusions.
 * - Assigning a new set of roles to that user in the same context.
 * - Returning the updated user context, which includes the user ID, the context details, and the assigned roles.
 *
 * Input:
 * - A `Result` wrapped `Contextual<PutUserRoleContext>` object that includes the details of the user, context,
 *   and the intended roles to assign.
 *
 * Output:
 * - A `Result` of type `UserContext` that contains the updated roles for the user within the target context
 *   along with the context information.
 *
 * Behaviour:
 * - Performs a database transaction to update the roles. Rolls back if an error occurs during execution.
 * - Throws an exception if the context specified in the input does not exist in the database.
 * - Ensures consistency of user-role assignments within the given context.
 */
@MathDsl
@Suppress("FunctionName")
val PutUserRoleContext: KlAction<Result<Contextual<PutUserRoleContext>>, Result<UserContext>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<PutUserRoleContext> ->
        resultTransaction(database){
            val userId = UUID.fromString(data.data.userId)
            val roleIds = data.data.roleIds.map { UUID.fromString(it) }
            val contextId = UUID.fromString(data.data.contextId)

            putUserRoleContext(userId, contextId, roleIds)
    } } x database }
}

/**
 * Represents an action to read and map parent-child relationships between contexts
 * from a database. This action transforms a `Result` containing context IDs into a
 * `Result` containing a comprehensive hierarchical representation of parent-child relationships.
 *
 * The action:
 * - Extracts context IDs from the input data and fetches the corresponding context entities
 *   from the database.
 * - Identifies root contexts of all provided contexts and collects their
 *   descendants -> The minimal tree containing the provided contexts
 * - Constructs a flattened structure of all contexts, ensuring uniqueness and maintaining relationships.
 * - Maps the collected data into `ParentChildRelationsOfContext` objects, capturing each context's
 *   ID, name, root ID (if available), and the IDs of its children.
 *
 * This action operates within the boundaries of a database transaction and ensures safe error
 * handling by utilizing the `Result` type.
 */
@MathDsl
@Suppress("FunctionName", "UnsafeCallOnNullableType")
val ReadParentChildRelationsOfContexts: KlAction<Result<Contextual<ReadParentChildRelationsOfContexts>>, Result<ParentChildRelationsOfContexts>> =
    KlAction { result ->
        DbAction { database ->
            result bindSuspend {contextual: Contextual<ReadParentChildRelationsOfContexts> ->
                resultTransaction(database) {
                    readParentChildRelationsOfContexts(contextual.data)
                }
            } x database
        }
    }
