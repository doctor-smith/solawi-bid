package org.solyton.solawi.bid.module.bid.data.internal

sealed class ShareStatus {
    // Active
    data object Subscribed : ShareStatus()

    // Temporary states
    data object Paused : ShareStatus()
    data object Suspended : ShareStatus()

    // flow
    data object PendingActivation : ShareStatus()
    data object ActivationRejected : ShareStatus()
    data object AwaitingAhcAuthorization : ShareStatus()
    data object PaymentFailed : ShareStatus()

    // End states
    data object Cancelled : ShareStatus()
    data object Expired : ShareStatus()
    data object RolledOver : ShareStatus()
    data object RollingOver: ShareStatus()

    override fun toString(): String = when(this){
        is PendingActivation -> "PENDING_ACTIVATION"
        is ActivationRejected -> "ACTIVATION_REJECTED"
        is Subscribed -> "SUBSCRIBED"
        is AwaitingAhcAuthorization -> "AWAITING_AHC_AUTHORIZATION"
        is Cancelled -> "CANCELLED"
        is Paused -> "PAUSED"
        is PaymentFailed -> "PAYMENT_FAILED"
        is Suspended -> "SUSPENDED"
        is Expired -> "EXPIRED"
        is RollingOver -> "ROLLING_OVER"
        is RolledOver -> "ROLLED_OVER"

    }

    companion object {
        fun from(state: String): ShareStatus =
            when (state) {
                "PENDING_ACTIVATION" -> PendingActivation
                "ACTIVATION_REJECTED" -> ActivationRejected
                "AWAITING_AHC_AUTHORIZATION" -> AwaitingAhcAuthorization
                "SUBSCRIBED" -> Subscribed
                "PAUSED" -> Paused
                "PAYMENT_FAILED" -> PaymentFailed
                "SUSPENDED" -> Suspended
                "EXPIRED" -> Expired
                "CANCELLED" -> Cancelled
                "ROLLED_OVER" -> RolledOver
                "ROLLING_OVER" -> RollingOver
                else -> error("Unknown ShareStatus: $state")
            }
    }
}

val shareStatusTransitions: Map<ShareStatus, Set<ShareStatus>> by lazy {
    mapOf(

        ShareStatus.PendingActivation to setOf(
            ShareStatus.ActivationRejected,       // requirements aren't met
            ShareStatus.AwaitingAhcAuthorization, // payment mandate requested
            ShareStatus.Subscribed,               // no payment mandate required
            ShareStatus.Cancelled                 // user cancels before activation
        ),

        ShareStatus.ActivationRejected to setOf(
            ShareStatus.PendingActivation,        // user came up with new requirements
            ShareStatus.Expired,                  // rejection isn't resolved in time
            ShareStatus.Cancelled                 // user cancels subscription
        ),

        ShareStatus.AwaitingAhcAuthorization to setOf(
            ShareStatus.Subscribed,               // authorization completed successfully
            ShareStatus.Suspended,                // authorization failed or timed out
            ShareStatus.Cancelled                 // user cancels during authorization
        ),

        ShareStatus.Subscribed to setOf(
            ShareStatus.RolledOver,               // rolled over to next period
            ShareStatus.Paused,                   // user pauses participation
            ShareStatus.PaymentFailed,            // payment attempt failed
            ShareStatus.Suspended,                // automatic suspension (e.g. unpaid)
            ShareStatus.Expired,                  // subscription period ends
            ShareStatus.Cancelled                 // user cancels explicitly
        ),

        ShareStatus.PaymentFailed to setOf(
            ShareStatus.Subscribed,               // payment recovered
            ShareStatus.Suspended,                // payment not resolved in time
            ShareStatus.Cancelled                 // user cancels after failure
        ),

        ShareStatus.Paused to setOf(
            ShareStatus.Subscribed,               // user resumes
            ShareStatus.Expired,                  // period ends while paused
            ShareStatus.Cancelled                 // user cancels while paused
        ),

        ShareStatus.Suspended to setOf(
            ShareStatus.Subscribed,               // issue resolved (payment/admin)
            ShareStatus.Expired,                  // unresolved until the period ends
            ShareStatus.Cancelled                 // admin or user terminates
        ),

        ShareStatus.Expired to setOf(
            ShareStatus.PendingActivation         // new period / renewal starts
        ),

        ShareStatus.Cancelled to emptySet(),      // terminal state

        ShareStatus.RolledOver to emptySet(),     // terminal state at the end of season

        ShareStatus.RollingOver to setOf(
            ShareStatus.PendingActivation         // user eventually changes his share - needs approval
        )
    )
}



