package org.solyton.solawi.bid.module.statistics

import io.ktor.http.HttpHeaders.Range
import org.solyton.solawi.bid.module.statistics.data.DistributionConfiguration
import org.solyton.solawi.bid.module.statistics.service.distribute
import kotlin.test.Test
import kotlin.test.assertEquals

class DistributionTest {

    @Test
    fun distributeTest() {
        val numbers = listOf(1,2,3,4,5,6,7,8,9,10).map{it.toDouble()}
        val config = DistributionConfiguration(0.0, 11.0,10)

        val distribution = numbers.distribute(config)

        println(distribution)

        (1..10).forEach {
            assertEquals(1.0,distribution.percentiles[it]!!.height)
        }
    }
}