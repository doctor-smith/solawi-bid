package org.solyton.solawi.bid.module.bid.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.bid.exception.ShareException
import org.solyton.solawi.bid.module.bid.exception.ShareStatusException
import org.solyton.solawi.bid.module.bid.data.internal.ChangeReason
import org.solyton.solawi.bid.module.bid.data.internal.ChangedBy
import org.solyton.solawi.bid.module.bid.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.bid.schema.ShareStatusEntity
import org.solyton.solawi.bid.module.bid.schema.ShareStatusTable
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionEntity
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionStatusHistory
import org.solyton.solawi.bid.module.bid.schema.ShareSubscriptionStatusHistoryEntry
import org.solyton.solawi.bid.module.system.repository.validatedSystemProcess
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

    val currentStatusEntity = shareSubscription.status
    val currentStatus = ShareStatus.from(currentStatusEntity.name)

    validateTransition(
        currentStatus,
        nextState,
        reason,
        changedBy
    )

    val nextStatusEntity = statusEntity(nextState)
    shareSubscription.status = nextStatusEntity
    shareSubscription.statusUpdatedAt = DateTime.now()

    // history entries !!!
    try{
        ShareSubscriptionStatusHistoryEntry.new {
            this.shareSubscription = shareSubscription
            this.fromStatus = currentStatusEntity
            this.toStatus = nextStatusEntity
            this.reason = reason
            this.changedBy = changedBy
            this.humanModifierId = modifier
            this.comment = comment
        }
    } catch(exception: Exception) {
        throw ShareStatusException.InvalidHistoryEntry(exception.message?: "No message provided")
    }


    if(nextState != ShareStatus.Subscribed)  return shareSubscription

    // check if there is a rolling-over companion
    // Yes => move companions' state to RolledOver
    val rollingOverCompanion = ShareSubscriptionStatusHistoryEntry.find {
        (ShareSubscriptionStatusHistory.shareSubscriptionId eq shareSubscriptionId) and
        (ShareSubscriptionStatusHistory.rollingOverFromSubscriptionId neq null)
    }.firstOrNull()
    if(rollingOverCompanion != null) {
        next(
            rollingOverCompanion.id.value,
            ShareStatus.RolledOver,
            ChangeReason.ROLLOVER,
            changedBy,
            modifier,
            "state moved automatically during rollover"
        )
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

    validateTransition(
        ShareStatus.from(shareSubscription.status.name),
        ShareStatus.RollingOver,
        ChangeReason.NEW_PERIOD,
        changedBy
    )

    val rollingOverStatus = statusEntity(ShareStatus.RollingOver)

    next(
        shareSubscription.id.value,
        ShareStatus.RollingOver,
        ChangeReason.NEW_PERIOD,
        changedBy,
        modifiedBy,
        "state moved automatically during rollover"
    )


    val toShareOffer = validatedShareOffer(toShareOfferId)

    val changesDoneBy = modifiedBy?: validatedSystemProcess("").id.value
    val rolledOverShareSubscription = ShareSubscriptionEntity.new {
        createdBy = changesDoneBy
        this.status = rollingOverStatus
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
            this.rollingOverFromShareSubscription = shareSubscription
            this.shareSubscription = rolledOverShareSubscription
            this.fromStatus = null
            this.toStatus = rollingOverStatus
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


fun validateTransition(
    fromStatus: ShareStatus,
    toStatus: ShareStatus,
    reason: ChangeReason,
    modifier: ChangedBy
) {
    val sharePermissions = shareStatusTransitionsWithPermissions[fromStatus]
        ?: throw ShareStatusException.NoSuchStatusTransition("$fromStatus", "$toStatus")

    val permissions = sharePermissions.firstOrNull {
            permissions -> permissions.shareStatus == toStatus
    }?.permissions?: throw ShareStatusException.NoSuchStatusTransition("$fromStatus", "$toStatus")

    val reasons = permissions[modifier]?: throw ShareStatusException.TransitionNotAllowedForModifier(
        "$fromStatus", "$toStatus", "$modifier"
    )
    if(!reasons.contains(reason)) throw ShareStatusException.MissingTransitionPermission(
        "$fromStatus", "$toStatus", "$modifier", "$reason"
    )
}
