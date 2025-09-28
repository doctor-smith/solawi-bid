package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import java.util.*

fun Transaction.createApplication(
    applicationName: String,
    applicationDescription: String,
    creator: UUID,
    isMandatory: Boolean = false,
    defaultContextId: UUID? = null
): ApplicationEntity  {
    val appExists = !ApplicationEntity.find { ApplicationsTable.name eq applicationName }.empty()
    if(appExists) throw ApplicationException.DuplicateApplicationName(applicationName)

    val defaultContext = getDefaultContext(defaultContextId)

    return ApplicationEntity.new {
        name = applicationName
        description = applicationDescription
        this.isMandatory = isMandatory
        this.defaultContext = defaultContext
        createdBy = creator
    }
}

fun Transaction.updateApplication(
    applicationId: UUID,
    newName: String,
    newDescription: String,
    modifier: UUID,
    // todo:dev it is not good to provide default values in update functions -> refactor!
    isMandatory: Boolean,// = false,
    newDefaultContextId: UUID?
): ApplicationEntity {
    val application = ApplicationEntity.find { ApplicationsTable.id eq applicationId }.firstOrNull()
        ?:throw ApplicationException.NoSuchApplication(applicationId.toString())

    @Suppress("ComplexCondition") if(
        newName == application.name &&
        newDescription == application.description &&
        isMandatory == application.isMandatory &&
        newDefaultContextId == application.defaultContext.id.value
    ) return application

    if(application.hasDuplicateApplicationName(newName)) throw ApplicationException.DuplicateApplicationName(newName)

    application.name = newName
    application.description = newDescription
    application.isMandatory = isMandatory

    if(newDefaultContextId != application.defaultContext.id.value) {
        application.defaultContext = getDefaultContext(newDefaultContextId)
    }

    application.modifiedBy = modifier
    application.modifiedAt = DateTime.now()

    return application
}

fun ApplicationEntity.hasDuplicateApplicationName(newName: String): Boolean =
    !ApplicationEntity.find { ApplicationsTable.id neq id and (ApplicationsTable.name eq newName)}.empty()
