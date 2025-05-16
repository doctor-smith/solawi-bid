package org.solyton.solawi.bid.module.db.repository

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.solyton.solawi.bid.module.db.ContextException
import org.solyton.solawi.bid.module.db.schema.*
import java.util.*

fun ContextEntity.rootId(): EntityID<UUID> = root?.id?:id

fun ContextEntity.ancestors(): SizedIterable<ContextEntity> {
    if(root == null) {
        return emptySized()
    }
    require(root != null)
    val rootId = root!!.id
    val ancs = ContextEntity.find {
        (ContextsTable.rootId eq rootId or (ContextsTable.id eq rootId)) and (ContextsTable.left less this@ancestors.left) and (ContextsTable.right greater this@ancestors.right)
    }
    return ancs.orderBy(ContextsTable.left to SortOrder.ASC)
}

fun ContextEntity.descendants(): SizedIterable<ContextEntity> = with(root?.id?:id) {
    val rootId = this
    ContextEntity.find {
        ContextsTable.rootId eq rootId and (ContextsTable.left greater this@descendants.left) and (ContextsTable.right less this@descendants.right)
    }.orderBy(ContextsTable.left to org.jetbrains.exposed.sql.SortOrder.ASC)
}

fun createRootContext(contextName: String): ContextEntity {
    return ContextEntity.new {
        name = contextName.replace(" ", "_")
    }
}


fun ContextEntity.createChild(name: String): ContextEntity {
    val ancestors = ancestors()
    val maxRight = root?.right?:1
    val siblings = ContextEntity.find {
        ContextsTable.rootId eq root?.id and (ContextsTable.left greater right) and (ContextsTable.right less maxRight)
    }
    val oldRight = right
    right += 2
    ancestors.forEach {
        it.right += 2
    }
    siblings.forEach {
        it.left += 2
        it.right += 2
    }
    val rootOrg = when{
        level == 0 -> this
        else -> root!!
    }
    val childLevel = level +1
    return ContextEntity.new {
        root = rootOrg
        this.name = name
        left = oldRight
        right = oldRight + 1
        level = childLevel
    }
}

fun ContextEntity.addChild(newChild: ContextEntity): ContextEntity {
    val ancestors = ancestors()
    val maxRight = root?.right?:1
    val siblings = ContextEntity.find {
        ContextsTable.rootId eq root?.id and (ContextsTable.left greater right) and (ContextsTable.right less maxRight)
    }
    val oldRight = right
    val diff = newChild.right - newChild.left + 1
    right += diff
    ancestors.forEach {
        it.right += diff
    }
    siblings.forEach {
        it.left += diff
        it.right += diff
    }
    val rootOrg = when{
        level == 0 -> this
        else -> root!!
    }
    val childLevel = level + 1
    val childLevelDiff = level - newChild.level + 1
    val shift = oldRight - newChild.left



    val descendants = newChild.descendants()
    descendants.forEach {

       // val descDiff = it.right - it.left
        it.root = rootOrg
        it.level += childLevelDiff
        it.left +=  shift
        it.right += shift
    }

    newChild.root = rootOrg
    newChild.left += shift
    newChild.right += shift // + diff - 2
    newChild.level += childLevelDiff
    return this
}

fun ContextEntity.removeChild(childId: UUID) : ContextEntity {
    val child = ContextEntity.findById(childId)?: throw org.solyton.solawi.bid.module.db.ContextException.NoSuchChildContext(childId)
    child.remove()
    return this
}


fun ContextEntity.remove(): Unit {

    val maxRight = root?.right?:1
    val diff = right - left +1

    val descendants = descendants()
    val ancestors = ancestors()
    val siblings = ContextEntity.find {
        ContextsTable.rootId eq root?.id and (ContextsTable.left greater left) and (ContextsTable.right less maxRight)
    }

    ancestors.forEach {
        it.right -= diff
    }
    siblings.forEach {
        it.right -= diff
        it.left -= diff
    }
    descendants.forEach { it.delete() }
    delete()
}

fun ContextEntity.getChildren(): SizedIterable<ContextEntity> {
    val rootId = root?.id?:id
    val childLevel = level + 1
    return ContextEntity.find {
        ContextsTable.rootId eq rootId and (ContextsTable.level eq childLevel) and (ContextsTable.left greater left) and (ContextsTable.right less right)
    }
}

fun ContextEntity.parent(): ContextEntity? = when {
    root == null -> null
    else -> ContextEntity.find {
        (ContextsTable.rootId eq root!!.id or (ContextsTable.id eq root!!.id)) and
        (ContextsTable.level eq (level -1)) and
        (ContextsTable.left less left) and
        (ContextsTable.right greater right)
    }.first()
}

fun ContextEntity.pathAsList(): List<ContextEntity> = when {
    root == null -> listOf(this)
    else -> listOf(
        *ancestors().filter {
            it.left <= left && it.right >= right
        }.sortedBy { it.level }.toTypedArray(),
        this
    )
}

fun ContextEntity.path(): String = pathAsList().join()

fun List<ContextEntity>.join(): String = joinToString("/") { it.name }

data class ContextToNest(val context: ContextEntity,val  names: List<String>, val roots: Set<ContextEntity>)

fun ContextToNest.nest(): ContextToNest = when{
    names.isEmpty() -> this
    else -> {
        val name = names.first()
        val rest = names.drop(1)
        val root = roots.firstOrNull { it.name == name }
        if(root != null) {
            context.delete()
            ContextToNest(root, rest, setOf(root)).nest()
        } else {
            val found = roots.first().getChildren().firstOrNull { it.name == name }
            val newContext = when {
                found != null -> found
                else -> context.createChild(name)
            }
            ContextToNest(newContext, rest, roots).nest()
        }

    }
}

/**
 * Clone a context together with all its children
 */
fun Transaction.cloneContext(contextId: UUID, identifier: String): List<ContextEntity> {

    val contextToClone = ContextEntity.findById(contextId)?: throw ContextException.NoSuchContext(contextId.toString())
    if(contextToClone.root != null) throw ContextException.NoSuchRootContext(contextId.toString())

    val descendants = contextToClone.descendants().toList()

    val rootContext = ContextEntity.new {
        name = contextToClone.name + identifier
        level = contextToClone.level
        left = contextToClone.left
        right = contextToClone.right
    }
    val contexts =descendants.map { context ->
        ContextEntity.new{
            name = context.name + identifier
            root = rootContext
            level = context.level
            left = context.left
            right = context.right
        }
    }
    return listOf(
        rootContext,
        *contexts.toTypedArray()
    )
}

/*
data class IndexedSegment(val index: Int, val segment: String)

fun findContextByPath(path: String): ContextEntity {
    val pathAsList = path.split("/").mapIndexed{index, segment -> IndexedSegment(index, segment)}
    val rootSegment = pathAsList.first()

    val rootContext = ContextEntity.find { ContextsTable.name eq rootSegment.segment and (ContextsTable.level eq rootSegment.index) }.firstOrNull()
        ?: throw ContextException.NoSuchContextPath(path, rootSegment.segment, rootSegment.index)

    if(pathAsList.size == 1) return rootContext

    val last = pathAsList.last()
    val leafs = ContextEntity.find { ContextsTable.name eq last.segment and (ContextsTable.level eq last.index) and (ContextsTable.rootId eq rootContext.id) }.toList()

    if(leafs.size == 1) return leafs.first()


}


 */
