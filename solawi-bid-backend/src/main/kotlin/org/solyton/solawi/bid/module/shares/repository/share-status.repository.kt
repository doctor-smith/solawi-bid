package org.solyton.solawi.bid.module.shares.repository

import eq
import org.evoleq.uuid.toUuid
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.exception.ShareException
import org.solyton.solawi.bid.module.shares.exception.ShareStatusException
import org.solyton.solawi.bid.module.shares.processes.ShareManagementProcesses
import org.solyton.solawi.bid.module.shares.schema.*
import org.solyton.solawi.bid.module.system.repository.validatedSystemProcess
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.UserProfileId
import java.util.*

/**
 * Initializes and retrieves the initial status entity for a transaction.
 * This method queries the `ShareStatusTable` for the initial status matching
 * the status `PendingActivation`. If no matching status is found, an exception
 * is thrown indicating the absence of an initial state.
 *
 * @return The `ShareStatusEntity` representing the initial status for the transaction.
 * @throws ShareStatusException.NoInitialState if no initial status is found.
 */
fun Transaction.initStatus(): ShareStatusEntity = ShareStatusEntity.find {
    ShareStatusTable.name eq ShareStatus.PendingActivation
}.firstOrNull()?: throw  ShareStatusException.NoInitialState

/**
 * Retrieves the corresponding `ShareStatusEntity` for the given `ShareStatus`, or throws an exception if no matching status is found.
 *
 * @param status The `ShareStatus` object for which the corresponding entity is to be retrieved.
 * @return The `ShareStatusEntity` matching the given `ShareStatus`.
 * @throws ShareStatusException.NoSuchStatus if no entity is found for the provided `ShareStatus`.
 */
fun Transaction.statusEntity(status: ShareStatus): ShareStatusEntity = ShareStatusEntity.find {
    ShareStatusTable.name eq status
}.firstOrNull() ?: throw ShareStatusException.NoSuchStatus(status.toString())

/**
 * Transitions a share subscription to the specified next state while validating the transition and reason.
 * Updates the share subscription status, creates a history entry, and handles potential companion rollover scenarios.
 *
 * @param shareSubscriptionId Identifier of the share subscription to be transitioned.
 * @param nextState The next state to which the share subscription should be transitioned.
 * @param reason The reason for the state transition.
 * @param changedBy The entity that triggered the change (USER, PROVIDER, SYSTEM).
 * @param modifier Optional UUID of an entity modifying the state.
 * @param comment Optional comment providing additional context for the state change.
 * @return The updated share subscription entity representing the new state.
 * @throws ShareStatusException.ForbiddenChangeReason if the reason for the transition is forbidden.
 * @throws ShareException.NoSuchShareSubscription if the share subscription with the given ID does not exist.
 * @throws ShareStatusException.InvalidHistoryEntry if the history entry creation fails.
 */
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

fun Transaction.rollover(
    shareSubscriptionIds: List<UUID>,
    toShareOfferId: UUID,
    changedBy: ChangedBy,
    modifiedBy: UUID?
): List<ShareSubscriptionEntity> {
    return shareSubscriptionIds.flatMap { shareSubscriptionId ->
        rollover(
            shareSubscriptionId,
            toShareOfferId,
            changedBy,
            modifiedBy
        )
    }
}

/**
 * Rollover [org.solyton.solawi.bid.module.shares.schema.ShareSubscription] to the next period
 */
