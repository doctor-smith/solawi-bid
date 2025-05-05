package org.solyton.solawi.bid.application.ui.page.auction.action

import org.evoleq.math.Reader
import org.evoleq.math.contraMap
import org.evoleq.optics.storage.Action
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.bidderMailAddresses
import org.solyton.solawi.bid.module.bid.data.api.ApiBidderMails
import org.solyton.solawi.bid.module.bid.data.api.SearchBidderData
import org.solyton.solawi.bid.module.bid.data.bidder.BidderMails

fun searchUsernameOfBidder(bidder: SearchBidderData) =
    Action<Application, SearchBidderData, ApiBidderMails>(
        name = "AddBidders",
        reader =  Reader{ _ -> bidder },
        endPoint = SearchBidderData::class,
        writer = bidderMailAddresses .set contraMap { bidders: ApiBidderMails ->
            BidderMails(bidders.emails)
        }
    )