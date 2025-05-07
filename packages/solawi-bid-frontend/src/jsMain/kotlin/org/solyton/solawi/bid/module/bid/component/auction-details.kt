package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.date.format
import org.evoleq.compose.layout.PropertiesStyles
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.language.date
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.module.bid.data.Auction
import org.solyton.solawi.bid.module.bid.data.reader.*

@Markup
@Composable
@Suppress("FunctionName")
fun AuctionDetails(
    auction: Storage<Auction>,
    locale: Source<Locale>,
    texts: Reader<Unit, Lang.Block>,
    styles: PropertiesStyles = PropertiesStyles()
) {
    val details = auction.read().auctionDetails
    ReadOnlyProperties(
        listOf(
            Property((texts * targetAmount).emit(), "${details.targetAmount?:"-"}"),
            Property((texts * benchmark).emit(), "${details.benchmark?:"-"}"),
            // Property((texts * minimalBid).emit(), "${details.minimalBid?:"-"}"),
            Property((texts * solidarityContribution).emit(), "${details.solidarityContribution?:"-"}")
        ),
        styles
    )
    ReadOnlyProperties(
        listOf(
            Property((texts * date).emit(), with(auction.read()) { date.format(locale.emit()) }),
            Property((texts * numberOfBidders).emit(), (auction * countBidders).emit()),
            // todo:dev count number of shares
            Property((texts * numberOfShares).emit(),(auction * countShares).emit())
        ),
        styles
    )
}
