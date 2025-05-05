package org.solyton.solawi.bid.module.bid.data.reader

import org.evoleq.language.Lang
import org.evoleq.language.LangComponent
import org.evoleq.language.get
import org.evoleq.math.Reader

sealed class BidComponent(override val path: String, override val value: String = "solyton.auction"): LangComponent {
    data object AuctionsPage : BidComponent("solyton.auction.auctionsPage")
    data object AuctionPage : BidComponent("solyton.auction.auctionPage")
    data object SearchBiddersPage : BidComponent("solyton.auction.searchBiddersPage")
    data object Round : BidComponent("solyton.auction.round")
}

val targetAmount: Reader<Lang.Block, String> = Reader { lang -> lang["targetAmount"] }
val benchmark: Reader<Lang.Block, String> = Reader { lang -> lang["benchmark"] }
val solidarityContribution: Reader<Lang.Block, String> = Reader { lang -> lang["solidarityContribution"] }
val minimalBid: Reader<Lang.Block, String> = Reader { lang -> lang["minimalBid"] }
val numberOfShares: Reader<Lang.Block, String> = Reader { lang -> lang["numberOfShares"] }
val numberOfBidders: Reader<Lang.Block, String> = Reader { lang -> lang["numberOfBidders"] }
val rounds: Reader<Lang.Block, String> = Reader { lang -> lang["rounds"] }

val firstname: Reader<Lang.Block, String> = Reader { lang -> lang["firstname"] }
val lastname: Reader<Lang.Block, String> = Reader { lang -> lang["lastname"] }
val emailAddress: Reader<Lang.Block, String> = Reader { lang -> lang["emailAddress"] }
val relatedEmailAddresses: Reader<Lang.Block, String> = Reader { lang -> lang["relatedEmailAddresses"] }
val relatedData: Reader<Lang.Block, String> = Reader { lang -> lang["relatedData"] }
