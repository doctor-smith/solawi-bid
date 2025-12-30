package org.solyton.solawi.bid.module.permission.action.db

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.solyton.solawi.bid.module.permission.data.api.*
import org.solyton.solawi.bid.module.permission.data.api.Context
import org.solyton.solawi.bid.module.permission.data.api.Right
import org.solyton.solawi.bid.module.permission.data.api.Role
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.RightsTable
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import java.util.*
import kotlin.Pair
import kotlin.String
import kotlin.Suppress
import kotlin.Triple

@MathDsl
@Suppress("FunctionName")
val ReadAvailableRightRoleContexts: KlAction<Result<Contextual<ReadRightRoleContexts>>, Result<Contexts>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContexts> ->
        resultTransaction(database){
            Contexts(getRightRoleContexts(data.data.contextIds.map {  UUID.fromString(it) }))
        }
    } x database }
}

@MathDsl
@Suppress("FunctionName")
val GetRoleRightContexts: KlAction<Result<Contextual<ReadRightRoleContextsOfUser>>, Result<Contexts>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContextsOfUser> ->
        resultTransaction(database){
            Contexts(getRoleRightContexts(UUID.fromString(data.data.userId)))
    } } x database }
}
@MathDsl
@Suppress("FunctionName")
val GetRoleRightContextsOfUsers: KlAction<Result<Contextual<ReadRightRoleContextsOfUsers>>, Result<UserToContextsMap>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<ReadRightRoleContextsOfUsers> ->
        resultTransaction(database){
            UserToContextsMap(getRoleRightContexts(data.data.userIds.map { UUID.fromString(it) }).mapKeys { it.key.toString() })
    } } x database }
}

