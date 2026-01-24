package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.banking.schema.FiscalYearEntity
import org.solyton.solawi.bid.module.banking.schema.FiscalYears
import java.util.UUID

typealias ShareOfferEntity = ShareOffer
typealias ShareOffersTable = ShareStatusTable

object ShareOffers : AuditableUUIDTable("share_offers") {
    val shareTypeId = reference("share_type_id", ShareTypes)

    val fiscalYearId = reference("fiscal_year_id", FiscalYears)

    val price = double("price").nullable()

    val pricingType = enumeration<PricingType>("pricing_type")

    val ahcAuthorizationRequired = bool("ahc_auth_required").default(false)
}

enum class PricingType {
    FIXED,
    FLEXIBLE
}

class ShareOffer(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<ShareOffer>(ShareOffers)

    var shareType by ShareTypeEntity referencedOn ShareOffers.shareTypeId
    var price by ShareOffers.price
    var pricingType by ShareOffers.pricingType
    var fiscalYear by FiscalYearEntity referencedOn ShareOffers.fiscalYearId
    var ahcAuthorizationRequired by ShareOffers.ahcAuthorizationRequired

    val shareSubscriptions by ShareSubscriptionEntity referrersOn ShareSubscriptionsTable.shareOfferId


    override var createdAt: DateTime by ShareOffers.createdAt
    override var createdBy: UUID by ShareOffers.createdBy
    override var modifiedAt: DateTime? by ShareOffers.modifiedAt
    override var modifiedBy: UUID? by ShareOffers.modifiedBy
}
