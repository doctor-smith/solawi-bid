package org.solyton.solawi.bid.module.shares.data.api

import kotlinx.serialization.Serializable

typealias ApiShareStatus = ShareStatus

@Serializable
sealed class ShareStatus(protected open val value: String) {
    // External
    @Serializable
    data object External : ShareStatus("EXTERNAL")

    // Active
    @Serializable
    data object Subscribed : ShareStatus("SUBSCRIBED")

    // Temporary states
    data object Paused : ShareStatus("PAUSED")
    @Serializable
    data object Suspended : ShareStatus("SUSPENDED")

    // flow
    @Serializable
    data object PendingActivation : ShareStatus("PENDING_ACTIVATION")
    @Serializable
    data object ClearedForAuction : ShareStatus("CLEARED_FOR_AUCTION")

    @Serializable
    data object ActivationRejected : ShareStatus("ACTIVATION_REJECTED")
    @Serializable
    data object AwaitingAhcAuthorization : ShareStatus("AWAITING_AHC_AUTHORIZATION")
    @Serializable
    data object PaymentFailed : ShareStatus("PAYMENT_FAILED")

    // End states
    @Serializable
    data object Cancelled : ShareStatus("CANCELLED")
    @Serializable
    data object Expired : ShareStatus("EXPIRED")
    @Serializable
    data object RolledOver : ShareStatus("ROLLED_OVER")
    @Serializable
    data object RollingOver: ShareStatus( "ROLLING_OVER")

    override fun toString(): String = value
}
