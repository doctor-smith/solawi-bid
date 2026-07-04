package org.solyton.solawi.bid.module.shares.component.modal


import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.evoleq.change.data.Change
import org.evoleq.change.data.Keep
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.conditional.When
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.now
import org.evoleq.language.*
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.SquareCheckButton
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.loading.component.LoadingContent
import org.solyton.solawi.bid.module.modal.constants.DIALOG_LAYER_INDEX
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.deviceData
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.i18n.Component
import org.solyton.solawi.bid.module.shares.i18n.LangField
import org.solyton.solawi.bid.module.shares.i18n.LangForm
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.zindex.zIndex
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.service.profile.fullname
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement
import kotlin.time.Duration.Companion.milliseconds


@Markup
fun Storage<Modals<Int>>.showUpsertShareSubscriptionModal(
    storage: Storage<ShareManagement>,
    texts: Lang.Block = upsertShareSubscriptionModalTexts.emit(),
    device: Source<DeviceType>,
    fiscalYears: Source<List<FiscalYear>>,
    distributionPoints: Source<List<DistributionPoint>>,
    shareOffers: Source<List<ShareOffer>>,
    providerId: ProviderId,
    users: Source<List<ManagedUser>>,
    changesDoneBy: ChangedBy,
    shareSubscription: ShareSubscription? = null,
    setShareSubscription: (ShareSubscription) -> Unit = {},
    update: () -> Unit
) = with(nextId()) {
    put(
        this to ModalData(
            this,
            ModalType.Dialog,
            UpsertShareSubscriptionModal(
                this,
                texts,
                this@showUpsertShareSubscriptionModal,
                device,
                storage,
                fiscalYears,
                distributionPoints,
                shareOffers,
                providerId,
                users.emit(),
                changesDoneBy,
                shareSubscription,
                setShareSubscription,
                update = update
            )
        )
    )
}

