package org.solyton.solawi.bid.module.bid.data.internal


enum class ChangeReason {
    // === INITIAL CREATION & ROLLOVER ===
    INITIAL_CREATION,       // First creation of the share
    ROLLOVER,               // Subscription moved to next period
    ROLLED_OVER,            // Completed rollover at end of season
    NEW_PERIOD,             // Start of a new subscription/fiscal period
    IMPORT,                 // Import share subscriptions

    // === USER ACTIONS ===
    USER_ACTION,                   // Generic user action (pause, cancel, resume)
    USER_CANCELLED_BEFORE_ACTIVATION, // User cancels before share is active
    PROVIDER_CANCELLED_BEFORE_ACTIVATION, // Provider cancels before share is active
    USER_CANCEL,                   // Generic user cancellation
    USER_CANCEL_AFTER_FAILURE,     // User cancels after a failed payment
    USER_OR_PROVIDER_CANCEL,       // Either user or provider cancels
    USER_PAUSED,                   // User pauses subscription
    RESUME,                        // User resumes subscription
    USER_EVENTUAL_CHANGE,          // User eventually changes subscription

    // === PAYMENT EVENTS ===
    PAYMENT_EVENT,                 // Generic payment-related event
    PAYMENT_FAILED,                // Payment attempt failed
    PAYMENT_RECOVERED,             // Payment successfully recovered
    PAYMENT_NOT_RESOLVED,          // Payment failed and not resolved in time
    PAYMENT_MANDATE_REQUESTED,     // Payment mandate requested
    PAYMENT_MANDATE_APPROVED,      // Payment mandate approved
    NO_PAYMENT_MANDATE_REQUIRED,   // No payment mandate needed

    // === AUTHORIZATION EVENTS ===
    AUTHORIZATION_EVENT,           // Generic authorization event
    AUTHORIZATION_COMPLETED,       // Authorization completed successfully
    AUTHORIZATION_FAILED,          // Authorization failed (e.g., AHC)
    SUBSCRIPTION_APPROVED,         // Subscription approved after review

    // === ADMIN ACTIONS ===
    ADMIN_ACTION,                  // Manual override by admin or provider
    ISSUE_RESOLVED,                // Admin/system resolves an issue
    TERMINATION,                   // Admin or system terminates subscription

    // === SYSTEM EVENTS ===
    SYSTEM_EVENT,                  // Automatic system-triggered event
    AUTO_SUSPENSION,               // System suspension (e.g., unpaid)
    PERIOD_END,                    // Subscription period ends
    SUBSCRIPTION_END,              // End of subscription period
    UNRESOLVED_PERIOD_END,         // Subscription period ended without resolution
    REJECTION_NOT_RESOLVED,        // Rejection unresolved until expiration
    REQUIREMENTS_NOT_MET,          // Share creation requirements not met
    NEW_REQUIREMENTS                // New requirements from user
}

enum class ChangedBy {
    USER,
    PROVIDER,
    SYSTEM
}
