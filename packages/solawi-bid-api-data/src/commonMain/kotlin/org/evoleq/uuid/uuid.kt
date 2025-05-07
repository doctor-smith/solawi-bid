package org.evoleq.uuid

import com.benasher44.uuid.uuidFrom


fun String.isUuid(): Boolean = try {
    uuidFrom(this)
    true
} catch(ignore: Exception) {
    false
}