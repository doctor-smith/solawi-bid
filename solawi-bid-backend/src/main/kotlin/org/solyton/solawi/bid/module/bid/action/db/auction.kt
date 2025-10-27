package org.solyton.solawi.bid.module.bid.action.db

import io.ktor.util.*
import kotlinx.datetime.LocalDateTime
import org.evoleq.exposedx.joda.toJoda
import org.evoleq.exposedx.transaction.resultTransaction
import org.evoleq.ktorx.Contextual
import org.evoleq.ktorx.DbAction
import org.evoleq.ktorx.KlAction
import org.evoleq.ktorx.map
import org.evoleq.ktorx.result.Result
import org.evoleq.ktorx.result.bindSuspend
import org.evoleq.ktorx.result.map
import org.evoleq.math.MathDsl
import org.evoleq.math.x
import org.evoleq.uuid.UUID_ZERO
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.toApiType
import org.solyton.solawi.bid.module.bid.exception.BidRoundException
import org.solyton.solawi.bid.module.bid.schema.*
import org.solyton.solawi.bid.module.bid.schema.AuctionEntity
import org.solyton.solawi.bid.module.bid.service.addUserAsOwnerToContext
import org.solyton.solawi.bid.module.bid.service.cloneDefaultAuctionContext
import org.solyton.solawi.bid.module.permission.exception.ContextException
import org.solyton.solawi.bid.module.permission.schema.ContextEntity
import org.solyton.solawi.bid.module.permission.schema.ContextsTable
import org.solyton.solawi.bid.module.permission.schema.RoleEntity
import org.solyton.solawi.bid.module.permission.schema.RolesTable
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext
import org.solyton.solawi.bid.module.permission.schema.UserRoleContext.userId
import java.util.*
import org.solyton.solawi.bid.module.bid.data.api.Auctions as ApiAuctions

@MathDsl
val CreateAuction = KlAction<Result<Contextual<CreateAuction>>, Result<ApiAuction>> {
    auction: Result<Contextual<CreateAuction>> -> DbAction {
        database -> auction bindSuspend  { contextual -> resultTransaction(database) {
            val auctionContextId = UUID.fromString(contextual.data.contextId)
            cloneDefaultAuctionContext(contextual.userId, auctionContextId)

            addUserAsOwnerToContext(contextual.userId, auctionContextId)

            createAuction(
                contextual.data.name,
                contextual.data.date,
                contextId = auctionContextId
            ).toApiType()
        } }  x database
    }
}

fun Transaction.createAuction(
    name: String,
    date: LocalDateTime,
    type: String = "SOLAWI_TUEBINGEN",
    contextId: UUID
): AuctionEntity {
    val auctionType = AuctionType.find { AuctionTypes.type eq type.toUpperCasePreservingASCIIRules() }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuctionType(type)

    val context = ContextEntity.find { ContextsTable.id eq contextId }.firstOrNull()
        ?: throw ContextException.NoSuchContext(contextId.toString())

    return AuctionEntity.new {
        this.name = name
        this.date = date.toJoda()
        this.type = auctionType
        createdBy = UUID_ZERO
        this.context = context
    }
}

@MathDsl
val ReadAllAuctions = KlAction<Result<Contextual<GetAuctions>>, Result<ApiAuctions>> {
    result -> DbAction { database -> result bindSuspend {contextual ->  resultTransaction(database) {
        val auctionRoleIds = RoleEntity.find { RolesTable.name inList listOf(
            "BIDDER", "USER", "OWNER", "AUCTION_MANAGER", "AUCTION_TEAMMATE", "AUCTION_MODERATOR"
        ) }.map { it.id.value }
        val contextIds = UserRoleContext.selectAll().where {
            UserRoleContext.userId eq contextual.userId and (UserRoleContext.roleId inList auctionRoleIds)
        }.map { row -> row[UserRoleContext.contextId].value }

        val auctions = readAuctions(contextIds).map {
            it.toApiType().copy(
                bidderInfo = getBidderDetails(it).map { det -> det.toBidderInfo() },
                auctionDetails = getAuctionDetails(it)
            )
        }
        ApiAuctions(auctions)

    }
    // TODO(use identifier to return all auction which are accessible as identified person)
    } x database }
}

fun Transaction.getAuctionDetails(auction: AuctionEntity): AuctionDetails {
    val tue = AuctionDetailsSolawiTuebingen.find { AuctionDetailsSolawiTuebingenTable.auctionId eq auction.id.value }.firstOrNull()
    return if(tue != null) {
        AuctionDetails.SolawiTuebingen(
            tue.minimalBid,
            tue.benchmark,
            tue.targetAmount,
            tue.solidarityContribution
        )
    } else {
        AuctionDetails.Empty
    }
}

