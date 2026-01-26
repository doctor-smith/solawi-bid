package org.solyton.solawi.bid.module.bid.data.internal


val shareStatusTransitionsWithPermissions: Map<ShareStatus, Set<ShareStatusPermissions>> by lazy {
    mapOf(

        ShareStatus.PendingActivation to setOf(
            ShareStatus.ActivationRejected permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.REQUIREMENTS_NOT_MET)
            ),
            ShareStatus.AwaitingAhcAuthorization permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.PAYMENT_MANDATE_REQUESTED),
                ChangedBy.SYSTEM to setOf(ChangeReason.PAYMENT_MANDATE_REQUESTED)
            ),
            ShareStatus.Subscribed permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.NO_PAYMENT_MANDATE_REQUIRED, ChangeReason.SUBSCRIPTION_APPROVED),
                ChangedBy.SYSTEM to setOf(ChangeReason.NO_PAYMENT_MANDATE_REQUIRED, ChangeReason.PAYMENT_MANDATE_APPROVED)
            ),
            ShareStatus.Cancelled permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.PROVIDER_CANCELLED_BEFORE_ACTIVATION),
                ChangedBy.USER to setOf(ChangeReason.USER_CANCELLED_BEFORE_ACTIVATION)
            )
        ),

        ShareStatus.ActivationRejected to setOf(
            ShareStatus.PendingActivation permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.NEW_REQUIREMENTS)
            ),
            ShareStatus.Expired permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.REJECTION_NOT_RESOLVED),
                ChangedBy.SYSTEM to setOf(ChangeReason.REJECTION_NOT_RESOLVED)
            ),
            ShareStatus.Cancelled permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.USER_OR_PROVIDER_CANCEL),
                ChangedBy.PROVIDER to setOf(ChangeReason.USER_OR_PROVIDER_CANCEL),
                ChangedBy.SYSTEM to setOf(ChangeReason.USER_OR_PROVIDER_CANCEL)
            )
        ),

        ShareStatus.AwaitingAhcAuthorization to setOf(
            ShareStatus.Subscribed permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.AUTHORIZATION_COMPLETED),
                ChangedBy.SYSTEM to setOf(ChangeReason.AUTHORIZATION_COMPLETED)
            ),
            ShareStatus.Suspended permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.AUTHORIZATION_FAILED),
                ChangedBy.SYSTEM to setOf(ChangeReason.AUTHORIZATION_FAILED)
            ),
            ShareStatus.Cancelled permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.USER_CANCEL)
            )
        ),

        ShareStatus.Subscribed to setOf(

            ShareStatus.RollingOver permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.NEW_PERIOD),
                ChangedBy.SYSTEM to setOf(ChangeReason.NEW_PERIOD)
            ),
            ShareStatus.Paused permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.USER_PAUSED)
            ),
            ShareStatus.PaymentFailed permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.PAYMENT_FAILED),
                ChangedBy.SYSTEM to setOf(ChangeReason.PAYMENT_FAILED)
            ),
            ShareStatus.Suspended permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.AUTO_SUSPENSION),
                ChangedBy.SYSTEM to setOf(ChangeReason.AUTO_SUSPENSION)
            ),
            ShareStatus.Expired permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.SUBSCRIPTION_END),
                ChangedBy.SYSTEM to setOf(ChangeReason.SUBSCRIPTION_END)
            ),
            ShareStatus.Cancelled permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.USER_CANCEL),
                ChangedBy.PROVIDER to setOf(ChangeReason.USER_CANCEL)
            )
        ),

        ShareStatus.PaymentFailed to setOf(
            ShareStatus.Subscribed permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.PAYMENT_RECOVERED)
            ),
            ShareStatus.Suspended permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.PAYMENT_NOT_RESOLVED),
                ChangedBy.SYSTEM to setOf(ChangeReason.PAYMENT_NOT_RESOLVED)
            ),
            ShareStatus.Cancelled permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.USER_CANCEL_AFTER_FAILURE)
            )
        ),

        ShareStatus.Paused to setOf(
            ShareStatus.Subscribed permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.RESUME)
            ),
            ShareStatus.Expired permit mapOf(
                ChangedBy.SYSTEM to setOf(ChangeReason.PERIOD_END)
            ),
            ShareStatus.Cancelled permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.USER_CANCEL)
            )
        ),

        ShareStatus.Suspended to setOf(
            ShareStatus.Subscribed permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.ISSUE_RESOLVED),
                ChangedBy.SYSTEM to setOf(ChangeReason.ISSUE_RESOLVED)
            ),
            ShareStatus.Expired permit mapOf(
                ChangedBy.SYSTEM to setOf(ChangeReason.UNRESOLVED_PERIOD_END)
            ),
            ShareStatus.Cancelled permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.TERMINATION),
                ChangedBy.PROVIDER to setOf(ChangeReason.TERMINATION),
                ChangedBy.SYSTEM to setOf(ChangeReason.TERMINATION)
            )
        ),

        ShareStatus.Expired to setOf(
            ShareStatus.PendingActivation permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.NEW_PERIOD)
            )
        ),



        ShareStatus.RollingOver to setOf(
            ShareStatus.PendingActivation permit mapOf(
                ChangedBy.USER to setOf(ChangeReason.USER_EVENTUAL_CHANGE)
            ),
            // Happens when the provider accepts a share in pending-activation state
            // which has a rolling-over companion. The rolling-over companions state
            // will be set to RolledOver in this case
            ShareStatus.RolledOver permit mapOf(
                ChangedBy.PROVIDER to setOf(ChangeReason.ROLLED_OVER),
                ChangedBy.SYSTEM to setOf(ChangeReason.ROLLED_OVER)
            ),
        ),

        // Terminal states
        ShareStatus.Cancelled to emptySet(),
        ShareStatus.RolledOver to emptySet(),
    )
}
