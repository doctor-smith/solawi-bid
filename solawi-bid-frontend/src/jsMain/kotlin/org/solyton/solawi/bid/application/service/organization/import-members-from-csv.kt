package org.solyton.solawi.bid.application.service.organization

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.csv.parseCsv
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.page.user.action.importMembersToOrganization
import org.solyton.solawi.bid.application.ui.page.user.action.importUserProfiles
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.userActions

fun Storage<Application>.importMembersFromCsv(organizationId: String, csv: String, delimiter: Char = ',') {
    val memberMaps: List<Map<String, String>> = parseCsv(csv, delimiter.toString())
    val membersToImport = memberMaps.map { it["username"]!! }
    val importMembers = ImportMembers(organizationId, membersToImport)

    val userProfilesToImport = memberMaps.map {
        entry -> UserProfileToImport(
            username = entry["username"]!!,
            firstName = entry["firstname"]!!,
            lastName = entry["lastname"]!!,
            title = entry["title"]!!,
            phoneNumber = entry["phone_number"],
            address = CreateAddress(
                recipientName = entry["recipient_name"]!!,
                organizationName = entry["organization_name"],
                addressLine1 =  entry["address_line_1"]!!,
                addressLine2 = entry["address_line_2"]!!,
                city = entry["city"]!!,
                stateOrProvince = entry["state_or_province"]!!,
                postalCode = entry["postal_code"]!!,
                countryCode = entry["country_code"]!!
            )
        )
    }
    val importUserProfiles = ImportUserProfiles(userProfilesToImport)


    val userActionStorage = this * userIso * userActions
    CoroutineScope(Job()).launch {
        userActionStorage.dispatch(importMembersToOrganization(importMembers))
    }

    CoroutineScope(Job()).launch {
        userActionStorage.dispatch(importUserProfiles(importUserProfiles))
    }
}
