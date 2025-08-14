package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

typealias UserProfilesTable = UserProfiles
typealias UserProfileEntity = UserProfile


object UserProfiles : AuditableUUIDTable("user_profiles") {
    val userId = reference("user_id", Users)

    // val addressId = reference("address_id", Addresses).nullable()

    val phoneNumber = varchar("phone_number", 15).nullable()

    // val bankAccountId = reference("bank_account_id", BankAccounts).nullable()// nullable default
}

class UserProfile(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<UserProfile>(UserProfiles)

    var user by User referencedOn UserProfiles.userId

   // var address by Address optionalReferencedOn  UserProfiles.addressId

    var phoneNumber by UserProfiles.phoneNumber

    // var bankAccount by BankAccount optionalReferencedOn UserProfiles.bankAccountId

    // val shares: SizedIterable<Share> by Share.Companion referrersOn Shares.userProfileId


    override var createdAt: DateTime by UserProfiles.createdAt
    override var createdBy: UUID by UserProfiles.createdBy
    override var modifiedAt: DateTime? by UserProfiles.modifiedAt
    override var modifiedBy: UUID? by UserProfiles.modifiedBy
}
