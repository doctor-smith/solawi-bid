package org.solyton.solawi.bid.module.statistics.service

import org.solyton.solawi.bid.module.statistics.data.Box
import org.solyton.solawi.bid.module.statistics.data.Distribution
import org.solyton.solawi.bid.module.statistics.data.DistributionConfiguration


fun List<Double>.distribute(configuration: DistributionConfiguration): Distribution {
    // only take values in the defined range
    val relevantData = filter {  it >= configuration.min && it <= configuration.max }
    //val min = relevantData.min()
    //val max = relevantData.max()

    val n = configuration.resolution
    val step = (configuration.max - configuration.min) / n.toDouble()

    // prepare boxes
    val boxes = IntRange(1, n).associateWith {
        val from = (it - 1.0) * step
        val to = it * step
        Box(from, to, relevantData.filter { jt -> jt >= from && jt < to }.size.toDouble(), n)
    }

    val maxHeight = boxes.values.maxBy { it.height }.height

    return Distribution(
        configuration.min, configuration.max, n,maxHeight, boxes
    )
}
