package org.solyton.solawi.bid.module.shares.data

import org.solyton.solawi.bid.module.shares.data.api.ApiChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy

fun ApiChangedBy.toDomainType() = when(this) {
    ApiChangedBy.USER -> ChangedBy.USER
    ApiChangedBy.SYSTEM -> ChangedBy.SYSTEM
    ApiChangedBy.PROVIDER -> ChangedBy.PROVIDER
}

fun ChangedBy.toApiType() = when(this) {
    ChangedBy.USER -> ApiChangedBy.USER
    ChangedBy.SYSTEM -> ApiChangedBy.SYSTEM
    ChangedBy.PROVIDER -> ApiChangedBy.PROVIDER
}
