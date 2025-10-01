package org.evoleq.compose.routing

import androidx.compose.runtime.Composable
import io.ktor.util.*
import org.evoleq.compose.Markup
import org.evoleq.configuration.Configuration

// @DslMarker annotation class RoutingDsl
typealias RoutingDsl = Markup

/*
class RouteConfiguration : Configuration<Route> {

    private val segments : ArrayList<RouteSegment> = arrayListOf()
    private val queryParameters: ArrayList<Parameter> = arrayListOf()

    override fun configure(): Route = Route(
        segments,
        queryParameters
    )
}

 */

class RoutesConfiguration : Configuration<Routes> {

    lateinit var segment: RouteSegment
    var component: (@Composable ComposableRoute.()->Unit)? = null
    var wrap: (@Composable ComposableRoute.(@Composable ComposableRoute.()->Unit) -> @Composable ComposableRoute.()->Unit)? = null
    var layoutChildrenOnly: Boolean = true
    var accessGranted: Boolean = true

    val routes: ArrayList<Routes> = arrayListOf()

    override fun configure(): Routes = Routes(
        segment,
        routes.merge(),
        component
    )

    @RoutingDsl
    fun route(path: String, routesConfiguration: RoutesConfiguration.()->Unit) {
        val cleanPath = extract(path)
        if("/" in cleanPath) {
            route(path.split("/"), routesConfiguration)
        } else {
            val newSegment = Segment().run(cleanPath).result!!
            val newRoutes = with(RoutesConfiguration()) {
                routesConfiguration()
                segment = newSegment
                configure()
            }
            if (newSegment is RouteSegment.Variable) {
                routes.add(newRoutes)
            }
            if (newSegment is RouteSegment.Static) {
                routes.add(0, newRoutes)
            }
        }
    }

    private fun route(path: List<String>, routesConfiguration: RoutesConfiguration.() -> Unit) {
        if(path.size == 1) {
            route(path.first(),routesConfiguration)
        } else {
            val first = path.first()
            val tail = path.drop(1)

            val newSegment = Segment().run(first).result!!
            val newRoutes = with(RoutesConfiguration()) {
                route(tail, routesConfiguration)
                segment = newSegment
                configure()
            }
            if (newSegment is RouteSegment.Variable) {
                routes.add(newRoutes)
            }
            if (newSegment is RouteSegment.Static) {
                routes.add(0, newRoutes)
            }
        }
    }


    @RoutingDsl
    fun nonProdRoute(path: String, env: String, routesConfiguration: RoutesConfiguration.()->Unit) {
        if(env.toLowerCasePreservingASCIIRules() !in listOf( "p", "prod", "production" ) ) {
            route(path,routesConfiguration)
        }
    }


    @Markup
    fun component(content: @Composable ComposableRoute.()->Unit) {
        component = content
    }

    @Markup
    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    fun wrap(routesConfiguration: RoutesConfiguration.() -> Unit) {
        val wRC = RoutesConfiguration()

        wRC.routesConfiguration()
        val wrapper = wRC.wrap
        val wrappedRoutes = wRC.routes.map { r -> Routes(
            segment = r.segment,
            children = when{
                wrapper == null -> r.children
                else -> r.children .map { child -> child.wrap(wrapper) }
            } ) {
                    if(r.component != null && accessGranted) {
                        if (wrapper != null) {
                            this.wrapper(r.component)() }
                        else {
                            r.component!!(this)
                        }
                    }
                }
            }
        this@RoutesConfiguration.routes.addAll(wrappedRoutes)
    }

    @Markup
    fun layout(childrenOnly: Boolean = true, wrapper: @Composable ComposableRoute.(ch: @Composable ComposableRoute.()->Unit) -> @Composable ComposableRoute.()->Unit) {
        layoutChildrenOnly = childrenOnly
        wrap = wrapper

    }

    @Markup
    fun access(claim: ()->Boolean) {
        accessGranted = claim()
    }

}

@RoutingDsl
fun routing(
    routes: RoutesConfiguration.()->Unit
): Routes = with(RoutesConfiguration()) {
    routes()
    segment = RouteSegment.Root
    configure()
}
