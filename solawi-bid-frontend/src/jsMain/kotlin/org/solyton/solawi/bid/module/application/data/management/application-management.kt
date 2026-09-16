package org.solyton.solawi.bid.module.application.data.management

import org.evoleq.axioms.definition.Lensify
import org.evoleq.axioms.definition.ReadOnly
import org.evoleq.axioms.definition.ReadWrite
import org.evoleq.compose.modal.Modals
import org.evoleq.device.data.Device
import org.evoleq.optics.storage.ActionDispatcher
import org.solyton.solawi.bid.module.application.data.application.Application
import org.solyton.solawi.bid.module.application.data.organizationrelation.ApplicationOrganizationRelation
import org.solyton.solawi.bid.module.application.data.userapplication.UserApplications
import org.solyton.solawi.bid.module.i18n.data.Environment
import org.solyton.solawi.bid.module.i18n.data.I18N
import org.solyton.solawi.bid.module.permissions.data.Permissions
import org.solyton.solawi.bid.module.permissions.data.relations.ContextRelation


@Lensify
data class ApplicationManagement(
    @ReadOnly val actions: ActionDispatcher<ApplicationManagement> = ActionDispatcher {  },
    @ReadOnly val deviceData: Device = Device(),
    @ReadOnly val environment: Environment,
    @ReadWrite val modals: Modals<Int> = mapOf(),
    @ReadWrite val i18n: I18N = I18N(),
    @ReadWrite val availableApplications: List<Application> = listOf(),
    @ReadWrite val availablePermissions: Permissions = Permissions(),
    @ReadWrite val personalApplications: List<Application> = listOf(),
    @ReadWrite val personalApplicationContextRelations: List<ContextRelation> = listOf(),
    @ReadWrite val personalModuleContextRelations: List<ContextRelation> = listOf(),
    @ReadWrite val userApplications: List<UserApplications> = listOf(),
    @ReadWrite val applicationOrganizationRelations: List<ApplicationOrganizationRelation> = listOf(),
)
