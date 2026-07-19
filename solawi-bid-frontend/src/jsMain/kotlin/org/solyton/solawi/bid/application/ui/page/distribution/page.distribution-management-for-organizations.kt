package org.solyton.solawi.bid.application.ui.page.distribution

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.distribution.distributionManagementIso
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.distribution.action.createDistributionPoint
import org.solyton.solawi.bid.module.distribution.action.readDistributionPoints
import org.solyton.solawi.bid.module.distribution.action.updateDistributionPoint
import org.solyton.solawi.bid.module.distribution.component.modal.showUpsertDistributionPointModal
import org.solyton.solawi.bid.module.distribution.data.distributionManagementActions
import org.solyton.solawi.bid.module.distribution.data.distributionManagementModals
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.management.deviceData
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.card.cardStyle
import org.solyton.solawi.bid.module.style.list.cardListStyles
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.ProviderId

@Markup
@Composable
@Suppress("FunctionName")
fun DistributionManagementForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    val scope = rememberCoroutineScope()

    val distributionManagementStore = storage * distributionManagementIso
    val distributionManagementActions = distributionManagementStore * distributionManagementActions
    val distributionManagementModals = distributionManagementStore * distributionManagementModals

    LaunchedEffect(providerId) {
        distributionManagementActions dispatch readDistributionPoints(providerId.value)
    }

    val distributionPoints = distributionManagementStore * distributionPoints
    val deviceType = distributionManagementStore * deviceData * mediaType.get

    Page(verticalPageStyle) {
        Wrap {
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle("Distribution Management for Organizations")
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
            SubTitle("Manage your organizations distribution points")
        }
        Wrap(cardStyle) {
            ListWrapper(cardListStyles.listWrapper ) {
                TitleWrapper(cardListStyles.titleWrapper) {
                    Title { H3 { Text("Distribution Management") } }
                    var distributionPointState by remember { mutableStateOf<DistributionPoint?>(null) }
                    ActionsWrapper({
                        with(cardListStyles){actionsWrapper()}
                        flexGrow(1.0)
                    }) {
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                        ) {
                            distributionManagementModals.showUpsertDistributionPointModal(
                                distributionManagementStore,
                                dialogModalTexts(""),
                                deviceType,
                                providerId,
                                distributionPointState,
                                { point ->
                                    distributionPointState = point
                                }) {
                                val state = distributionPointState
                                requireNotNull(state)
                                scope.launch {
                                    distributionManagementActions dispatch createDistributionPoint(
                                        state.name,
                                        providerId.value,
                                        null
                                    )
                                }
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header(cardListStyles.header) {
                        HeaderCell("Name") { width(10.percent) }
                        HeaderCell("Address") { width(30.percent) }
                    }
                }
                ListItemsIndexed(distributionPoints.read()) { index, distributionPoint ->
                    ListItemWrapper({
                        listItemWrapperStyle(this, index)
                    }) {
                        DataWrapper(cardListStyles.dataWrapper) {
                            TextCell(distributionPoint.name) { width(10.percent) }
                            TextCell("Placeholder") { width(30.percent) }
                        }
                        ActionsWrapper {
                            var distributionPointState by remember {
                                mutableStateOf<DistributionPoint?>(
                                    distributionPoint
                                )
                            }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                            ) {
                                distributionManagementModals.showUpsertDistributionPointModal(
                                    distributionManagementStore,
                                    dialogModalTexts(""),
                                    deviceType,
                                    providerId,
                                    distributionPointState,
                                    { point ->
                                        distributionPointState = point
                                    }) {
                                    val state = distributionPointState
                                    requireNotNull(state)
                                    scope.launch {
                                        distributionManagementActions dispatch updateDistributionPoint(
                                            distributionPoint.distributionPointId,
                                            state.name,
                                            providerId.value,
                                            null
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
