package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias UserEntity = User
typealias UsersTable = Users

object Users : UUIDTable("users") {
    val username = varchar("username", 50).index()
    val password = varchar("varchar", 500)
   // val profile = reference("profile", Profiles)
}


class User(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)

    var username by Users.username
    var password by Users.password

    // var rights by Right via RoleRightContexts
}

