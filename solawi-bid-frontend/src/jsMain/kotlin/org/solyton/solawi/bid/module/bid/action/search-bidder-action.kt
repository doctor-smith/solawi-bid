package org.solyton.solawi.bid.module.bid.action

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.ApiBidderMails
import org.solyton.solawi.bid.module.bid.data.api.SearchBidderData
import org.solyton.solawi.bid.module.bid.data.bidder.BidderMails
import org.solyton.solawi.bid.module.bid.data.bidderMailAddresses

fun searchUsernameOfBidder(bidder: SearchBidderData) =
    Action<BidApplication, SearchBidderData, ApiBidderMails>(
        name = "AddBidders",
        reader =  Reader{ _ -> bidder },
        endPoint = SearchBidderData::class,
        writer = bidderMailAddresses .set contraMap { bidders: ApiBidderMails ->
            BidderMails(bidders.emails)
        }
    )
