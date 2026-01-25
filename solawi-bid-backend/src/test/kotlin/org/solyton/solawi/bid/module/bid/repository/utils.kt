package org.solyton.solawi.bid.module.bid.repository

import org.jetbrains.exposed.sql.Transaction
import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus
import org.solyton.solawi.bid.module.bid.data.internal.ChangeReason
import org.solyton.solawi.bid.module.bid.data.internal.ChangedBy
import org.solyton.solawi.bid.module.bid.schema.ShareStatusEntity


fun Transaction.createTestShareStatus(shareStatus: ShareStatus) {
    ShareStatusEntity.new {
        name = "$shareStatus"
        description = "description"
    }
}

fun Transaction.createTestShareStatuses() {
    createTestShareStatus(ShareStatus.ActivationRejected)
    createTestShareStatus(ShareStatus.PendingActivation)
    createTestShareStatus(ShareStatus.Suspended)
    createTestShareStatus(ShareStatus.Subscribed)
    createTestShareStatus(ShareStatus.Paused)
    createTestShareStatus(ShareStatus.PaymentFailed)
    createTestShareStatus(ShareStatus.RolledOver)
    createTestShareStatus(ShareStatus.RollingOver)
    createTestShareStatus(ShareStatus.Cancelled)
    createTestShareStatus(ShareStatus.Expired)
    createTestShareStatus(ShareStatus.AwaitingAhcAuthorization)
}


val changeReasons = arrayOf(
    ChangeReason.INITIAL_CREATION,
    ChangeReason.ADMIN_ACTION,
    ChangeReason.SYSTEM_EVENT,
    ChangeReason.PAYMENT_EVENT,
    ChangeReason.AUTHORIZATION_EVENT,
    ChangeReason.USER_ACTION,
    ChangeReason.ROLLOVER,
)

val shareStatuses = arrayOf(
    ShareStatus.PendingActivation,
    ShareStatus.Subscribed,
    ShareStatus.Paused,
    ShareStatus.Expired,
    ShareStatus.PaymentFailed,
    ShareStatus.Cancelled,
    ShareStatus.ActivationRejected,
    ShareStatus.AwaitingAhcAuthorization,
    ShareStatus.RollingOver,
    ShareStatus.RolledOver,
    ShareStatus.Suspended
)

val modifiers = arrayOf(
    ChangedBy.USER,
    ChangedBy.PROVIDER,
    ChangedBy.SYSTEM
)

interface TestCaseSpecification {
    val testId: String
    val description: String
}

infix fun <S, T> Array<S>.cross(other: Array<T>): Array<Pair<S, T>> = flatMap { s ->
    other.map { t ->
        s to t
    }
}.toTypedArray()

fun <R, S, T> Triple<Array<R>, Array<S>,Array<T>>.crossed(): Array<Triple<R, S, T>> =
    ((first cross second) cross third).map {
            (pair: Pair<R, S>, t: T) -> Triple(pair.first, pair.second, t)
    }.toTypedArray()
