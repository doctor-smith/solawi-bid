package org.evoleq.csv

fun parseCsv(csv: String, delimiter: String = ";"): List<Map<String, String>> {
    val lines = csv.split("\r\n", "\r", "\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.split(delimiter).map { i -> i.trim() }}

    val (headers, data) = Pair(lines.first(), lines.drop(1).map { it.toTypedArray() })
    val result = data.map {
        mapOf(*headers.mapIndexed { index, s ->  s to it[index]}.toTypedArray())
    }
    return  result
}