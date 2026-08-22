package org.evoleq.exposedx.iql

enum class EntityNameStrategy {
    EXACT,
    LOWER_CASE,
    UPPER_CASE,
    PASCAL_CASE,
    CAMEL_CASE
}

enum class FieldNameStrategy {
    EXACT,
    SNAKE_CASE
}


fun EntityNameStrategy.apply(name: String): String =
    when (this) {
        EntityNameStrategy.EXACT ->
            name

        EntityNameStrategy.LOWER_CASE ->
            name.lowercase()

        EntityNameStrategy.UPPER_CASE ->
            name.uppercase()

        EntityNameStrategy.PASCAL_CASE ->
            name.toPascalCase()

        EntityNameStrategy.CAMEL_CASE ->
            name.toCamelCase()
    }

private fun String.words(): List<String> =
    split(
        Regex("[\\s_\\-.]+")
    )
        .filter { it.isNotEmpty() }

fun String.toPascalCase(): String =
    words().joinToString("") {
        it.replaceFirstChar(Char::uppercaseChar)
    }

fun String.toCamelCase(): String =
    toPascalCase()
        .replaceFirstChar(Char::lowercaseChar)
