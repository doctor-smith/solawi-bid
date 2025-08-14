package org.solyton.solawi.bid.module.authentication.service

import java.util.*

fun String.isUuid(): Boolean = try {
    UUID.fromString(this)
    true
} catch (ex: Exception) {
    false
}
