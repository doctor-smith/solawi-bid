package org.solyton.solawi.bid.application.ui.page.user.action


import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.times
import org.evoleq.uuid.NIL_UUID
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.module.banking.action.IMPORT_BANK_ACCOUNTS
import org.solyton.solawi.bid.module.banking.action.importBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccount
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.process.service.process.next
import org.solyton.solawi.bid.module.process.service.process.sequence
import org.solyton.solawi.bid.module.shares.action.IMPORT_SHARE_SUBSCRIPTIONS
import org.solyton.solawi.bid.module.shares.action.importShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.ImportShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.user.action.user.IMPORT_MEMBERS_TO_ORGANIZATION
import org.solyton.solawi.bid.module.user.action.user.IMPORT_USER_PROFILES
import org.solyton.solawi.bid.module.user.action.user.importMembersToOrganization
import org.solyton.solawi.bid.module.user.action.user.importUserProfiles
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.user.data.api.organization.ImportMembers
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.ImportUserProfiles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.values.AccessorId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.Username

data class Requirements(
    val profileRequired: Boolean = true,
    val addressRequired: Boolean = true,
    val bankAccountRequired: Boolean = true,
    val shareSubscriptionsRequired: Boolean = true
)

fun memberCreateAction(
    providerId: ProviderId,
    username: Username,
    userProfileChange: Change<UserProfile>,
    bankAccountChange: Change<BankAccount>,
    shareSubscriptionsChange: Change<ShareSubscriptions>,
    requirements: Requirements = Requirements(
        profileRequired = true,
        addressRequired = true,
        bankAccountRequired = false,
        shareSubscriptionsRequired = false
    )
): ActionEnvelope<Application, *, *> {

    val importMembers = ImportMembers(providerId.value, listOf(username.value))

    @Suppress("UseCheckOrError")
    val userProfile = userProfileChange.new?: when {
        requirements.profileRequired -> throw IllegalStateException("User Profile is required!")
        else -> null
    }

    val address: CreateAddress = userProfile?.addresses?.firstOrNull() ?.let { add: Address -> CreateAddress(
        recipientName = add.recipientName,
        organizationName = add.organizationName,
        addressLine1 = add.addressLine1,
        addressLine2 = add.addressLine2,
        city = add.city,
        stateOrProvince = add.stateOrProvince,
        postalCode = add.postalCode,
        countryCode = add.countryCode
    ) }?: when {
        requirements.addressRequired -> @Suppress("UseCheckOrError") throw IllegalStateException("User Profile has no address!")
        else -> CreateAddress.empty
    }

    val importUserProfiles = ImportUserProfiles(listOf(
        UserProfileToImport(
            username = username.value,
            firstName = userProfile!!.firstname,
            lastName = userProfile.lastname,
            title = userProfile.title,
            phoneNumber = userProfile.phoneNumber,
            phoneNumber1 = userProfile.phoneNumber1,
            address = address,
        )
    ))
    val shareSubscriptionsToImport: List<ImportShareSubscription>? = shareSubscriptionsChange.new?.all?.map {
        ImportShareSubscription(
            shareOfferId = it.shareOfferId,
            username = username.value,
            distributionPointId = it.distributionPointId,
            fiscalYearId = it.fiscalYearId,
            numberOfShares = it.numberOfShares,
            pricePerShare = it.pricePerShare,
            ahcAuthorized = it.ahcAuthorized,
            status = it.status.toApiType(),
            coSubscribers = it.coSubscribers,

        )
    }
    val bankAccountsToImport: List<ImportBankAccount>? = bankAccountChange.new?.let { listOf(
        ImportBankAccount(
            username = username,
            bankAccountHolder = it.bankAccountHolder,
            bic = it.bic,
            iban = it.iban,
            isActive = it.isActive,
            accountType = it.bankAccountType.toApiType(),
        )
    ) }

    val action = sequence(
        ActionEnvelope(
            action = userIso * importMembersToOrganization(importMembers),
            id = IMPORT_MEMBERS_TO_ORGANIZATION,
            clearOnFinish = true
        )
        ,ActionEnvelope(
            userIso * importUserProfiles(importUserProfiles),
            IMPORT_USER_PROFILES,
            clearOnFinish = true
        ).next(
            ActionEnvelope(
                run = shareSubscriptionsToImport != null,
                action = shareManagementIso * importShareSubscriptions(
                    override = true,
                    providerId = providerId.value,
                    fiscalYearId = shareSubscriptionsToImport?.firstOrNull()?.fiscalYearId?: NIL_UUID,
                    shareSubscriptionsToImport = shareSubscriptionsToImport.orEmpty()
                ),
                id = IMPORT_SHARE_SUBSCRIPTIONS,
                clearOnFinish = true
            ),
            ActionEnvelope(
                run = bankAccountsToImport != null,
                action = bankingApplicationIso * importBankAccounts(
                    override = true,
                    accessorId = AccessorId(providerId.value),
                    bankAccountsToImport = bankAccountsToImport.orEmpty()
                ),
                id = IMPORT_BANK_ACCOUNTS,
                clearOnFinish = true
            )
        ),
    )

    return action
}
