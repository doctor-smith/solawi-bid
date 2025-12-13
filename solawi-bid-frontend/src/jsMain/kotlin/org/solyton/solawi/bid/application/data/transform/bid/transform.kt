package org.solyton.solawi.bid.application.data.transform.bid

import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.storage.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.bidenv.Environment
import org.solyton.solawi.bid.module.bid.data.biduser.User

val bidApplicationIso: Lens<Application, BidApplication> by lazy {
    Lens<Application, BidApplication>(
        get =  {whole -> bidApplicationPreIso.get(whole).copy(actions = whole.actions * bidApplicationPreIso)},
        set = bidApplicationPreIso.set
    )
}

val bidApplicationPreIso: Lens<Application, BidApplication> by lazy {
    Lens<Application, BidApplication>(
        get = { whole ->
            BidApplication(
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
                auctions = whole.auctions,
                bidRounds = whole.bidRounds,
                bidderMailAddresses = whole.bidderMailAddresses,
                applicationOrganizationRelations = whole.applicationOrganizationRelations
            )
        },
        set = { part -> { whole ->
              whole.copy(
                  modals = part.modals,
                  i18N = part.i18N,
                  auctions = part.auctions,
                  bidRounds = part.bidRounds,
                  bidderMailAddresses = part.bidderMailAddresses,
                  applicationOrganizationRelations = part.applicationOrganizationRelations
              )
        } }
    )
}
