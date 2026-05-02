package org.solyton.solawi.bid.module.banking.data.internal


data class Money(
    /**
     * The value of the money.
     * In most of the countries, the value is in cents.
     */
    val value: Long,
    val currency: Currency = Currency.EUR
)

sealed class Currency(val prc: Precision) {
    data object EUR : Currency(Precision.TWO)
    data object DOLLAR : Currency(Precision.TWO)
    data object BITCOIN : Currency(Precision.EIGHT)


}

fun Currency.toSymbol(): String = when (this) {
    Currency.EUR -> "€"
    Currency.DOLLAR -> "$"
    Currency.BITCOIN -> "₿"
}

enum class Precision {
    TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT
}

fun Precision.toInt(): Int = when (this) {
    Precision.TWO -> 2
    Precision.THREE -> 3
    Precision.FOUR -> 4
    Precision.FIVE -> 5
    Precision.SIX -> 6
    Precision.SEVEN -> 7
    Precision.EIGHT -> 8
}

fun Double.toMoney(currency: Currency = Currency.EUR): Money = when (currency) {
    Currency.EUR -> Money((this * 100).toLong(),currency)
    Currency.DOLLAR -> Money((this * 100).toLong(),currency)
    Currency.BITCOIN -> Money((this * 100000000).toLong(),currency)
}

fun Money.format(): String {
    if(value == 0L) return "0${currency.toSymbol()}"

    val stringValue = value.toString()
    val symbol = currency.toSymbol()
    val precision = currency.prc

    val integerPart = stringValue.dropLast(precision.toInt()).prettyNumber()
    val fractionalPart = stringValue.takeLast(precision.toInt()).map{superscriptDigits[it]}.joinToString("")
    val result = "$integerPart$fractionalPart$symbol"

    return result
}



val superscriptDigits = mapOf(
    '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
    '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹'
)



fun toSuperscript(number: Long): String =
    number.toString().map { superscriptDigits[it] ?: it }.joinToString("")

fun String.prettyNumber(): String = reversed().chunked(3).map { it.reversed() }.reversed().joinToString(".")
fun Long.pretty(): String = toString().prettyNumber()

