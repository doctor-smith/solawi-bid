package org.solyton.solawi.bid.application.service.organization

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.csv.parseCsvWithGroupedHeaders
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.module.shares.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shared.parser.csv.toColumnType
import org.solyton.solawi.bid.module.shares.action.importShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ImportShareSubscription
import org.solyton.solawi.bid.module.shares.data.mappings.ShareManagementMappings
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.service.computeShareSubscriptionDataForImport
import org.solyton.solawi.bid.module.user.action.user.importMembersToOrganization
import org.solyton.solawi.bid.module.user.action.user.importUserProfiles
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.userActions


fun Storage<Application>.importMembersFromCsv(
    organizationId: String,
    csv: String, delimiter: Char = ';',
    shareManagementMappings: ShareManagementMappings? = null
) {
    val typedMemberMaps: List<Map<String, Map<String, String>>> = parseCsvWithGroupedHeaders(csv, delimiter.toString())
    val membersToImport = typedMemberMaps.map { it["user_profiles"]!!["username"]!! }
    val importMembers = ImportMembers(organizationId, membersToImport)
    val userProfilesToImport = typedMemberMaps.map { type ->
        with(type["user_profiles"]!!) {
            UserProfileToImport(
                username = this["username"]!!,
                firstName = this["firstname"]!!,
                lastName = this["lastname"]!!,
                title = this["title"]!!,
                phoneNumber = this["phone_number"],
                address = CreateAddress(
                    recipientName = this["recipient_name"]!!,
                    organizationName = this["organization_name"],
                    addressLine1 = this["address_line_1"]!!,
                    addressLine2 = this["address_line_2"]!!,
                    city = this["city"]!!,
                    stateOrProvince = this["state_or_province"]!!,
                    postalCode = this["postal_code"]!!,
                    countryCode = this["country_code"]!!
                )
            )
        }
    }
    val importUserProfiles = ImportUserProfiles(userProfilesToImport)



    if (shareManagementMappings != null) {
        val shareSubscriptionsToImport = computeShareSubscriptionDataForImport(
            typedMemberMaps,
            shareManagementMappings
        )

        val userActionStorage = this * userIso * userActions
        CoroutineScope(Job()).launch {
            userActionStorage.dispatch(importMembersToOrganization(importMembers))
        }

        CoroutineScope(Job()).launch {
            userActionStorage.dispatch(importUserProfiles(importUserProfiles))
        }

        val shareManagementActionStorage = this * shareManagementIso * shareManagementActions
        CoroutineScope(Job()).launch {
            shareManagementActionStorage.dispatch(importShareSubscriptions(
                override = shareManagementMappings.override,
                providerId = organizationId,
                fiscalYearId = shareManagementMappings.fiscalYearId,
                shareSubscriptionsToImport = shareSubscriptionsToImport
            ))
        }
    }
}
