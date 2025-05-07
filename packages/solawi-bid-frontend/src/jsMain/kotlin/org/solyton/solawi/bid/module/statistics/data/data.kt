package org.solyton.solawi.bid.module.statistics.data

data class DistributionConfiguration(
    val min: Double,
    val max: Double,
    val resolution: Int
)

data class Distribution(
    val min: Double,
    val max: Double,
    val resolution: Int,
    val maxHeight: Double,
    val percentiles: Map<Int, Box>
)

data class Box(
    val from: Double,
    val to: Double,
    val height: Double,
    val resolution: Int
)
