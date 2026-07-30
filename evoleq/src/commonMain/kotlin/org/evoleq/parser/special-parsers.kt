package org.evoleq.parser

import org.evoleq.math.x

val All: Parser<String> = Parser { s -> Result(s, "") }

val First: Parser<Char> = Parser {
    s -> when{
        s.isEmpty() -> Result(null,"")
        else -> Result(
            s.first(),
            s.drop(1)
        )
    }
}

val FirstMatches: (Char)-> Parser<Char> = { symbol:Char -> First * { first:Char ->
    when(symbol == first){
        true -> Parser { s -> Result(first, s) }
        false -> Fail()
    }
}}

val StartsWith: (String)-> Parser<String> = { symbols ->
    symbols.map { FirstMatches(it) }
        .sequenceA()
        .map { list -> list.joinToString("") { "$it" } }
}

val Whitespace: Parser<String> = First * { s -> when("$s".isBlank()){
    true -> Parser { Result("", it) }
    else -> Fail()
} }

@Suppress("FunctionName")
fun DropAllWhitespace(): Parser<String> = Parser { input ->
    var i = 0
    while (i < input.length && "${input[i]}".isBlank()) i++
    Result("", input.substring(i))
}

val Newline: Parser<String> = First * {
    s -> when("$s" == "\n") {
        true -> Parser { Result("", it) }
        false -> Fail()
    }
}

@Suppress("FunctionName")
fun DropAllNewline(): Parser<String> = Parser { input ->
    var i = 0
    while (i < input.length && input[i] == '\n') i++
    Result("", input.substring(i))
}


val When: ((Char)->Boolean)-> Parser<Char> = {
    predicate -> First * { first ->
        when(predicate(first)){
            true -> Parser { state ->
                Result(
                first,
                state
            )
            }
            false -> Fail()
        }
    }
}

val Drop: Parser<String> = First map {""} OR Succeed("")

@Suppress("FunctionName")
fun CollectWhile(predicate:(Char)->Boolean): Parser<String> = Parser { input ->
    var i = 0
    while (i < input.length && predicate(input[i])) i++
    Result(input.substring(0, i), input.substring(i))
}


@Suppress("FunctionName")
fun DropWhile(predicate:(Char)->Boolean): Parser<String> = Parser { input ->
    var i = 0
    while (i < input.length && predicate(input[i])) i++
    Result("", input.substring(i))
}

@Suppress("FunctionName")
fun Between(left: Char, right: Char): Parser<String> = FirstMatches(left) dL CollectWhile { it != right } dR FirstMatches(right)


@Suppress("FunctionName")
fun BetweenNested(left: Char, right: Char): Parser<String> =
    Balance(left, right) map { it.drop(1).dropLast(1)}

@Suppress("FunctionName")
fun BalanceBase(left: Char, right: Char): Parser<Pair<Int, String>> =
    First * {first ->
        when (first) {
            left -> ReturnParser(1 x "$left")
            right -> ReturnParser(-1 x "$right")
            else -> ReturnParser(0 x "$first")
        }
    }

@Suppress("FunctionName")
fun Balance(left: Char, right: Char, pair: Pair<Int, String>): Parser<String> = Parser { input ->
    var depth = pair.first
    val sb = StringBuilder(pair.second)
    var i = 0
    while (i < input.length && depth != 0) {
        val c = input[i]
        sb.append(c)
        when (c) {
            left -> depth++
            right -> depth--
        }
        i++
    }
    if (depth == 0) Result(sb.toString(), input.substring(i)) else Result(null, input)
}

@Suppress("FunctionName")
fun Balance(left: Char, right: Char): Parser<String> = Parser { input ->
    if (input.isEmpty() || input[0] != left) return@Parser Result(null, input)
    var depth = 1
    val sb = StringBuilder().append(left)
    var i = 1
    while (i < input.length && depth != 0) {
        val c = input[i]
        sb.append(c)
        when (c) {
            left -> depth++
            right -> depth--
        }
        i++
    }
    if (depth == 0) Result(sb.toString(), input.substring(i)) else Result(null, input)
}

@Suppress("FunctionName")
fun SplitAtFirst(separator: Char) = seqA(
    CollectWhile { it != separator },
    Drop
).map { list -> list[0]} * {
        start -> All map { rest -> Pair(start, rest)  }
}

@Suppress("FunctionName", "CognitiveComplexMethod")
fun Split(separator: Char): Parser<List<String>> = Parser { input ->
    if (input.isEmpty()) return@Parser Result(emptyList(), input)
    val segments = mutableListOf<String>()
    var i = 0
    while (i < input.length) {
        // drop leading separators
        while (i < input.length && input[i] == separator) i++
        if (i >= input.length) break
        val start = i
        while (i < input.length && input[i] != separator) i++
        segments.add(input.substring(start, i))
        // drop one separator (matches original `Drop` after the segment)
        if (i < input.length) i++
    }
    Result(segments, "")
}
