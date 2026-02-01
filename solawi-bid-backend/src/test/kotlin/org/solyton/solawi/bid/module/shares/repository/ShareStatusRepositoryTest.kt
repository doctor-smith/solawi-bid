package org.solyton.solawi.bid.module.shares.repository

import org.evoleq.exposedx.test.runSimpleH2Test
import org.jetbrains.exposed.sql.Transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.schema.ShareStatusTable
import kotlin.test.assertEquals


class ShareStatusRepositoryTest {

    val tables = arrayOf(
        ShareStatusTable
    )


    fun Transaction.setup() {
        createTestShareStatuses()
    }

    @DbFunctional@Test
    fun initStatus() = runSimpleH2Test(*tables) {
        setup()
        val initStatus = initStatus()
        assertEquals(initStatus.name, ShareStatus.PendingActivation.toString())
    }



    @DbFunctional@ParameterizedTest(name = "Test ShareStatus: {0}")
    @MethodSource("allShareStatuses")
    fun testShareStatusTransition(status: ShareStatus) = runSimpleH2Test(*tables){
            setup()
            val statusEntity = statusEntity(status)
            assertEquals("$status", statusEntity.name)

    }

    companion object {
        @JvmStatic
        fun allShareStatuses(): Array<ShareStatus> = shareStatuses

    }
}
