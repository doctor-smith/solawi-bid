package org.solyton.solawi.bid.module.application.data

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.permission.data.ContextName
import org.solyton.solawi.bid.module.values.Description

@Serializable
data class CreateApplication(
    val name: ApplicationName,
    val description: Description,
    val context: ContextName,
    val isMandatory: Boolean = false,
    val modules: List<CreateModuleOnTheFly> = emptyList()
)

@Serializable
data class UpdateApplication(
    val id: ApplicationId,
    val name: ApplicationName,
    val context: ContextName,
    val description: Description,
    val isMandatory: Boolean,
)

@Serializable
data class AddModulesToApplication(
    val applicationId: ApplicationId,
    val moduleIds: List<ModuleId>,
)

@Serializable
data class RemoveModulesFromApplication(
    val applicationId: ApplicationId,
    val moduleIds: List<ModuleId>,
)

@Serializable
data class DeleteApplication(
    val id: ApplicationId,
)



