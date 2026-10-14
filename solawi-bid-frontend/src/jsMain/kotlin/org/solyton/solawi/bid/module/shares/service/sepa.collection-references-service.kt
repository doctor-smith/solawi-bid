package org.solyton.solawi.bid.module.shares.service

import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId


fun ShareSubscription.relatedBankAccountExists(
    collection: SepaCollection,
    userProfilesToBankAccount: Map<String?, BankAccount>
): Boolean =
    collection.sepaMandates.any {
            mandate -> mandate.debtorBankAccountId == userProfilesToBankAccount[userProfileId]?.bankAccountId
    }

fun ShareSubscription.hasAssociatedSepaMandate(
    shareSubscriptionIdToMandateMap: Map<ShareSubscriptionId, SepaMandate?>
): Boolean {
    return shareSubscriptionIdToMandateMap.containsKey(ShareSubscriptionId(shareSubscriptionId))
}

fun SepaCollection.refersTo(subscription: ShareSubscription): Boolean =
    subscription.shareOfferId in referenceIds.map { it.value }
