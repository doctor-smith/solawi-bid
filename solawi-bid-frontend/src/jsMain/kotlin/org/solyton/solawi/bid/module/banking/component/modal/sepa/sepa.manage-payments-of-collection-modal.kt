package org.solyton.solawi.bid.module.banking.component.modal.sepa

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
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
import org.evoleq.math.emit
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderWidth
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.api.solawiApi
import org.solyton.solawi.bid.module.banking.action.updateSepaPaymentExecutionStatuses
import org.solyton.solawi.bid.module.banking.component.list.ListOfPayments
import org.solyton.solawi.bid.module.banking.component.properties.PaymentsProperties
import org.solyton.solawi.bid.module.banking.component.tab.TabParagraphWrapper
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaPaymentExecutionStatuses
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.toApiType
import org.solyton.solawi.bid.module.control.button.AnglesLeftButton
import org.solyton.solawi.bid.module.control.button.AnglesRightButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.tabs.component.TabContent
import org.solyton.solawi.bid.module.tabs.component.TabContentWrapper
import org.solyton.solawi.bid.module.tabs.component.TabParagraph
import org.solyton.solawi.bid.module.tabs.component.TabSelectionBar
import org.solyton.solawi.bid.module.tabs.component.TabTitle
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

sealed class Tabs {
    class Payments {
        enum class Paragraphs {
            OVERVIEW,
            CREATE_NEW_PAYMENTS,
            PAYMENTS_CREATED,
            PAYMENTS_MESSAGE_CREATED,
            PAYMENTS_SENT,
            PAYMENTS_PENDING,
            PAYMENTS_FAILED,
            PAYMENTS_CONFIRMED
        }
    }
}

data class UIState(
    val selectedTab: Int = 0,
    val selectedParagraph: Tabs.Payments.Paragraphs = Tabs.Payments.Paragraphs.OVERVIEW
)

