package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import java.util.UUID

fun Transaction.createApplication(applicationName: String, applicationDescription: String, creator: UUID): ApplicationEntity  {
    val appExists = !ApplicationEntity.find { ApplicationsTable.name eq applicationName }.empty()
    if(appExists) throw ApplicationException.DuplicateApplicationName(applicationName)

    return ApplicationEntity.new {
        name = applicationName
        description = applicationDescription
        createdBy = creator
    }
}

fun Transaction.updateApplication(applicationId: UUID, newName: String, newDescription: String, modifier: UUID): ApplicationEntity {
    val application = ApplicationEntity.find { ApplicationsTable.id eq applicationId }.firstOrNull()
        ?:throw ApplicationException.NoSuchApplication(applicationId.toString())

    if(newName == application.name && newDescription == application.description) return application

    if(application.hasDuplicateApplicationName(newName)) throw ApplicationException.DuplicateApplicationName(newName)

    application.name = newName
    application.description = newDescription
    application.modifiedBy = modifier
    application.modifiedAt = DateTime.now()

    return application
}

fun Transaction.createModule(moduleName: String, moduleDescription: String, applicationId: UUID, creator: UUID): ModuleEntity  {
    val baseApplication = ApplicationEntity.find { ApplicationsTable.id eq applicationId }.firstOrNull()
        ?: throw ApplicationException.NoSuchApplication(applicationId.toString())

    val modules = ModuleEntity.find { ModulesTable.name eq moduleName }.filter{ it.application.id.value == applicationId }
    if(modules.count() > 0) throw ApplicationException.DuplicateModuleName(moduleName, modules.first().application.name)


    return ModuleEntity.new {
        name = moduleName
        description = moduleDescription
        application = baseApplication
        createdBy = creator
    }
}

fun Transaction.updateModule(
    moduleId: UUID,
    newName: String,
    newDescription: String,
    newApplicationId: UUID,
    modifier: UUID
): ModuleEntity {
    val module = ModuleEntity.find { ModulesTable.id eq moduleId }.firstOrNull()
        ?:throw ApplicationException.NoSuchModule(moduleId.toString())

    if(
        newName == module.name &&
        newDescription == module.description &&
        newApplicationId == module.application.id.value
    ) return module

    val targetApplication = ApplicationEntity.find { ApplicationsTable.id eq newApplicationId }.firstOrNull()
        ?:throw ApplicationException.NoSuchApplication(newApplicationId.toString())

    if(module.hasDuplicateModuleName(newName, newApplicationId)) {
        throw ApplicationException.DuplicateModuleName(newName, targetApplication.name)
    }

    module.name = newName
    module.description = newDescription
    module.application = targetApplication
    module.modifiedBy = modifier
    module.modifiedAt = DateTime.now()

    return module
}

fun ModuleEntity.hasDuplicateModuleName(newName: String, newApplicationId: UUID): Boolean = !when{
    newApplicationId == application.id.value -> ModuleEntity.find {
        ModulesTable.id neq id and
        (ModulesTable.name eq newName) and
        (ModulesTable.applicationId eq newApplicationId)
    }.empty()
    else -> ModuleEntity.find {
        (ModulesTable.name eq newName) and
        (ModulesTable.applicationId eq newApplicationId)
    }.empty()
}

fun ApplicationEntity.hasDuplicateApplicationName(newName: String): Boolean =
    !ApplicationEntity.find { ApplicationsTable.id neq id and (ApplicationsTable.name eq newName)}.empty()
