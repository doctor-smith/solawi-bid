package org.solyton.solawi.bid.module.permissions.data.transform

import org.solyton.solawi.bid.module.permission.data.api.ApiContext
import org.solyton.solawi.bid.module.permission.data.api.ApiRight
import org.solyton.solawi.bid.module.permission.data.api.ApiRole
import org.solyton.solawi.bid.module.permission.data.api.ParentChildRelationsOfContexts
import org.solyton.solawi.bid.module.permissions.data.Context
import org.solyton.solawi.bid.module.permissions.data.Right
import org.solyton.solawi.bid.module.permissions.data.Role


fun ApiContext.toDomainType(): Context = Context(
    contextId = id,
    contextName = name,
    roles = roles.map {
        role -> role.toDomainType()
    }
)

fun ApiRole.toDomainType(): Role = Role(
    roleId = id,
    roleName = name,
    roleDescription = description,
    rights = rights.map {
        right -> right.toDomainType()
    }
)

fun ApiRight.toDomainType(): Right = Right(
    rightId = id,
    rightName = name,
    rightDescription = description
)

fun List<Context>.structureBy(parentChildRelations: ParentChildRelationsOfContexts): List<Context> {
    val roots: List<Context> = filter { ctx -> parentChildRelations.list.any { rel -> rel.contextId == ctx.contextId && rel.rootId == null } }
    val nonRoots = filter { ctx -> ctx !in roots }
    return roots.map { it.setupChildren(parentChildRelations, nonRoots) }
}

fun Context.setupChildren(parentChildRelations: ParentChildRelationsOfContexts, contextPool: List<Context>): Context {
    val relations = parentChildRelations.list.firstOrNull{ it.contextId == contextId }
    if(relations == null) return this

    val children = contextPool.filter { it.contextId in relations.children }.map {
        it.setupChildren(
            parentChildRelations,
            contextPool.filter { it.contextId !in relations.children }
        )
    }
    return copy(children = children)
}
