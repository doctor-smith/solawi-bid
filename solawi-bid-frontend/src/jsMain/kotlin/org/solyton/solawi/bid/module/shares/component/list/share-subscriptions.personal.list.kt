package org.solyton.solawi.bid.module.shares.component.list

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.*
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.application.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.service.toFullYearString
import org.solyton.solawi.bid.module.bid.component.dropdown.flatById
import org.solyton.solawi.bid.module.constants.checkIcon
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.distribution.data.management.DistributionManagement
import org.solyton.solawi.bid.module.distribution.data.management.personalDistributionPoints
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.shares.action.updateShareSubscription
import org.solyton.solawi.bid.module.shares.component.modal.UpsertShareSubscriptionModalConfiguration
import org.solyton.solawi.bid.module.shares.component.modal.showUpsertShareSubscriptionModal
import org.solyton.solawi.bid.module.shares.component.modal.upsertShareSubscriptionModalTexts
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.personalShareOffers
import org.solyton.solawi.bid.module.shares.data.management.personalShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.shareManagementModals
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.user.User
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.user.username
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.Username

@Composable
fun ShareSubscriptionsPersonalList(
    userDataStorage: Storage<User>,
    managedUsersStorage: Storage<List<ManagedUser>>,
    shareManagementStorage: Storage<ShareManagement>,
    distributionManagementStorage: Storage<DistributionManagement>,
    bankingApplicationStorage: Storage<BankingApplication>,
    deviceData: Source<DeviceType>,
    listStyles: ListStyles = ListStyles()
) {
    val scope = rememberCoroutineScope ()

    val offers = shareManagementStorage * personalShareOffers
    val subscriptions = shareManagementStorage * personalShareSubscriptions
    if(Read(subscriptions) * IsNotEmpty && Read(offers) * IsEmpty) return
    /*
    val requestStatuses = listOf(
        ShareStatus.RollingOver,
        ShareStatus.PaymentFailed,
        ShareStatus.ActivationRejected
    )

     */
    // val subscriptionsWithOpenRequests = subscriptions * FilterBy { it.status in requestStatuses }

    val distributionPoints = distributionManagementStorage * personalDistributionPoints
    val providerToDistributionPoints = Read(distributionPoints) map {
        distributionPoints -> distributionPoints.groupBy { it.organizationId }
    }

    val organizationsMap = (Read(userDataStorage * organizations) map { os -> os.flatById() }) * Emit
    val offersMap = Read(offers) map {list -> list.associateBy { it.shareOfferId }}
    val providerMap = Read(offers) map {
            list -> list.associateBy ({ it.shareOfferId }){
        organizationsMap[it.shareType.providerId]
    }
    }
    val distributionPointsMap = Read(distributionPoints) map {list -> list.associateBy { it.distributionPointId }}


    val sortedSubscriptions = Read(subscriptions) map {list -> list.sortedByDescending { subscription ->
        (offersMap * Get(subscription.shareOfferId) /* * assureValue("share offer of subscription is null / not loaded")*/ map {offer -> offer?.fiscalYear?.toFullYearString()?:"_"}) * Emit
    } }

    val hasSubscriptions = Read(subscriptions) map {list -> list.isNotEmpty()}
    if(hasSubscriptions * IsFalse) {
        Text("Keine Anteile gezeichnet")
        return
    }

    val fiscalYears = bankingApplicationStorage * fiscalYears
    val fiscalYearsMap = Read(fiscalYears) map {
        list -> list.associateBy { it.fiscalYearId }
    }
    val subscriptionToFiscalYears= Read(subscriptions) map  {
        list -> list.map { it.shareSubscriptionId to
            fiscalYearsMap * Get(it.fiscalYearId) * Emit
        }.groupBy { it.first }.mapValues { it.value.mapNotNull{v -> v.second }}
    }

    val username = userDataStorage * username
    val managedUserSingleton = Read(managedUsersStorage) map { users -> users.filter{user -> user.username == Read(username) * Emit}.take(1) }

    /*
    When(subscriptionsWithOpenRequests.isEmpty()) {
        // TODO open subscription dialog
    }

     */

    ListWrapper(listStyles.listWrapper) {

        HeaderWrapper(listStyles.headerWrapper) {
            Header(listStyles.header) {
                HeaderCell("Jahr", tooltip = "Wirtschaftsjahr") { width(10.percent) }
                HeaderCell("Organisation", tooltip = "Organisation") { width(10.percent) }
                HeaderCell("Typ", tooltip = "Typ des Anteils") { width(10.percent) }
                HeaderCell("Anzahl", tooltip = "Anzahl der Anteile") { width(5.percent) }
                HeaderCell("Preis", tooltip = "Preis pro Anteil") { width(5.percent) }
                HeaderCell("Status", tooltip = "Status des Anteils") { width(10.percent) }
                HeaderCell("SEPA", tooltip = "SEPA Lastschrift-Mandat erteilt") { width(5.percent) }
                HeaderCell("Depot", tooltip =  "Zugeordnetes Depot") { width(5.percent) }
                HeaderCell("Kommentar"){width(40.percent)}
            }
        }

        ListItemsIndexed(sortedSubscriptions ) { index, subscription ->

            var subscriptionState by remember { mutableStateOf(subscription) }

            val offer = offersMap * Get(subscriptionState.shareOfferId) * assureValue("offer is null") * Emit
            val provider = providerMap * Get(subscriptionState.shareOfferId) * assureValue("provider is null") * Emit
            val distributionPoint = distributionPointsMap * Get(subscriptionState.distributionPointId) * Emit
            val fiscalYears = subscriptionToFiscalYears * GetForSure(subscriptionState.shareSubscriptionId)




            ListItemWrapper({listItemWrapperStyle(index)}) {
                DataWrapper(listStyles.dataWrapper) {
                    TextCell(offer.fiscalYear.toFullYearString()) { width(10.percent) }
                    TextCell(provider.name, useTextForTooltip = true) { width(10.percent) }
                    TextCell(offer.shareType.name, useTextForTooltip = true) { width(10.percent) }
                    NumberCell(subscriptionState.numberOfShares) { width(5.percent) }
                    PriceCell(subscriptionState.pricePerShare?:0.0) { width(5.percent) }
                    TextCell(subscriptionState.status.value, useTextForTooltip = true) { width(10.percent) }
                    TextCell(subscriptionState.ahcAuthorized.checkIcon()) { width(5.percent) }
                    TextCell(distributionPoint?.name?:"-") { width(5.percent) }
                    TextCell("") { width(40.percent) }

                }
                ActionsWrapper(listStyles.actionsWrapper) {

                    val isEditButtonDisabled = subscription.status !in listOf(
                        ShareStatus.PendingActivation
                    )

                    EditButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = { "not implemented yet!" },
                        deviceType = deviceData,
                        isDisabled = isEditButtonDisabled
                    ) {
                        (shareManagementStorage * shareManagementModals).showUpsertShareSubscriptionModal(
                            shareManagementStorage,
                            upsertShareSubscriptionModalTexts.emit(),
                            deviceData,
                            UpsertShareSubscriptionModalConfiguration.configure(subscriptionState, ChangedBy.USER),
                            fiscalYears,
                            providerToDistributionPoints * GetForSure(provider.organizationId),
                            {listOf(offer)},
                            ProviderId(provider.organizationId),
                            managedUserSingleton,
                            ChangedBy.USER,
                            shareSubscription = subscriptionState,
                            setShareSubscription = { subscriptionState = it }
                        ) {

                            scope.launch {
                                shareManagementStorage * shareManagementActions dispatch updateShareSubscription(
                                    subscriptionState.shareSubscriptionId,
                                    provider.organizationId,
                                    subscriptionState.shareOfferId,
                                    (managedUserSingleton map { list ->
                                        list.first().profile?.userProfileId ?: ""
                                    }) * Emit,
                                    subscriptionState.status,
                                    subscriptionState.distributionPointId,
                                    subscriptionState.fiscalYearId,
                                    subscriptionState.numberOfShares,
                                    subscriptionState.pricePerShare,
                                    subscriptionState.ahcAuthorized,
                                    subscriptionState.coSubscribers.map { Username(it) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