@Markup
@Suppress("FunctionName", "CyclomaticComplexMethod")
fun UpsertShareSubscriptionModal(
    id: Int,
    texts: Lang.Block = upsertShareSubscriptionModalTexts.emit(),
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    // Data
    storage: Storage<ShareManagement>,
    fiscalYears: Source<List<FiscalYear>>,
    distributionPoints: Source<List<DistributionPoint>>,
    shareOffers: Source<List<ShareOffer>>,
    providerId: ProviderId,
    users: List<ManagedUser>,
    changesDoneBy: ChangedBy,
    shareSubscription: ShareSubscription?,
    // Action
    setShareSubscription: (ShareSubscription) -> Unit,
    update: () -> Unit
): @Composable ElementScope<HTMLElement>.() -> Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = storage * deviceData * mediaType.get,
    onOk = {
        update()
    },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device).modifyContainerStyle {
        height(80.vh)
    }.modifyContentWrapperStyle {
        minWidth(50.vw)
    }.compact(),
) {

    val scope = rememberCoroutineScope()

    val shareSubscriptionId = shareSubscription?.shareSubscriptionId ?: NIL_UUID

    // user state
    val initialUser = shareSubscription?.userProfileId?.let { userProfileId ->
        users.first { it.profile?.userProfileId == userProfileId }
    }
    var userState by remember { mutableStateOf(initialUser) }

    // share offer state
    val initialShareOffer = shareSubscription?.shareOfferId?.let { shareOfferId ->
        shareOffers.emit().first { it.shareOfferId == shareOfferId }
    }
    var shareOfferState by remember { mutableStateOf(initialShareOffer) }

    // fiscal year state
    // If no fiscal year is selected, we use the current fiscal year
    // If the modal is opened for a new share subscription, we use the current fiscal year
    // Otherwise, we use the fiscal year of the share subscription
    // IMPORTANT:
    // - If the share subscription is has status other then EXTERNAL or PENDING_ACTIVATION, the fiscal year is not to be edited at all!
    // - The set of available share offers varies with fiscal year
    val today = now()
    val currentFiscalYear = fiscalYears.emit().first {
        today.year in it.start.year..it.end.year
    }
    val initialFiscalYear = shareSubscription?.fiscalYearId?.let { fiscalYearId ->
        fiscalYears.emit().first { it.fiscalYearId == fiscalYearId }
    } ?: currentFiscalYear
    var fiscalYearState by remember { mutableStateOf(initialFiscalYear) }

    // Price per Share
    // Here we guarantee that the initial price per share is set correctly:
    // if the price is null and the share type is FIXED, we use the price from the share-offer,
    // otherwise we use 0.0
    val initialPricePerShare = shareSubscription?.pricePerShare ?: initialShareOffer?.let{
        when(it.shareType.name) {
            "FIXED" -> it.price
            else -> null
        }
    } ?: 0.0
    var pricePerShareState by remember { mutableStateOf(initialPricePerShare) }

    // Number of Shares
    var numberOfSharesState by remember { mutableStateOf(shareSubscription?.numberOfShares ?: 0) }

    // AHC Authorized
    var ahcAuthorizedState by remember { mutableStateOf(shareSubscription?.ahcAuthorized ?: false) }

    // distribution point state
    val initialDistributionPoint = shareSubscription?.distributionPointId?.let { distributionPointId ->
        distributionPoints.emit().first { it.distributionPointId == distributionPointId }
    }

    // initial distribution point state
    var distributionPointState by remember { mutableStateOf(initialDistributionPoint) }

    // CoSubscribers
    var coSubscribersState by remember { mutableStateOf(shareSubscription?.coSubscribers ?: emptyList()) }

    // Status
    var statusState by remember { mutableStateOf(shareSubscription?.status ?: ShareStatus.PendingActivation) }

    // Effects
    LaunchedEffect(fiscalYearState) {
        // Restrict the
    }


    // Styles
    val dropdownStyles = DropdownStyles().modifyContainerStyle {
        width(100.percent)
    }.modifyDropdownContentStyle {
        zIndex(DIALOG_LAYER_INDEX + 100)
    }
    val dropdownFieldStyles: StyleScope.() -> Unit = {
        width(100.percent)
        fieldDesktopStyle()
        alignItems(AlignItems.Start)
    }

    // I18n
    val inputs = Source{texts} * upsertShareSubscriptionForm * LangForm.inputs


    Scrollable(ScrollableStyles().modifyContainerStyle {}) {
        Form(formDesktopStyle) {
            Field(dropdownFieldStyles){
                val userField = inputs * LangField.user
                val listOfUsers = userField * LangField.listOfUsers
                val searchBox = userField * LangField.searchBox
                Label(
                    text = userField * title,
                    id = "user",
                    labelStyle = {formLabelDesktopStyle()},
                    isRequired = true
                )
                TextInput(userState?.profile?.fullname() ?: "") {
                    disabled()
                    placeholder((searchBox * LangField.placeholder).emit())
                    style{
                        marginBottom(10.px)
                        width(100.percent)
                    }
                }
                When(shareSubscription == null) {
                    // Member selection box (Search box)
                    var searchUsersResult by remember { mutableStateOf(users) }
                    var isListReady by remember { mutableStateOf(false) }

                    val selectUser = { user: ManagedUser -> userState = when {
                        userState != user -> user
                        else -> null
                    }}

                    LaunchedEffect(searchUsersResult) {
                        isListReady = false
                        delay(10.milliseconds) // Small delay to allow UI to render first
                        isListReady = true
                    }

                    SearchInput(
                        initialUser?.profile.fullname(),
                        SearchInputStyles().modifyContainerStyle {
                            width(100.percent)
                        },
                        true
                    ) {
                        scope.launch {
                            searchUsersResult = users.filter { user ->
                                user.profile.fullname().contains(it, true) ||
                                        user.username.contains(it, true)
                            }
                        }
                    }
                    val listStyles = ListStyles().modifyListItemWrapper {
                        minHeight(250.px)
                    }

                    ListWrapper {
                        HeaderWrapper {
                            Header {
                                HeaderCell(listOfUsers * LangField.header * LangField.username * title) { width(50.percent) }
                                HeaderCell(listOfUsers * LangField.header * LangField.fullName * title) { width(50.percent) }
                            }
                        }
                        Scrollable(ScrollableStyles().modifyContainerStyle {
                            maxHeight(300.px)
                            minHeight(300.px)
                        }) {
                            When(!isListReady) {
                                LoadingContent()
                            }
                            When(isListReady) {
                                
                                ListItemsIndexed(searchUsersResult) { index, user ->
                                    ListItemWrapper({
                                        listItemWrapperStyle(this, index)
                                    }) {
                                        DataWrapper(listStyles.dataWrapper) {
                                            TextCell(user.username) { width(50.percent) }
                                            TextCell(user.profile.fullname()) { width(50.percent) }
                                        }
                                        ActionsWrapper {
                                            SquareCheckButton(
                                                Color.black,
                                                Color.white,
                                                listOfUsers * LangField.actions * LangField.selectUser * LangField.tooltip * text,
                                                device,
                                                false,
                                                null,
                                            ) {
                                                
                                                scope.launch {
                                                    selectUser(user)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Fiscal year dropdown
            Field(dropdownFieldStyles) {
                val fiscalYearField = inputs * LangField.fiscalYear
                Label(
                    text = fiscalYearField * title,
                    id = "fiscal-year",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )

                val options = fiscalYears.emit().associateBy { it.format() }
                Dropdown(
                    options = options,
                    selected = fiscalYearState.format(),
                    styles = dropdownStyles,
                    iconContent = { expanded -> SimpleUpDown(expanded) }
                ) { (_, value) ->
                    update(
                        ShareSubscriptionChange(
                            shareSubscriptionId,
                            providerId,
                            Change(
                                fiscalYearState,
                                value
                            ) {
                                fiscalYearState = value
                            },
                            Keep(userState),
                            Keep(shareOfferState),
                            Keep(distributionPointState),
                            Keep(pricePerShareState),
                            Keep(numberOfSharesState),
                            Keep(coSubscribersState),
                            Keep(ahcAuthorizedState),
                            Keep(statusState)
                        )
                    ) { data ->
                        setShareSubscription(data)
                    }
                }
            }


            // Share offer dropdown
            Field(dropdownFieldStyles) {
                val shareOfferField = inputs * LangField.shareOffer

                val options = shareOffers.emit().filter { it.fiscalYear == fiscalYearState }.associateBy { it.shareType.name + " - " + it.fiscalYear.format() }
                Label(
                    text = shareOfferField * title,
                    id = "share-offer",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                Dropdown(
                    options = options,
                    selected = shareOfferState?.let { it.shareType.name + " - " + it.fiscalYear.format() }
                        ?: (shareOfferField * LangField.placeholder).emit(),
                    styles = dropdownStyles,
                    iconContent = { expanded -> SimpleUpDown(expanded) }
                ) { (_, value) ->
                    update(
                        ShareSubscriptionChange(
                            shareSubscriptionId,
                            providerId,
                            Keep(fiscalYearState),
                            Keep(userState),
                            Change(
                                shareOfferState,
                                value
                            ) {
                                shareOfferState = value
                            },
                            Keep(distributionPointState),
                            Keep(pricePerShareState),
                            Keep(numberOfSharesState),
                            Keep(coSubscribersState),
                            Keep(ahcAuthorizedState),
                            Keep(statusState)
                        )
                    ) { data ->
                        setShareSubscription(data)
                    }
                }
            }
            // Distribution point dropdown
            Field(dropdownFieldStyles) {
                val distributionPointField = inputs * LangField.distributionPoint
                Label(
                    text = distributionPointField * title,
                    id = "distribution-point",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                val options = distributionPoints.emit().associateBy { it.name }
                Dropdown(
                    options = options,
                    selected = distributionPointState?.name ?: "Select distribution point",
                    styles = dropdownStyles,
                    iconContent = { expanded -> SimpleUpDown(expanded) }
                ) { (_, value) ->
                    update(
                        ShareSubscriptionChange(
                            shareSubscriptionId,
                            providerId,
                            Keep(fiscalYearState),
                            Keep(userState),
                            Keep(shareOfferState),
                            Change(
                                distributionPointState,
                                value
                            ) { distributionPointState = value },
                            Keep(pricePerShareState),
                            Keep(numberOfSharesState),
                            Keep(coSubscribersState),
                            Keep(ahcAuthorizedState),
                            Keep(statusState)

                        )
                    ) { data ->
                        setShareSubscription(data)
                    }
                }
            }
            // Share status dropdown
            Field(dropdownFieldStyles) {
                Label(
                    text = inputs * LangField.shareStatus * title,
                    id = "share-status",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                val allowedShareStatusTransitionTargets = requireNotNull(
                    shareStatusTransitionsWithPermissions[statusState]
                ) {
                    "Share status transition not found for status $statusState"
                }.filter { it.permissions[changesDoneBy] != null }.associateBy({ it.shareStatus.value }) {
                    it.shareStatus
                }// + (shareSubscription.status.value to shareSubscription.status)
                @Suppress("UnusedPrivateProperty")
                val changeReasons = requireNotNull(
                    shareStatusTransitionsWithPermissions[statusState]
                ) {
                    "Share status transition not found for status $statusState"
                }.filter { it.permissions[changesDoneBy] != null }.associateBy({ it.shareStatus.value }) {
                    it.permissions[changesDoneBy].orEmpty()
                }

                Dropdown(
                    options = allowedShareStatusTransitionTargets,
                    selected = statusState.value,
                    styles = dropdownStyles,
                    iconContent = { expanded -> SimpleUpDown(expanded) }
                ) {
                    (_, value) ->
                    update(ShareSubscriptionChange(
                        shareSubscriptionId,
                        providerId,
                        Keep(fiscalYearState),
                        Keep(userState),
                        Keep(shareOfferState),
                        Keep(distributionPointState),
                        Keep(pricePerShareState),
                        Keep(numberOfSharesState),
                        Keep(coSubscribersState),
                        Keep(ahcAuthorizedState),
                        Change(statusState, value) { statusState = value },

                    )) {
                        data -> setShareSubscription(data)
                    }
                }
            }
            // Price per share
            Field(dropdownFieldStyles) {
                Label(
                    text  = inputs * LangField.pricePerShare * title,
                    id = "price-per-share",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                TextInput(
                    pricePerShareState.toString(),
                ) {
                    style{width(100.percent)}
                    onInput {
                        event ->
                        val value = event.value.toDoubleOrNull() ?: 0.0
                        update(
                            ShareSubscriptionChange(
                                shareSubscriptionId,
                                providerId,
                                Keep(fiscalYearState),
                                Keep(userState),
                                Keep(shareOfferState),
                                Keep(distributionPointState),
                                Change(
                                    pricePerShareState,
                                    value
                                ) {
                                    pricePerShareState = value
                                },
                                Keep(numberOfSharesState),
                                Keep(coSubscribersState),
                                Keep(ahcAuthorizedState),
                                Keep(statusState),
                            )
                        ) {
                            data -> setShareSubscription(data)
                        }
                    }
                }
            }
            // Number of shares
            Field(dropdownFieldStyles) {
                Label(
                    text = inputs * LangField.numberOfShares * title,
                    id = "number-of-shares",
                    labelStyle = {formLabelDesktopStyle(); width(100.percent)},
                    isRequired = true
                )
                TextInput(
                    numberOfSharesState.toString(),
                ) {
                    style{width(100.percent)}
                    onInput { event ->
                            val value = event.value.toIntOrNull() ?: 0
                            update(
                                ShareSubscriptionChange(
                                    shareSubscriptionId,
                                    providerId, Keep(fiscalYearState),
                                    Keep(userState),
                                    Keep(shareOfferState),
                                    Keep(distributionPointState),
                                    Keep(pricePerShareState),
                                    Change(
                                        numberOfSharesState,
                                        value
                                    ) {
                                        numberOfSharesState = value
                                    },
                                    Keep(coSubscribersState),
                                    Keep(ahcAuthorizedState),
                                    Keep(statusState)
                                )
                            ) { data ->
                                setShareSubscription(data)
                            }

                    }
                }
            }
            // AHC authorized checkbox
            Field(dropdownFieldStyles) {
                Label(
                    text = inputs * LangField.ahcAuthorized * title,
                    id = "ahc-authorized",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                val checkIt: (Boolean) -> Reader<Lang.Block, String> = { bool: Boolean ->
                    Reader { lang: Lang.Block ->
                        lang["$bool"]
                    }
                }
                val check = { checked: Boolean ->
                    (inputs * LangField.ahcAuthorized * checkIt(
                        checked
                    )).emit()
                }
                val options = mapOf(
                    check(true) to true,
                    check(false) to false
                )
                Dropdown(
                    options = options,
                    selected = check(ahcAuthorizedState),
                    styles = dropdownStyles,
                    iconContent = { expanded -> SimpleUpDown(expanded) }
                ) {
                    (_, checked) ->
                    update(
                        ShareSubscriptionChange(
                            shareSubscriptionId,
                            providerId,Keep(fiscalYearState),
                            Keep(userState),
                            Keep(shareOfferState),
                            Keep(distributionPointState),
                            Keep(pricePerShareState),
                            Keep(numberOfSharesState),
                            Keep(coSubscribersState),
                            Change(
                                ahcAuthorizedState,
                                checked
                            ) {
                                ahcAuthorizedState = checked
                            },
                            Keep(statusState)
                        )
                    ) {
                        data -> setShareSubscription(data)
                    }
                }
            }
            // Co-subscribers (Search box + list of so-subscribers)
            Field(dropdownFieldStyles) {
                var forbiddenUsernames by remember { mutableStateOf(emptyList<String>()) }
                Label(
                    text = inputs * LangField.coSubscribers * title,
                    id = "co-subscribers",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                TextArea(coSubscribersState.joinToString(",\n")) {
                    style{
                        width(100.percent)
                        height(150.px)
                    }
                    onInput { event ->
                        val value = event.value.trim()
                        val values = value.split(",\n", ",", "\n").map { it.trim() }
                        val coSubscribers = when {
                            value.endsWith(",\n") ||
                            value.endsWith(",") ||
                            value.endsWith("\n") -> values
                            else -> values.filter { it.isNotEmpty() }
                        }

                        val usernames = users.map { it.username }.toSet()
                        forbiddenUsernames = coSubscribers.filter { coSubscriber -> usernames.none { it == coSubscriber } }
                        update(
                            ShareSubscriptionChange(
                                shareSubscriptionId,
                                providerId, Keep(fiscalYearState),
                                Keep(userState),
                                Keep(shareOfferState),
                                Keep(distributionPointState),
                                Keep(pricePerShareState),
                                Keep(numberOfSharesState),
                                Change(
                                    coSubscribersState,
                                    coSubscribers
                                ) {
                                    coSubscribersState = coSubscribers
                                },
                                Keep(ahcAuthorizedState),
                                Keep(statusState)
                            )
                        ) { data ->
                            setShareSubscription(data)
                        }

                    }
                }
                When(forbiddenUsernames.isNotEmpty()) {
                    Div({
                        style {
                            color(Color.red)
                        }
                    }) {
                        val message = (inputs * LangField.coSubscribers * LangField.messages * LangField.forbiddenUsers).emit()
                        P { Text(message) }
                        forbiddenUsernames.forEach { username ->
                        P {
                            Text(username) }
                        }
                    }
                }
            }
        }
    }
}



data class ShareSubscriptionChange(
    val shareSubscriptionId: String,
    val providerId: ProviderId,
    val fiscalYear: Change<FiscalYear>,
    val user: Change<ManagedUser>,
    val shareOffer: Change<ShareOffer>,
    val distributionPoint: Change<DistributionPoint>,
    val pricePerShare: Change<Double>,
    val numberOfShares: Change<Int>,
    val coSubscribers: Change<List<String>>,
    val ahcAuthorized: Change<Boolean>,
    val status: Change<ShareStatus>,
)

fun validate(change: ShareSubscriptionChange) {
    val errors = mutableListOf<Exception>(

    )
    if (change.user.new == null) {
        errors + IllegalArgumentException("User is required")
    }
    if (change.shareOffer.new == null) {
        errors + IllegalArgumentException("Share offer is required")
    }
    if (change.distributionPoint.new == null) {
        errors + IllegalArgumentException("Distribution point is required")
    }
    if (change.pricePerShare.new == null) {
        errors + IllegalArgumentException("Price per share is required")
    }
}

fun update(change: ShareSubscriptionChange, update: (ShareSubscription) -> Unit) {
    try {
        update(
            ShareSubscription(
                shareSubscriptionId = change.shareSubscriptionId,
                providerId = change.providerId.value,
                fiscalYearId = change.fiscalYear.new!!.fiscalYearId,
                userProfileId = change.user.new!!.profile!!.userProfileId,
                shareOfferId = change.shareOffer.new!!.shareOfferId,
                distributionPointId = change.distributionPoint.new!!.distributionPointId,
                pricePerShare = change.pricePerShare.new!!,
                numberOfShares = change.numberOfShares.new!!,
                coSubscribers = change.coSubscribers.new!!,
                ahcAuthorized = change.ahcAuthorized.new!!,
                status = change.status.new!!,
            )
        )
    } catch (_: Exception) {
    } finally {
        change.fiscalYear.onChange()
        change.user.onChange()
        change.shareOffer.onChange()
        change.distributionPoint.onChange()
        change.pricePerShare.onChange()
        change.numberOfShares.onChange()
        change.coSubscribers.onChange()
        change.ahcAuthorized.onChange()
        change.status.onChange()
    }
}


@I18N
val upsertShareSubscriptionModalTexts = Source {
    "upsertShareSubscriptionModal" texts {
        "title" colon "Create or Update Share Subscription"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
        +upsertShareSubscriptionFormTexts.emit()
    }
}

@I18N
val upsertShareSubscriptionFormTexts = Source {
    "upsertShareSubscriptionForm" texts {
        "inputs" block {
            "ahcAuthorized" block{
                "title"  colon "SEPA Mandate signed"
                "true"  colon "☑️"
                "false" colon "❌"
                "description"  colon "AHC Authorized is a flag that indicates whether the user has signed the AHC Authorized Agreement."
                "placeholder" colon "Select AHC Authorized"
            }
            "coSubscribers" block{
                "title"  colon "Co-Subscribers"
                "description"  colon "Co-subscribers are the users that are subscribed to the share offer."
                "placeholder" colon "Select co-subscribers"
                "messages" block {
                    "forbiddenUsers" colon "The following usernames are not allowed until you add them as members to your organization: "
                }
            }

            "distributionPoint" block{
               "title"  colon "Distribution Point"
               "description"  colon "Distribution point is the distribution point of the share offer that the user is subscribing to."
                "placeholder" colon "Select distribution point"
            }
            "fiscalYear" block{
                "title"  colon "Fiscal Year"
                "description"  colon "Fiscal year is the fiscal year of the share offer that the user is subscribing to."
                "placeholder" colon "Select fiscal year"
            }
            "numberOfShares" block{
               "title"  colon "Number of Shares"
                "description"  colon "Number of shares is the number of shares of the share offer that the user is subscribing to."
            }
            "pricePerShare" block{
                "title"  colon "Price Per Share"
                "description"  colon "Price per share is the price of a single share of the share offer."
            }
            "shareOffer" block{
                "title"  colon "Share Offer"
                "description"  colon "Share offer is the share offer that the user is subscribing to."
                "placeholder" colon "Select share offer"
            }
            "shareStatus" block{
                "title"  colon "Share Status"
                "description"  colon "Share status is the status of the share subscription."
                "placeholder" colon "Select share status"
            }
            "user" block {
                "title" colon "User Profile"
                "description" colon "User profile is the user profile of the user that is subscribing to the share offer."
                "listOfUsers" block {
                    "title" colon "List of Users"
                    "header" block {
                        "username" block {"title" colon "Username" }
                        "fullName" block {"title" colon "Full name"}
                    }
                    "actions" block {
                        "selectUser" block {
                            "tooltip" block {
                                "text" colon "Select / Deselect user"
                            }
                        }
                    }
                    "noUsers" colon "No users found"
                }
                "searchBox" block {
                    "title" colon "Search Box"
                    "placeholder" colon "Select user"
                }
            }
        }
    }
}


val upsertShareSubscriptionModal: Component = { block -> block.component("upsertShareSubscriptionModal") }
val upsertShareSubscriptionForm: Component = { block -> block.component("upsertShareSubscriptionForm") }
