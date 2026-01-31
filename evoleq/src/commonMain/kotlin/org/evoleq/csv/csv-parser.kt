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

/**
 * Parses a CSV string with grouped headers and maps its data into a structured list of maps.
 *
 * The CSV is required to have at least two lines:
 * - The first line defines header groups (areas), with the start of a group marked by a non-empty value.
 * - The second line provides field headers for each group.
 * The subsequent lines represent data rows, where fields are matched to their respective headers and areas.
 *
 * @param csv The CSV content to be parsed as a string.
 * @param delimiter The delimiter used to separate fields in the CSV. Defaults to ";" if not provided.
 * @return A list of maps where the keys are composed of the area and corresponding header (formatted as "area.header")
 * and the values are the associated data fields from the CSV.
 * Each map represents the parsed data of one row.
 */
fun parseCsvWithGroupedHeaders(
    csv: String,
    delimiter: String = ";"
): List<Map<String, Map<String, String>>> {
    val lines = csv.split("\r\n", "\r", "\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { it.split(delimiter).map { cell -> cell.trim() } }

    require(lines.size >= 2) { "Expecting at least 2 lines (Areas + Header line)." }

    val areasRow = lines[0]
    val headersRow = lines[1]

    val areaGroups: List<List<String>> = areasRow.groupOnNonEmptyStart()
    val chunkDefs: IntArray = areaGroups.map { it.size }.toIntArray()

    val headerGroups: List<List<String>> = headersRow.chunkBySizes(*chunkDefs)
    require(headerGroups.size == areaGroups.size) { "Header groups do not match area groups." }

    val dataRows = lines.drop(2)

    return dataRows.map { row ->
        val valueGroups: List<List<String>> = row.chunkBySizes(*chunkDefs)

        buildMap<String, Map<String, String>> {
            for (i in areaGroups.indices) {
                val areaName = areaGroups[i].first()
                val headers = headerGroups[i]
                val values = valueGroups[i]

                val inner = buildMap<String, String> {
                    headers.zip(values).forEach { (h, v) ->
                        if (h.isNotEmpty()) put(h, v) // optional: skip empty headers
                    }
                }

                put(areaName, inner)
            }
        }
    }
}

/**
 * Groups elements of a list into sublists based on non-empty starting elements.
 * Each new sublist starts with the first encountered non-empty string, and all subsequent empty strings
 * are grouped into the current sublist until another non-empty string is found.
 *
 * If the list contains leading empty elements, they are ignored. If all elements are empty,
 * the result will be an empty list. Empty strings occurring between or after non-empty elements
 * are included in their respective groups.
 *
 * @return A list of sublists, where each sublist contains a group initiated by a non-empty string
 * followed by its associated empty strings, if any.
 */
fun List<String>.groupOnNonEmptyStart(): List<List<String>> {
    val result = mutableListOf<MutableList<String>>()
    var current: MutableList<String>? = null

    for (s in this) {
        val isStart = s.isNotEmpty()
        if (isStart) {
            current = mutableListOf(s)
            result += current
        } else {
            // empty belongs to current group (if one was already started)
            if (current != null) current += s
            // if leading empties occur: ignore them (or alternatively create own group)
        }
    }
    return result
}

/**
 * Splits the list into a series of sublists based on the given sizes.
 * Each size specifies the number of elements in the corresponding chunk.
 * The sum of the provided sizes must equal the size of the list, and all sizes must be non-negative.
 *
 * @param sizes Vararg parameter specifying the size of each chunk. All values must be non-negative.
 * @return A list of sublists where each sublist has the size defined by the corresponding value in the sizes parameter.
 * @throws IllegalArgumentException if any size is negative, if the sum of sizes exceeds the list size,
 * or if unprocessed elements remain after splitting.
 */
fun <T> List<T>.chunkBySizes(vararg sizes: Int): List<List<T>> {
    require(sizes.all { it >= 0 }) { "sizes must be >= 0" }

    val result = ArrayList<List<T>>(sizes.size)
    var index = 0

    for (size in sizes) {
        require(index + size <= this.size) {
            "Too few elements: need ${index + size}, have ${this.size}"
        }
        result += this.subList(index, index + size)
        index += size
    }

    require(index == this.size) {
        "Remaining elements: processed $index of ${this.size}"
    }

    return result
}
