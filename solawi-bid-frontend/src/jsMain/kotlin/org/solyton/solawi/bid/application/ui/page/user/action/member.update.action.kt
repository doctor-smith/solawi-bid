package org.solyton.solawi.bid.application.ui.page.user.action

import org.evoleq.math.Reader
import org.evoleq.math.emit
import org.evoleq.optics.storage.ActionEnvelope
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.times
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.module.banking.action.CREATE_BANK_ACCOUNT
import org.solyton.solawi.bid.module.banking.action.UPDATE_BANK_ACCOUNT
import org.solyton.solawi.bid.module.banking.action.createBankAccount
import org.solyton.solawi.bid.module.banking.action.updateBankAccount
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.shares.action.CREATE_SHARE_SUBSCRIPTION
import org.solyton.solawi.bid.module.shares.action.UPDATE_SHARE_SUBSCRIPTION
import org.solyton.solawi.bid.module.shares.action.createShareSubscription
import org.solyton.solawi.bid.module.shares.action.updateShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.user.action.user.CREATE_USER_PROFILE
import org.solyton.solawi.bid.module.user.action.user.UPDATE_USER_PROFILE
import org.solyton.solawi.bid.module.user.action.user.createUserProfile
import org.solyton.solawi.bid.module.user.action.user.updateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateUserProfile
import org.solyton.solawi.bid.module.user.data.member.Member
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.values.*

data class Change<T>(
    val old: T?,
    val new: T?
)

@Suppress("CognitiveComplexMethod", "UnusedParameter")
fun Storage<Application>.memberUpdateAction(
    member: Reader<Application, Member>,
    usernameChange: Change<Username>,
    userProfileChange: Change<UserProfile>,
    bankAccountChange: Change<BankAccount>,
    shareSubscriptionsChange: Change<ShareSubscriptions>
    ): Array<ActionEnvelope<Application, *, *>> {

    val userId = (this * member).emit().memberId

    // Update UserProfile (todo:dev endpoints and actions for user profiles)
    val (userProfile, userProfileState) = userProfileChange
    val userProfileAction = if(userProfileState != null) {
        requireNotNull(userProfileState) {
            "User Profile State is null!"
        }
        if(userProfile == null) {
            ActionEnvelope(
                action = userIso * createUserProfile(
                    CreateUserProfile(
                        userId = UserId(userId),
                        firstname = Firstname(userProfileState.firstname),
                        lastname = Lastname(userProfileState.lastname),
                        title = when(val title = userProfileState.title){
                            null -> null
                            else -> Title(title)
                        },
                        phoneNumber = when(val number = userProfileState.phoneNumber) {
                            null -> null
                            else -> PhoneNumber(number)
                        },
                        address = when(val address = userProfileState.addresses.firstOrNull()) {

                            null -> @Suppress("UseCheckOrError") throw IllegalStateException("User Profile State has no address!")
                            else -> CreateAddress(
                                address.recipientName,
                                address.organizationName,
                                address.addressLine1,
                                address.addressLine2,
                                address.city,
                                address.stateOrProvince,
                                address.postalCode,
                                address.countryCode
                            )
                        }
                    )
                ),
                id = CREATE_USER_PROFILE,
                clearOnFinish = true
            )
        } else {
            ActionEnvelope(
                action = userIso * updateUserProfile(
                    UpdateUserProfile(
                        userId = UserId(userId),
                        userProfileId = UserProfileId(userProfile.userProfileId),
                        firstname = Firstname(userProfileState.firstname),
                        lastname = Lastname(userProfileState.lastname),
                        title = when(val title = userProfileState.title){
                            null -> null
                            else -> Title(title)
                        },
                        phoneNumber = when(val number = userProfileState.phoneNumber) {
                            null -> null
                            else -> PhoneNumber(number)
                        },
                        addresses = userProfileState.addresses.map {address ->
                            UpdateAddress(
                                address.addressId,
                                address.recipientName,
                                address.organizationName,
                                address.addressLine1,
                                address.addressLine2,
                                address.city,
                                address.stateOrProvince,
                                address.postalCode,
                                address.countryCode
                            )
                        }
                    )
                ),
                id = UPDATE_USER_PROFILE,
                clearOnFinish = true
            )
        }
    } else {
        null
    }

    // Update BankAccounts
    val (bankAccount, bankAccountState) = bankAccountChange
    val bankAccountAction = if(bankAccountState != null) {
        requireNotNull(bankAccountState){
            "Bank Account State is null!"
        }
        if(bankAccount == null) {
            ActionEnvelope(
                action = bankingApplicationIso * createBankAccount(
                    UserId(userId),
                    bankAccountState.iban,
                    bankAccountState.bic
                ),
                id = CREATE_BANK_ACCOUNT,
                run = true,
                clearOnFinish = true
            )
        } else {
            ActionEnvelope(
                action = bankingApplicationIso * updateBankAccount(
                    bankAccountId = bankAccount.bankAccountId,
                    userId = UserId(userId),
                    iban = bankAccountState.iban,
                    bic = bankAccountState.bic
                ),
                id = UPDATE_BANK_ACCOUNT,
                clearOnFinish = true
            )
        }
    } else {
        null
    }

    // Update ShareSubscriptions
    val (shareSubscriptions, shareSubscriptionsState) = shareSubscriptionsChange
    val shareSubscriptionsActions = if(shareSubscriptionsState != null) {
        requireNotNull(shareSubscriptionsState) {
            "Share Subscriptions State is null!"
        }
        val shareSubscriptionIds = shareSubscriptions?.all.orEmpty().map {it.shareSubscriptionId}
        val (update, create) = shareSubscriptionsState.all.partition {
            it.shareSubscriptionId in shareSubscriptionIds
        }
        listOf(
            update.filter{ updateCandidate ->
                val original = shareSubscriptions?.all.orEmpty().find{
                        org -> org.shareSubscriptionId == updateCandidate.shareSubscriptionId
                }
                original != updateCandidate
            }.mapIndexed { index , shareSubscription ->
                ActionEnvelope(
                    action = shareManagementIso * updateShareSubscription(
                        shareSubscription.shareSubscriptionId,
                        shareSubscription.providerId,
                        shareSubscription.shareOfferId,
                        shareSubscription.userProfileId,
                        shareSubscription.distributionPointId,
                        shareSubscription.fiscalYearId,
                        shareSubscription.numberOfShares,
                        shareSubscription.pricePerShare,
                        shareSubscription.ahcAuthorized,
                        shareSubscription.coSubscribers,
                    ),
                    id = UPDATE_SHARE_SUBSCRIPTION+"_$index",
                    clearOnFinish = true
                )
            },
            create.mapIndexed { index, shareSubscription ->
                ActionEnvelope(
                    action = shareManagementIso * createShareSubscription(
                        shareSubscription.providerId,
                        shareSubscription.shareOfferId,
                        shareSubscription.userProfileId,
                        shareSubscription.distributionPointId,
                        shareSubscription.fiscalYearId,
                        shareSubscription.numberOfShares,
                        shareSubscription.pricePerShare,
                        shareSubscription.ahcAuthorized,
                        shareSubscription.coSubscribers,
                    ),
                    id = CREATE_SHARE_SUBSCRIPTION+"_$index",
                    clearOnFinish = true
                )
            }
        ).flatten().toTypedArray()
    } else {emptyArray()}

    @Suppress("UNCHECKED_CAST")
    return listOfNotNull(
        userProfileAction,
        bankAccountAction,
        *shareSubscriptionsActions
    ).toTypedArray<ActionEnvelope<Application, *, *>>()
}
