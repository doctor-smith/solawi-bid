package org.solyton.solawi.bid.module.shares.util

import org.evoleq.exposedx.test.runSimpleH2Test
import org.junit.jupiter.api.Test
import org.solyton.solawi.bid.DbFunctional
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.schema.ShareStatusEntity
import org.solyton.solawi.bid.module.shares.schema.ShareStatusTable
import org.solyton.solawi.bid.module.shares.schema.isEqualTo
import kotlin.test.assertTrue

class ShareStatusEqualityTest {

    val tables = arrayOf(
        ShareStatusTable
    )

    @DbFunctional@Test
    fun `equality operators should behave correctly`() = runSimpleH2Test(*tables) {


        val internalStatus = ShareStatus.External
        val statusEntity = ShareStatusEntity.new {
            name = internalStatus.value
            description = ""

        }
        assertTrue{ statusEntity.isEqualTo(internalStatus) }
        assertTrue { internalStatus.isEqualTo(statusEntity) }
    }
}
