package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias UserEntity = User
typealias UsersTable = Users

object Users : AuditableUUIDTable("users")  {
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("varchar", 500)
}


class User(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<User>(Users)

    var username by Users.username
    var password by Users.password

    override var createdAt: DateTime by Users.createdAt
    override var createdBy: UUID by Users.createdBy
    override var modifiedAt: DateTime? by Users.modifiedAt
    override var modifiedBy: UUID? by Users.modifiedBy

    var organizations by Organization via UserOrganization
}

