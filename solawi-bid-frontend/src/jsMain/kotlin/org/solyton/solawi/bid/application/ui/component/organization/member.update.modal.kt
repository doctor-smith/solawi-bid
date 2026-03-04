package org.solyton.solawi.bid.application.ui.component.organization

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
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
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.ActionDispatcher
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.storage.read
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.shares.component.dropdown.ShareOffersDropdown
import org.solyton.solawi.bid.module.shares.data.api.ApiChangedBy
import org.solyton.solawi.bid.module.shares.data.api.ChangeReason
import org.solyton.solawi.bid.module.shares.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.*
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.component.styles.modalStyles
import org.solyton.solawi.bid.module.user.data.address.*
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.user.data.profile.addresses
import org.solyton.solawi.bid.module.values.ModifierId
import org.solyton.solawi.bid.module.values.Price
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.Username
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName", "UNUSED_PARAMETER", "CyclomaticComplexMethod")
fun UpdateMemberOfOrganizationModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    actions: ActionDispatcher<Application>,
    changesDoneBy: ChangedBy,
    currentUser: ManagedUser,
    organizationId: ProviderId,
    username: Username?,
    setUsername: (Username) -> Unit,
    userProfile: UserProfile?,
    setUserProfile: (UserProfile) -> Unit,
    importUserProfile: (UserProfileToImport) -> Unit,
    distributionPoints: List<DistributionPoint>,
    shareOffers: List<ShareOffer>,
    shareSubscriptions: ShareSubscriptions?,
    setShareSubscriptions: (ShareSubscriptions) -> Unit,
    updateShareStatus: suspend (UpdateShareStatus) -> Unit,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount) -> Unit,
    isOkButtonDisabled: ()->Boolean,
    cancel: ()->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id,
    modals,
    device,
    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    isOkButtonDisabled = isOkButtonDisabled,
    texts = texts.emit(),
    styles = modalStyles(device),
) {
    val scope = rememberCoroutineScope()
    val inputs = texts * subComp("inputs")



    Vertical({
        height(100.percent)
        overflowY("auto")}) {
        var userProfileState by remember { mutableStateOf(userProfile) }

        Form(formDesktopStyle) {

            // User Profile is used everywhere
            val userProfileInputs = inputs * subComp("userProfile")
            var usernameState by remember { mutableStateOf(username?.value) }


            Horizontal {

                Vertical({ width(50.percent) }) {
                    H3{Text((userProfileInputs * Reader{lang ->lang["formTitle"]}).emit())}
                    Field(fieldDesktopStyle) {
                        Label(
                            (userProfileInputs * subComp("username") * title).emit(),
                            id = "username",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(usernameState ?: "") {
                            id("username")
                            style { textInputDesktopStyle() }
                            onInput {
                                usernameState = it.value
                                try {
                                    val username = Username(it.value.trim().lowercase())
                                    setUsername(username)
                                } catch (exception: Exception) {
                                    console.log("Username is not valid")
                                }
                            }
                        }
                    }
                    Field(fieldDesktopStyle) {
                        Label(
                            (userProfileInputs * subComp("title") * title).emit(),
                            id = "title",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(userProfileState?.title ?: "") {
                            id("title")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile =
                                    userProfileState?.copy(title = it.value) ?: UserProfile("", "", "", it.value)
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (userProfileInputs * subComp("firstname") * title).emit(),
                            id = "firstname",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(userProfileState?.firstname ?: "") {
                            id("firstname")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile =
                                    userProfileState?.copy(firstname = it.value) ?: UserProfile("", it.value, "")
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (userProfileInputs * subComp("lastname") * title).emit(),
                            id = "lastname",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(userProfileState?.lastname ?: "") {
                            id("lastname")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile =
                                    userProfileState?.copy(lastname = it.value) ?: UserProfile("", "", it.value)
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }
                }

                Vertical({width(50.percent)}) {
                    // Address
                    val addressInputs = userProfileInputs * subComp("address")
                    val address = userProfileState?.addresses?.firstOrNull()

                    H3{Text((addressInputs * title).emit())}
                    Field(fieldDesktopStyle) {

                        Label(
                            (addressInputs * subComp("recipientName") * title).emit(),
                            id = "recipientName",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.recipientName ?: "") {
                            id("recipientName")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.recipientName { it.value }?: Address.default().recipientName{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().recipientName{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("organizationName") * title).emit(),
                            id = "organizationName",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.organizationName?: "") {
                            id("organizationName")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.organizationName { it.value }?: Address.default().organizationName{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().organizationName{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("addressLine1") * title).emit(),
                            id = "addressLine1",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.addressLine1 ?: "") {
                            id("addressLine1")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.addressLine1 { it.value }?: Address.default().addressLine1{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().addressLine1{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("addressLine2") * title).emit(),
                            id = "addressLine2",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.addressLine2 ?: "") {
                            id("addressLine2")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.addressLine2 { it.value }?: Address.default().addressLine2{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().addressLine2{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("city") * title).emit(),
                            id = "city",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.city ?: "") {
                            id("city")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.city { it.value }?: Address.default().city{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().city{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("stateOrProvince") * title).emit(),
                            id = "stateOrProvince",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.stateOrProvince ?: "") {
                            id("stateOrProvince")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.stateOrProvince { it.value }?: Address.default().stateOrProvince{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().stateOrProvince{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("postalCode") * title).emit(),
                            id = "postalCode",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.postalCode ?: "") {
                            id("postalCode")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.postalCode { it.value }?: Address.default().postalCode{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().postalCode{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("countryCode") * title).emit(),
                            id = "countryCode",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(address?.countryCode ?: "") {
                            id("countryCode")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses{
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.countryCode { it.value }?: Address.default().countryCode{it.value}
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().countryCode{it.value}) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }
                }
            }
            // var showUpdateProfileButton by remember {mutableStateOf(true)}
            if(( username == null || userProfile == null)) {
                StdButton(
                    texts = {"Create Profile"},
                    deviceType = device,
                    disabled =  usernameState == null || userProfileState == null || userProfileState?.addresses?.isEmpty()?:false,
                    styles = {},
                    dataId = "updateProfileButton",
                ) {
                    val un = requireNotNull(usernameState) { "Username is null" }
                    val up = requireNotNull(userProfileState) { "User profile is null" }
                    scope.launch {

                        importUserProfile(UserProfileToImport(
                            username = un,
                            firstName = up.firstname,
                            lastName = up.lastname,
                            title = up.title,
                            phoneNumber = up.phoneNumber,
                            address = with(up.addresses.first()) {
                                CreateAddress(
                                    recipientName = recipientName,
                                    organizationName = organizationName,
                                    addressLine1 = addressLine1,
                                    addressLine2 = addressLine2,
                                    city = city,
                                    stateOrProvince = stateOrProvince,
                                    postalCode = postalCode,
                                    countryCode = countryCode

                                )
                            }))

                        /*
                        val action = memberCreateAction(
                            organizationId,
                            Username(un),
                            Change(null, up),
                            Change<BankAccount>(null, null),
                            Change<ShareSubscriptions>(null, null)
                        ).first()
                        actions dispatchEnvelope action
                        // set data and trigger rerendering
                        // todo:dev rerender does not work!! For subscriptions, a real userprofile is needed
                        setUsername(Username(un))
                        setUserProfile(up)

                         */
                    }
                }
            }
        }
        Form(formDesktopStyle) {
            // Bank account
            val bankAccountInputs = inputs * subComp("bankAccount")
            var ibanState by remember { mutableStateOf(bankAccount?.iban?.value) }

            H3{Text((bankAccountInputs * title).emit())}
            Field(fieldDesktopStyle) {

                Label(
                    (bankAccountInputs * subComp("iban") * title).emit(),
                    id = "iban",
                    labelStyle = formLabelDesktopStyle
                )
                TextInput(ibanState ?: "") {
                    id("iban")
                    style { textInputDesktopStyle() }
                    onInput {
                        try {
                            val newBankAccount = requireNotNull(bankAccount).copy(iban = IBAN(it.value))
                            setBankAccount(newBankAccount)
                        } catch (exception: Exception) {
                            // validation stuff
                        } finally {
                            ibanState = it.value
                        }
                    }
                }
            }
            var bicState by remember { mutableStateOf(bankAccount?.bic?.value) }
            Field(fieldDesktopStyle) {
                Label(
                    (bankAccountInputs * subComp("bic") * title).emit(),
                    id = "bic",
                    labelStyle = formLabelDesktopStyle
                )
                TextInput(bicState ?: "") {
                    id("bic")
                    style { textInputDesktopStyle() }
                    onInput {
                        try {
                            val newBankAccount = requireNotNull(bankAccount).copy(bic = BIC(it.value))
                            setBankAccount(newBankAccount)
                        } catch (exception: Exception) {
                            // validation stuff
                        } finally {
                            bicState = it.value
                        }
                    }
                }
            }
        }

        if(userProfile == null || distributionPoints.isEmpty()) return@Vertical
        Form(formDesktopStyle) {
            val shareOffersMap = shareOffers.associateBy {
                shareOffer -> shareOffer.shareOfferId
            }
            // Subscriptions
            val subscriptionsInputs = inputs * subComp("listOfShareSubscriptions")
            val subscriptionHeaders = subscriptionsInputs * subComp("headers")
            var shareSubscriptions by remember { mutableStateOf(shareSubscriptions) }
            val unsubscribedShareOffers = shareOffers.filter { shareOffer ->
                shareOffer.shareOfferId !in (shareSubscriptions?.all?.map { it.shareOfferId } ?: emptyList())
            }
            ListWrapper {
                TitleWrapper {
                    Title { H3{Text((subscriptionsInputs * title).emit())} }
                    if(unsubscribedShareOffers.isEmpty()) return@TitleWrapper
                    ShareOffersDropdown(
                        options = unsubscribedShareOffers.associateBy {
                            it.fiscalYear.format() + "/" +it.shareType.name
                        },
                        selected = null,
                        closeOnSelect = true
                    ) { (_, shareOffer) ->

                        shareSubscriptions = (shareSubscriptions?.all.orEmpty() + ShareSubscription(
                            NIL_UUID,
                            shareOffer.shareType.providerId,
                            shareOffer.shareOfferId,
                            userProfile.userProfileId,
                            null,
                            shareOffer.fiscalYear.fiscalYearId,
                            1,
                            null,
                            false,
                            ShareStatus.PendingActivation,
                            emptyList()

                        )).let { ShareSubscriptions(it) }
                    }
                }
                val tableStyles = defaultListStyles

                    .modifyHeaderWrapper {
                        width(100.percent)
                        justifyContent(JustifyContent.SpaceBetween)
                    }
                /*
                        .modifyHeader { width(90.percent) }
                    .modifyDataWrapper { width(90.percent) }
                    .modifyActionsWrapper { width(10.percent) }
                */

                HeaderWrapper(tableStyles.headerWrapper) {
                    Header(tableStyles.header) {
                        HeaderCell(subscriptionHeaders * subComp("fiscalYear") * title){
                            width(10.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("shareType") * title){
                            width(10.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("pricingType") * title){
                            width(10.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("numberOfShares") * title){
                            width(5.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("pricePerShare") * title){
                            width(5.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("state") * title){
                            width(10.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("ahcAuthorized") * title){
                            width(5.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("depository") * title){
                            width(5.percent)
                        }
                        HeaderCell(subscriptionHeaders * subComp("coSubscribers") * title){
                            width(40.percent)
                        }
                    }
                }
                val checkIt: (Boolean) -> Reader<Lang.Block, String> = {bool: Boolean -> Reader{
                    lang: Lang.Block -> lang["$bool"]
                }}
                ListItemsIndexed( shareSubscriptions?.all?:emptyList()) { index, shareSubscription ->
                    var editShareSubscriptionState by remember { mutableStateOf(false) }
                    val shareOffer = requireNotNull(shareOffersMap[shareSubscription.shareOfferId]) {
                        "Share offer not found"
                    }
                    ListItemWrapper({

                        listItemWrapperStyle(this, index)
                        if (editShareSubscriptionState) {
                            backgroundColor(Color.orange)
                            border {
                                style(LineStyle.Solid)
                                color(Color.orange)
                                width(1.px)
                            }
                        }
                    }) {
                        DataWrapper(defaultListStyles.dataWrapper) {
                            TextCell(shareOffer.fiscalYear.format()) {
                                width(10.percent)
                            }
                            TextCell(shareOffer.shareType.name) {
                                width(10.percent)
                            }
                            TextCell(shareOffer.pricingType.name) {
                                width(10.percent)
                            }
                            EditableIntCell(
                                initValue = shareSubscription.numberOfShares,
                                disabled = !editShareSubscriptionState,
                                style = { width(5.percent) }
                            ) {
                                numberOfShares ->
                                shareSubscriptions = requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when(shareSubscriptionIndex) {
                                        index -> shareSubscription.numberOfShares {
                                            numberOfShares?:0
                                        }
                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                                setShareSubscriptions(shareSubscriptions!!)
                            }
                            EditableNullablePriceCell(
                                initValue = (shareSubscription.pricePerShare ?: shareOffer.price)?.let { Price(it) },
                                disabled = shareOffer.pricingType == PricingType.FIXED || !editShareSubscriptionState,
                                style = { width(5.percent) }
                            ) { price ->
                                shareSubscriptions = requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when(shareSubscriptionIndex){
                                        index -> shareSubscription.pricePerShare{
                                            price?.value
                                        }
                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                                setShareSubscriptions(shareSubscriptions!!)
                            }

                            var shareStatusState by remember { mutableStateOf(shareSubscription.status) }
                            val allowedShareStatusTransitionTargets = requireNotNull(
                                shareStatusTransitionsWithPermissions[shareStatusState]
                            ) {
                                "Share status transition not found for status $shareStatusState"
                            }.filter { it.permissions[changesDoneBy] != null }.associateBy ({ it.shareStatus.value }){
                                it.shareStatus
                            } + (shareSubscription.status.value to shareSubscription.status)
                            val changeReasons = requireNotNull(
                                shareStatusTransitionsWithPermissions[shareStatusState]
                            ) {
                                "Share status transition not found for status $shareStatusState"
                            }.filter { it.permissions[changesDoneBy] != null }.associateBy ({ it.shareStatus.value }){
                                it.permissions[changesDoneBy].orEmpty()
                            }

                            EditableSelectCell(
                                options = allowedShareStatusTransitionTargets,
                                selected = shareStatusState,
                                disabled = !editShareSubscriptionState,
                                styles = EditableSelectCellStyles.modifyContainerStyle {
                                    width(10.percent)
                                },
                                iconContent = { expanded ->
                                    SimpleUpDown(expanded)
                                }
                            ) { shareStatus ->
                                scope.launch {
                                    updateShareStatus(UpdateShareStatus(
                                        providerId = organizationId,
                                        shareSubscriptionId = ShareSubscriptionId(shareSubscription.shareSubscriptionId),
                                        nextState = shareStatus.toApiType(),
                                        reason = changeReasons[shareStatus.value]!!.first().toApiType(), // todo:dev chose in dialog?
                                        changedBy = changesDoneBy.toApiType(), //
                                        modifier = ModifierId(currentUser.id),
                                        comment = "Subscription status changed by user '${currentUser.username}'" // todo:dev set in dialog?
                                    ))
                                }
                                shareStatusState = shareStatus
                            }

                            val check  = { checked: Boolean -> (subscriptionHeaders * subComp("ahcAuthorized") * checkIt(
                                checked
                            )).emit()}
                            val ahcAuthorized = shareSubscription.ahcAuthorized ?: false
                            EditableSelectCell(
                                options = mapOf(check(true) to true, check(false) to false),
                                selected = ahcAuthorized,disabled = !editShareSubscriptionState,
                                styles = EditableSelectCellStyles.modifyContainerStyle {
                                    width(5.percent)
                                },
                                iconContent = { expanded ->
                                    SimpleUpDown(expanded)
                                }
                            ) {
                                ahcAuthorized ->
                                shareSubscriptions = requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when(shareSubscriptionIndex){
                                        index -> shareSubscription.ahcAuthorized{
                                            ahcAuthorized
                                        }
                                        else -> shareSubscription
                                    }
                                }.let { list -> ShareSubscriptions(list) }
                                setShareSubscriptions(shareSubscriptions!!)
                            }


                            val selected = distributionPoints.firstOrNull {
                                it.distributionPointId == shareSubscription.distributionPointId
                            }
                            EditableSelectCell(
                                options = distributionPoints.associateBy { it.name  },
                                selected = selected,
                                disabled = !editShareSubscriptionState,
                                styles = EditableSelectCellStyles.modifyContainerStyle {
                                    width(5.percent)
                                },
                                iconContent = { expanded ->
                                    SimpleUpDown(expanded)
                                }
                            ) {
                                distributionPoint ->
                                shareSubscriptions = requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when (shareSubscriptionIndex) {
                                        index -> shareSubscription.distributionPointId {
                                            distributionPoint.distributionPointId
                                        }
                                        else -> shareSubscription
                                    }
                                }.let { list->ShareSubscriptions(list) }
                                setShareSubscriptions(shareSubscriptions!!)
                            }

                            EditableTextCell(
                                text = shareSubscription.coSubscribers.joinToString(", "),
                                disabled = !editShareSubscriptionState,
                                style = { width(40.percent) }
                            ) { coSubscribers ->
                                shareSubscriptions = requireNotNull(shareSubscriptions).all.mapIndexed { shareSubscriptionIndex, shareSubscription ->
                                    when (shareSubscriptionIndex) {
                                        index -> shareSubscription.coSubscribers{
                                            coSubscribers.split(",").map { it.trim() }
                                        }
                                        else -> shareSubscription
                                    }
                                }.let { list->ShareSubscriptions(list) }
                                setShareSubscriptions(shareSubscriptions!!)
                            }
                        }
                        ActionsWrapper(tableStyles.actionsWrapper) {
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                texts = {""},
                                deviceType = device,
                                isDisabled = false
                            ) {
                                editShareSubscriptionState = !editShareSubscriptionState
                            }
                        }
                    }
                }
            }
        }
    }
}


@Markup
fun Storage<Modals<Int>>.showUpdateMembersOfOrganizationModal(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    actions: ActionDispatcher<Application>,
    changesDoneBy: ChangedBy,
    currentUser: ManagedUser,
    organizationId: ProviderId,
    username: Username?,
    setUsername: (Username) -> Unit,
    userProfile: UserProfile?,
    setUserProfile: (UserProfile) -> Unit,
    importUserProfile: (UserProfileToImport) -> Unit,
    distributionPoints: List<DistributionPoint>,
    shareOffers: List<ShareOffer>,
    shareSubscriptions: ShareSubscriptions? = null,
    setShareSubscriptions: (ShareSubscriptions) -> Unit,
    updateShareStatus: suspend (UpdateShareStatus) -> Unit,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount) -> Unit,
    isOkButtonDisabled: ()->Boolean = {false},
    cancel: ()->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpdateMemberOfOrganizationModal(
            this,
            texts,
            this@showUpdateMembersOfOrganizationModal,
            device,
            actions,
            changesDoneBy,
            currentUser,
            organizationId,
            username,
            setUsername,
            userProfile,
            setUserProfile,
            importUserProfile,
            distributionPoints,
            shareOffers,
            shareSubscriptions,
            setShareSubscriptions,
            updateShareStatus,
            bankAccount,
            setBankAccount,
            isOkButtonDisabled,
            cancel,
            update
        )
    ) )
}
