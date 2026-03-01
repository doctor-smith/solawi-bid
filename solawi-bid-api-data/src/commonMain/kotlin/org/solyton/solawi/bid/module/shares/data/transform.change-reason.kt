package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.shares.data.api.ApiChangeReason
import org.solyton.solawi.bid.module.shares.data.internal.ChangeReason

@Suppress("CyclomaticComplexMethod")
fun ApiChangeReason.toDomainType() = when(this) {
    ApiChangeReason.INITIAL_CREATION -> ChangeReason.INITIAL_CREATION
    ApiChangeReason.ROLLOVER -> ChangeReason.ROLLOVER
    ApiChangeReason.ROLLED_OVER -> ChangeReason.ROLLED_OVER
    ApiChangeReason.NEW_PERIOD -> ChangeReason.NEW_PERIOD
    ApiChangeReason.IMPORT -> ChangeReason.IMPORT
    ApiChangeReason.USER_ACTION -> ChangeReason.USER_ACTION
    ApiChangeReason.USER_CANCELLED_BEFORE_ACTIVATION -> ChangeReason.USER_CANCELLED_BEFORE_ACTIVATION
    ApiChangeReason.PROVIDER_CANCELLED_BEFORE_ACTIVATION -> ChangeReason.PROVIDER_CANCELLED_BEFORE_ACTIVATION
    ApiChangeReason.USER_CANCEL -> ChangeReason.USER_CANCEL
    ApiChangeReason.USER_CANCEL_AFTER_FAILURE -> ChangeReason.USER_CANCEL_AFTER_FAILURE
    ApiChangeReason.USER_OR_PROVIDER_CANCEL -> ChangeReason.USER_OR_PROVIDER_CANCEL
    ApiChangeReason.USER_PAUSED -> ChangeReason.USER_PAUSED
    ApiChangeReason.RESUME -> ChangeReason.RESUME
    ApiChangeReason.USER_EVENTUAL_CHANGE -> ChangeReason.USER_EVENTUAL_CHANGE
    ApiChangeReason.PAYMENT_EVENT -> ChangeReason.PAYMENT_EVENT
    ApiChangeReason.PAYMENT_FAILED -> ChangeReason.PAYMENT_FAILED
    ApiChangeReason.PAYMENT_RECOVERED -> ChangeReason.PAYMENT_RECOVERED
    ApiChangeReason.PAYMENT_NOT_RESOLVED -> ChangeReason.PAYMENT_NOT_RESOLVED
    ApiChangeReason.PAYMENT_MANDATE_REQUESTED -> ChangeReason.PAYMENT_MANDATE_REQUESTED
    ApiChangeReason.PAYMENT_MANDATE_APPROVED -> ChangeReason.PAYMENT_MANDATE_APPROVED
    ApiChangeReason.NO_PAYMENT_MANDATE_REQUIRED -> ChangeReason.NO_PAYMENT_MANDATE_REQUIRED
    ApiChangeReason.AUTHORIZATION_EVENT -> ChangeReason.AUTHORIZATION_EVENT
    ApiChangeReason.AUTHORIZATION_COMPLETED -> ChangeReason.AUTHORIZATION_COMPLETED
    ApiChangeReason.AUTHORIZATION_FAILED -> ChangeReason.AUTHORIZATION_FAILED
    ApiChangeReason.SUBSCRIPTION_APPROVED -> ChangeReason.SUBSCRIPTION_APPROVED
    ApiChangeReason.ADMIN_ACTION -> ChangeReason.ADMIN_ACTION
    ApiChangeReason.ISSUE_RESOLVED -> ChangeReason.ISSUE_RESOLVED
    ApiChangeReason.TERMINATION -> ChangeReason.TERMINATION
    ApiChangeReason.SYSTEM_EVENT -> ChangeReason.SYSTEM_EVENT
    ApiChangeReason.AUTO_SUSPENSION -> ChangeReason.AUTO_SUSPENSION
    ApiChangeReason.PERIOD_END -> ChangeReason.PERIOD_END
    ApiChangeReason.SUBSCRIPTION_END -> ChangeReason.SUBSCRIPTION_END
    ApiChangeReason.UNRESOLVED_PERIOD_END -> ChangeReason.UNRESOLVED_PERIOD_END
    ApiChangeReason.REJECTION_NOT_RESOLVED -> ChangeReason.REJECTION_NOT_RESOLVED
    ApiChangeReason.REQUIREMENTS_NOT_MET -> ChangeReason.REQUIREMENTS_NOT_MET
    ApiChangeReason.NEW_REQUIREMENTS -> ChangeReason.NEW_REQUIREMENTS
}

