package org.solyton.solawi.bid.module.shares.service

import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription


fun ShareSubscription.relatedBankAccountExists(
    collection: SepaCollection,
    userProfilesToBankAccount: Map<String?, BankAccount>
): Boolean =
    collection.sepaMandates.any {
            mandate -> mandate.debtorBankAccountId == userProfilesToBankAccount[userProfileId]?.bankAccountId
    }

fun SepaCollection.refersTo(subscription: ShareSubscription): Boolean =
    subscription.shareOfferId in referenceIds.map { it.value }
