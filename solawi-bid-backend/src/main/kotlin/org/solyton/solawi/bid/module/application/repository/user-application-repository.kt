package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.*
import org.solyton.solawi.bid.module.application.schema.LifecycleStageEntity
import java.util.UUID

fun Transaction.registerForApplication(userId: UUID, applicationId: UUID): UserApplication {
    val application = Application.find{ Applications.id eq applicationId }.firstOrNull()
        ?: throw ApplicationException.NoSuchApplication(applicationId.toString())

    val registrationPossible = UserApplication.find {
        (UserApplications.applicationId eq applicationId) and
        (UserApplications.userId eq userId)
    }.empty()

    if(!registrationPossible) throw ApplicationException.ApplicationRegistrationImpossible(userId.toString(), applicationId.toString())

    val registeredStage = LifecycleStageEntity.find { LifecycleStages.name eq "REGISTERED" }.firstOrNull()
        ?: throw ApplicationException.NoSuchLifecycleStage("REGISTERED")

    return UserApplication.new{
        this.userId = userId
        this.application = application
        lifecycleStage = registeredStage
        createdBy = userId
    }
}

fun Transaction.moveLifecycleStage(application: UserApplication, toId: UUID, modifierId: UUID): UserApplication {
    val toStage = transitionTargetOf(application.lifecycleStage.id.value, toId)

    application.lifecycleStage = toStage
    application.modifiedBy = modifierId
    application.modifiedAt = DateTime.now()

    return application
}
