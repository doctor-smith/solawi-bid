package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.SizedIterable
import org.solyton.solawi.bid.module.bid.schema.Share
import org.solyton.solawi.bid.module.bid.schema.Shares
import org.solyton.solawi.bid.module.banking.schema.BankAccount
import org.solyton.solawi.bid.module.banking.schema.BankAccounts
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

    val shares: SizedIterable<Share> by Share.Companion referrersOn Shares.userProfileId
}
