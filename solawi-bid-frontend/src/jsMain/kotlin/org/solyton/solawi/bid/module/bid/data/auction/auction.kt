// This file has been partially auto generated. 
// Please don't make any changes to the lenses.
// Feel free to add or remove annotated properties from
// the generator data class. The corresponding lenses 
// will be removed or added on the next run of the 
// lens generator. See below for more details.
package org.solyton.solawi.bid.module.bid.data.auction

import org.evoleq.optics.Lensify
import org.evoleq.optics.ReadOnly
import org.evoleq.optics.ReadWrite
import org.evoleq.optics.lens.Lens
import org.solyton.solawi.bid.module.bid.data.bidder.BidderInfo
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import kotlinx.datetime.LocalDate as Date

/**
 * Generator class.
 * Feel free to add or remove annotated properties from
 * the class. Make sure that they are annotated with
 * - @ReadOnly
 * - @ReadWrite
 * If you want that a property-lens will be generated
 * on the next run of the lens generator.
 * If not, just omit the annotation or annotate it with @Ignore.
 */
@Lensify data class Auction(
    @ReadOnly val auctionId: String,
    @ReadWrite val name: String,
    @ReadWrite val date: Date,
    @ReadWrite val rounds: List<Round> = listOf(),
    @ReadWrite val bidderInfo: List<BidderInfo> = listOf(),
    @ReadWrite val auctionDetails: AuctionDetails = AuctionDetails(),
    @ReadOnly val acceptedRoundId: String? = null
)

/**
 * Autogenerated ReadOnly Lens.
 * Read [Auction.auctionId]
 */
@ReadOnly val auctionId: Lens<Auction, String> by lazy{ Lens(
    get = {whole -> whole.auctionId},
    set = {{it}}
) }
/**
 * Autogenerated Lens.
 * Read and manipulate [Auction.name]
 */
@ReadWrite val name: Lens<Auction, String> by lazy{ Lens(
    get = {whole -> whole.name},
    set = {part -> {whole -> whole.copy(name = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [Auction.name]
 */
@ReadWrite fun Auction.name(set: String.()->String ): Auction = copy(name = set(name)) 
/**
 * Autogenerated Lens.
 * Read and manipulate [Auction.date]
 */
@ReadWrite val date: Lens<Auction, Date> by lazy{ Lens(
    get = {whole -> whole.date},
    set = {part -> {whole -> whole.copy(date = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [Auction.date]
 */
@ReadWrite fun Auction.date(set: Date.()->Date ): Auction = copy(date = set(date)) 
/**
 * Autogenerated Lens.
 * Read and manipulate [Auction.rounds]
 */
@ReadWrite val rounds: Lens<Auction, List<Round>> by lazy{ Lens(
    get = {whole -> whole.rounds},
    set = {part -> {whole -> whole.copy(rounds = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [Auction.rounds]
 */
@ReadWrite fun Auction.rounds(set: List<Round>.()->List<Round> ): Auction = copy(rounds = set(rounds))
/**
 * Autogenerated Lens.
 * Read and manipulate [Auction.bidderInfo]
 */
@ReadWrite val bidderInfo: Lens<Auction, List<BidderInfo>> by lazy{ Lens(
    get = {whole -> whole.bidderInfo},
    set = {part -> {whole -> whole.copy(bidderInfo = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [Auction.bidderInfo]
 */
@ReadWrite fun Auction.bidderInfo(set: List<BidderInfo>.()->List<BidderInfo> ): Auction = copy(bidderInfo = set(bidderInfo))
/**
 * Autogenerated Lens.
 * Read and manipulate [Auction.auctionDetails]
 */
@ReadWrite val auctionDetails: Lens<Auction, AuctionDetails> by lazy{ Lens(
    get = {whole -> whole.auctionDetails},
    set = {part -> {whole -> whole.copy(auctionDetails = part)}}
) }
/**
 * Autogenerated Setter of a Pseudo Lens
 * Manipulate [Auction.auctionDetails]
 */
@ReadWrite fun Auction.auctionDetails(set: AuctionDetails.()->AuctionDetails ): Auction = copy(auctionDetails = set(auctionDetails)) 
/**
 * Autogenerated ReadOnly Lens.
 * Read [Auction.acceptedRoundId]
 */
@ReadOnly val acceptedRoundId: Lens<Auction, String?> by lazy{ Lens(
    get = {whole -> whole.acceptedRoundId},
    set = {{it}}
) }