@MathDsl
@Suppress("FunctionName")
val PutUserRoleContext: KlAction<Result<Contextual<PutUserRoleContext>>, Result<UserContext>> = KlAction {
    result ->  DbAction { database -> result bindSuspend { data: Contextual<PutUserRoleContext> ->
        resultTransaction(database){
            val userId = UUID.fromString(data.data.userId)
            val roleIds = data.data.roleIds.map { UUID.fromString(it) }
            val contextId = UUID.fromString(data.data.contextId)

            // delete all roles except owner
            UserRoleContext.deleteWhere { (UserRoleContext.userId eq userId) and (UserRoleContext.contextId eq contextId) }
            roleIds.forEach { roleId -> UserRoleContext.insert { it[this.userId] = userId; it[this.contextId] = contextId; it[this.roleId] = roleId } }

            val context = ContextEntity.findById(contextId) ?: throw NoSuchElementException("Context with id $contextId not found")

            UserContext(
                data.data.userId,
                Context(data.data.contextId,
                    context.name,
                getRolesByUserAndContext(userId, contextId).map {
                    role -> Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    role.rights.map {
                        right -> Right(
                            right.id.value.toString(),
                            right.name,
                            right.description
                        )
                    } )
                } )
            )
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

/**
 * Retrieves a list of contexts with their associated roles and rights for a specific user.
 *
 * The method processes the user's role assignments across different contexts, fetches the associated
 * rights for those roles within the relevant contexts, and constructs a hierarchical model of
 * contexts, roles, and rights.
 *
 * But Note:
 * The returned structure is flat: no parent-child structure of contexts is established
 *
 * @param userId The unique identifier of the user for whom to retrieve role and right contexts.
 * @return A list of Context objects, each containing roles with their associated rights for the user.
 */
fun Transaction.getRoleRightContexts(userId: UUID): List<Context> {
    // 1. Get all role-context assignments of the user
    val userAssignments = UserRoleContext.selectAll()
        .where { UserRoleContext.userId eq userId }
        .map { it[UserRoleContext.roleId].value to it[UserRoleContext.contextId].value }

    if (userAssignments.isEmpty()) return emptyList()

    val uniqueContextIds = userAssignments.map { it.second }.distinct()
    val uniqueRoleIds = userAssignments.map { it.first }.distinct()

    // 2. Get all relevant relations between roles, rights and contexts
    // -> Structure of the returned triples: (RoleId, ContextId, RightId)
    val roleRightContextRelations = RoleRightContexts.selectAll()
        .where { (RoleRightContexts.contextId inList uniqueContextIds) and (RoleRightContexts.roleId inList uniqueRoleIds) }
        .map {
            Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value, it[RoleRightContexts.rightId].value)
        }
        .filter { (roleId, contextId, _) -> (roleId to contextId) in userAssignments }

    // 3. Prepare data models and Lookup-Maps
    val rightIds = roleRightContextRelations.map { it.third }.distinct()
    val rightModelsById = RightEntity.find { RightsTable.id inList rightIds }
        .associate { it.id.value to Right(it.id.value.toString(), it.name, it.description) }

    val roleEntitiesById = RoleEntity.find { RolesTable.id inList uniqueRoleIds }
        .associateBy { it.id.value }

    // Group rights w.r.t. roles and contexts: Map<(ContextId, RoleId), List<Right>>
    val rightsMap = roleRightContextRelations.groupBy(
        keySelector = { (roleId, contextId, _) -> contextId to roleId },
        valueTransform = { (_, _, rightId) -> rightModelsById[rightId] }
    ).mapValues { it.value.filterNotNull() }

    // 4. Build the final structure
    return ContextEntity.find { ContextsTable.id inList uniqueContextIds }.map { contextEntity ->
        val currentContextId = contextEntity.id.value

        // Find all context-specific roles of the user
        val rolesForThisContext = userAssignments
            .filter { (_, contextId) -> contextId == currentContextId }
            .mapNotNull { (roleId, _) -> roleEntitiesById[roleId] }
            .map { roleEntity ->
                val currentRoleId = roleEntity.id.value
                Role(
                    id = currentRoleId.toString(),
                    name = roleEntity.name,
                    description = roleEntity.description,
                    rights = rightsMap[currentContextId to currentRoleId].orEmpty()
                )
            }

        Context(
            id = currentContextId.toString(),
            name = contextEntity.name,
            roles = rolesForThisContext
        )
    }
}

/**
 * Returns a complete map of cumulated user-right-role-contexts [Context]
 * Note:
 * - structure is flat: no parent-child structure of contexts is established
 */
fun Transaction.getRoleRightContexts(userIds: List<UUID>): Map<UUID, List<Context>> {
    val userRoleContexts = UserRoleContext.selectAll().where {
        UserRoleContext.userId inList  userIds
    }.map { Triple(it[UserRoleContext.roleId].value, it[UserRoleContext.contextId].value, it[UserRoleContext.userId])  }.toList()

    val roleContexts = userRoleContexts.map { Pair(it.first, it.second) }
    val contextIds = userRoleContexts.map { it.second }.distinct()
    val roleIds = userRoleContexts.map { it.first }.distinct()

    val roleRightContexts = RoleRightContexts.selectAll().where {
        (RoleRightContexts.contextId inList contextIds) and
            (RoleRightContexts.roleId inList roleIds)
    }.map {
        Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value,  it[RoleRightContexts.rightId].value)
    }.filter { Pair(it.first, it.second) in roleContexts }

    val rightIds = roleRightContexts.map { it.third }.distinct()

    val rights = RightEntity.find{
        RightsTable.id inList rightIds
    }.toList()

    val roles: List<RoleEntity> = RoleEntity.find {
        RolesTable.id inList roleIds
    }.toList()

    val contexts =  ContextEntity
        .find { ContextsTable.id inList contextIds }
        .map { context -> Context(
            context.id.value.toString(),
            context.name,
            roles.filter{ role: RoleEntity ->
                userRoleContexts.any {
                        pair -> pair.second == context.id.value && pair.first == role.id.value
                }
            }.map { role ->
                Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    rights.filter { right -> roleRightContexts.any{ triple ->
                        triple.first == role.id.value &&
                            triple.second == context.id.value &&
                            triple.third == right.id.value
                    }  }.map { right ->
                        Right(
                            right.id.value.toString(),
                            right.name,
                            right.description
                        )
                    }
                )
            }
        ) }

    return userIds.associateWith { userId ->
        contexts.filter { context -> userRoleContexts.any { it.second == UUID.fromString(context.id) && it.third == userId } }
    }
}


