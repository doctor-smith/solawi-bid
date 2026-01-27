package org.solyton.solawi.bid.module.bid.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import org.solyton.solawi.bid.module.banking.schema.FiscalYear
import org.solyton.solawi.bid.module.banking.schema.FiscalYears
import org.solyton.solawi.bid.module.user.schema.UserProfile
import org.solyton.solawi.bid.module.user.schema.UserProfiles
import java.util.*

typealias ShareSubscriptionEntity = ShareSubscription
typealias ShareSubscriptionsTable = ShareSubscriptions

object ShareSubscriptions : AuditableUUIDTable("share_subscriptions") {
    val shareOfferId = reference("share_offer_id", ShareOffers)
    val userProfileId = reference("user_profile_id", UserProfiles)
    val distributionPointId = optReference("distribution_point_id", DistributionPoints)
    val numberOfShares = integer("number_of_shares").default(1)
    val pricePerShare = double("price_per_share").nullable()
    val ahcAuthorized = bool("ahc_authorized").nullable()
    val fiscalYearId = reference("fiscal_year_id", FiscalYears)

    val statusId = reference("status_id", ShareStatusTable)

    val statusUpdatedAt = datetime("status_updated_at").default(DateTime.now())
}

class ShareSubscription(id: EntityID<UUID>): UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<ShareSubscription>(ShareSubscriptions)

    var shareOffer by ShareOffer referencedOn ShareSubscriptions.shareOfferId
    var userProfile by UserProfile referencedOn ShareSubscriptions.userProfileId
    var distributionPoint by DistributionPoint optionalReferencedOn ShareSubscriptions.distributionPointId

    var numberOfShares by ShareSubscriptions.numberOfShares
    var pricePerShare by ShareSubscriptions.pricePerShare
    var ahcAuthorized by ShareSubscriptions.ahcAuthorized

    var fiscalYear by FiscalYear referencedOn ShareSubscriptions.fiscalYearId

    var status by ShareStatusEntity referencedOn ShareSubscriptions.statusId
    var statusUpdatedAt by ShareSubscriptions.statusUpdatedAt

    val coSubscribers by CoSubscriberEntity referrersOn CoSubscribersTable.shareSubscriptionId

    override var createdAt: DateTime by ShareSubscriptions.createdAt
    override var createdBy: UUID by ShareSubscriptions.createdBy
    override var modifiedAt: DateTime? by ShareSubscriptions.modifiedAt
    override var modifiedBy: UUID? by ShareSubscriptions.modifiedBy
}
