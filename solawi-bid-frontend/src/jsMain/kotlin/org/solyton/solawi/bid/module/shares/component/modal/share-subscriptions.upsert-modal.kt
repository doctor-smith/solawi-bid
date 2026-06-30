package org.solyton.solawi.bid.module.shares.component.modal


import androidx.compose.runtime.*
import org.evoleq.change.data.Change
import org.evoleq.change.data.Keep
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.now
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.language.subComp
import org.evoleq.language.texts
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
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
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.service.profile.fullname
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement


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
    },
) {

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
    val today = now()
    val currentFiscalYear = fiscalYears.emit().first {
        today.year in it.start.year..it.end.year
    }
    val initialFiscalYear = shareSubscription?.fiscalYearId?.let { fiscalYearId ->
        fiscalYears.emit().first { it.fiscalYearId == fiscalYearId }
    } ?: currentFiscalYear
    var fiscalYearState by remember { mutableStateOf(initialFiscalYear) }

    var pricePerShareState by remember { mutableStateOf(shareSubscription?.pricePerShare ?: 0.0) }
    var numberOfSharesState by remember { mutableStateOf(shareSubscription?.numberOfShares ?: 0) }
    var ahcAuthorizedState by remember { mutableStateOf(shareSubscription?.ahcAuthorized ?: false) }

    // distribution point state
    val initialDistributionPoint = shareSubscription?.distributionPointId?.let { distributionPointId ->
        distributionPoints.emit().first { it.distributionPointId == distributionPointId }
    }
    var distributionPointState by remember { mutableStateOf(initialDistributionPoint) }

    var coSubscribersState by remember { mutableStateOf(shareSubscription?.coSubscribers ?: emptyList()) }

    var statusState by remember { mutableStateOf(shareSubscription?.status ?: ShareStatus.PendingActivation) }

    val dropdownStyles = DropdownStyles().modifyContainerStyle {
        width(200.px)
    }
    val dropdownFieldStyles: StyleScope.() -> Unit = {
        fieldDesktopStyle()
        alignItems(AlignItems.Start)
    }

    val inputs = Source{texts} * subComp("upsertShareSubscriptionForm") * subComp("inputs")

    Scrollable(ScrollableStyles().modifyContainerStyle {}) {
        Form(formDesktopStyle) {

            Field(dropdownFieldStyles){
                Label(
                    text = "Member",
                    id = "member",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                P{Text(userState?.profile?.fullname() ?: "Select member")}
                When(shareSubscription == null) {
                    // Member selection box (Search box)
                    var searchUsersResult by remember { mutableStateOf(emptyList<ManagedUser>()) }
                    SearchInput(
                        initialUser?.profile.fullname(),
                        SearchInputStyles(),
                        true
                    ) {
                        searchUsersResult = users.filter { user ->
                            user.profile.fullname().contains(it, true) ||
                                    user.username.contains(it, true)
                        }
                    }
                    val listStyles = ListStyles().modifyListItemWrapper {
                        minHeight(300.px)
                    }
                    ListWrapper {
                        HeaderWrapper {
                            Header {
                                HeaderCell("Username") { width(50.percent) }
                                HeaderCell("Fullname") { width(50.percent) }
                            }
                        }
                        Scrollable(ScrollableStyles().modifyContainerStyle {
                            maxHeight(300.px)
                        }) {
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
                                            { "Select / Deselect user" },
                                            device,
                                            false,
                                            null,
                                        ) {
                                            userState = when {
                                                userState != user -> user
                                                else -> null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Horizontal({
                JustifyContent.FlexStart
            }) {
                // Fiscal year dropdown
                Field(dropdownFieldStyles) {
                    Label(
                        text = "Fiscal Year",
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
                    val options = shareOffers.emit().associateBy { it.shareType.name + " - " + it.fiscalYear.format() }
                    Label(
                        text = "Share Offer",
                        id = "share-offer",
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    Dropdown(
                        options = options,
                        selected = shareOfferState?.let { it.shareType.name + " - " + it.fiscalYear.format() }
                            ?: "Select share offer",
                        styles = dropdownStyles.modifyContainerStyle {
                            width(300.px)
                        },
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
                    Label(
                        text = "Distribution Point",
                        id = "distribution-point",
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    val options = distributionPoints.emit().associateBy { it.name }
                    Dropdown(
                        options = options,
                        selected = distributionPointState?.name ?: "Select distribution point",
                        styles = dropdownStyles.modifyContainerStyle {
                            width(300.px)
                        },
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
            }
            Horizontal({
                JustifyContent.FlexStart
            }) {
                // Share status dropdown
                Field(dropdownFieldStyles) {
                    Label(
                        text = "Share Status",
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
                        styles = dropdownStyles.modifyContainerStyle {
                            width(300.px)
                        },
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
                        text  ="Price per share",
                        id = "price-per-share",
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    TextInput(
                        pricePerShareState.toString(),
                    ) {
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
                        text = "Number of shares",
                        id = "number-of-shares",
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    TextInput(
                        numberOfSharesState.toString(),
                    ) {
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
                                            console.log("numberOfSharesState: $numberOfSharesState")
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
                        text = "AHC authorized",
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
                        (inputs * subComp("ahcAuthorized") * checkIt(
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
                        styles = dropdownStyles.modifyContainerStyle {
                            width(75.px)
                        },
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

val upsertShareSubscriptionFormTexts = Source {
    "upsertShareSubscriptionForm" texts {
        "inputs" block {
            "fiscalYear" block{
               "title"  colon "Fiscal Year"
            }
            "shareOffer" block{
               "title"  colon "Share Offer"
            }
            "distributionPoint" block{
               "title"  colon "Distribution Point"
            }
            "numberOfShares" block{
               "title"  colon "Number of Shares"
            }
            "pricePerShare" block{
                "title"  colon "Price Per Share"
            }
            "ahcAuthorized" block{
                "title"  colon "AHC Authorized"
                "true"  colon "☑️"
                "false" colon "❌"
            }
            "coSubscribers" block{
                "title"  colon "Co-Subscribers"
            }
        }
    }
}
