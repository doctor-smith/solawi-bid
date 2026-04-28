package org.solyton.solawi.bid.module.banking.component.modal.sepa

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
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
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.flex
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.border
import org.jetbrains.compose.web.css.borderWidth
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.paddingLeft
import org.jetbrains.compose.web.css.paddingRight
import org.jetbrains.compose.web.css.paddingTop
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.H5
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.banking.component.list.ListOfPayments
import org.solyton.solawi.bid.module.banking.component.properties.PaymentsProperties
import org.solyton.solawi.bid.module.banking.component.tab.TabParagraphWrapper
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.sepa.PaymentExecutionStatus
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.tabs.component.TabContent
import org.solyton.solawi.bid.module.tabs.component.TabContentWrapper
import org.solyton.solawi.bid.module.tabs.component.TabParagraph
import org.solyton.solawi.bid.module.tabs.component.TabSelectionBar
import org.solyton.solawi.bid.module.tabs.component.TabTitle
import org.solyton.solawi.bid.module.tabs.component.TabTrigger
import org.solyton.solawi.bid.module.tabs.component.TabsWrapper
import org.solyton.solawi.bid.module.tabs.style.TabStyles
import org.solyton.solawi.bid.module.user.component.dropdown.dropdownStyles
import org.w3c.dom.HTMLElement
import kotlin.time.Duration.Companion.days

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

        val tabStyles = TabStyles()
        val dropdownStyles = DropdownStyles()
            .modifyContainerStyle{
                alignSelf(AlignSelf.Start)
            }
        val scrollableStyles = ScrollableStyles().modifyContainerStyle {
            //height(100.percent)

            //flexGrow(1)
        }
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

                    var paragraphState by remember { mutableStateOf(Tabs.Payments.Paragraphs.OVERVIEW)}

                    // GUI states
                    // var detailsHeightState by remember { mutableStateOf(60.0)}

                    Horizontal {
                        Vertical({
                            width(20.percent)
                            flexGrow(1)
                        }) {
                            // TabTitle("Manage Payments")
                            TabParagraphWrapper(
                                onClick =  { paragraphState = Tabs.Payments.Paragraphs.OVERVIEW }
                            ) {
                                TabParagraph("Overview")
                                PaymentsProperties(sepaCollection.sepaPayments)
                            }
                            Scrollable(scrollableStyles
                                // .modifyContainerStyle { minHeight(detailsHeightState.vh) }
                            ) {
                                TabParagraphWrapper(
                                    onClick =  { paragraphState = Tabs.Payments.Paragraphs.CREATE_NEW_PAYMENTS }
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
                            border{
                                style = LineStyle.Solid
                                width = 1.px
                                color = Color.gray
                            }
                            borderWidth(0.px,0.px,0.px,1.px)
                        }) {
                            When(paragraphState == Tabs.Payments.Paragraphs.OVERVIEW) {
                                TabTitle("Overview")
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.CREATE_NEW_PAYMENTS) {
                                TabTitle("Create New payments")
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_CREATED) {
                                TabTitle("Recently created Payments")
                                ListOfPayments(
                                    null,
                                    openPayments,
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_MESSAGE_CREATED) {
                                TabTitle("Ready to be sent to the bank")
                                val messageCreatedPayments = (1..100).map { messageCreatedPayments }.flatten()
                                ListOfPayments(
                                    null,
                                    messageCreatedPayments,
                                )

                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_SENT) {
                                TabTitle("Payments sent to to the bank")
                                ListOfPayments(
                                    null,
                                    sentPayments,
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_PENDING) {
                                TabTitle("Pending Payments")
                                ListOfPayments(
                                    null,
                                    pendingPayments,
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_CONFIRMED) {
                                TabTitle("Confirmed Payments")
                                ListOfPayments(
                                    null,
                                    confirmedPayments,
                                )
                            }
                            When(paragraphState == Tabs.Payments.Paragraphs.PAYMENTS_FAILED) {
                                TabTitle("Failed Payments")
                                ListOfPayments(
                                    null,
                                    failedPayments,
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
                    val openPayments = sepaCollection.sepaPayments.filter { it.status == PaymentExecutionStatus.CREATED }
                    val executionDates = openPayments.map{it.executionDate}.sortedBy { it }
                    var executionDateState by remember { mutableStateOf(executionDates.firstOrNull()) }

                    Text("Manage Messages")
                    Form(formDesktopStyle) {

                        Field(fieldDesktopStyle) {

                            val isValid = executionDates.isNotEmpty()
                            if(!isValid) Text("No Payments defined")

                            // State
                            val initDate = (executionDateState?:today()).format(Locale.Iso)
                            val dateOptions = executionDates.associateBy {
                                date -> date.format(Locale.Iso)
                            }
                            Label("Execution Date", id = "date" , labelStyle = formLabelDesktopStyle)
                            Dropdown(
                                options = dateOptions,
                                selected = initDate,
                                styles = dropdownStyles,
                                iconContent = {opened -> SimpleUpDown(opened) }
                            ) {
                                (_, value) -> executionDateState = value
                            }
                        }
                    }
                    Button({
                        if(executionDateState == null) disabled()
                        onClick {
                            if(executionDateState == null) return@onClick
                            setManageCollectionPayments(ManageCollectionPayments.CreateMessage(
                            executionDateState!!
                        )) }
                    }) {
                        Text("Generate Sepa Message")
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
