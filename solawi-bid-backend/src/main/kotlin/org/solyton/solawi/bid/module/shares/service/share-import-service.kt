package org.solyton.solawi.bid.module.shares.service

import org.evoleq.exposedx.NO_MESSAGE_PROVIDED
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.distribution.repository.validatedDistributionPoint
import org.solyton.solawi.bid.module.shares.exception.ShareException
import org.solyton.solawi.bid.module.shares.exception.ShareStatusException
import org.solyton.solawi.bid.module.shares.repository.validatedShareOffer
import org.solyton.solawi.bid.module.shares.repository.validatedUserProfile
import org.solyton.solawi.bid.module.shares.schema.CoSubscriberEntity
import org.solyton.solawi.bid.module.shares.schema.CoSubscribersTable
import org.solyton.solawi.bid.module.shares.schema.ShareStatusEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionStatusHistoryEntry
import org.solyton.solawi.bid.module.shares.schema.ShareSubscriptionsTable
import org.solyton.solawi.bid.module.user.schema.UserEntity
import org.solyton.solawi.bid.module.user.schema.UserStatus
import org.solyton.solawi.bid.module.user.schema.repository.readUserByUsername
import java.util.*

data class ShareToImport(
    val shareOfferId: UUID,
    val userProfileId: UUID,
    val distributionPointId: UUID?,
    val numberOfShares: Int,
    val pricePerShare: Double?,
    val ahcAuthorized: Boolean?,
    val status: ShareStatus,
    // usernames / mail addresses of co-subscribers
    val coSubscribers: List<String>
)

