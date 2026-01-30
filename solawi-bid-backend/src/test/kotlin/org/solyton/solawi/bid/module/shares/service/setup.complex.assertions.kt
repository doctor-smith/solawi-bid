package org.solyton.solawi.bid.module.shares.service

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionEntity
import kotlin.test.assertEquals


@Suppress("MapGetWithNotNullAssertionOperator")
fun Transaction.actAndAssertInComplexCase(override: Boolean, testCase: TestCase) {
    require(testCase is TestCase.Complex)
    val providerId = providerByCreator(testCase.providerIds["provider_1"]!!).id.value
    val fiscalYearId = fiscalYearByCreator(UUID_1 /* <- providerId */).id.value
    val sharesToImport = testCase.sharesToImport.map { shareToImport ->
        transform(shareToImport)
    }
    val importer = UUID_1

    val importedShareSubscriptions = importShareSubscriptions(
        override,
        providerId,
        fiscalYearId,
        sharesToImport,
        importer
    )

    // Assertions
    // 1. Updated subscriptions
    val updatedShareSubscriptions = importedShareSubscriptions.filter { subscription ->
        subscription.shareOffer.id.value in sharesToImport.map { it.shareOfferId }
    }
    // assertEquals(testCase.sharesToImport.size, updatedShareSubscriptions.size)
    updatedShareSubscriptions.forEachIndexed { index, shareSubscription ->
        val shareToImport = sharesToImport.first { it.shareOfferId == shareSubscription.shareOffer.id.value }
        assertEquals(shareToImport.shareOfferId, shareSubscription.shareOffer.id.value)
        assertEquals(shareToImport.distributionPointId, shareSubscription.distributionPoint?.id?.value)
        assertEquals(shareToImport.userProfileId, shareSubscription.userProfile.id.value)
        assertEquals(shareToImport.numberOfShares, shareSubscription.numberOfShares)
        assertEquals(shareToImport.pricePerShare, shareSubscription.pricePerShare)
        assertEquals(shareToImport.ahcAuthorized, shareSubscription.ahcAuthorized)
        assertEquals(shareToImport.status, ShareStatus.from(shareSubscription.status.name))

        // Co subscribers
        val coSubscribers = shareSubscription.coSubscribers.map { it.user.username }
        assertEquals(shareToImport.coSubscribers, coSubscribers)
    }

    // 2. Invariant subscriptions
    // Note: All other share-subscriptions have not been touched!!!
    val invariantShareSubscriptions = ShareSubscriptionEntity.all().filter { subscription ->
        subscription.shareOffer.id.value !in sharesToImport.map { it.shareOfferId }
    }
    // assertEquals(testCase.shareSubscriptionIds.size - testCase.sharesToImport.size, invariantShareSubscriptions.size)
    invariantShareSubscriptions.forEachIndexed { index, shareSubscription ->
        // stored entities are defined by their creatorIs by design,
        // this info is stored in the testcase
        val creatorId = shareSubscription.createdBy

        val shareSubscriptionDefinition = testCase.shareSubscriptionIds.entries.first { it.value.first == creatorId }
        val keyOfShare = shareSubscriptionDefinition.key

        val (shareCreator, offer, depot) = shareSubscriptionDefinition.value
        assertEquals(shareCreator, creatorId)

        val shareOfferDefinition = testCase.shareOfferIds.entries.first { it.key == offer }
        assertEquals(shareOfferDefinition.value.first, shareSubscription.shareOffer.createdBy)

        val depotDefinition = testCase.distributionPointIds.entries.first { it.key == depot }
        assertEquals(depotDefinition.value.first, shareSubscription.distributionPoint?.createdBy)

        val shareTypeDefinition = testCase.shareTypeIds.entries.first { it.key == shareOfferDefinition.value.second }
        assertEquals(shareTypeDefinition.value.first, shareSubscription.shareOffer.shareType.createdBy)


        assertEquals(testCase.shareStatuses[keyOfShare], ShareStatus.from(shareSubscription.status.name))

        assertEquals(DEFAULT_NUMBER_OF_SHARES, shareSubscription.numberOfShares)
        assertEquals(DEFAULT_PRICE_PER_SHARE, shareSubscription.pricePerShare)
        assertEquals(DEFAULT_AHC_AUTHORIZED, shareSubscription.ahcAuthorized)

    }
}
