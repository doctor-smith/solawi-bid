package org.solyton.solawi.bid.application.ui.page.shares

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.evoleq.compose.effect.LaunchedEffectOnSource
import org.evoleq.device.data.mediaType
import org.evoleq.math.Emit
import org.evoleq.math.emit
import org.evoleq.math.map
import org.evoleq.math.times
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.managedUsers
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.distribution.distributionManagementIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.data.userData
import org.solyton.solawi.bid.application.service.setContext
import org.solyton.solawi.bid.module.banking.action.readFiscalYears
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.context.data.isEmpty
import org.solyton.solawi.bid.module.distribution.action.readPersonalDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.distributionManagementActions
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.shares.action.readPersonalShareOffers
import org.solyton.solawi.bid.module.shares.action.readPersonalShareSubscriptions
import org.solyton.solawi.bid.module.shares.action.readShareSubscriptionStatusHistories
import org.solyton.solawi.bid.module.shares.component.list.ShareSubscriptionsPersonalList
import org.solyton.solawi.bid.module.shares.data.management.personalShareOffers
import org.solyton.solawi.bid.module.shares.data.management.personalShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.action.user.getUsers
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
import org.solyton.solawi.bid.module.user.data.deviceData
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.userActions
import org.solyton.solawi.bid.module.user.service.user.userIdFromToken

@Composable
fun PersonalShareManagementPage(storage: Storage<Application>) {
    if((storage * context * isEmpty()).emit() ) return@PersonalShareManagementPage

    val scope = rememberCoroutineScope()

    // Data
    val deviceData = storage * userIso * deviceData * mediaType.get
    val userDataStorage = storage * userData
    val managedUsersStorage = storage * managedUsers
    val shareManagementStorage = storage * shareManagementIso
    val bankingApplicationStorage = storage * bankingApplicationIso
    val distributionManagementStorage = storage * distributionManagementIso

    storage.setContext("APPLICATION")

    LaunchedEffect(Unit) {
        scope.launch {
        (storage * userIso * userActions).dispatch(readUserPermissionsAction())
    }
    }

    LaunchedEffect((userDataStorage * userIdFromToken).emit()) {
        val userId = (userDataStorage * userIdFromToken).emit()
        if(userId != null) {
            (storage * userIso * userActions).dispatch(readOrganizations())
        }
        (storage * userIso * userActions).dispatch(getUsers())
    }
    LaunchedEffect(Read(managedUsersStorage) * Emit) {
        val userIds = (Read(managedUsersStorage) map {list -> list.map { user -> user.id}} )* Emit
        (storage * userIso * userActions).dispatch(readUserProfiles(
            userIds
        ))
    }

    LaunchedEffectOnSource(Read(userDataStorage * organizations) map {it.map { org -> org.name }}) {
        // if the organizations change we have to reload all offers of all organizations the current user is a member of
        scope.launch {
            shareManagementStorage * shareManagementActions dispatch readPersonalShareOffers()
        }
        scope.launch {
            shareManagementStorage * shareManagementActions dispatch readPersonalShareSubscriptions()
        }
        scope.launch {
            distributionManagementStorage * distributionManagementActions dispatch readPersonalDistributionPoints()
        }
    }

    val shareSubscriptionIds = Read(shareManagementStorage * personalShareSubscriptions) map {
        subs -> subs.map{ ShareSubscriptionId(it.shareSubscriptionId)}
    }

    LaunchedEffectOnSource(shareSubscriptionIds) {
        scope.launch {
            shareManagementStorage * shareManagementActions dispatch readShareSubscriptionStatusHistories(
                shareSubscriptionIds * Emit
            )
        }

        val providerIds = (Read(shareManagementStorage * personalShareOffers) map { list -> list.map { it.shareType.providerId }.distinct() }) * Emit
        scope.launch {
            if(providerIds.isEmpty()) return@launch
            bankingApplicationStorage * bankingApplicationActions dispatch readFiscalYears(providerIds)
        }
    }


    Page(verticalPageStyle) {
        PageTitle("Persönliche Anteile Verwalten")
        ShareSubscriptionsPersonalList(
            userDataStorage = userDataStorage,
            managedUsersStorage = managedUsersStorage,
            shareManagementStorage = shareManagementStorage,
            bankingApplicationStorage = bankingApplicationStorage,
            distributionManagementStorage = distributionManagementStorage,
            deviceData = deviceData
        )
    }
}