@Suppress("CognitiveComplexMethod", "ThrowsCount","MapGetWithNotNullAssertionOperator","UnsafeCallOnNullableType")
fun Transaction.importShareSubscriptions(
    // Each user has at most on share-subscription per
    // share-offer and fiscal year
    override: Boolean = false,
    providerId: UUID,
    fiscalYearId: UUID,
    shares: List<ShareToImport>,
    importer: UUID
): List<ShareSubscriptionEntity> {
    val shareOfferIds = shares.map { it.shareOfferId }
    val userProfileIds = shares.map { it.userProfileId }
    val existingShareSubscriptionsMap: Map<UUID, ShareSubscriptionEntity> = ShareSubscriptionEntity.find {
        ShareSubscriptionsTable.fiscalYearId eq fiscalYearId and
        (ShareSubscriptionsTable.shareOfferId inList shareOfferIds) and
        (ShareSubscriptionsTable.userProfileId inList userProfileIds)
    }.filter {
        shareSubscription -> shareSubscription.shareOffer.shareType.providerId == providerId
    }.associateBy { shareSubscription -> shareSubscription.userProfile.id.value }
    val existingSHareSubscriptionIds = existingShareSubscriptionsMap.keys

    val shareSubscriptionsToImport = shares.filterNot { it.userProfileId in existingSHareSubscriptionIds }
    val shareSubscriptionsToOverride = shares.filter { it.userProfileId in existingSHareSubscriptionIds }

    val shareStatusEntities = ShareStatusEntity.all().associateBy{
        ShareStatus.from(it.name)
    }


    // Add new shares
    val importedShareSubscriptions = shareSubscriptionsToImport.map { shareToImport ->
        // Collect data
        val userProfile = validatedUserProfile(shareToImport.userProfileId)
        val shareOffer = validatedShareOffer(shareToImport.shareOfferId)
        if(shareOffer.shareType.providerId != providerId) throw ShareException.ProviderMismatch
        if(shareOffer.fiscalYear.id.value != fiscalYearId) throw ShareException.FiscalYearMismatch(
            fiscalYearId.toString(),
            shareOffer.id.value.toString()
        )
        val distributionPoint = if(shareToImport.distributionPointId != null) {
            validatedDistributionPoint(shareToImport.distributionPointId)
        } else null

        // Create a new ShareSubscription
        val shareSubscriptionEntity = ShareSubscriptionEntity.new {
            createdBy = importer
            this.userProfile = userProfile
            this.shareOffer = shareOffer
            numberOfShares = shareToImport.numberOfShares
            pricePerShare = shareToImport.pricePerShare
            ahcAuthorized = shareToImport.ahcAuthorized
            fiscalYear = shareOffer.fiscalYear
            this.distributionPoint = distributionPoint
            this.status = shareStatusEntities[shareToImport.status]!!
        }

        // Add co-subscribers
        shareToImport.coSubscribers.forEach { coSubscriber ->
            val existingUser = readUserByUsername(coSubscriber)
            @Suppress("UnusedPrivateProperty")
            val user = when{
                existingUser != null -> existingUser
                else -> UserEntity.new {
                    createdBy = importer
                    username = coSubscriber
                    status = UserStatus.PENDING
                    password = null
                }
            }
            CoSubscriberEntity.new {
                createdBy = importer
                this.shareSubscription = shareSubscriptionEntity
                this.user = user
            }
        }

        // Add history entry
        try {
            ShareSubscriptionStatusHistoryEntry.new {
                shareSubscription = shareSubscriptionEntity
                fromStatus = shareStatusEntities[ShareStatus.External]
                toStatus = shareSubscriptionEntity.status
                reason = ChangeReason.IMPORT
                changedBy = ChangedBy.PROVIDER
                humanModifierId = importer
                comment = "Import"
            }
        } catch (exception: Exception) {
            throw ShareStatusException.InvalidHistoryEntry(exception.message?: NO_MESSAGE_PROVIDED)
        }

        shareSubscriptionEntity
    }

    // Eventually override existing shares
    val overriddenShareSubscriptions: List<ShareSubscriptionEntity> = if(override) {
        shareSubscriptionsToOverride.map { shareToOverride ->
            val shareSubscriptionEntity = existingShareSubscriptionsMap[shareToOverride.userProfileId]!!

            // Collect data
            val shareOffer = validatedShareOffer(shareToOverride.shareOfferId)
            if(shareOffer.shareType.providerId != providerId) throw ShareException.ProviderMismatch
            if(shareOffer.fiscalYear.id.value != fiscalYearId) throw ShareException.FiscalYearMismatch(
                fiscalYearId.toString(),
                shareOffer.id.value.toString()
            )
            val distributionPoint = if(shareToOverride.distributionPointId != null) {
                validatedDistributionPoint(shareToOverride.distributionPointId)
            } else null

            // Update share
            shareSubscriptionEntity.shareOffer = shareOffer
            shareSubscriptionEntity.numberOfShares = shareToOverride.numberOfShares
            shareSubscriptionEntity.pricePerShare = shareToOverride.pricePerShare
            shareSubscriptionEntity.ahcAuthorized = shareToOverride.ahcAuthorized
            shareSubscriptionEntity.distributionPoint = distributionPoint
            shareSubscriptionEntity.status = shareStatusEntities[shareToOverride.status]!!
            shareSubscriptionEntity.modifiedBy = importer
            shareSubscriptionEntity.modifiedAt = DateTime.now()

            // Update list of co-subscribers
            // 1. Delete co-subscribers
            CoSubscribersTable.deleteWhere {
                CoSubscribersTable.shareSubscriptionId eq shareSubscriptionEntity.id.value
            }
            // 2. Add co-subscribers
            shareToOverride.coSubscribers.forEach { coSubscriber ->
                val existingUser = readUserByUsername(coSubscriber)
                @Suppress("UnusedPrivateProperty")
                val user = when{
                    existingUser != null -> existingUser
                    else -> UserEntity.new {
                        createdBy = importer
                        username = coSubscriber
                        status = UserStatus.PENDING
                        password = null
                    }
                }
                CoSubscriberEntity.new {
                    createdBy = importer
                    this.shareSubscription = shareSubscriptionEntity
                    this.user = user
                }
            }

            // Add history entry
            try {
                ShareSubscriptionStatusHistoryEntry.new {
                    shareSubscription = shareSubscriptionEntity
                    fromStatus = shareStatusEntities[ShareStatus.External]
                    toStatus = shareSubscriptionEntity.status
                    reason = ChangeReason.IMPORT
                    changedBy = ChangedBy.PROVIDER
                    humanModifierId = importer
                    comment = "Import"
                }
            } catch (exception: Exception) {
                throw ShareStatusException.InvalidHistoryEntry(exception.message?: NO_MESSAGE_PROVIDED)
            }

            shareSubscriptionEntity
        }
    } else {
        emptyList()
    }

    return listOf(
        importedShareSubscriptions,
        overriddenShareSubscriptions
    ).flatten()
}