@Suppress("CyclomaticComplexMethod")
fun ChangeReason.toApiType() = when(this) {
    ChangeReason.INITIAL_CREATION -> ApiChangeReason.INITIAL_CREATION
    ChangeReason.ROLLOVER -> ApiChangeReason.ROLLOVER
    ChangeReason.ROLLED_OVER -> ApiChangeReason.ROLLED_OVER
    ChangeReason.NEW_PERIOD -> ApiChangeReason.NEW_PERIOD
    ChangeReason.IMPORT -> ApiChangeReason.IMPORT
    ChangeReason.USER_ACTION -> ApiChangeReason.USER_ACTION
    ChangeReason.USER_CANCELLED_BEFORE_ACTIVATION -> ApiChangeReason.USER_CANCELLED_BEFORE_ACTIVATION
    ChangeReason.PROVIDER_CANCELLED_BEFORE_ACTIVATION -> ApiChangeReason.PROVIDER_CANCELLED_BEFORE_ACTIVATION
    ChangeReason.USER_CANCEL -> ApiChangeReason.USER_CANCEL
    ChangeReason.USER_CANCEL_AFTER_FAILURE -> ApiChangeReason.USER_CANCEL_AFTER_FAILURE
    ChangeReason.USER_OR_PROVIDER_CANCEL -> ApiChangeReason.USER_OR_PROVIDER_CANCEL
    ChangeReason.USER_PAUSED -> ApiChangeReason.USER_PAUSED
    ChangeReason.RESUME -> ApiChangeReason.RESUME
    ChangeReason.USER_EVENTUAL_CHANGE -> ApiChangeReason.USER_EVENTUAL_CHANGE
    ChangeReason.PAYMENT_EVENT -> ApiChangeReason.PAYMENT_EVENT
    ChangeReason.PAYMENT_FAILED -> ApiChangeReason.PAYMENT_FAILED
    ChangeReason.PAYMENT_RECOVERED -> ApiChangeReason.PAYMENT_RECOVERED
    ChangeReason.PAYMENT_NOT_RESOLVED -> ApiChangeReason.PAYMENT_NOT_RESOLVED
    ChangeReason.PAYMENT_MANDATE_REQUESTED -> ApiChangeReason.PAYMENT_MANDATE_REQUESTED
    ChangeReason.PAYMENT_MANDATE_APPROVED -> ApiChangeReason.PAYMENT_MANDATE_APPROVED
    ChangeReason.NO_PAYMENT_MANDATE_REQUIRED -> ApiChangeReason.NO_PAYMENT_MANDATE_REQUIRED
    ChangeReason.AUTHORIZATION_EVENT -> ApiChangeReason.AUTHORIZATION_EVENT
    ChangeReason.AUTHORIZATION_COMPLETED -> ApiChangeReason.AUTHORIZATION_COMPLETED
    ChangeReason.AUTHORIZATION_FAILED -> ApiChangeReason.AUTHORIZATION_FAILED
    ChangeReason.SUBSCRIPTION_APPROVED -> ApiChangeReason.SUBSCRIPTION_APPROVED
    ChangeReason.ADMIN_ACTION -> ApiChangeReason.ADMIN_ACTION
    ChangeReason.ISSUE_RESOLVED -> ApiChangeReason.ISSUE_RESOLVED
    ChangeReason.TERMINATION -> ApiChangeReason.TERMINATION
    ChangeReason.SYSTEM_EVENT -> ApiChangeReason.SYSTEM_EVENT
    ChangeReason.AUTO_SUSPENSION -> ApiChangeReason.AUTO_SUSPENSION
    ChangeReason.PERIOD_END -> ApiChangeReason.PERIOD_END
    ChangeReason.SUBSCRIPTION_END -> ApiChangeReason.SUBSCRIPTION_END
    ChangeReason.UNRESOLVED_PERIOD_END -> ApiChangeReason.UNRESOLVED_PERIOD_END
    ChangeReason.REJECTION_NOT_RESOLVED -> ApiChangeReason.REJECTION_NOT_RESOLVED
    ChangeReason.REQUIREMENTS_NOT_MET -> ApiChangeReason.REQUIREMENTS_NOT_MET
    ChangeReason.NEW_REQUIREMENTS -> ApiChangeReason.NEW_REQUIREMENTS
}
