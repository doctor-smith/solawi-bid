package org.evoleq.language

sealed class Locale(open val value: String) {
    data object De : Locale("de")
    data object En : Locale("en")

    data object Iso : Locale("iso")

    override fun toString(): String = value

    companion object {
        fun from(locale: String) = when (locale) {
            De.value -> De
            En.value -> En
            Iso.value -> Iso
            else -> throw IllegalArgumentException("Unknown locale $locale")
        }
    }
}