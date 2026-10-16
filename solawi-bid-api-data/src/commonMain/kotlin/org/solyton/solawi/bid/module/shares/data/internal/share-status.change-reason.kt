package org.solyton.solawi.bid.module.shares.data.internal


enum class ChangeReason(val value: Int) {
    // === INITIAL CREATION & ROLLOVER ===
    INITIAL_CREATION(0),       // First creation of the share
    ROLLOVER(1),               // Subscription moved to next period
    ROLLED_OVER(2),            // Completed rollover at end of season
    NEW_PERIOD(3),             // Start of a new subscription/fiscal period
    IMPORT(4),                // Import share subscriptions
    TRANSFER_OWNERSHIP(36),    // Transfer ownership of subscription

    // === USER ACTIONS ===
    USER_ACTION(5),                   // Generic user action (pause, cancel, resume)
    USER_CANCELLED_BEFORE_ACTIVATION(6), // User cancels before share is active
    PROVIDER_CANCELLED_BEFORE_ACTIVATION(7), // Provider cancels before share is active
    USER_CANCEL(8),                   // Generic user cancellation
    USER_CANCEL_AFTER_FAILURE(9),     // User cancels after a failed payment
    USER_OR_PROVIDER_CANCEL(10),       // Either user or provider cancels
    USER_PAUSED(11),                  // User pauses subscription
    RESUME(12),                       // User resumes subscription
    USER_EVENTUAL_CHANGE(13),         // User eventually changes subscription

    // === PAYMENT EVENTS ===
    PAYMENT_EVENT(14),                 // Generic payment-related event
    PAYMENT_FAILED(15),                // Payment attempt failed
    PAYMENT_RECOVERED(16),             // Payment successfully recovered
    PAYMENT_NOT_RESOLVED(17),          // Payment failed and not resolved in time
    PAYMENT_MANDATE_REQUESTED(18),     // Payment mandate requested
    PAYMENT_MANDATE_APPROVED(19),      // Payment mandate approved
    NO_PAYMENT_MANDATE_REQUIRED(20),   // No payment mandate needed

    // === AUTHORIZATION EVENTS ===
    AUTHORIZATION_EVENT(21),           // Generic authorization event
    AUTHORIZATION_COMPLETED(22),       // Authorization completed successfully
    AUTHORIZATION_FAILED(23),          // Authorization failed (e.g., AHC)
    SUBSCRIPTION_APPROVED(24),         // Subscription approved after review

    // === ADMIN ACTIONS ===
    ADMIN_ACTION(25),                  // Manual override by admin or provider
    ISSUE_RESOLVED(26),                // Admin/system resolves an issue
    TERMINATION(27),                   // Admin or system terminates subscription

    // === SYSTEM EVENTS ===
    SYSTEM_EVENT(28),                  // Automatic system-triggered event
    AUTO_SUSPENSION(29),               // System suspension (e.g., unpaid)
    PERIOD_END(30),                    // Subscription period ends
    SUBSCRIPTION_END(31),              // End of subscription period
    UNRESOLVED_PERIOD_END(32),         // Subscription period ended without resolution
    REJECTION_NOT_RESOLVED(33),        // Rejection unresolved until expiration
    REQUIREMENTS_NOT_MET(34),          // Share creation requirements not met
    NEW_REQUIREMENTS(35)                // New requirements from user
}

enum class ChangedBy(val value: Int) {
    USER(0),
    PROVIDER(1),
    SYSTEM(2)
}
