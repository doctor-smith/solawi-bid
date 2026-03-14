package org.solyton.solawi.bid.application.ui.component.organization

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.form.Form
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
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.shares.action.CREATE_SHARE_SUBSCRIPTION
import org.solyton.solawi.bid.module.shares.component.dropdown.ShareOffersDropdown
import org.solyton.solawi.bid.module.shares.data.api.PricingType
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.subscriptions.*
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.user.component.styles.modalStyles
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.values.ModifierId
import org.solyton.solawi.bid.module.values.Price
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.Username
import org.w3c.dom.HTMLElement

enum class OperatingMode {
    INIT,
    CREATE_USER,
    CREATE_PROFILE,
    UPDATE_MEMBER
}

enum class CanUpdate {
    USER_PROFILE,
    SHARE_SUBSCRIPTIONS,
    BANK_ACCOUNT,
}

enum class CanCreate {
    USER_PROFILE,
    BANK_ACCOUNT,
    SHARE_SUBSCRIPTIONS,
}

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

    var mode by remember { mutableStateOf(OperatingMode.INIT) }
    var canUpdate by remember { mutableStateOf(setOf<CanUpdate>()) }
    var canCreate by remember { mutableStateOf(setOf<CanCreate>()) }

    class Change(
        val username: Username?,
        val userProfile: UserProfile?,
        val bankAccount: BankAccount?,
        val shareSubscriptions: ShareSubscriptions?,
        val distributionPoints: List<DistributionPoint>,
        val shareOffers: List<ShareOffer>
    )

    LaunchedEffect(username, userProfile, shareSubscriptions, distributionPoints, shareOffers) {

        snapshotFlow {
            Change(username, userProfile, bankAccount, shareSubscriptions, distributionPoints, shareOffers)
        }.collect { change ->
            if(username == null) {
                mode = OperatingMode.CREATE_USER
                return@collect
            }
            if(userProfile == null) {
                mode = OperatingMode.CREATE_PROFILE
                canCreate = setOf(CanCreate.USER_PROFILE)
                return@collect
            }
            mode = OperatingMode.UPDATE_MEMBER
            canUpdate = canUpdate + setOf(CanUpdate.USER_PROFILE)
            if(bankAccount == null) {
                canCreate = canCreate + CanCreate.BANK_ACCOUNT
            } else {
                canUpdate = canUpdate + CanUpdate.BANK_ACCOUNT
            }
            if(distributionPoints.isEmpty() || shareOffers.isEmpty()) return@collect

            canCreate = canCreate + CanCreate.SHARE_SUBSCRIPTIONS
            if(shareSubscriptions != null) {
                canUpdate = canUpdate + CanUpdate.SHARE_SUBSCRIPTIONS
            }
        }
    }

    Vertical({
        height(100.percent)
        overflowY("auto")}) {
        var userProfileState by remember { mutableStateOf(userProfile) }
        var usernameState by remember { mutableStateOf(username) }
        UserProfileForm(
            device,
            inputs,
            username, setUsername,
            userProfileState,
            {uP ->
                setUserProfile(uP)
                userProfileState = uP
            },
            importUserProfile
        )

        if(canCreate.contains(CanCreate.BANK_ACCOUNT) || canUpdate.contains(CanUpdate.BANK_ACCOUNT)) {
            BankAccountForm(inputs, bankAccount, setBankAccount)
        }

        val cannotCreateOrUpdateShareSubscriptions = !(canCreate.contains(CanCreate.SHARE_SUBSCRIPTIONS) || canUpdate.contains(CanUpdate.SHARE_SUBSCRIPTIONS))

        if ( mode != OperatingMode.UPDATE_MEMBER || cannotCreateOrUpdateShareSubscriptions ) return@Vertical

        requireNotNull(userProfile) { "User profile not found - but mode is UPDATE_MEMBER" }

        ShareSubscriptionsForm(
            inputs,
            device,
            organizationId,
            changesDoneBy,
            currentUser,
            userProfile,
            shareSubscriptions,
            setShareSubscriptions,
            updateShareStatus,
            shareOffers,
            distributionPoints
        )
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
