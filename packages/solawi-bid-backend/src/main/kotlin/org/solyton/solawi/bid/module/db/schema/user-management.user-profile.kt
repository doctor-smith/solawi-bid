package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

typealias UserProfilesTable = UserProfiles
typealias UserProfileEntity = UserProfile


object UserProfiles : UUIDTable("user_profiles") {
    val userId = reference("user_id", Users)

    val addressId = reference("address_id", Addresses).nullable()

    val phoneNumber = varchar("phone_number", 15).nullable()

    val bankAccountId = reference("bank_account_id", BankAccounts).nullable()// nullable default
}

class UserProfile(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserProfile>(UserProfiles)

    var user by User referencedOn UserProfiles.userId

    var address by Address optionalReferencedOn  UserProfiles.addressId

    var phoneNumber by UserProfiles.phoneNumber

    var bankAccount by BankAccount optionalReferencedOn UserProfiles.bankAccountId

    val shares: SizedIterable<Share> by Share referrersOn Shares.userProfileId
}