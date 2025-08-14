package org.evoleq.compose

import org.evoleq.compose.routing.*
import org.evoleq.parser.Result
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.testutils.ComposeWebExperimentalTestsApi
import org.jetbrains.compose.web.testutils.runTest
import kotlin.test.*

@OptIn(ComposeWebExperimentalTestsApi::class)
class RoutingTest {

    @Test
    fun parseRoute() {
        val routeString = "/aaa/bbb/123/:id?x=y;a=b"
        val result = RouteParser().run(routeString)

        assertEquals(
            Result(
                Route(
                listOf(
                    RouteSegment.Static("aaa"),
                    RouteSegment.Static("bbb"),
                    RouteSegment.Static("123"),
                    RouteSegment.Variable("id")
                ),
                listOf(
                    Parameter("x", "y"),
                    Parameter("a","b")
                )
            ), ""),
            result
        )
    }

     @Test
    fun routesConfiguration() {
        val routes = org.evoleq.compose.routing.routing {
            route("x/y/z") {
                route(":id") {
                    route("a") {
                        component { Div { Text("Hello") } }
                    }
                    route("b") {}
                    route("c") {}
                }
                route("h/i/j/k") {}
            }
            route("alfred/E/neumann") {}
        }
        assertTrue(routes != null)
    }

    @Test
    fun routesConfigurationMerge() {
        val routes = routing{
            route("x/a"){component { Div{Text("x/a")} }}
            route("x/b"){component { Div{Text("x/b")} }}
        }
        val n1 = routes.children.size
        assertEquals(1,n1)

        val x = routes.match("x")
        assertNull(x)

        val a = routes.match("x/a")
        assertNotNull(a)

        val b = routes.match("x/b")
        assertNotNull(b)
    }

    @Test
    fun matchRoute() {
        val routes = org.evoleq.compose.routing.routing {
            component {
                Div { Text("Root") }
            }
            route("x") {
                route(":id") {
                    component {
                        Text("Hello")
                    }
                }
                route("y") {
                    component {

                    }
                }
            }
        }
        assertNotNull(routes.component)

        val root = routes.match("")
        assertNotNull(root)

        val r0 = routes.match("/x")
        assertNull(r0)

        val r1 = routes.match("/x/1")
        assertNotNull(r1)

        val r2 = routes.match("x/y")
        assertNotNull(r2)

        val r3 = routes.match("x/y/z")
        assertNull(r3)
    }

    @Test
    fun compose() = runTest{
        val routes = org.evoleq.compose.routing.routing {
            route("x") {
                component {
                    Div { Text("Hello") }
                }
                route(":id") {
                    component {
                        Div { Text("Hello ${parameter("id")}") }
                    }
                }
            }
        }

        composition{
            routes.compose("/x")
        }

        assertEquals("<div>Hello</div>", root.innerHTML)

        composition{
            routes.compose("x/1")
        }

        assertEquals("<div>Hello 1</div>", root.innerHTML)
    }

    @Test
    fun routing() = runTest {

        composition {
            Routing("/"){
                component { Div{ Text("ROOT") } }
                route("/x"){
                    component { Div{ Text("x")} }
                }
                route("/y"){
                    component { Div{ Text("y")} }
                }
            }
        }

        // assertEquals("<div>ROOT</div>", root.innerHTML)

        navigate("/x")
        waitForRecompositionComplete()
        assertEquals("<div>x</div>", root.innerHTML)

        navigate("/y")
        waitForRecompositionComplete()
        assertEquals("<div>y</div>", root.innerHTML)

        navigate("/")
        waitForRecompositionComplete()
        assertEquals("<div>ROOT</div>", root.innerHTML)
    }





    @Test
    fun wrapRoutes() = runTest {
        composition {
            Routing("/") {
                component {
                    Div {
                        Text("ROOT")
                    }
                }
                route("/x") {
                    component { Div { Text("x") } }
                }

                wrap {
                    layout {
                        {
                            Div {
                                Text("hello wrapper")
                                it()
                            }
                        }
                    }

                    route("/y") {
                        component {
                            Div { Text("y") }
                        }

                        route("z") {
                            component { Div { Text("z") } }
                        }
                    }
                }
            }
        }
        navigate("/x")
        waitForRecompositionComplete()
        assertEquals("<div>x</div>", root.innerHTML)

        // Checks that wrap lifts child configurations
        // and
        // 1. wraps component y
        // 2. wraps child component z as well
        navigate("/y")
        waitForRecompositionComplete()
        assertEquals("<div>hello wrapper<div>y</div></div>", root.innerHTML)
        navigate("/y/z")
        waitForRecompositionComplete()
        assertEquals("<div>hello wrapper<div>z</div></div>", root.innerHTML)
    }
}