@Markup
@Suppress("FunctionName")
fun ManagePaymentsOfSepaCollectionModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    storage: Storage<BankingApplication>,
    device: Source<DeviceType>,
    uiState: UIState,
    setUiState: (UIState) -> Unit,
    sepaCollectionSource: Source<SepaCollection>,
    executionDate: LocalDate?,
    setManageCollectionPayments: (ManageCollectionPayments) -> Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device),
) {
    val scope = rememberCoroutineScope()
    val sepaCollection = sepaCollectionSource.emit()
    val tabStyles = TabStyles()
    val dropdownStyles = DropdownStyles()
        .modifyContainerStyle {
            alignSelf(AlignSelf.Start)
        }
    val scrollableStyles = ScrollableStyles().modifyContainerStyle {
        //height(100.percent)

        //flexGrow(1)
    }

    var selectedTab by remember { mutableStateOf(uiState.selectedTab) }
    var paragraphState by remember { mutableStateOf(Tabs.Payments.Paragraphs.OVERVIEW) }
    LaunchedEffect(selectedTab, paragraphState) {
        setUiState(UIState(selectedTab, paragraphState))
    }

    key(sepaCollection) {
        TabsWrapper(tabStyles.tabsWrapperStyles) {
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
            TabContentWrapper(tabStyles.tabContentWrapperStyles) {
                TabContent(
                    tabStyles.tabContentStyles,
                    0,
                    selectedTab
                ) {


                    val openPayments =
                        sepaCollection.sepaPayments.filter { payment -> payment.status == PaymentExecutionStatus.CREATED }

                    val messageCreatedPayments =
                        sepaCollection.sepaPayments.filter { payment -> payment.status == PaymentExecutionStatus.MESSAGE_CREATED }

                    val sentPayments =
                        sepaCollection.sepaPayments.filter { payment -> payment.status == PaymentExecutionStatus.SENT }
                    val pendingPayments =
                        sepaCollection.sepaPayments.filter { payment -> payment.status == PaymentExecutionStatus.PENDING }
                    val failedPayments =
                        sepaCollection.sepaPayments.filter { payment -> payment.status == PaymentExecutionStatus.FAILED }
                    val confirmedPayments =
                        sepaCollection.sepaPayments.filter { payment -> payment.status == PaymentExecutionStatus.CONFIRMED }


                    // GUI states
                    // var detailsHeightState by remember { mutableStateOf(60.0)}

                    Horizontal {
                        Vertical({
                            width(20.percent)
                            flexGrow(1)
                        }) {
                            // TabTitle("Manage Payments")
                            TabParagraphWrapper(
                                onClick = { paragraphState = Tabs.Payments.Paragraphs.OVERVIEW }
                            ) {
                                TabParagraph("Overview")
                                PaymentsProperties(sepaCollection.sepaPayments)
                            }
                            Scrollable(
                                scrollableStyles
                                // .modifyContainerStyle { minHeight(detailsHeightState.vh) }
                            ) {
                                TabParagraphWrapper(
                                    onClick = { paragraphState = Tabs.Payments.Paragraphs.CREATE_NEW_PAYMENTS }
                                ) {
                                    TabParagraph("Create new Payments")
                                    Form(formDesktopStyle) {
                                        Field(fieldDesktopStyle) {
                                            // State
                                            var executionDateState by remember {
                                                mutableStateOf(
                                                    executionDate ?: today()
                                                )
                                            }
                                            val initDate = executionDateState.format(Locale.Iso)

                                            Label("Execution Date", id = "date", labelStyle = formLabelDesktopStyle)
                                            Input(InputType.Date) {
                                                id("date")
                                                // dataId("create-auction.form.input.date")
                                                value(initDate)
                                                style {
                                                    dateInputDesktopStyle()
                                                    alignSelf(AlignSelf.Start)
                                                }
                                                onInput {
                                                    executionDateState = LocalDate.parse(it.value)
                                                    setManageCollectionPayments(
                                                        ManageCollectionPayments.AttachPayments(
                                                            executionDateState
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                TabParagraphWrapper(
                                    onClick = { paragraphState = Tabs.Payments.Paragraphs.PAYMENTS_CREATED }
                                ) {
                                    TabParagraph("Open Payments / Recurring")
                                    PaymentsProperties(openPayments)
                                }
                                TabParagraphWrapper(
                                    onClick = { paragraphState = Tabs.Payments.Paragraphs.PAYMENTS_MESSAGE_CREATED }
                                ) {
                                    TabParagraph("Ready to be sent")
                                    PaymentsProperties(messageCreatedPayments)
                                }
                                TabParagraphWrapper(
                                    onClick = { paragraphState = Tabs.Payments.Paragraphs.PAYMENTS_SENT }
                                ) {
                                    TabParagraph("Sent Payments (Sent to Bank)")
                                    PaymentsProperties(sentPayments)
                                }
                                TabParagraphWrapper(
                                    onClick = { paragraphState = Tabs.Payments.Paragraphs.PAYMENTS_PENDING }
                                ) {
                                    TabParagraph("Pending Payments")
                                    PaymentsProperties(pendingPayments)
                                }
                                TabParagraphWrapper(
                                    onClick = { paragraphState = Tabs.Payments.Paragraphs.PAYMENTS_FAILED }
                                ) {
                                    TabParagraph("Failed Payments")
                                    PaymentsProperties(failedPayments)
                                }
                                TabParagraphWrapper(
                                    isLast = true,
                                    onClick = { paragraphState = Tabs.Payments.Paragraphs.PAYMENTS_CONFIRMED }
                                ) {
                                    TabParagraph("Confirmed Payments")
                                    PaymentsProperties(confirmedPayments)
                                }
                            }
                        }
                        Vertical({
                            paddingLeft(5.px)
                            width(80.percent)
                            border {
                                style = LineStyle.Solid
                                width = 1.px
                                color = Color.gray
                            }
                            borderWidth(0.px, 0.px, 0.px, 1.px)
                        }) {
                            When(paragraphState == Tabs.Payments.Paragraphs.OVERVIEW) {
                                TabTitle("Overview")
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.CREATE_NEW_PAYMENTS) {
                                TabTitle("Create New payments")
                            }
                            val listStyles = defaultListStyles.modifyFilter {
                                width(10.percent)
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_CREATED) {
                                TabTitle("Recently created Payments")
                                ListOfPayments(
                                    null,
                                    sepaCollection.sepaMandates,
                                    openPayments,
                                    listStyles
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_MESSAGE_CREATED) {
                                TabTitle("Ready to be sent to the bank")
                                ListOfPayments(
                                    null,
                                    sepaCollection.sepaMandates,
                                    messageCreatedPayments,
                                    listStyles,
                                    overallActions = { data ->
                                        Horizontal {
                                            AnglesLeftButton(
                                                color = Color.black,
                                                bgColor = Color.white,
                                                { "Move selected & visible Payments to the previous state" },
                                                device,
                                                isDisabled = true
                                            ) {
                                            }
                                            AnglesRightButton(
                                                color = Color.black,
                                                bgColor = Color.white,
                                                { "Move selected & visible Payments to the next state" },
                                                device
                                            ) {
                                                scope.launch {
                                                    val selectedPaymentIds = data.itemsMap.filter {
                                                        it.key in data.visibleItems &&
                                                                data.checkedPayments[it.key.paymentId] == true
                                                    }.map { it.key.paymentId }
                                                    (storage * bankingApplicationActions) dispatch updateSepaPaymentExecutionStatuses(
                                                        UpdateSepaPaymentExecutionStatuses(
                                                            PaymentExecutionStatus.SENT.toApiType(),
                                                            selectedPaymentIds,
                                                        ),
                                                        sepaCollection.sepaCollectionId
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_SENT) {
                                TabTitle("Payments sent to to the bank")
                                ListOfPayments(
                                    null,
                                    sepaCollection.sepaMandates,
                                    sentPayments,
                                    listStyles,
                                    overallActions = { data ->
                                        Horizontal {
                                            AnglesLeftButton(
                                                color = Color.black,
                                                bgColor = Color.white,
                                                { "Move selected Payments to the previous state" },
                                                device,
                                            ) {
                                                scope.launch {
                                                    val selectedPaymentIds = data.itemsMap.filter {
                                                        it.key in data.visibleItems &&
                                                                data.checkedPayments[it.key.paymentId] == true
                                                    }.map { it.key.paymentId }
                                                    (storage * bankingApplicationActions) dispatch updateSepaPaymentExecutionStatuses(
                                                        UpdateSepaPaymentExecutionStatuses(
                                                            PaymentExecutionStatus.MESSAGE_CREATED.toApiType(),
                                                            selectedPaymentIds,
                                                        ),
                                                        sepaCollection.sepaCollectionId
                                                    )
                                                }
                                            }
                                            AnglesRightButton(
                                                color = Color.black,
                                                bgColor = Color.white,
                                                { "Move selected Payments to the next state" },
                                                device
                                            ) {
                                                scope.launch {
                                                    val selectedPaymentIds = data.itemsMap.filter {
                                                        it.key in data.visibleItems &&
                                                                data.checkedPayments[it.key.paymentId] == true
                                                    }.map { it.key.paymentId }
                                                    (storage * bankingApplicationActions) dispatch updateSepaPaymentExecutionStatuses(
                                                        UpdateSepaPaymentExecutionStatuses(
                                                            PaymentExecutionStatus.PENDING.toApiType(),
                                                            selectedPaymentIds,
                                                        ),
                                                        sepaCollection.sepaCollectionId
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_PENDING) {
                                TabTitle("Pending Payments")
                                ListOfPayments(
                                    null,
                                    sepaCollection.sepaMandates,
                                    pendingPayments,
                                    listStyles,
                                    overallActions = { data ->
                                        Horizontal {
                                            AnglesLeftButton(
                                                color = Color.black,
                                                bgColor = Color.white,
                                                { "Move selected Payments to the previous state" },
                                                device,
                                            ) {
                                                scope.launch {
                                                    val selectedPaymentIds = data.itemsMap.filter {
                                                        it.key in data.visibleItems &&
                                                                data.checkedPayments[it.key.paymentId] == true
                                                    }.map { it.key.paymentId }
                                                    (storage * bankingApplicationActions) dispatch updateSepaPaymentExecutionStatuses(
                                                        UpdateSepaPaymentExecutionStatuses(
                                                            PaymentExecutionStatus.MESSAGE_CREATED.toApiType(),
                                                            selectedPaymentIds,
                                                        ),
                                                        sepaCollection.sepaCollectionId
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    actions = {}
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_CONFIRMED) {
                                TabTitle("Confirmed Payments")
                                ListOfPayments(
                                    null,
                                    sepaCollection.sepaMandates,
                                    confirmedPayments,
                                    listStyles
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_FAILED) {
                                TabTitle("Failed Payments")
                                ListOfPayments(
                                    null,
                                    sepaCollection.sepaMandates,
                                    failedPayments,
                                    listStyles
                                )
                            }
                        }
                    }
                }
                TabContent(
                    tabStyles.tabContentStyles,
                    1,
                    selectedTab
                ) {
                    val openPayments =
                        sepaCollection.sepaPayments.filter { it.status == PaymentExecutionStatus.CREATED }

                    val executionDatesOfOpenPayments = openPayments.map { it.executionDate }.sortedBy { it }
                    val allExecutionDates =
                        sepaCollection.sepaPayments.filter { it.status != PaymentExecutionStatus.CREATED }
                            .map { it.executionDate }.distinct().sortedBy { it }
                    var executionDateState by remember { mutableStateOf(executionDatesOfOpenPayments.firstOrNull()) }

                    TabTitle("Manage Messages")
                    Horizontal {
                        val isValid = executionDatesOfOpenPayments.isNotEmpty()
                        if (!isValid) Text("No open Payments defined")
                        Form(formDesktopStyle) {

                            Field(fieldDesktopStyle) {

                                // State
                                val initDate = (executionDateState ?: today()).format(Locale.Iso)
                                val dateOptions = executionDatesOfOpenPayments.associateBy { date ->
                                    date.format(Locale.Iso)
                                }
                                Label("Execution Date", id = "date", labelStyle = formLabelDesktopStyle)
                                Dropdown(
                                    options = dateOptions,
                                    selected = initDate,
                                    styles = dropdownStyles,
                                    iconContent = { opened -> SimpleUpDown(opened) }
                                ) { (_, value) ->
                                    executionDateState = value
                                }
                            }
                        }
                        Button({
                            if (executionDateState == null || !isValid) disabled()
                            onClick {
                                if (executionDateState == null) return@onClick
                                setManageCollectionPayments(
                                    ManageCollectionPayments.CreateMessage(
                                        executionDateState!!
                                    )
                                )
                            }
                        }) {
                            Text("Generate Sepa Message")
                        }
                    }

                    TabParagraph("Sepa messages by execution date")
                    allExecutionDates.forEach { date ->
                        val payments = sepaCollection.sepaPayments.filter { it.executionDate == date }
                        // distinguish status?
                        ListOfPayments(
                            "Message for $date",
                            sepaCollection.sepaMandates,
                            payments = payments,
                            overallActions = {

                            },
                            actions = {

                            }
                        )
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
    uiState: UIState,
    setUiState: (UIState) -> Unit,
    sepaCollection: Source<SepaCollection>,
    executionDate: LocalDate?,
    setManageCollectionPayments: (ManageCollectionPayments) -> Unit,
    update: () -> Unit
) = with(nextId()) {
    put(
        this to ModalData(this,
            ModalType.Dialog,
            ManagePaymentsOfSepaCollectionModal(
                this,
                texts,
                this@showManagePaymentsOfSepaCollectionModal,
                storage,
                device,
                uiState,
                setUiState,
                sepaCollection,
                executionDate,
                setManageCollectionPayments,
                update = update
            )
        )
    )
}


// suspend managePaymentsOfSepaCollection(collection: SepaCollection, data: ManageCollectionPayments)
