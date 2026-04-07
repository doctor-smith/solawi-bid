package org.solyton.solawi.bid.application.data.transform.banking

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.creditorIdentifier
import org.solyton.solawi.bid.module.banking.data.environment.Environment
import org.solyton.solawi.bid.module.banking.data.user.User


val bankingApplicationIso: Lens<Application, BankingApplication> by lazy {
    Lens<Application, BankingApplication>(
        get =  {whole -> bankingApplicationPreIso.get(whole).copy(actions = whole.actions * bankingApplicationIso)},
        set = bankingApplicationPreIso.set
    )
}

val bankingApplicationPreIso: Lens<Application, BankingApplication> by lazy {
    Lens<Application, BankingApplication>(
        get = { whole ->
            BankingApplication(
                environment = with(whole.environment) {
                    Environment(
                        type = type,
                        frontendUrl = frontendUrl,
                        frontendPort = frontendPort,
                        backendUrl = backendUrl,
                        backendPort = backendPort
                    )
                },
                actions = ActionDispatcher {  },
                modals = whole.modals,
                deviceData = whole.deviceData,
                i18N = whole.i18N,
                user = User(
                    username = whole.userData.username,
                    permissions = whole.userData.permissions,
                    organizations = whole.userData.organizations
                ),
                bankAccounts = whole.bankAccounts,
                fiscalYears = whole.fiscalYears,
                legalEntity = whole.legalEntity,
                creditorIdentifier = whole.creditorIdentifier,
                sepaModule = whole.sepaModule,
            )
        },
        set = { part -> { whole ->
            whole.copy(
                modals = part.modals,
                i18N = part.i18N,
                bankAccounts = part.bankAccounts,
                fiscalYears = part.fiscalYears,
                legalEntity = part.legalEntity,
                creditorIdentifier = part.creditorIdentifier,
                sepaModule = part.sepaModule
            )
        } }
    )
}
