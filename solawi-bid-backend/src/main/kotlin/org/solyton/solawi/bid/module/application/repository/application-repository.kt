package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import java.util.UUID

fun Transaction.createApplication(applicationName: String, applicationDescription: String, creator: UUID, isMandatory: Boolean = false): ApplicationEntity  {
    val appExists = !ApplicationEntity.find { ApplicationsTable.name eq applicationName }.empty()
    if(appExists) throw ApplicationException.DuplicateApplicationName(applicationName)

    return ApplicationEntity.new {
        name = applicationName
        description = applicationDescription
        this.isMandatory = isMandatory
        createdBy = creator
    }
}

fun Transaction.updateApplication(applicationId: UUID, newName: String, newDescription: String, modifier: UUID,  isMandatory: Boolean = false): ApplicationEntity {
    val application = ApplicationEntity.find { ApplicationsTable.id eq applicationId }.firstOrNull()
        ?:throw ApplicationException.NoSuchApplication(applicationId.toString())

    if(newName == application.name && newDescription == application.description) return application

    if(application.hasDuplicateApplicationName(newName)) throw ApplicationException.DuplicateApplicationName(newName)

    application.name = newName
    application.description = newDescription
    application.isMandatory = isMandatory
    application.modifiedBy = modifier
    application.modifiedAt = DateTime.now()

    return application
}
