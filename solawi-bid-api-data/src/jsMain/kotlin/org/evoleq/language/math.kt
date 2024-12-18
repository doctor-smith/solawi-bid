package org.evoleq.language


fun String.isDouble(locale: Locale): Boolean = when(locale){
    is Locale.En, is Locale.Iso -> Regex("^-?\\d*\\.?\\d*\$").matches(this)
    is Locale.De -> Regex("^-?\\d*,?\\d*\$").matches(this)
}