package org.evoleq.uuid

import com.benasher44.uuid.uuidFrom

const val NIL_UUID = "00000000-0000-0000-0000-000000000000"

fun String.isUuid(): Boolean = try {
    uuidFrom(this)
    true
} catch(ignore: Exception) {
    false
}
