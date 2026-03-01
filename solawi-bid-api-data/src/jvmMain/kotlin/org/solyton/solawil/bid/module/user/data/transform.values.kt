package org.solyton.solawil.bid.module.user.data

import org.solyton.solawi.bid.module.values.ProviderId
import java.util.UUID

fun ProviderId.toUUID(): UUID = UUID.fromString(value)
fun UUID.toProviderId(): ProviderId = ProviderId(toString())
