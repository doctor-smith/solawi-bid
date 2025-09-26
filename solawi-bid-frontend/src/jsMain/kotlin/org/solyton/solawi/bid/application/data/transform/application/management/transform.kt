package org.solyton.solawi.bid.application.data.transform.application.management

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.module.application.data.management.ApplicationManagement

val applicationManagementModule: Lens<Application, ApplicationManagement> by lazy {
    Lens(
        get = { whole -> preApplicationManagement.get(whole).copy(actions = whole.actions * preApplicationManagement ) },
        set = preApplicationManagement.set
    )
}

private val preApplicationManagement: Lens<Application, ApplicationManagement> by lazy {
    Lens(
        get = {whole -> ApplicationManagement(
            deviceData = whole.deviceData,
            modals = whole.modals,
            i18n = whole.i18N,
            environment = whole.environment.useI18nTransform(),
            availableApplications = whole.availableApplications,
            personalApplications = whole.personalApplications,
            personalApplicationContextRelations = whole.personalApplicationContextRelations,
            personalModuleContextRelations = whole.personalModuleContextRelations,
            userApplications = whole.userApplications
        )},
        set = {part -> {whole -> whole.copy(
            i18N = part.i18n,
            modals = part.modals,
            availableApplications = part.availableApplications,
            personalApplications = part.personalApplications,
            personalApplicationContextRelations = part.personalApplicationContextRelations,
            personalModuleContextRelations = part.personalModuleContextRelations,
            userApplications = part.userApplications
        )}}
    )
}
