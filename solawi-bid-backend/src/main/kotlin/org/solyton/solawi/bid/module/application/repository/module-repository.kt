package org.solyton.solawi.bid.module.application.repository

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.joda.time.DateTime
import org.solyton.solawi.bid.module.application.exception.ApplicationException
import org.solyton.solawi.bid.module.application.schema.ApplicationEntity
import org.solyton.solawi.bid.module.application.schema.ApplicationsTable
import org.solyton.solawi.bid.module.application.schema.ModuleEntity
import org.solyton.solawi.bid.module.application.schema.ModulesTable
import java.util.*


fun Transaction.createModule(
    moduleName: String,
    moduleDescription: String,
    applicationId: UUID,
    creator: UUID,
    isMandatory: Boolean = false,
    defaultContextId: UUID? = null
): ModuleEntity  {
    val baseApplication = ApplicationEntity.find { ApplicationsTable.id eq applicationId }.firstOrNull()
        ?: throw ApplicationException.NoSuchApplication(applicationId.toString())

    val modules = ModuleEntity.find { ModulesTable.name eq moduleName }.filter{ it.application.id.value == applicationId }
    if(modules.count() > 0) throw ApplicationException.DuplicateModuleName(moduleName, modules.first().application.name)

    val defaultContext = getDefaultContext(defaultContextId)

    return ModuleEntity.new {
        name = moduleName
        description = moduleDescription
        application = baseApplication
        this.isMandatory = isMandatory
        this.defaultContext = defaultContext
        createdBy = creator
    }
}

fun Transaction.updateModule(
    moduleId: UUID,
    newName: String,
    newDescription: String,
    newApplicationId: UUID,
    modifier: UUID,
    // todo:dev it is not good to provide default values in update functions -> refactor!
    isMandatory: Boolean,// = false,
    newDefaultContextId: UUID?
): ModuleEntity {
    val module = ModuleEntity.find { ModulesTable.id eq moduleId }.firstOrNull()
        ?:throw ApplicationException.NoSuchModule(moduleId.toString())

    @Suppress("ComplexCondition") if(
        newName == module.name &&
        newDescription == module.description &&
        newApplicationId == module.application.id.value &&
        isMandatory == module.isMandatory &&
        newDefaultContextId == module.defaultContext.id
    ) return module

    val targetApplication = ApplicationEntity.find { ApplicationsTable.id eq newApplicationId }.firstOrNull()
        ?:throw ApplicationException.NoSuchApplication(newApplicationId.toString())

    if(module.hasDuplicateModuleName(newName, newApplicationId)) {
        throw ApplicationException.DuplicateModuleName(newName, targetApplication.name)
    }

    module.name = newName
    module.description = newDescription
    module.application = targetApplication
    module.isMandatory = isMandatory
    if(newDefaultContextId == module.defaultContext.id) {
        module.defaultContext = getDefaultContext(newDefaultContextId)
    }

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

