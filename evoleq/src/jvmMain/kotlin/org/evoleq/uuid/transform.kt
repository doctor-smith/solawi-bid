package org.evoleq.uuid

import java.util.UUID

fun String?.toUuidOrNull(): UUID? = when{
    this == null -> null
    else -> UUID.fromString(this)
}

fun String.toUuid(): UUID = UUID.fromString(this)
