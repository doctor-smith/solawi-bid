package org.solyton.solawi.bid.module.shares.action.api

import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.solyton.solawi.bid.module.shares.data.api.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatuses
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.toDomainType
import org.solyton.solawi.bid.module.shares.data.toInternalType
import org.solyton.solawi.bid.module.shares.exception.ShareException
import org.solyton.solawi.bid.module.shares.repository.next
import org.solyton.solawil.bid.module.shares.data.toUUID
import java.util.*

@MathDsl
@Suppress("FunctionName")
fun UpdateShareStatus() = KlAction<Result<Contextual<UpdateShareStatus>>, Result<ShareSubscriptions>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {
        // val userId = contextual.userId
        val data = contextual.data
        val subscription = next(
            shareSubscriptionId = data.shareSubscriptionId.toUUID(),
            nextState = data.nextState.toInternalType(),
            reason = data.reason.toDomainType(),
            changedBy = data.changedBy.toDomainType(),
            modifier = data.modifier?.let { UUID.fromString(it.value) },
            comment = data.comment
        )
        // Add potential companions
        // 1. pending rollover companion
        val rolloverCompanion = subscription.pendingRolloverCompanion
        val updates = listOfNotNull(
            subscription.toApiType(),
            rolloverCompanion?.toApiType()
        )

        ShareSubscriptions(updates)
    } }  x database
} }

/**
 * Updates the statuses of multiple share subscriptions based on the provided data payload.
 *
 * This method performs the following operations:
 * - Processes each share subscription update, transitioning its state and recording any relevant metadata (e.g., reason, changedBy, modifier, and comment).
 * - Includes companion share subscriptions, such as pending rollover companions, if applicable.
 *
 * The result of the operation is a `ShareSubscriptions` object containing all successfully updated share subscriptions.
 *
 * Functional concepts involved:
 * - Uses a monadic `bindSuspend` to handle chaining transformations and validations of `Result`.
 * - Executes a database transaction (`resultTransaction`) to ensure atomicity and consistency during the update process.
 *
 * @return A `KlAction` that processes the input contextual payload and returns the appropriately updated share subscriptions wrapped in a `Result`.
 * @throws ShareException.ProviderMismatch If the provider IDs of the provided share subscription updates do not match.
 */
@MathDsl
@Suppress("FunctionName")
fun UpdateShareStatuses() = KlAction<Result<Contextual<UpdateShareStatuses>>, Result<ShareSubscriptions>> { result ->
    DbAction { database -> result bindSuspend { contextual -> resultTransaction(database) {

        val (providerId, all) = contextual.data

        // nothing is to do when there are no candidates provided for update
        if(all.isEmpty()) ShareSubscriptions(emptyList())

        // Perform update
        val allUpdated = all.flatMap { data ->
            val subscription = next(
                shareSubscriptionId = data.shareSubscriptionId.toUUID(),
                nextState = data.nextState.toInternalType(),
                reason = data.reason.toDomainType(),
                changedBy = data.changedBy.toDomainType(),
                modifier = data.modifier?.let { UUID.fromString(it.value) },
                comment = data.comment
            )
            // Add potential companions
            // 1. pending rollover companion
            val rolloverCompanion = subscription.pendingRolloverCompanion
            listOfNotNull(
                subscription.toApiType(),
                rolloverCompanion?.toApiType()
            )
        }

        ShareSubscriptions(allUpdated)
    } }  x database
} }

