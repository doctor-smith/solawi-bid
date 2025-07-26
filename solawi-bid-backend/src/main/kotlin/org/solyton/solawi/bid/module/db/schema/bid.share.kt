package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

typealias ShareEntity = Share
typealias SharesTable = Shares

object Shares : UUIDTable("shares") {
    val typeId = reference("type_id", ShareTypes)
    val userProfileId = reference("user_profile_id", UserProfiles)
    val distributionPointId = optReference("distribution_point_id", DistributionPoints)
    val numberOfShares = integer("number_of_shares").default(1)
    val pricePerShare = double("price_per_share").nullable()
    val ahcAuthorized = bool("ahc_authorized").nullable()
    val fiscalYearId = reference("fiscal_year_id", FiscalYears)
}

class Share(id: EntityID<UUID>): UUIDEntity(id) {
    companion object : UUIDEntityClass<Share>(Shares)

    var type by ShareType referencedOn Shares.typeId
    var userProfile by UserProfile referencedOn Shares.userProfileId
    var distributionPoint by DistributionPoint optionalReferencedOn Shares.distributionPointId

    var numberOfShares by Shares.numberOfShares
    var pricePerShare by Shares.pricePerShare
    var ahcAuthorized by Shares.ahcAuthorized

    var fiscalYear by FiscalYear referencedOn Shares.fiscalYearId
}
