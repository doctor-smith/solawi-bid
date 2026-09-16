package org.solyton.solawi.bid.module.application.data

import kotlinx.serialization.Serializable
import org.solyton.solawi.bid.module.permission.data.api.Role

@Serializable
data class UpdateStandardApplicationContext(
    val applicationId: ApplicationId,
    val roles: List<Role>
)

@Serializable
data class UpdateStandardModuleContext(
    val moduleId: ModuleId,
    val roles: List<Role>
)

