package org.solyton.solawi.bid.module.banking.component.modal.sepa

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.date.format
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.today
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.tabs.component.TabContent
import org.solyton.solawi.bid.module.tabs.component.TabContentWrapper
import org.solyton.solawi.bid.module.tabs.component.TabSelectionBar
import org.solyton.solawi.bid.module.tabs.component.TabTrigger
import org.solyton.solawi.bid.module.tabs.component.TabsWrapper
import org.solyton.solawi.bid.module.tabs.style.TabStyles
import org.w3c.dom.HTMLElement

sealed class ManageCollectionPayments {
    data class AttachPayments(
        val executionDate: LocalDate
    ): ManageCollectionPayments()

    data class CreateMessage(
        val executionDate: LocalDate
    ): ManageCollectionPayments()
}

@Markup
@Suppress("FunctionName")
fun ManagePaymentsOfSepaCollectionModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    sepaCollection: SepaCollection,
    executionDate: LocalDate?,
    setManageCollectionPayments: (ManageCollectionPayments) -> Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device),
) {
    Wrap {


        val tabStyles = TabStyles()
        TabsWrapper(tabStyles.tabsWrapperStyles) {
            var selectedTab by remember { mutableStateOf(0)}
            TabSelectionBar(tabStyles.tabSelectionBarStyles) {
                TabTrigger(
                    tabStyles.tabTriggerStyles,
                    id = 0,
                    currentTab = selectedTab,
                    trigger = { selectedTab = 0 }
                ) {
                    Text("Payments")
                }
                TabTrigger(
                    tabStyles.tabTriggerStyles,
                    id = 1,
                    currentTab = selectedTab,
                    trigger = { selectedTab = 1 }
                ) {
                    Text("Messages")
                }
            }
            TabContentWrapper(tabStyles.tabContentWrapperStyles){
                TabContent(
                    tabStyles.tabContentStyles,
                    0,
                    selectedTab
                ) {
                    Form(formDesktopStyle) {
                        Field(fieldDesktopStyle) {
                            // State
                            var executionDateState by remember { mutableStateOf(executionDate?: today())  }
                            val initDate = executionDateState.format(Locale.Iso)

                            Label("Execution Date", id = "date" , labelStyle = formLabelDesktopStyle)
                            Input(InputType.Date) {
                                id("date")
                                // dataId("create-auction.form.input.date")
                                value(initDate)
                                style { dateInputDesktopStyle() }
                                onInput {
                                    executionDateState = LocalDate.parse(it.value)
                                    setManageCollectionPayments(ManageCollectionPayments.AttachPayments(
                                        executionDateState
                                    ))
                                }
                            }
                        }
                    }
                }
                TabContent(
                    tabStyles.tabContentStyles,
                    1,
                    selectedTab
                ) {
                    val openPayments = sepaCollection.sepaPayments.filter { it.status == PaymentExecutionStatus.CREATED }
                    val executionDates = openPayments.map{it.executionDate}.sortedBy { it }
                    var executionDateState by remember { mutableStateOf(executionDates.first()) }

                    Text("Manage Messages")
                    Form(formDesktopStyle) {

                        Field(fieldDesktopStyle) {

                            val isValid = executionDates.isNotEmpty()
                            if(!isValid) Text("No Payments defined")

                            // State
                            val initDate = executionDateState.format(Locale.Iso)
                            val dateOptions = executionDates.associateBy {
                                date -> date.format(Locale.Iso)
                            }
                            Label("Execution Date", id = "date" , labelStyle = formLabelDesktopStyle)
                            Dropdown(
                                options = dateOptions,
                                selected = initDate,
                                iconContent = {opened -> SimpleUpDown(opened) }
                            ) {
                                (_, value) -> executionDateState = value
                            }
                        }
                    }
                    Button({
                        onClick {
                            setManageCollectionPayments(ManageCollectionPayments.CreateMessage(
                            executionDateState
                        )) }
                    }) {
                        Text("Generate Message")
                    }
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showManagePaymentsOfSepaCollectionModal(
    storage: Storage<BankingApplication>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    sepaCollection: SepaCollection,
    executionDate: LocalDate?,
    setManageCollectionPayments: (ManageCollectionPayments) -> Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        ManagePaymentsOfSepaCollectionModal(
            this,
            texts,
            this@showManagePaymentsOfSepaCollectionModal,
            storage,
            device,sepaCollection,
            executionDate,
            setManageCollectionPayments,
            update = update
        )
    ) )
}

// suspend managePaymentsOfSepaCollection(collection: SepaCollection, data: ManageCollectionPayments)
