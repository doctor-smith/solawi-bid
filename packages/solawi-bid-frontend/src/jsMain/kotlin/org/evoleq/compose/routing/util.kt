package org.evoleq.compose.routing

import androidx.compose.runtime.Composable
import org.evoleq.math.x
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text

/**
 * Find routes by segment
 */
fun Routes.find(segment: RouteSegment): Routes? = children.find { it.segment == segment }

/**
 * Find routes by segment-name
 */
fun Routes.find(string: String): Routes? = children.find { it.segment.value == string }

/**
 * Merge route into a bunch of routes
 */
fun Routes.merge(route: Route): Routes =
    if(route.segments.isNotEmpty()) {
        val rest = route.segments.drop(1)

        when(val segment = route.segments.first()) {
            is RouteSegment.Root -> if(this.segment is RouteSegment.Root){
                merge(Route(rest, listOf()))
            } else {
                throw Exception("Cannot merge Roots into other")
            }
            else -> {
                val found = find(segment)
                val newRoutes =
                    found?.merge(Route(rest, listOf())) ?:
                    Routes(segment, listOf()).merge(Route(rest, listOf()))

                if(segment is RouteSegment.Static) {
                    this@merge.prepend(newRoutes)
                }
                if(segment is RouteSegment.Variable) {
                    this@merge.append(newRoutes)
                }
                this@merge
            }
        }
    } else {
        this
    }

/**
 * Append some routes to another bunch of routes
 */
fun Routes.append(routes: Routes): Routes = Routes(
    segment,
    listOf(
        *children.toTypedArray(),
        routes
    )
)

/**
 * Prepend some routes to another bunch of routes
 */
fun Routes.prepend(routes: Routes): Routes = Routes(
    segment,
    listOf(
        routes,
        *children.toTypedArray()
    )
)

/**
 * Append a segment to a route
 */
fun Route.append(segment: String): Route = Route(
    listOf(
        *segments.toTypedArray(),
        Segment().run(segment).result!!
    ),
    queryParameters
)

/**
 * Append a route segment to a route
 */
fun Route.append(segment: RouteSegment) = Route(
    listOf(
        *segments.toTypedArray(),
        segment
    ),
    queryParameters
)

/**
 * Append a route segment to a composable route
 */
fun ComposableRoute.append(segment: RouteSegment) = ComposableRoute(
    listOf(
        *segments.toTypedArray(),
        segment
    ),
    queryParameters,
    component
)

/**
 * Given a path, find the matching route
 */
fun Routes.match(path: String): ComposableRoute? = with(
    path.dropWhile { it == '/' }.dropLastWhile { it == '/' }.split("/").map{it.trim()}.filter { it != "" }
) {
    if(component != null) {
        if(size == 0) {
            ComposableRoute(listOf(), listOf(), component)
        } else {
            this@match.match(ComposableRoute(listOf(), listOf(), component) x this).first
        }
    } else {
        this@match.match(ComposableRoute(listOf(), listOf()){} x this).first
    }
}
/**
 * Given a path, find the matching route
 */
tailrec fun Routes.match(pair: Pair<ComposableRoute,List<String>>): Pair<ComposableRoute?, List<String>> {
    val (route, list) = pair

    if(list.isEmpty()) {
        return when(component != null) {
            true -> pair.copy(first = pair.first.copy(component = component))
            false -> null x list
        }
    }

    val (head, tail) = Pair(list.first(), list.drop(1))

    val found = find(head) ?: children.find { it.segment is RouteSegment.Variable }
    // return found?.match(route.append(head) x tail) ?: (null x list) -> not tailrec
    return if(found == null) {
        null x list
    } else {
        found.match(
            route.append(
                when(val s = found.segment){
                    is RouteSegment.Static -> s
                    is RouteSegment.Variable -> s.copy(value = head)
                    else -> s
                }
            ) x tail
        )
    }
}

/**
 * Get the value behind a parameter
 */
fun ComposableRoute.parameter(name: String): String? = segments.filterIsInstance<RouteSegment.Variable>().find { it.name == name }?.value

/**
 * Render component with a certain path
 */
@RoutingDsl
@Composable
fun Routes.compose(path: String): Boolean = with(match(path) ) {
    if(this != null) {
        val route = this
        route.component()
        true
    } else {
        // Page404()
        false
    }
}

@RoutingDsl
@Composable
fun Page404() {
    H1{ Text("404 - Page not found") }
}
