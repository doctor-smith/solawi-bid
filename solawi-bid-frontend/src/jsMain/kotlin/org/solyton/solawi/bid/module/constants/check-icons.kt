package org.solyton.solawi.bid.module.constants


const val CHECK_TRUE = "☑\uFE0F"
const val CHECK_FALSE ="❌"

fun Boolean?.checkIcon(onNull: String = CHECK_FALSE): String = when(this) {
    null -> onNull
    true -> CHECK_TRUE
    false -> CHECK_FALSE
}
