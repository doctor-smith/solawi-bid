package org.solyton.solawi.bid.application.ui.page.shares

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.distribution.distributionManagementIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.action.readFiscalYears
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.distribution.action.readDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.distributionManagementActions
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.list.component.ActionsWrapper
import org.solyton.solawi.bid.module.list.component.DataWrapper
import org.solyton.solawi.bid.module.list.component.Header
import org.solyton.solawi.bid.module.list.component.HeaderCell
import org.solyton.solawi.bid.module.list.component.HeaderWrapper
import org.solyton.solawi.bid.module.list.component.ListItemWrapper
import org.solyton.solawi.bid.module.list.component.ListItemsIndexed
import org.solyton.solawi.bid.module.list.component.ListWrapper
import org.solyton.solawi.bid.module.list.component.TextCell
import org.solyton.solawi.bid.module.list.component.Title
import org.solyton.solawi.bid.module.list.component.TitleWrapper
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.shares.action.createShareOffer
import org.solyton.solawi.bid.module.shares.action.createShareType
import org.solyton.solawi.bid.module.shares.action.readShareOffers
import org.solyton.solawi.bid.module.shares.action.readShareTypes
import org.solyton.solawi.bid.module.shares.action.updateShareType
import org.solyton.solawi.bid.module.shares.component.modal.showUpsertShareOffersModal
import org.solyton.solawi.bid.module.shares.component.modal.showUpsertShareTypeModal
import org.solyton.solawi.bid.module.shares.data.management.deviceData
import org.solyton.solawi.bid.module.shares.data.management.shareTypes
import org.solyton.solawi.bid.module.shares.data.management.shareOffers
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.shareManagementModals
import org.solyton.solawi.bid.module.shares.data.types.ShareType
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.ProviderId

