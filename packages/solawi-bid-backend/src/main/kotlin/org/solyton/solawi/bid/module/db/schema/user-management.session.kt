package org.solyton.solawi.bid.module.db.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import java.util.*

object Sessions : UUIDTable("sessions"){
    val userId = reference("user_id",Users) // Foreign key to users table
    val startTime = datetime("start_time").default(DateTime.now()) // Session start time
    val endTime = datetime("end_time").nullable() // Session end time, nullable if session is active
    val isActive = bool("is_active").default(true) // Flag indicating if the session is active
    val ipAddress = varchar("ip_address", 45).nullable() // IP address from which the session started
    val userAgent = varchar("user_agent", 255).nullable() // User agent string
    val createdAt = datetime("created_at").default(DateTime.now()) // Timestamp of record creation
    val updatedAt = datetime("updated_at").default(DateTime.now()) // Timestamp of the last update
}

class Session(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Session>(Sessions)

    var user by User referencedOn Users.id
    var startTime by Sessions.startTime
    var entTime by Sessions.endTime
    var isActive by Sessions.isActive
    var ipAddress by Sessions.ipAddress
    var userArray by Sessions.userAgent
    var createdAt by Sessions.createdAt
    var updatedAt by Sessions.updatedAt
}