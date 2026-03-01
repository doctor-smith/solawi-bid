package org.solyton.solawi.bid.module.bid.data.api

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.bid.data.values.AuctionId
import org.solyton.solawi.bid.module.values.ProviderId

@Serializable
data class ImportBiddersFromOrganization(
    val organizationId: ProviderId,
    val auctionId: AuctionId,
)
