package org.solyton.solawi.bid.application.data.transform.user

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.module.user.data.Application as UserModule

val userIso: Lens<Application, UserModule> by lazy {
    Lens(
        get = { whole -> preUserIso.get(whole).copy(actions = whole.actions * preUserIso) },
        set = preUserIso.set
    )
}

private val preUserIso: Lens<Application, UserModule> by lazy {
    Lens(
        get = { whole -> UserModule(
            actions = ActionDispatcher { },
            deviceData = whole.deviceData,
            modals = whole.modals,
            i18n = whole.i18N,
            environment = whole.environment.useI18nTransform(),
            user = whole.userData,
            managedUsers = whole.managedUsers,
            availablePermissions = whole.availablePermissions
        ) },
        set = { part -> { whole -> whole.copy(
            modals = part.modals,
            i18N = part.i18n,
            userData = part.user,
            managedUsers = part.managedUsers,
            availablePermissions = part.availablePermissions
        ) } }
    )
}
