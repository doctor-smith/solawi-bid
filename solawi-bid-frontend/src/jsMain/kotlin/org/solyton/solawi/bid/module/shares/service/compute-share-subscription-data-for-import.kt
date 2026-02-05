package org.solyton.solawi.bid.module.shares.service

import org.solyton.solawi.bid.module.shared.parser.csv.toColumnType
import org.solyton.solawi.bid.module.shares.data.api.ImportShareSubscription
import org.solyton.solawi.bid.module.shares.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.mappings.ShareManagementMappings
import org.solyton.solawi.bid.module.shares.data.toApiType

fun computeShareSubscriptionDataForImport(
    typedMemberMaps: List<Map<String,Map<String, String>>>,
    shareManagementMappings: ShareManagementMappings
) :  List<ImportShareSubscription>{

    return typedMemberMaps.map{
        val userProfiles = it["user_profiles"]!!
        it.filterKeys { key -> key.startsWith("share_subscriptions")}.map { (key, value) ->
            // Get column type
            // share_subscriptions:type.offer
            val (name, type, keyOfShareType) = key.toColumnType()
            requireNotNull(name) { "Part name is null: $key" }
            requireNotNull(type) { "Part type is null: $key" }
            requireNotNull(keyOfShareType) {  "Part keyOfShareType is null: $key"}


            val username: String = userProfiles["username"]!!
            val shareOfferId: String = shareManagementMappings.shareOffers[keyOfShareType]!!
            val distributionPoint = shareManagementMappings.distributionPoints[value["distribution_point"]!!]!!
            val numberOfShares: Int = value["number_of_shares"]!!.toInt()
            val pricePerShare: Double? = when {
                type.lowercase() == PricingType.FLEXIBLE.toString().lowercase() -> value["price_per_share"]!!.toDouble()
                else -> null
            }
            val ahcAuthorized: Boolean = value["ahc_authorized"]!!.toBoolean()
            val status = value["status"]!!
            val coSubscribers = value["co_subscribers"]?.split(",")
                ?.map { sub -> sub.trim() }
                ?.filterNot { sub -> sub.isBlank() }
                ?: emptyList()

            ImportShareSubscription(
                shareOfferId = shareOfferId,
                username = username,
                distributionPointId = distributionPoint,
                fiscalYearId = shareManagementMappings.fiscalYearId,
                numberOfShares = numberOfShares,
                pricePerShare = pricePerShare,
                ahcAuthorized = ahcAuthorized,
                status = ShareStatus.from(status).toApiType(),
                coSubscribers = coSubscribers
            )

    }}.flatten()
}
