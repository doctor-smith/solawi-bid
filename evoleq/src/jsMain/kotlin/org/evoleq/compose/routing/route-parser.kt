package org.evoleq.compose.routing

import org.evoleq.math.x
import org.evoleq.parser.*

@Suppress("FunctionName")
fun Param(): Parser<Parameter> = SplitAtFirst('=') map {
    Parameter(it.first,it.second)
}

@Suppress("FunctionName")
fun Params(): Parser<List<Parameter>> = Split(';') map {
    it.map { string -> requireNotNull(Param().run(string).result) {"Parameter requirement not satisfied: $string"} }
}

@Suppress("FunctionName")
fun Segment(): Parser<RouteSegment> =
    (FirstMatches(':') * { Parser{ s -> Result(RouteSegment.Variable(s) as RouteSegment, "") } }) OR
            Parser { s ->  Result(RouteSegment.Static(s) as RouteSegment,"") }

@Suppress("FunctionName")
fun Segments(): Parser<List<RouteSegment>> = Split('/') map { list -> list.map { requireNotNull(Segment().run(it).result) {
    "Segment requirements not satisfied: $it"
} } }

@Suppress("FunctionName")
fun RouteParser(): Parser<Route> = SplitAtFirst('?') map {
        pair ->
    val (path, params) = pair
    Segments().run(path) x Params().run(params)
} map {
    Route(
        requireNotNull(it.first.result) {"Path requirements not satisfied: ${it.first.result}"},
        requireNotNull(it.second.result){"Parameter requirements not satisfied: ${it.second.result}"}
    )
}
