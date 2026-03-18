package org.solyton.solawi.bid.module.application.data

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.permission.data.ContextName
import org.solyton.solawi.bid.module.values.Description

@Serializable
data class CreateModuleOnTheFly(
    val name: ModuleName,
    val context: ContextName,
    val description: Description,
    val isMandatory: Boolean = false,
)
@Serializable
data class CreateModule(
    val name: ModuleName,
    val context: ContextName,
    val description: Description,
    val applicationId: ApplicationId,
    val isMandatory: Boolean = false,
)

@Serializable
data class UpdateModule(
    val id: ModuleId,
    val applicationId: ApplicationId,
    val name: ModuleName,
    val context: ContextName,
    val description: Description,
    val isMandatory: Boolean,
)

@Serializable
data class DeleteModule(
    val id: ModuleId,
)
