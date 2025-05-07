package org.solyton.solawi.bid.application.ui.page.auction.action

/*
@Markup
fun readRound(auctionId: String, roundId: String): Action<Application, GetRound, ApiRound> = Action(
    name ="ReadRound",
    reader = userData * Reader { user: User -> GetRound(roundId) },
    endPoint = GetRound::class,
    writer = (
        auctions * FirstBy { it.auctionId == auctionId } *
        rounds * FirstBy { it.roundId == roundId }
    ).set contraMap {
        a: ApiRound -> a.toDomainType()
    }
)

 */