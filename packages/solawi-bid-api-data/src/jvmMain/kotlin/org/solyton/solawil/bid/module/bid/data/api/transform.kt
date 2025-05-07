package org.solyton.solawil.bid.module.bid.data.api

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import java.util.*

fun UUID.toUuid(): Uuid = uuidFrom(toString())

fun Uuid.toUUID(): UUID = this