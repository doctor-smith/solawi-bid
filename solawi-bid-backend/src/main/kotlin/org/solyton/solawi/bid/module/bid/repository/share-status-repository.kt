package org.solyton.solawi.bid.module.bid.repository

import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.bid.data.internal.shareStatusTransitions
import org.solyton.solawi.bid.module.bid.exception.ShareException
import org.solyton.solawi.bid.module.bid.exception.ShareStatusException
import org.solyton.solawi.bid.module.bid.schema.ChangeReason
import org.solyton.solawi.bid.module.bid.schema.ChangedBy
import org.solyton.solawi.bid.module.bid.schema.ShareStatusEntity
import org.solyton.solawi.bid.module.bid.schema.ShareStatusTable
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionStatusHistoryEntry
import java.util.UUID

fun Transaction.initStatus(): ShareStatusEntity = ShareStatusEntity.find {
    ShareStatusTable.name eq ShareStatus.PendingActivation.toString()
}.firstOrNull()?: throw  ShareStatusException.NoInitialState

fun Transaction.statusEntity(status: ShareStatus): ShareStatusEntity =
    ShareStatusEntity.find { ShareStatusTable.name eq status.toString() }.firstOrNull()
        ?: throw ShareStatusException.NoSuchStatus(status.toString())


fun Transaction.next(
    shareSubscriptionId: UUID,
    nextState: ShareStatus,
    reason: ChangeReason,
    changedBy: ChangedBy,
    modifier: UUID?,
    comment: String?
): ShareSubscriptionEntity {
    if(reason == ChangeReason.ROLLOVER || reason == ChangeReason.INITIAL_CREATION)
        throw ShareStatusException.ForbiddenChangeReason(reason)

    val shareSubscription = ShareSubscriptionEntity.findById(shareSubscriptionId)
        ?: throw ShareException.NoSuchShareSubscription(shareSubscriptionId.toString())

    val currentStatus = ShareStatus.from(shareSubscription.status.name)
    val allowed = shareStatusTransitions[currentStatus]
    if (allowed == null) throw ShareStatusException.NoSuchStatusTransition(
        currentStatus.toString(),
        nextState.toString()
    )

    val nextStatusEntity = statusEntity(nextState)
    shareSubscription.status = nextStatusEntity
    shareSubscription.statusUpdatedAt = DateTime.now()

    // history entries !!!
    try{
        ShareSubscriptionStatusHistoryEntry.new {
            this.shareSubscription = shareSubscription
            this.fromStatus = shareSubscription.status
            this.toStatus = nextStatusEntity
            this.reason = reason
            this.changedBy = changedBy
            this.humanModifierId = modifier
            this.comment = comment
        }
    } catch(exception: Exception) {
        throw ShareStatusException.InvalidHistoryEntry(exception.message?: "No message provided")
    }

    return shareSubscription
}

/**
 * Rollover [org.solyton.solawi.bid.module.bid.schema.ShareSubscription] to the next period
 */
fun Transaction.rollover(
    shareSubscriptionId: UUID,
    toShareOfferId: UUID,
    changedBy: ChangedBy,
    modifiedBy: UUID?
): ShareSubscriptionEntity {
    val shareSubscription = ShareSubscriptionEntity.findById(shareSubscriptionId)
        ?: throw ShareException.NoSuchShareSubscription(shareSubscriptionId.toString())

    val toShareOffer = validatedShareOffer(toShareOfferId)

    val rolledOverStatus = statusEntity(ShareStatus.RolledOver)

    val rolledOverShareSubscription = ShareSubscriptionEntity.new {
        this.status = rolledOverStatus
        shareOffer = toShareOffer
        fiscalYear = toShareOffer.fiscalYear
        userProfile = shareSubscription.userProfile
        distributionPoint = shareSubscription.distributionPoint
        numberOfShares = shareSubscription.numberOfShares
        pricePerShare = shareSubscription.pricePerShare
        ahcAuthorized = shareSubscription.ahcAuthorized
        statusUpdatedAt = DateTime.now()
    }

    // history entries !!!
    try{
        ShareSubscriptionStatusHistoryEntry.new {
            this.shareSubscription = rolledOverShareSubscription
            this.fromStatus = null
            this.toStatus = rolledOverStatus
            this.reason = ChangeReason.ROLLOVER
            this.changedBy = changedBy
            this.humanModifierId = modifiedBy
            this.comment = ""
        }
    } catch(exception: Exception) {
        throw ShareStatusException.InvalidHistoryEntry(exception.message?: "No message provided")
    }

    return rolledOverShareSubscription
}
