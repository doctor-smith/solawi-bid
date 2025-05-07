package org.solyton.solawi.bid.module.bid.service

import org.evoleq.language.Locale
import kotlin.math.pow
import kotlin.math.roundToInt


fun Double.roundTo(precision: Int): Double = with(10.0.pow(precision)) {
    (this@roundTo * this).roundToInt().toDouble() / this
}


fun String.isDouble(locale: Locale): Boolean = when(locale){
    is Locale.En, is Locale.Iso -> Regex("^-?\\d*\\.?\\d+\$").matches(this)
    is Locale.De -> Regex("^-?\\d*,?\\d+\$").matches(this)
}

fun String.isDouble(): Boolean =
    isDouble(Locale.En) ||
    isDouble(Locale.De) ||
    isDouble(Locale.Iso)

fun String.isDecimal(precision: Int): Boolean =
    with(replace(",",".")) { when {
        !isDouble() -> false
        !contains(".") ->true
        else -> split(".")[1].length <= precision
    } }


fun String.toDecimal(): Double = when{
    isDouble() -> replace(",", ".").toDouble()
    else -> throw Exception("Not a Double")
}

fun <T > onNullEmpty(value: T?, manipulate: (String)->String = {s -> s}): String = when{
    value == null -> ""
    else -> manipulate("$value")
}

fun String.toPrice(): String = when{
    !contains(".") -> "$this.00"
    else -> this
}