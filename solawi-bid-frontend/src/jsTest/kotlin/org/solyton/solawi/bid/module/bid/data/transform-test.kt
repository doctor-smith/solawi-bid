package org.solyton.solawi.bid.module.bid.data

import kotlinx.datetime.LocalDate
import org.evoleq.kotlinx.date.todayWithTime
import org.solyton.solawi.bid.module.bid.data.api.ApiRoundComment
import org.solyton.solawi.bid.module.bid.data.api.ApiRoundComments
import org.solyton.solawi.bid.module.bid.data.bidround.RoundComment
import kotlin.test.Test
import kotlin.test.assertEquals

class BidTransformTest {
    @Test fun transformRoundCommentToDomainType() {
        val date = todayWithTime()
        val apiRoundComment = ApiRoundComment(
            "id", "comment", date, "me"
        )
        val expected = RoundComment(
            "id", "comment", "me", date
        )
        assertEquals(expected, apiRoundComment.toDomainType())
    }

    @Test fun transformRoundCommentsToDomainType() {
        val date = todayWithTime()
        val apiRoundComment = ApiRoundComment(
            "id", "comment", date, "me"
        )
        val expected = RoundComment(
            "id", "comment", "me", date
        )

        assertEquals(listOf(expected), ApiRoundComments(listOf(apiRoundComment)).toDomainType())
    }
}
