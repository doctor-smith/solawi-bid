package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias UserEntity = User
typealias UsersTable = Users

object Users : AuditableUUIDTable("users")  {
    val username = varchar("username", 100).uniqueIndex()
    val password = varchar("password", 500).nullable()
    val status = enumerationByName<UserStatus>("status", 20)

    init {
        // CHECK constraint: enforce password <-> status invariant
        check("chk_password_vs_status") {
            // Password must be null if PENDING or INVITED
            // Password must be set for REGISTERED, ACTIVE, or DISABLED
            (password.isNull() and (
                (status eq UserStatus.INVITED) or
                (status eq UserStatus.PENDING)
            )) or
            (password.isNotNull() and (
                (status eq UserStatus.ACTIVE) or
                (status eq UserStatus.REGISTERED) or
                (status eq UserStatus.DISABLED)
            ))
        }
    }
}


class User(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<User>(Users)

    var username by Users.username
    var password by Users.password
    var status by Users.status

    override var createdAt: DateTime by Users.createdAt
    override var createdBy: UUID by Users.createdBy
    override var modifiedAt: DateTime? by Users.modifiedAt
    override var modifiedBy: UUID? by Users.modifiedBy

    var organizations by Organization via UserOrganization
}

enum class UserStatus {
    REGISTERED, // waiting for email verification
    PENDING,    // User created, invite not sent yet, login not allowed
    INVITED, // Invite sent, password can be set, login still blocked
    ACTIVE,     // Password set, login allowed
    DISABLED    // Account disabled manually or automatically, login blocked
}

val userStatusTransitions: Map<UserStatus, Set<UserStatus>>  by lazy {
    mapOf(
        // User registered, can be activated directly (self-registration) or disabled by admin
        UserStatus.REGISTERED to setOf(UserStatus.ACTIVE, UserStatus.DISABLED),

        // Pending users must be invited first
        UserStatus.PENDING to setOf(UserStatus.INVITED),

        // Invited users can stay invited, activate, or be disabled
        UserStatus.INVITED to setOf(UserStatus.INVITED, UserStatus.ACTIVE, UserStatus.DISABLED),

        // Active users can only be disabled
        UserStatus.ACTIVE to setOf(UserStatus.DISABLED),

        // Disabled users are terminal
        UserStatus.DISABLED to setOf(UserStatus.ACTIVE)
    )
}
