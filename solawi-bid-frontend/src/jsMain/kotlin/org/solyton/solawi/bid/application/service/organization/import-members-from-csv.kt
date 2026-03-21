package org.solyton.solawi.bid.application.service.organization

import kotlinx.coroutines.CoroutineScope
import org.evoleq.csv.parseCsvWithGroupedHeaders
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.module.banking.action.IMPORT_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.action.importBankAccounts
import org.solyton.solawi.bid.module.banking.data.mappings.BankingMappings
import org.solyton.solawi.bid.module.banking.service.computeBankAccountsDataForImport
import org.solyton.solawi.bid.module.process.service.process.next
import org.solyton.solawi.bid.module.process.service.process.sequenceProcesses
import org.solyton.solawi.bid.module.shares.action.IMPORT_SHARE_SUBSCRIPTIONS
import org.solyton.solawi.bid.module.shares.action.importShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.mappings.ShareManagementMappings
import org.solyton.solawi.bid.module.shares.service.computeShareSubscriptionDataForImport
import org.solyton.solawi.bid.module.user.action.user.IMPORT_MEMBERS_TO_ORGANIZATION
import org.solyton.solawi.bid.module.user.action.user.IMPORT_USER_PROFILES
import org.solyton.solawi.bid.module.user.action.user.importMembersToOrganization
import org.solyton.solawi.bid.module.user.action.user.importUserProfiles
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.profile.phoneNumber1
import org.solyton.solawi.bid.module.values.AccessorId


fun Storage<Application>.importMembersFromCsv(
    scope: CoroutineScope,
    organizationId: String,
    csv: String, delimiter: Char = ';',
    shareManagementMappings: ShareManagementMappings? = null,
    bankingMappings: BankingMappings? = null
) {
    val hasCorrectDelimiter = csv.contains(delimiter)
    require(hasCorrectDelimiter) { "Wrong delimiter $delimiter" }

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
                phoneNumber1 = this["phone_number_1"],
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


    val shareSubscriptionsToImport = when {
        shareManagementMappings != null  -> computeShareSubscriptionDataForImport(typedMemberMaps, shareManagementMappings!!)
        else -> null
    }

    val bankAccountsToImport = when {
        bankingMappings != null -> computeBankAccountsDataForImport(typedMemberMaps, bankingMappings!!)
        else -> null
    }

    sequenceProcesses(
        scope,
        ActionEnvelope(
            userIso * importMembersToOrganization(importMembers),
            IMPORT_MEMBERS_TO_ORGANIZATION,)
        ,ActionEnvelope(
            userIso * importUserProfiles(importUserProfiles),
            IMPORT_USER_PROFILES,
        ).next(
            ActionEnvelope(
                run = shareSubscriptionsToImport != null && shareSubscriptionsToImport.isNotEmpty(),
                action = shareManagementIso * importShareSubscriptions(
                    override = shareManagementMappings!!.override,
                    providerId = organizationId,
                    fiscalYearId = shareSubscriptionsToImport!!.first().fiscalYearId,
                    shareSubscriptionsToImport = shareSubscriptionsToImport
                ),
                id = IMPORT_SHARE_SUBSCRIPTIONS
            ),
            ActionEnvelope(
                run = bankingMappings != null && bankAccountsToImport != null,
                action = bankingApplicationIso * importBankAccounts(
                    override = bankingMappings!!.override,
                    accessorId = AccessorId(bankingMappings.legalEntityId.value),
                    bankAccountsToImport = bankAccountsToImport!!
                ),
                id = IMPORT_BANK_ACCOUNTS
            )
        ),
    )
}
