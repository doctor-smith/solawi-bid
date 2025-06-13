package org.solyton.solawi.bid.module.user.schema.repository

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.user.exception.UserManagementException
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RightEntity
import org.solyton.solawi.bid.module.permission.schema.Rights
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RoleRightContexts
import org.solyton.solawi.bid.module.permission.schema.Roles
import org.solyton.solawi.bid.module.user.schema.OrganizationEntity
import org.solyton.solawi.bid.module.user.schema.OrganizationsTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserOrganization
import java.util.UUID

fun OrganizationEntity.ancestors(): SizedIterable<OrganizationEntity> {
    if(root == null) {
        return emptySized()
    }
    requireNotNull(root)
    val rootId = root!!.id
    val ancs = OrganizationEntity.find {
        (OrganizationsTable.rootId eq rootId or (OrganizationsTable.id eq rootId)) and (OrganizationsTable.left less this@ancestors.left) and (OrganizationsTable.right greater this@ancestors.right)
    }
    return ancs.orderBy(OrganizationsTable.left to SortOrder.ASC)
}

fun OrganizationEntity.descendants(): SizedIterable<OrganizationEntity> = with(root?.id?:id) {
    val rootId = this
    OrganizationEntity.find {
        OrganizationsTable.rootId eq rootId and (OrganizationsTable.left greater this@descendants.left) and (OrganizationsTable.right less this@descendants.right)
    }.orderBy(OrganizationsTable.left to SortOrder.ASC)
}

fun createRootOrganization(organizationName: String): OrganizationEntity {
    val finalOrganizationName = organizationName.replace(" ", "_").uppercase()
    val exists = !ContextEntity.find { ContextsTable.name eq finalOrganizationName and (ContextsTable.rootId eq null) }.empty()

    if(exists) throw ContextException.PathAlreadyExists(finalOrganizationName)

    val organizationContext = ContextEntity.new {
        name = finalOrganizationName
    }
    val manager = RoleEntity.find { Roles.name eq "MANAGER" }.first()
    val read = RightEntity.find { Rights.name eq "READ" }.first()
    val write = RightEntity.find { Rights.name eq "UPDATE" }.first()

    RoleRightContexts.insert {
        it[roleId] = manager.id
        it[rightId] = read.id
        it[contextId] = organizationContext.id
    }

    RoleRightContexts.insert {
        it[roleId] = manager.id
        it[rightId] = write.id
        it[contextId] = organizationContext.id
    }

    return OrganizationEntity.new {
        name = organizationName
        context = organizationContext
    }
}


fun OrganizationEntity.createChild(name: String): OrganizationEntity {
    val ancestors = ancestors()
    println(ancestors.map { it.name })
    val maxRight = root?.right?:1
    val siblings = OrganizationEntity.find {
        OrganizationsTable.rootId eq root?.id and (OrganizationsTable.left greater right) and (OrganizationsTable.right less maxRight)
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
    return OrganizationEntity.new {
        root = rootOrg
        context = rootOrg.context
        this.name = name
        left = oldRight
        right = oldRight + 1
        level = childLevel
    }
}

fun OrganizationEntity.removeChild(childId: UUID) : OrganizationEntity {
    val child = OrganizationEntity.findById(childId)?: throw UserManagementException.NoSuchChildOrganization(childId.toString())
    child.remove()
    return this
}


fun OrganizationEntity.remove(): Unit {

    val maxRight = root?.right?:1
    val diff = right - left +1

    val descendants = descendants()
    val ancestors = ancestors()
    val siblings = OrganizationEntity.find {
        OrganizationsTable.rootId eq root?.id and (OrganizationsTable.left greater left) and (OrganizationsTable.right less maxRight)
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

fun OrganizationEntity.getChildren(): SizedIterable<OrganizationEntity> {
    val rootId = root?.id?:id
    val childLevel = level + 1
    return OrganizationEntity.find {
        OrganizationsTable.rootId eq rootId and (OrganizationsTable.level eq childLevel) and (OrganizationsTable.left greater left) and (OrganizationsTable.right less right)
    }
}



fun OrganizationEntity.users():List<UserEntity> = when{
    users.empty() -> listOf()
    else -> users.toList()
}

fun getOrganizationByName(name: String): OrganizationEntity = OrganizationEntity.find {
    OrganizationsTable.name eq name
}.first()

fun OrganizationEntity.addUser(user: UserEntity): OrganizationEntity {
    if(!users.contains(user)) {
        UserOrganization.insert {
            it[userId] = user.id
            it[organizationId] = this@addUser.id
        }
    }
    return this
}

fun OrganizationEntity.removeUser(user: UserEntity): OrganizationEntity {
    UserOrganization.deleteWhere {
        userId eq user.id and  (organizationId eq this@removeUser.id)
    }
    return this
}

fun OrganizationEntity.removeUsers(): OrganizationEntity {
    UserOrganization.deleteWhere {
          organizationId eq this@removeUsers.id
    }
    return this
}