fun Transaction.getRolesAndRights(userIds: List<UUID>, contextId: UUID): Map<String, List<Role>> {
    val userRoles = UserRoleContext.selectAll().where {
        UserRoleContext.contextId eq contextId and
        (UserRoleContext.userId inList  userIds)
    }.map { Pair( it[UserRoleContext.userId], it[UserRoleContext.roleId].value)  }.toList()


    val roleIds = userRoles.map { it.second }.distinct()

    val roleRightContexts = RoleRightContexts.selectAll().where {
        (RoleRightContexts.contextId eq contextId) and
            (RoleRightContexts.roleId inList roleIds)
    }.map {
        Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value,  it[RoleRightContexts.rightId].value)
    }

    val rightIds = roleRightContexts.map { it.third }.distinct()

    val rights = RightEntity.find{
        RightsTable.id inList rightIds
    }.toList()

    val roles = RoleEntity.find {
        RolesTable.id inList roleIds
    }

    val userRolesMap = userRoles.associate { pair -> Pair(
        pair.first.toString(),
        roles.filter { role -> userRoles.contains(Pair(pair.first, role.id.value)) }
            .map { role ->
                Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    rights.filter { right ->
                        roleRightContexts.any { triple ->
                            triple.first == role.id.value &&
                                triple.second == contextId &&
                                triple.third == right.id.value
                        }
                    }.map { right ->
                        Right(
                            right.id.value.toString(),
                            right.name,
                            right.description
                        )
                    }
                )
            }
        )
    }
    return userRolesMap
}

/**
 * Get all users in a context together with their rights and roles
 */
fun Transaction.getUserRolesAndRights(contextId: UUID): Map<String, List<Role>> {
    val userRoles = UserRoleContext.selectAll().where {
        UserRoleContext.contextId eq contextId
    }.map { Pair( it[UserRoleContext.userId], it[UserRoleContext.roleId].value)  }.toList()


    val roleIds = userRoles.map { it.second }.distinct()

    val roleRightContexts = RoleRightContexts.selectAll().where {
        (RoleRightContexts.contextId eq contextId) and
            (RoleRightContexts.roleId inList roleIds)
    }.map {
        Triple(it[RoleRightContexts.roleId].value, it[RoleRightContexts.contextId].value,  it[RoleRightContexts.rightId].value)
    }

    val rightIds = roleRightContexts.map { it.third }.distinct()

    val rights = RightEntity.find{
        RightsTable.id inList rightIds
    }.toList()

    val roles = RoleEntity.find {
        RolesTable.id inList roleIds
    }

    val userRolesMap = userRoles.associate { pair -> Pair(
        pair.first.toString(),
        roles.filter { role -> userRoles.contains(Pair(pair.first, role.id.value)) }
            .map { role ->
                Role(
                    role.id.value.toString(),
                    role.name,
                    role.description,
                    rights.filter { right ->
                        roleRightContexts.any { triple ->
                            triple.first == role.id.value &&
                                triple.second == contextId &&
                                triple.third == right.id.value
                        }
                    }.map { right ->
                        Right(
                            right.id.value.toString(),
                            right.name,
                            right.description
                        )
                    }
                )
            }
        )
    }
    return userRolesMap
}

/**
 * Give a list of context ids return the associated contexts together with roles and rights
 */
fun Transaction.getRightRoleContexts(contextsIds: List<UUID>): List<Context> {
    val contexts = ContextEntity
        .find { ContextsTable.id inList contextsIds }
        .map { contextEntity -> Context(
            id = contextEntity.id.value.toString(),
            name = contextEntity.name,
            roles = contextEntity.roles
                .distinctBy { role -> role.id.value }
                .map { role -> Role(
                    id = role.id.value.toString(),
                    name = role.name,
                    description = role.description,
                    rights = role.rights
                        .distinctBy { right -> right.id.value }
                        .map{right -> Right(
                            id = right.id.value.toString(),
                            name = right.name,
                            description = right.description
                        )
                    }
                )
            }
        )
    }
    return contexts
}

fun Transaction.getUserRightContexts(userId: UUID, rights: List<String>): List<UUID> {
    val rightIds = RightEntity.find { RightsTable.name inList rights }.map { it.id.value }.distinct()
    val userRoleContexts = UserRoleContext.selectAll().where{
        UserRoleContext.userId eq userId
    }.map {
        it[UserRoleContext.roleId] x it[UserRoleContext.contextId]
    }
    val relevantUserRoleIds = RoleEntity.find { RolesTable.id inList userRoleContexts.map { it.first } }.distinctBy {
        role -> role.id.value
    }.filter {
        role -> role.rights.toList().map { it.id.value }.any { rightIds.contains(it) }
    }.map {
        role -> role.id.value
    }
    return userRoleContexts.filter {
        pair -> relevantUserRoleIds.contains(pair.first.value)
    }.map { pair -> pair.second.value }
}

fun Transaction.getRolesByUserAndContext(userId: UUID, contextId: UUID): List<RoleEntity> {
    val roleIds = UserRoleContext.selectAll().where{
        UserRoleContext.userId eq userId and (UserRoleContext.contextId eq contextId)
    }.map { it[UserRoleContext.roleId].value }
    return RoleEntity.find { RolesTable.id inList roleIds }.toList()
}
