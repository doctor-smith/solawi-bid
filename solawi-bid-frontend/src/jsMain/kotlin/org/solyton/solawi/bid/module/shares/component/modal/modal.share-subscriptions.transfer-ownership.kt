package org.solyton.solawi.bid.module.shares.component.modal


import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.evoleq.change.data.Change
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
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
import org.evoleq.language.*
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.control.button.SquareCheckButton
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.loading.component.LoadingContent
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.management.ShareManagement
import org.solyton.solawi.bid.module.shares.data.management.deviceData
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.i18n.Component
import org.solyton.solawi.bid.module.shares.i18n.LangField
import org.solyton.solawi.bid.module.shares.i18n.LangForm
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.user.service.profile.fullname
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement
import kotlin.time.Duration.Companion.milliseconds


@Markup
fun Storage<Modals<Int>>.showTransferOwnershipOfShareSubscriptionModal(
    storage: Storage<ShareManagement>,
    texts: Lang.Block = transferOwnershipOfShareSubscriptionModalTexts.emit(),
    device: Source<DeviceType>,
    providerId: ProviderId,
    users: Source<List<ManagedUser>>,
    changesDoneBy: ChangedBy,
    isOkButtonDisabled: () -> Boolean = { false },
    shareSubscription: ShareSubscription,
    newOwner: UserProfile?,
    setNewOwner: (UserProfile) -> Unit = {},
    update: () -> Unit = { }
) = with(nextId()) {
    put(
        this to ModalData(
            this,
            ModalType.Dialog,
            TransferOwnershipOfShareSubscriptionModal(
                this,
                texts,
                this@showTransferOwnershipOfShareSubscriptionModal,
                device,
                storage,
                providerId,
                users.emit(),
                changesDoneBy,
                isOkButtonDisabled,
                shareSubscription,
                newOwner,
                setNewOwner,
                update = update
            )
        )
    )
}

@Markup
@Suppress("FunctionName", "CyclomaticComplexMethod", "UnusedParameter")
fun TransferOwnershipOfShareSubscriptionModal(
    id: Int,
    texts: Lang.Block = transferOwnershipOfShareSubscriptionModalTexts.emit(),
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    // Data
    storage: Storage<ShareManagement>,
    providerId: ProviderId,
    users: List<ManagedUser>,
    changesDoneBy: ChangedBy,
    isOkButtonDisabled: () -> Boolean,
    shareSubscription: ShareSubscription,

    newOwner: UserProfile?,
    // Action
    setNewOwner: (UserProfile) -> Unit,
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
    isOkButtonDisabled = isOkButtonDisabled,
    dataId = "shares.modal.transfer-ownership",
) {

    val scope = rememberCoroutineScope()

    val shareSubscriptionId = shareSubscription?.shareSubscriptionId ?: NIL_UUID

    // user state
    val initialUser = shareSubscription?.userProfileId?.let { userProfileId ->
        requireNotNull(users.firstOrNull { it.profile?.userProfileId == userProfileId }) {
            "user is null"
        }
    }

    var userState by remember { mutableStateOf(initialUser) }


    // AHC Authorized
    // var ahcAuthorizedState by remember { mutableStateOf(shareSubscription?.ahcAuthorized ?: false) }


    // CoSubscribers
    // var coSubscribersState by remember { mutableStateOf(shareSubscription?.coSubscribers ?: emptyList()) }

    // Styles
    /*
    val dropdownStyles = DropdownStyles().modifyContainerStyle {
        width(100.percent)
    }.modifyDropdownContentStyle {
        zIndex(DIALOG_LAYER_INDEX + 100)
    }
    */
    val dropdownFieldStyles: StyleScope.() -> Unit = {
        width(100.percent)
        fieldDesktopStyle()
        alignItems(AlignItems.Start)
    }




    // I18n
    val inputs = Source{texts} * transferOwnershipOfShareSubscriptionForm * LangForm.inputs


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
                    dataId("shares.modal.transfer-ownership.user-display-input")
                    style{
                        marginBottom(10.px)
                        width(100.percent)
                    }
                }
                // When(shareSubscription == null) {
                    // Member selection box (Search box)
                    var searchUsersResult by remember { mutableStateOf(users) }
                    var isListReady by remember { mutableStateOf(false) }

                    val selectUser = { user: ManagedUser ->
                        update(
                            ShareHolderChange(
                                shareSubscriptionId,
                                providerId,
                                Change(userState, user) {
                                    userState = when {
                                        userState != user -> user
                                        else -> null
                                    }
                                },
                            )
                        ) { data ->
                            setNewOwner(data)
                        }
                    }

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
                        true,
                        isDisabled = {false},
                        dataId = "shares.modal.transfer-ownership.user-search-input",
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
                                                "shares.modal.transfer-ownership.user.${user.username}.select-button",
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


            /* Keep, maybe interesting later
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
                    iconContent = { expanded -> SimpleUpDown(expanded) },
                    isDisabled = {!configuration.enabledInputs.ahcAuthorized},
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
                    if(!configuration.enabledInputs.coSubscribers)
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
            */
        }
    }
}


data class ShareHolderChange(
    val shareSubscriptionId: String,
    val providerId: ProviderId,
    val user: Change<ManagedUser>,
    // val coSubscribers: Change<List<String>>,
    // val ahcAuthorized: Change<Boolean>,
)

fun validate(change: ShareHolderChange): List<Exception> {
    val errors = mutableListOf<Exception>(

    )
    if (change.user.new == null) {
        errors + IllegalArgumentException("User is required")
    }

    return errors
}



fun update(change: ShareHolderChange, update: (UserProfile) -> Unit) {
    try {
        update(
            change.user.new!!.profile!!
        )
    } catch (_: Exception) {
    } finally {
        change.user.onChange()
    }
}


@I18N
val transferOwnershipOfShareSubscriptionModalTexts = Source {
    "transferOwnershipOfShareSubscriptionModal" texts {
        "title" colon "Create or Update Share Subscription"
        "okButton" block {
            "title" colon "Ok"
        }
        "cancelButton" block {
            "title" colon "Cancel"
        }
        +transferOwnershipOfShareSubscriptionFormTexts.emit()
    }
}

@I18N
val transferOwnershipOfShareSubscriptionFormTexts = Source {
    "transferOwnershipOfShareSubscriptionForm" texts {
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


val transferOwnershipOfShareSubscriptionModal: Component = { block -> block.component("transferOwnershipOfShareSubscriptionModal") }
val transferOwnershipOfShareSubscriptionForm: Component = { block -> block.component("transferOwnershipOfShareSubscriptionForm") }
