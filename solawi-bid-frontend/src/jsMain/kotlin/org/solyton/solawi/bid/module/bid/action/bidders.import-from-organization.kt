package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.suffixed
import org.evoleq.optics.transform.update
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiAuction
import org.solyton.solawi.bid.module.bid.data.api.ImportBiddersFromOrganization
import org.solyton.solawi.bid.module.bid.data.auctions
import org.solyton.solawi.bid.module.bid.data.toDomainType

const val IMPORT_BIDDERS_FROM_ORGANIZATION = "ImportBiddersFromOrganization"

fun importBiddersFromOrganization(
    data: ImportBiddersFromOrganization,
    nameSuffix: String = ""
) = Action<BidApplication, ImportBiddersFromOrganization, ApiAuction>(
    name = IMPORT_BIDDERS_FROM_ORGANIZATION.suffixed(nameSuffix),
    reader = { data },
    endPoint = ImportBiddersFromOrganization::class,
    writer = auctions.update {
        p, q -> p.auctionId == q.auctionId
    } contraMap {apiAuction -> apiAuction.toDomainType() }
)
