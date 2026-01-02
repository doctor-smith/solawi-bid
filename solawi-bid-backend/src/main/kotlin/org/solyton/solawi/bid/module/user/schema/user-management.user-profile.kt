package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.banking.schema.BankAccount
import org.solyton.solawi.bid.module.banking.schema.BankAccounts
import java.util.UUID

typealias UserProfilesTable = UserProfiles
typealias UserProfileEntity = UserProfile


object UserProfiles : AuditableUUIDTable("user_profiles") {
    val userId = reference("user_id", Users)
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    // professor, doctor, ...
    val title = varchar("title", 50).nullable()

    val phoneNumber = varchar("phone_number", 15).nullable()
}

class UserProfile(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<UserProfile>(UserProfiles)

    var user by User referencedOn UserProfiles.userId

    var firstName by UserProfiles.firstName
    var lastName by UserProfiles.lastName
    var title by UserProfiles.title

    var phoneNumber by UserProfiles.phoneNumber
    val addresses by Address referrersOn Addresses.userProfile

    // val shares by Share referrersOn Shares.userProfileId


    override var createdAt: DateTime by UserProfiles.createdAt
    override var createdBy: UUID by UserProfiles.createdBy
    override var modifiedAt: DateTime? by UserProfiles.modifiedAt
    override var modifiedBy: UUID? by UserProfiles.modifiedBy
}
