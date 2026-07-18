package org.solyton.solawi.bid.module.user.schema

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.auditable.AuditableEntity
import org.solyton.solawi.bid.module.auditable.AuditableUUIDTable
import java.util.*

object UserOrganizationHistory : AuditableUUIDTable("user_organization_history") {
    val userOrganizationId = reference("user_organization_id", UserOrganization)
    val status = enumerationByName<MembershipStatus>("membership_status", 20, MembershipStatus::class).default(MembershipStatus.APPLICANT)
    val since = datetime("since").default(DateTime.now())
}

class UserOrganizationHistoryEntry(id: EntityID<UUID>) : UUIDEntity(id), AuditableEntity<UUID> {
    companion object : UUIDEntityClass<UserOrganizationHistoryEntry>(UserOrganizationHistory)

    var userOrganizationId by UserOrganizationHistory.userOrganizationId
    var status by UserOrganizationHistory.status
    var since by UserOrganizationHistory.since

    override var createdAt: DateTime by UserOrganizationHistory.createdAt
    override var createdBy: UUID by UserOrganizationHistory.createdBy
    override var modifiedBy: UUID? by UserOrganizationHistory.modifiedBy
    override var modifiedAt: DateTime? by UserOrganizationHistory.modifiedAt
}


enum class MembershipStatus {
    APPLICANT,
    ACTIVE,
    PAUSED,
    FORMER,
    REJECTED
}