fun Transaction.readAuctions(allowedContexts: List<UUID>): List<AuctionEntity> = with(AuctionEntity.find{
    AuctionsTable.contextId inList allowedContexts
}.map{it
}) {
   try {
       toList()
   } catch(exception: Exception)
   {
       listOf()
   }
}

@MathDsl
val ReadAuction = KlAction<Result<UUID>, Result<ApiAuction>> {
    auction -> DbAction {
        database ->  auction bindSuspend { resultTransaction(database) {
                readAuction(it).toApiType()

                // TODO(use identifier to return all auction which are accessible as identified person)
            }
        }  x database
    }}



fun Transaction.readAuction(auctionId: UUID): AuctionEntity {
    val auction = AuctionEntity.find { Auctions.id eq auctionId }.firstOrNull()
        ?: throw BidRoundException.NoSuchAuction

    return auction
}



@MathDsl
val DeleteAuctions = KlAction<Result<Contextual<DeleteAuctions>>, Result<Contextual<GetAuctions>>> {
    auctions -> DbAction {
        database -> auctions bindSuspend {
            contextual -> resultTransaction(database){
                deleteAuctions(contextual.data.auctionIds.map { UUID.fromString(it) })
                contextual map { GetAuctions }
        } }  x database
    }
}

fun Transaction.deleteAuctions(auctionIds: List<UUID>) {
    // todo:dev validation: There could be accepted auctions in the list -> What to do?
    Auctions.deleteWhere { Auctions.id inList auctionIds }
}

@MathDsl
val UpdateAuctions = KlAction<Result<Contextual<UpdateAuctions>>, Result<Contextual<GetAuctions>>> {
    auctions -> DbAction {
        database -> auctions bindSuspend {
            contextual -> resultTransaction(database) {
                updateAuctions(contextual.data.list )
                contextual map {GetAuctions}
            }
        } x database
    }
}


@Suppress("UNUSED_PARAMETER")
fun Transaction.updateAuctions(auctions: List<ApiAuction>) {
    TODO("Function updateAuctions not implemented yet! Do not forget validation! Auctions could be accepted")
}

@MathDsl
val ConfigureAuction = KlAction<Result< Contextual<ConfigureAuction>>, Result<ApiAuction>> {
    auction -> DbAction {
        database -> auction bindSuspend {
            contextual -> resultTransaction(database) {
                configureAuction(contextual.data)
            }
        }  x database
    }
}

fun Transaction.configureAuction(auction: ConfigureAuction): ApiAuction {
    val auctionId = UUID.fromString(auction.id)
    val auctionEntity = AuctionEntity.findById(auctionId)?:
        throw BidRoundException.NoSuchAuction

    validateAuctionNotAccepted(auctionId)

    val auctionDetails = setAuctionDetails(auctionEntity, auction.auctionDetails)

    return with(auctionEntity) {
        name = auction.name
        date = DateTime().withDate(auction.date.year, auction.date.monthNumber, auction.date.dayOfMonth)


        this
    }.toApiType().copy(
        bidderInfo = getBidderDetails(auctionEntity).map{det -> det.toBidderInfo()},
        auctionDetails = auctionDetails
    )
}

fun Transaction.setAuctionDetails(auction: AuctionEntity, auctionDetails: AuctionDetails): AuctionDetails =
    when(auctionDetails) {
        is AuctionDetails.Empty -> auctionDetails
        is AuctionDetails.SolawiTuebingen -> {
            val detailsEntity = AuctionDetailsSolawiTuebingen.find {
                AuctionDetailsSolawiTuebingenTable.auctionId eq auction.id.value
            }.firstOrNull()
            if(detailsEntity == null) {
                AuctionDetailsSolawiTuebingen.new {
                    this.auction = auction
                    benchmark = auctionDetails.benchmark
                    targetAmount = auctionDetails.targetAmount
                    solidarityContribution = auctionDetails.solidarityContribution
                    minimalBid = auctionDetails.minimalBid
                    createdBy = UUID_ZERO
                }
            } else {
                detailsEntity.benchmark = auctionDetails.benchmark
                detailsEntity.targetAmount = auctionDetails.targetAmount
                detailsEntity.solidarityContribution = auctionDetails.solidarityContribution
                detailsEntity.minimalBid = auctionDetails.minimalBid
            }
            auctionDetails
        }
    }

fun BidderDetails.toBidderInfo(): BidderInfo = when(this) {
    is BidderDetails.SolawiTuebingen -> BidderInfo(bidder.id.value.toString(), numberOfShares)
    // else -> throw Exception("No such BidderDetails")
}