@Markup
@Composable
@Suppress("FunctionName")
fun ShareManagementForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    val scope = rememberCoroutineScope()

    val shareManagementStore = storage * shareManagementIso
    val shareManagementActions = shareManagementStore * shareManagementActions
    val shareManagementModals = shareManagementStore * shareManagementModals

    val distributionManagementStorage = storage * distributionManagementIso
    val distributionManagementActions = distributionManagementStorage * distributionManagementActions

    val bankingApplicationStorage = storage * bankingApplicationIso
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val fiscalYears = bankingApplicationStorage * fiscalYears

    LaunchedEffect(providerId) {
        launch {
            shareManagementActions dispatch readShareTypes(providerId.value)
        }
        launch {
            shareManagementActions dispatch readShareOffers(providerId.value)
        }
        launch {
            distributionManagementActions dispatch readDistributionPoints(providerId.value)
        }
        launch {
           bankingApplicationActions dispatch readFiscalYears(providerId.value)
        }
    }

    val shareTypes = shareManagementStore * shareTypes
    val shareOffers = shareManagementStore * shareOffers
    val distributionPoints = distributionManagementStorage * distributionPoints
    val deviceType = shareManagementStore * deviceData * mediaType.get

    Page(verticalPageStyle) {
        Wrap{
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle("Share Management for Organizations")
                Horizontal {
                    ArrowUpButton(
                        Color.black,
                        Color.white,
                        { "UP" },
                        deviceType,
                        false,
                    ) {
                        navigate(up)
                    }
                }
            }
            SubTitle("Manage share types and offers for your organization.")
        }
        Wrap {
            ListWrapper({ defaultListStyles.listWrapper(this) }) {
                TitleWrapper {
                    Title { H3 { Text("Share Types") } }
                    var shareTypeState by remember { mutableStateOf<ShareType?>(null) }
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        deviceType = deviceType,
                    ) {
                        shareManagementModals.showUpsertShareTypeModal(
                            shareManagementStore,
                            dialogModalTexts(""),
                            deviceType,
                            providerId,
                            shareTypeState,
                            { point ->
                                shareTypeState = point
                            }) {
                            val state = shareTypeState
                            requireNotNull(state)
                            scope.launch {
                                shareManagementActions dispatch createShareType(
                                    providerId.value,
                                    state.name,
                                    state.key,
                                    state.description,
                                )
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header {
                        HeaderCell("Name") { width(20.percent) }
                        HeaderCell("key") { width(10.percent) }
                        HeaderCell("Description") { width(70.percent) }
                    }
                }
                ListItemsIndexed(shareTypes.read()) { index, shareType ->
                    ListItemWrapper({
                        listItemWrapperStyle(this, index)
                    }) {
                        DataWrapper() {
                            TextCell(shareType.name) { width(20.percent) }
                            TextCell(shareType.key) { width(10.percent) }
                            TextCell(shareType.description) { width(70.percent) }

                        }
                        ActionsWrapper {
                            var shareTypeState by remember { mutableStateOf<ShareType>(shareType) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                            ) {
                                shareManagementModals.showUpsertShareTypeModal(
                                    shareManagementStore,
                                    dialogModalTexts(""),
                                    deviceType,
                                    providerId,
                                    shareTypeState,
                                    { point ->
                                        shareTypeState = point
                                    }) {
                                    val state = shareTypeState
                                    requireNotNull(state)
                                    scope.launch {
                                        shareManagementActions dispatch updateShareType(
                                            shareType.shareTypeId,
                                            providerId.value,
                                            state.name,
                                            state.key,
                                            state.description
                                        )
                                    }
                                }
                            }
                            TrashCanButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                                isDisabled = true
                            ) {

                            }
                        }
                    }
                }
            }
        }

        Wrap{
            ListWrapper({ defaultListStyles.listWrapper(this) }) {
                TitleWrapper {
                    Title { H3 { Text("Share Offers") } }
                    var shareOfferState by remember { mutableStateOf<ShareOffer?>(null) }
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        deviceType = deviceType,
                    ) {
                        shareManagementModals.showUpsertShareOffersModal(
                            shareManagementStore,
                            dialogModalTexts(""),
                            deviceType,
                            fiscalYears.read(),
                            shareTypes.read(),
                            shareOfferState,
                            { state ->
                                shareOfferState = state
                            }) {
                            val state = shareOfferState
                            requireNotNull(state)
                            scope.launch {
                                shareManagementActions dispatch createShareOffer(
                                    providerId.value,
                                    state.shareType.shareTypeId,
                                    state.fiscalYear.fiscalYearId,
                                    state.price,
                                    state.pricingType,
                                    state.ahcAuthorizationRequired,
                                )
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header {
                        HeaderCell("Fiscal Year") { width(10.percent) }
                        HeaderCell("Share Type") { width(10.percent) }
                        HeaderCell("Price") { width(10.percent) }
                        HeaderCell("PricingType") { width(10.percent) }
                        HeaderCell("SEPA required") { width(10.percent) }
                    }
                }
                ListItemsIndexed(shareOffers.read()) { index, shareOffer ->
                    ListItemWrapper({
                        listItemWrapperStyle(this, index)
                    }) {
                        DataWrapper() {
                            TextCell(shareOffer.fiscalYear.format()) { width(10.percent) }
                            TextCell(shareOffer.shareType.name) { width(10.percent) }
                            TextCell("${shareOffer.price ?: "--"}") { width(10.percent) }
                            TextCell(shareOffer.pricingType.name) { width(10.percent) }
                            TextCell(shareOffer.ahcAuthorizationRequired.toString()) { width(10.percent) }
                        }
                        ActionsWrapper {
                            var shareOfferState by remember { mutableStateOf<ShareOffer>(shareOffer) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                            ) {
                                shareManagementModals.showUpsertShareOffersModal(
                                    shareManagementStore,
                                    dialogModalTexts(""),
                                    deviceType,
                                    fiscalYears.read(),
                                    shareTypes.read(),
                                    shareOfferState,
                                    { state ->
                                        shareOfferState = state
                                    }) {
                                    val state = shareOfferState
                                    requireNotNull(state)
                                    scope.launch {
                                        shareManagementActions dispatch createShareOffer(
                                            providerId.value,
                                            state.shareType.shareTypeId,
                                            state.fiscalYear.fiscalYearId,
                                            state.price,
                                            state.pricingType,
                                            state.ahcAuthorizationRequired,
                                        )
                                    }
                                }
                            }
                            TrashCanButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                                isDisabled = true
                            ) {

                            }
                        }
                    }
                }
            }
        }
    }
}
