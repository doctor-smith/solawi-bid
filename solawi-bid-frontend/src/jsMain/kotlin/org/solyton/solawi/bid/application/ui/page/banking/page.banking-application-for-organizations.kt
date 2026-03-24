package org.solyton.solawi.bid.application.ui.page.banking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.filter
import org.evoleq.optics.storage.sortBy
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
import org.solyton.solawi.bid.application.data.personalApplicationContextRelations
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.ui.page.dashboard.permissions.canAccessApplication
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.action.createFiscalYear
import org.solyton.solawi.bid.module.banking.action.readFiscalYears
import org.solyton.solawi.bid.module.banking.action.updateFiscalYear
import org.solyton.solawi.bid.module.banking.component.modal.showUpsertFiscalYearsModal
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.bankingApplicationModals
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
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
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.ProviderId

@Markup
@Composable
@Suppress("FunctionName")
fun BankingApplicationForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    val scope = rememberCoroutineScope()

    val bankingApplicationStorage = storage * bankingApplicationIso
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val bankingApplicationModals = bankingApplicationStorage * bankingApplicationModals
    val deviceType = bankingApplicationStorage * deviceData * mediaType.get

    val fiscalYears = bankingApplicationStorage * fiscalYears

    LaunchedEffect(providerId) {
        launch {
           bankingApplicationActions dispatch readFiscalYears(providerId.value)
        }
    }


    Page(verticalPageStyle) {
        Wrap {
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle("Banking for Organizations")
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
            SubTitle("Manage your banking for Organizations")
        }

        Wrap {
            ListWrapper {
                TitleWrapper {
                    Title { H3{ Text("Fiscal Years") } }

                    var fiscalYearState by remember { mutableStateOf<FiscalYear?>(null) }
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        deviceType = deviceType,
                    ) {
                        bankingApplicationModals.showUpsertFiscalYearsModal(
                            bankingApplicationStorage,
                            dialogModalTexts("Message"),
                            deviceType,
                            fiscalYears.read(),
                            fiscalYearState,
                            { fiscalYear -> fiscalYearState = fiscalYear },
                        ) {
                            val state = fiscalYearState
                            requireNotNull(state)
                            scope.launch {
                                bankingApplicationActions dispatch createFiscalYear(
                                    providerId.value,
                                    state.start,
                                    state.end
                                )
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header{
                        HeaderCell("Fiscal Year") { width(10.percent) }
                        HeaderCell("Start Date") { width(10.percent) }
                        HeaderCell("End Date") { width(10.percent) }
                    }
                }
                ListItemsIndexed(fiscalYears.read().let{
                    it.sortedByDescending { fiscalYear -> fiscalYear.format() }
                }) {index,  fiscalYear ->
                    ListItemWrapper({ listItemWrapperStyle(index) }) {
                        fun LocalDate.format(): String = "$year-$monthNumber-$dayOfMonth"
                        DataWrapper {
                            TextCell(fiscalYear.format())  { width(10.percent) }
                            TextCell(fiscalYear.start.toString()) { width(10.percent) }
                            TextCell(fiscalYear.end.toString()) { width(10.percent) }
                        }
                        ActionsWrapper {
                            var fiscalYearState by remember { mutableStateOf<FiscalYear?>(fiscalYear) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                            ) {
                                bankingApplicationModals.showUpsertFiscalYearsModal(
                                    bankingApplicationStorage,
                                    dialogModalTexts("Message"),
                                    deviceType,
                                    fiscalYears.read(),
                                    fiscalYearState,
                                    { fiscalYear -> fiscalYearState = fiscalYear },
                                ) {
                                    val state = fiscalYearState
                                    requireNotNull(state)
                                    scope.launch {
                                        bankingApplicationActions dispatch updateFiscalYear(
                                            fiscalYear.fiscalYearId,
                                            providerId.value,
                                            state.start,
                                            state.end
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
