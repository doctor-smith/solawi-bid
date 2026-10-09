package org.solyton.solawi.bid.module.shares.component.list

import androidx.compose.runtime.Composable
import org.evoleq.compose.conditional.When
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.math.*
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.isEmpty
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.application.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.service.toFullYearString
import org.solyton.solawi.bid.module.bid.component.dropdown.flatById
import org.solyton.solawi.bid.module.constants.checkIcon
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.distribution.data.management.DistributionManagement
import org.solyton.solawi.bid.module.distribution.data.management.personalDistributionPoints
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.personalShareOffers
import org.solyton.solawi.bid.module.shares.data.management.personalShareSubscriptions
import org.solyton.solawi.bid.module.user.data.user.User
import org.solyton.solawi.bid.module.user.data.user.organizations

@Composable
fun ShareSubscriptionsPersonalList(
    userDataStorage: Storage<User>,
    shareManagementStorage: Storage<ShareManagement>,
    distributionManagementStorage: Storage< DistributionManagement>,
    deviceData: Source<DeviceType>,
    listStyles: ListStyles = ListStyles()
) {
    val offers = shareManagementStorage * personalShareOffers
    val subscriptions = shareManagementStorage * personalShareSubscriptions
    val requestStatuses = listOf(
        ShareStatus.RollingOver,
        ShareStatus.PaymentFailed,
        ShareStatus.ActivationRejected
    )
    val subscriptionsWithOpenRequests = subscriptions * FilterBy { it.status in requestStatuses }

    val distributionPoints = distributionManagementStorage * personalDistributionPoints

    val organizationsMap = (Read(userDataStorage * organizations) map { os -> os.flatById() }) * Emit
    val offersMap = Read(offers) map {list -> list.associateBy { it.shareOfferId }}
    val providerMap = Read(offers) map {
            list -> list.associateBy ({ it.shareOfferId }){
        organizationsMap[it.shareType.providerId]
    }
    }
    val distributionPointsMap = Read(distributionPoints) map {list -> list.associateBy { it.distributionPointId }}


    val sortedSubscriptions = Read(subscriptions) map {list -> list.sortedByDescending { subscription ->
        (offersMap * Get(subscription.shareOfferId) * assureValue() map {offer -> offer.fiscalYear.toFullYearString()}) * Emit
    } }

    val hasSubscriptions = Read(subscriptions) map {list -> list.isNotEmpty()}
    if(hasSubscriptions * IsFalse) {
        Text("Keine Anteile gezeichnet")
        return
    }
    
    When(subscriptionsWithOpenRequests.isEmpty()) {
        // TODO open subscription dialog
    }

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

            val offer = offersMap * Get(subscription.shareOfferId) * assureValue() * Emit
            val provider = providerMap * Get(subscription.shareOfferId) * assureValue() * Emit
            val distributionPoint = distributionPointsMap * Get(subscription.distributionPointId) * Emit

            ListItemWrapper({listItemWrapperStyle(index)}) {
                DataWrapper(listStyles.dataWrapper) {
                    TextCell(offer.fiscalYear.toFullYearString()) { width(10.percent) }
                    TextCell(provider.name, useTextForTooltip = true) { width(10.percent) }
                    TextCell(offer.shareType.name, useTextForTooltip = true) { width(10.percent) }
                    NumberCell(subscription.numberOfShares) { width(5.percent) }
                    PriceCell(subscription.pricePerShare?:0.0) { width(5.percent) }
                    TextCell(subscription.status.value, useTextForTooltip = true) { width(10.percent) }
                    TextCell(subscription.ahcAuthorized.checkIcon()) { width(5.percent) }
                    TextCell(distributionPoint?.name?:"-") { width(5.percent) }
                    TextCell("") { width(40.percent) }

                }
                ActionsWrapper(listStyles.actionsWrapper) {
                    EditButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = { "not implemented yet!" },
                        deviceType = deviceData,
                        isDisabled = true
                    ) {

                    }
                }
            }
        }
    }
}
