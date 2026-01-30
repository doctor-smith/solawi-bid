package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.bid.data.internal.ShareStatus as InternalShareStatus
import org.solyton.solawi.bid.module.shares.data.api.ShareStatus as ApiShareStatus

fun InternalShareStatus.toApiType() : ApiShareStatus = when(this) {
    InternalShareStatus.ActivationRejected -> ApiShareStatus.ActivationRejected
    InternalShareStatus.ClearedForAuction -> ApiShareStatus.ClearedForAuction
    InternalShareStatus.External -> ApiShareStatus.External
    InternalShareStatus.AwaitingAhcAuthorization -> ApiShareStatus.AwaitingAhcAuthorization
    InternalShareStatus.Cancelled -> ApiShareStatus.Cancelled
    InternalShareStatus.Expired -> ApiShareStatus.Expired
    InternalShareStatus.Paused -> ApiShareStatus.Paused
    InternalShareStatus.PaymentFailed -> ApiShareStatus.PaymentFailed
    InternalShareStatus.PendingActivation -> ApiShareStatus.PendingActivation
    InternalShareStatus.RolledOver -> ApiShareStatus.RolledOver
    InternalShareStatus.RollingOver -> ApiShareStatus.RollingOver
    InternalShareStatus.Subscribed -> ApiShareStatus.Subscribed
    InternalShareStatus.Suspended -> ApiShareStatus.Suspended
}

fun ApiShareStatus.toInternalType(): InternalShareStatus = when (this) {
    ApiShareStatus.ActivationRejected -> InternalShareStatus.ActivationRejected
    ApiShareStatus.AwaitingAhcAuthorization -> InternalShareStatus.AwaitingAhcAuthorization
    ApiShareStatus.ClearedForAuction -> InternalShareStatus.ClearedForAuction
    ApiShareStatus.External -> InternalShareStatus.External
    ApiShareStatus.Cancelled -> InternalShareStatus.Cancelled
    ApiShareStatus.Expired -> InternalShareStatus.Expired
    ApiShareStatus.Paused -> InternalShareStatus.Paused
    ApiShareStatus.PaymentFailed -> InternalShareStatus.PaymentFailed
    ApiShareStatus.PendingActivation -> InternalShareStatus.PendingActivation
    ApiShareStatus.RolledOver -> InternalShareStatus.RolledOver
    ApiShareStatus.RollingOver -> InternalShareStatus.RollingOver
    ApiShareStatus.Subscribed -> InternalShareStatus.Subscribed
    ApiShareStatus.Suspended -> InternalShareStatus.Suspended
}

