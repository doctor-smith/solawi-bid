package org.evoleq.compose.routing

import androidx.compose.runtime.Composable
import org.evoleq.math.x


internal fun ArrayList<Routes>.merge(): ArrayList<Routes> {
    val staticSegments = filter { it.segment is RouteSegment.Static }
    val variableSegments = filter { it.segment is RouteSegment.Variable}

    val mergedStaticSegments = staticSegments
        .groupBy { it.segment.value }
        .map { entry -> (entry.value.firstOrNull{ it.component != null }?: entry.value.first()) x entry.value }
        .map{ pair -> Routes(
            segment = pair.first.segment,
            component = pair.first.component,
            children = pair.second.map { it.children }.flatten().let{ it.sortedBy { r -> when(r.segment){
                is RouteSegment.Static -> 0
                else -> 1
            } } }
        ) }

    val mergedVariableSegments = variableSegments
        .groupBy { it.segment.value }
        .map { entry -> (entry.value.firstOrNull{ it.component != null }?: entry.value.first()) x entry.value }
        .map{ pair -> Routes(
            segment = pair.first.segment,
            component = pair.first.component,
            children = pair.second.map { it.children }.flatten().let{ it.sortedBy { r -> when(r.segment){
                is RouteSegment.Static -> 0
                else -> 1
            } } }
        ) }

    return arrayListOf(
        *mergedStaticSegments.toTypedArray(),
        *mergedVariableSegments.toTypedArray()
    )
}

internal fun extract(name: String): String = name.dropWhile { it == '/' }.dropLastWhile { it == '/' }


internal fun Routes.wrap(wrap:  @Composable (ComposableRoute.(@Composable (ComposableRoute.() -> Unit)) -> @Composable (ComposableRoute.() -> Unit)) ): Routes {
    return Routes(
        segment = segment,
        children = children .map {it.wrap(wrap)}
    ) {
        if(this@wrap.component != null) {
            this.wrap(this@wrap.component)()
        }
    }

}
