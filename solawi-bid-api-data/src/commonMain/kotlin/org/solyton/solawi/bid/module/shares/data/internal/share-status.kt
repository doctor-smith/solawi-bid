package org.solyton.solawi.bid.module.shares.data.internal


sealed class ShareStatus(protected open val value: String) {
    // External
    data object External : ShareStatus("EXTERNAL")
    // Active
    data object Subscribed : ShareStatus("SUBSCRIBED")

    // Temporary states
    data object Paused : ShareStatus("PAUSED")
    data object Suspended : ShareStatus("SUSPENDED")

    // flow
    data object PendingActivation : ShareStatus("PENDING_ACTIVATION")
    data object ClearedForAuction : ShareStatus("CLEARED_FOR_AUCTION")
    data object ActivationRejected : ShareStatus("ACTIVATION_REJECTED")
    data object AwaitingAhcAuthorization : ShareStatus("AWAITING_AHC_AUTHORIZATION")
    data object PaymentFailed : ShareStatus("PAYMENT_FAILED")

    // End states
    data object Cancelled : ShareStatus("CANCELLED")
    data object Expired : ShareStatus("EXPIRED")
    data object RolledOver : ShareStatus("ROLLED_OVER")
    data object RollingOver: ShareStatus( "ROLLING_OVER")

    override fun toString(): String = value

    companion object {
        fun from(state: String): ShareStatus =
            when (state) {
                "$External", "EXTERNAL" -> External
                "$PendingActivation", "PENDING_ACTIVATION" -> PendingActivation
                "$ClearedForAuction", "CLEARED_FOR_AUCTION" -> ClearedForAuction
                "$ActivationRejected", "ACTIVATION_REJECTED" -> ActivationRejected
                "$AwaitingAhcAuthorization", "AWAITING_AHC_AUTHORIZATION" -> AwaitingAhcAuthorization
                "$Subscribed", "SUBSCRIBED" -> Subscribed
                "$Paused", "PAUSED" -> Paused
                "$PaymentFailed", "PAYMENT_FAILED" -> PaymentFailed
                "$Suspended", "SUSPENDED" -> Suspended
                "$Expired", "EXPIRED" -> Expired
                "$Cancelled", "CANCELLED" -> Cancelled
                "$RolledOver", "ROLLED_OVER" -> RolledOver
                "$RollingOver", "ROLLING_OVER" -> RollingOver
                else -> error("Unknown ShareStatus: $state")
            }
    }
}

data class ShareStatusPermissions(
    val shareStatus: ShareStatus,
    val permissions: Map<ChangedBy, Set<ChangeReason>>
)

infix fun ShareStatus.permit(
    permissions: Map<ChangedBy, Set<ChangeReason>>
) : ShareStatusPermissions = ShareStatusPermissions(this, permissions)


