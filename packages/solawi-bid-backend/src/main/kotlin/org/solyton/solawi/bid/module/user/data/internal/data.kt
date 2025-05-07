package org.solyton.solawi.bid.module.user.data.internal

import org.joda.time.DateTime
import java.util.*


data class Session(
    val sessionId: UUID,
    val userId: UUID,
    val startTime: DateTime,
    val endTime: DateTime?,
    val isActive: Boolean,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: DateTime,
    val updatedAt: DateTime
)