fun Transaction.rollover(
    shareSubscriptionId: UUID,
    toShareOfferId: UUID,
    changedBy: ChangedBy,
    modifiedBy: UUID?
): List<ShareSubscriptionEntity> {
    val shareSubscription = ShareSubscriptionEntity.findById(shareSubscriptionId)
        ?: throw ShareException.NoSuchShareSubscription(shareSubscriptionId.toString())

    validateTransition(
        ShareStatus.from(shareSubscription.status.name),
        ShareStatus.RollingOver,
        ChangeReason.NEW_PERIOD,
        changedBy
    )

    val rollingOverStatus = statusEntity(ShareStatus.RollingOver)

    val currentSubscription = next(
        shareSubscription.id.value,
        ShareStatus.RollingOver,
        ChangeReason.NEW_PERIOD,
        changedBy,
        modifiedBy,
        "state moved automatically during rollover"
    )


    val toShareOffer = validatedShareOffer(toShareOfferId)

    val changesDoneBy = modifiedBy?: validatedSystemProcess(ShareManagementProcesses.SHARE_MANAGEMENT).id.value
    val rolledOverShareSubscription = ShareSubscriptionEntity.new {
        createdBy = changesDoneBy
        this.status = statusEntity(ShareStatus.PendingActivation)
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

    return listOf(currentSubscription, rolledOverShareSubscription)
}


/**
 * Transfers the ownership of a share subscription from the current owner to a new owner.
 * The method performs necessary validations and updates the status history of the subscription.
 *
 * @param modifier The ID of the user modifying the transfer.
 * @param shareSubscriptionId The ID of the share subscription to be transferred.
 * @param newOwnerId The ID of the new owner of the share subscription.
 * @param changedBy The entity responsible for the change. Defaults to `ChangedBy.PROVIDER`.
 * @return The updated `ShareSubscription` instance after the ownership transfer.
 * @throws ShareStatusException.MissingTransitionPermission If the transition is not allowed for the specified modifier or entity.
 */
fun Transaction.transfer(
    modifier: UserId,
    shareSubscriptionId: ShareSubscriptionId,
    newOwnerId: UserProfileId,
    changedBy: ChangedBy = ChangedBy.PROVIDER,
): ShareSubscription  {
    if(changedBy != ChangedBy.PROVIDER)
        throw ShareStatusException.MissingTransitionPermission(
            "---", "TRANSFER", "$changedBy", "${ChangeReason.TRANSFER_OWNERSHIP}"
        )

    // Get current subscription
    val subscription = validatedShareSubscription(shareSubscriptionId.value.toUuid())

    validateTransition(
        ShareStatus.from(subscription.status.name),
        ShareStatus.Transfer,
        ChangeReason.TRANSFER_OWNERSHIP,
        ChangedBy.PROVIDER,
    )

    // collect and setup other data
    val modifierId = modifier.value.toUuid()
    val newOwnerProfile = validatedUserProfile(newOwnerId.value.toUuid())

    val currentStatus = subscription.status
    val transferStatus = statusEntity(ShareStatus.Transfer)

    // Start Transfer
    subscription.status = transferStatus
    subscription.modifiedBy = modifierId
    subscription.modifiedAt = DateTime.now()

    ShareSubscriptionStatusHistoryEntry.new {
        this.shareSubscription = subscription
        this.fromStatus = currentStatus
        this.toStatus = transferStatus
        this.reason = ChangeReason.TRANSFER_OWNERSHIP
        this.changedBy = changedBy
        this.humanModifierId = modifierId
        this.comment = "Transfer started"
    }
    flushCache()

    // Complete Transfer
    subscription.userProfile = newOwnerProfile
    subscription.status = currentStatus
    subscription.modifiedBy = modifierId
    subscription.modifiedAt = DateTime.now()

    ShareSubscriptionStatusHistoryEntry.new {
        this.shareSubscription = subscription
        this.fromStatus = transferStatus
        this.toStatus = currentStatus
        this.reason = ChangeReason.TRANSFER_OWNERSHIP
        this.changedBy = changedBy
        this.humanModifierId = modifierId
        this.comment = "Transfer completed"
    }

    return subscription
}

/**
 * Validates a status transition in a share's lifecycle by checking permissions, reasons,
 * and modifiers for the transition between the given statuses.
 *
 * @param fromStatus The current status of the share.
 * @param toStatus The target status for the transition.
 * @param reason The reason for the status change.
 * @param modifier The entity (user, provider, or system) initiating the change.
 * @throws ShareStatusException.NoSuchStatusTransition If no valid transition exists between the statuses.
 * @throws ShareStatusException.TransitionNotAllowedForModifier If the transition is not allowed for the given modifier.
 * @throws ShareStatusException.MissingTransitionPermission If the transition is not permitted for the provided reason.
 */
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
