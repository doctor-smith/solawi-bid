package org.solyton.solawi.bid.application.ui.page.user

import org.evoleq.compose.download.downloadCsv
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.user.data.member.Member
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import kotlin.js.Date

data class ColumnGroup(val id: String, val name: String, val columns: List<String>)

data class ColumnGroups(val all: List<ColumnGroup>)

fun downloadMembers(
    members: List<Member>,
    memberProfilesMap: Map<String, UserProfile?>,
    shareOffers: List<ShareOffer>,
    shareSubscriptionsMap: Map<String, List<ShareSubscription>>,
    distributionPointsMap: Map<String, String>
) {
    val columnGroups = ColumnGroups(listOf(
        ColumnGroup("user_profiles","user_profiles", listOf(
            "username", "firstname", "lastname", "title","phone_number",
            "recipient_name", "organization", "address_line_1", "address_line_2",
            "postal_code", "city",  "state_or_province", "country_code"
        )),
        *shareOffers.map {
            ColumnGroup(
                it.shareOfferId,
                "share_subscriptions.${it.pricingType.name}?key=${it.shareType.key}",
                listOf(
                    "number_of_shares", "price_per_share", "ahc_authorized", "status", "co_subscribers", "distribution_point", "fiscal_year"
                )
            )
        }.toTypedArray()
    ))


    val memberLines = members.map { member ->
        val userProfile = memberProfilesMap[member.memberId]
        val shareSubscriptions = shareSubscriptionsMap [userProfile?.userProfileId?:""]
        val shareSubscriptionStrings = shareOffers.map { shareOffer ->
            val share = shareSubscriptions?.firstOrNull { it.shareOfferId == shareOffer.shareOfferId }
            val distributionPoint = share?.distributionPointId?.let { distributionPointsMap[it] }
            if (share == null) {
                ";;;;;;"
            } else {
                with(share){"$numberOfShares;$pricePerShare;$ahcAuthorized;$status;${coSubscribers.joinToString(","){it}};${distributionPoint?:""};${shareOffer.fiscalYear.format()}"}
            }
        }

        "${member.username};${userProfile.toColumnValues().joinToString(";")};${shareSubscriptionStrings.joinToString(";")}"
    }
    val headerLines = columnGroups.all.map {
        val size = it.columns.size
        it.name + ";".repeat(size -1 ) to it.columns.joinToString (";") {x -> x}
    }.fold(Pair("", "")) {
            acc, pair -> Pair(acc.first +";" + pair.first, acc.second + ";" + pair.second)
    }
    val csv = """
        |${headerLines.first.drop(1)}
        |${headerLines.second.drop(1)}
        |${memberLines.joinToString("\n")}
    """.trimMargin()

    val timestamp = Date.now()
    downloadCsv(csv, "members_$timestamp.csv")
}

fun Address.toColumnValues(): List<String> = listOf(
    recipientName, organizationName?:"", addressLine1, addressLine2, postalCode, city, stateOrProvince,  countryCode
)
fun Address?.toColumnValues(): List<String> = this?.toColumnValues() ?: listOf("","","","","","","","")
fun UserProfile.toColumnValues(): List<String> = listOf(
    firstname,lastname,title?:"",phoneNumber?:"", *addresses.firstOrNull().toColumnValues().toTypedArray(),
)
fun UserProfile?.toColumnValues(): List<String> = this?.toColumnValues() ?: listOf("","","","","","","","","","","","","")
