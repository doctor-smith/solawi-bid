package org.solyton.solawi.bid.application.ui.page.shares

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.math.Source
import org.evoleq.math.invert
import org.evoleq.optics.lens.BiMap
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.filter
import org.evoleq.optics.storage.none
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.AlignItems
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.FlexWrap.Companion.Wrap
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.alignItems
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.flexGrow
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.marginRight
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.personalModuleContextRelations
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.distribution.distributionManagementIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.action.readFiscalYears
import org.solyton.solawi.bid.module.banking.data.FiscalYearId
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.BulkEditButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.dialog.component.showDialogModal
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.distribution.action.readDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.distributionManagementActions
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.list.component.ActionsWrapper
import org.solyton.solawi.bid.module.list.component.CheckBoxCell
import org.solyton.solawi.bid.module.list.component.DataWrapper
import org.solyton.solawi.bid.module.list.component.Header
import org.solyton.solawi.bid.module.list.component.HeaderCell
import org.solyton.solawi.bid.module.list.component.HeaderWrapper
import org.solyton.solawi.bid.module.list.component.ListItemWrapper
import org.solyton.solawi.bid.module.list.component.ListItemsIndexed
import org.solyton.solawi.bid.module.list.component.ListWrapper
import org.solyton.solawi.bid.module.list.component.NumberCell
import org.solyton.solawi.bid.module.list.component.TextCell
import org.solyton.solawi.bid.module.list.component.Title
import org.solyton.solawi.bid.module.list.component.TitleWrapper
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.shares.action.createShareOffer
import org.solyton.solawi.bid.module.shares.action.createShareSubscription
import org.solyton.solawi.bid.module.shares.action.createShareType
import org.solyton.solawi.bid.module.shares.action.readShareOffers
import org.solyton.solawi.bid.module.shares.action.readShareSubscriptions
import org.solyton.solawi.bid.module.shares.action.readShareTypes
import org.solyton.solawi.bid.module.shares.action.updateShareOffer
import org.solyton.solawi.bid.module.shares.action.updateShareStatus
import org.solyton.solawi.bid.module.shares.action.updateShareSubscription
import org.solyton.solawi.bid.module.shares.action.updateShareType
import org.solyton.solawi.bid.module.shares.component.modal.BulkEditShareShareSubscriptionsModal
import org.solyton.solawi.bid.module.shares.component.modal.BulkEditShareSubscriptionChanges
import org.solyton.solawi.bid.module.shares.component.modal.defaultBulkEditTexts
import org.solyton.solawi.bid.module.shares.component.modal.showBulkEditShareShareSubscriptionsModal
import org.solyton.solawi.bid.module.shares.component.modal.showUpsertShareOffersModal
import org.solyton.solawi.bid.module.shares.component.modal.showUpsertShareTypeModal
import org.solyton.solawi.bid.module.shares.data.api.ChangeReason
import org.solyton.solawi.bid.module.shares.data.api.Share
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.ChangedBy
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.shareStatusTransitionsWithPermissions
import org.solyton.solawi.bid.module.shares.data.management.deviceData
import org.solyton.solawi.bid.module.shares.data.management.shareTypes
import org.solyton.solawi.bid.module.shares.data.management.shareOffers
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.offers.shareType
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.shareManagementModals
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.subscriptions.userProfileId
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.types.ShareType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.data.values.ShareTypeId
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.user.getUsers
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
import org.solyton.solawi.bid.module.user.data.managed.id
import org.solyton.solawi.bid.module.user.data.managedUsers
import org.solyton.solawi.bid.module.user.data.organization.members
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.user.username
import org.solyton.solawi.bid.module.user.data.userActions
import org.solyton.solawi.bid.module.user.service.profile.fullname
import org.solyton.solawi.bid.module.values.ModifierId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId
import kotlin.collections.emptyMap
import kotlin.collections.toMutableMap

data class CheckedShareSubscription(val checked: Boolean, val shareSubscription: ShareSubscription)
data class ShareSubscriptionFilter(
    val fiscalYears: List<FiscalYearId>? = null,
    val shareTypes: List<ShareTypeId>? = null,
    val statuses: List<ShareStatus>? = null,
    val isAhcAuthorized: Boolean? = null,
    val userProfiles: List<String>? = null,
    val distributionPoints: List<String>? = null,
)

const val CHECK_TRUE = "☑\uFE0F"
const val CHECK_FALSE ="❌"

fun Boolean?.checkIcon(onNull: String = CHECK_FALSE): String = when(this) {
    null -> onNull
    true -> CHECK_TRUE
    false -> CHECK_FALSE
}

@Markup
@Composable
@Suppress("FunctionName", "CyclomaticComplexMethod", "CognitiveComplexMethod")
fun ShareManagementForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    val scope = rememberCoroutineScope()



    val shareManagementStore = storage * shareManagementIso
    val shareManagementActions = shareManagementStore * shareManagementActions
    val shareManagementModals = shareManagementStore * shareManagementModals

    val distributionManagementStorage = storage * distributionManagementIso
    val distributionManagementActions = distributionManagementStorage * distributionManagementActions

    val bankingApplicationStorage = storage * bankingApplicationIso
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val fiscalYears = bankingApplicationStorage * fiscalYears

    val userActions = storage * userIso * userActions
    val organizationsStorage = storage * userIso * user * organizations
    val organizationStorage = organizationsStorage * DeepSearch { it.organizationId == providerId.value }
    val memberStorage = organizationStorage * members
    val usersStorage = storage * userIso * managedUsers
    val currentUserId = usersStorage * FirstBy { it.username == (storage * userIso * user * username).read() } * id

    LaunchedEffect(providerId) {
        launch {
            shareManagementActions dispatch readShareTypes(providerId.value)
        }
        launch {
            shareManagementActions dispatch readShareOffers(providerId.value)
        }
        launch {
            shareManagementActions dispatch readShareSubscriptions(providerId.value)
        }
        launch {
            distributionManagementActions dispatch readDistributionPoints(providerId.value)
        }
        launch {
           bankingApplicationActions dispatch readFiscalYears(providerId.value)
        }
        launch {
            userActions dispatch readOrganizations()

        }
        launch {
            userActions dispatch getUsers(providerId.value)
        }
    }
    if(organizationsStorage.read().isEmpty()) return
    if(usersStorage.read().isEmpty()) return
    LaunchedEffect(usersStorage.read()) {
        launch {
            userActions dispatch readUserProfiles(memberStorage.read().map { it.memberId })
        }
    }


    val shareTypes = shareManagementStore * shareTypes
    val shareOffers = shareManagementStore * shareOffers
    val shareSubscriptions = shareManagementStore * shareSubscriptions
    val distributionPoints = distributionManagementStorage * distributionPoints
    val deviceType = shareManagementStore * deviceData * mediaType.get

    Page(verticalPageStyle) {
        Wrap{
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle("Share Management for Organizations")
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
            SubTitle("Manage share types and offers for your organization.")
        }
        Wrap {
            ListWrapper({ defaultListStyles.listWrapper(this) }) {
                TitleWrapper {
                    Title { H3 { Text("Share Types") } }
                    var shareTypeState by remember { mutableStateOf<ShareType?>(null) }
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        deviceType = deviceType,
                    ) {
                        shareManagementModals.showUpsertShareTypeModal(
                            shareManagementStore,
                            dialogModalTexts(""),
                            deviceType,
                            providerId,
                            shareTypeState,
                            { point ->
                                shareTypeState = point
                            }) {
                            val state = shareTypeState
                            requireNotNull(state)
                            scope.launch {
                                shareManagementActions dispatch createShareType(
                                    providerId.value,
                                    state.name,
                                    state.key,
                                    state.description,
                                )
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header {
                        HeaderCell("Name") { width(20.percent) }
                        HeaderCell("key") { width(10.percent) }
                        HeaderCell("Description") { width(70.percent) }
                    }
                }
                ListItemsIndexed(shareTypes.read()) { index, shareType ->
                    ListItemWrapper({
                        listItemWrapperStyle(this, index)
                    }) {
                        DataWrapper() {
                            TextCell(shareType.name) { width(20.percent) }
                            TextCell(shareType.key) { width(10.percent) }
                            TextCell(shareType.description) { width(70.percent) }

                        }
                        ActionsWrapper {
                            var shareTypeState by remember { mutableStateOf<ShareType>(shareType) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                            ) {
                                shareManagementModals.showUpsertShareTypeModal(
                                    shareManagementStore,
                                    dialogModalTexts(""),
                                    deviceType,
                                    providerId,
                                    shareTypeState,
                                    { point ->
                                        shareTypeState = point
                                    }) {
                                    val state = shareTypeState
                                    requireNotNull(state)
                                    scope.launch {
                                        shareManagementActions dispatch updateShareType(
                                            shareType.shareTypeId,
                                            providerId.value,
                                            state.name,
                                            state.key,
                                            state.description
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

        Wrap{
            ListWrapper({ defaultListStyles.listWrapper(this) }) {
                TitleWrapper {
                    Title { H3 { Text("Share Offers") } }
                    var shareOfferState by remember { mutableStateOf<ShareOffer?>(null) }
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        deviceType = deviceType,
                    ) {
                        shareManagementModals.showUpsertShareOffersModal(
                            shareManagementStore,
                            dialogModalTexts(""),
                            deviceType,
                            fiscalYears.read(),
                            shareTypes.read(),
                            shareOfferState,
                            { state ->
                                shareOfferState = state
                            }) {
                            val state = shareOfferState
                            requireNotNull(state)
                            scope.launch {
                                shareManagementActions dispatch createShareOffer(
                                    providerId.value,
                                    state.shareType.shareTypeId,
                                    state.fiscalYear.fiscalYearId,
                                    state.price,
                                    state.pricingType,
                                    state.ahcAuthorizationRequired,
                                )
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header {
                        HeaderCell("Fiscal Year") { width(10.percent) }
                        HeaderCell("Share Type") { width(10.percent) }
                        HeaderCell("Price") { width(10.percent) }
                        HeaderCell("PricingType") { width(10.percent) }
                        HeaderCell("SEPA required") { width(10.percent) }
                    }
                }
                ListItemsIndexed(shareOffers.read().let{
                    it.sortedByDescending { s -> s.fiscalYear.format() }
                }) { index, shareOffer ->
                    ListItemWrapper({
                        listItemWrapperStyle(this, index)
                    }) {
                        val booleansMap: Map<String, Boolean> = mapOf(
                            CHECK_TRUE to true,
                            CHECK_FALSE to false
                        )
                        fun getKeyOf(value: Boolean): String = booleansMap.filter { it.value == value }.keys.first()
                        DataWrapper() {
                            TextCell(shareOffer.fiscalYear.format()) { width(10.percent) }
                            TextCell(shareOffer.shareType.name) { width(10.percent) }
                            TextCell("${shareOffer.price ?: "--"}") { width(10.percent) }
                            TextCell(shareOffer.pricingType.name) { width(10.percent) }
                            TextCell(getKeyOf(shareOffer.ahcAuthorizationRequired)) { width(10.percent) }
                        }
                        ActionsWrapper {
                            var shareOfferState by remember { mutableStateOf<ShareOffer>(shareOffer) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                            ) {
                                shareManagementModals.showUpsertShareOffersModal(
                                    shareManagementStore,
                                    dialogModalTexts(""),
                                    deviceType,
                                    fiscalYears.read(),
                                    shareTypes.read(),
                                    shareOfferState,
                                    { state ->
                                        shareOfferState = state
                                    }) {
                                    val state = shareOfferState
                                    requireNotNull(state)
                                    scope.launch {
                                        shareManagementActions dispatch updateShareOffer(
                                            state.shareOfferId,
                                            providerId.value,
                                            state.shareType.shareTypeId,
                                            state.fiscalYear.fiscalYearId,
                                            state.price,
                                            state.pricingType,
                                            state.ahcAuthorizationRequired,
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

        Wrap{
            val checkedMap = remember {  mutableStateMapOf<String, Boolean>() }
            fun isChecked(id: String): Boolean = checkedMap[id] ?: false
            val checkedSubscriptions = shareSubscriptions * BiMap<ShareSubscription, CheckedShareSubscription>(
                { s -> CheckedShareSubscription(isChecked(s.shareSubscriptionId), s)},
                {cS->
                    checkedMap[cS.shareSubscription.shareSubscriptionId] = cS.checked
                    cS.shareSubscription
                }
            )
            val fiscalYearsMap = fiscalYears.read().associateBy { it.fiscalYearId }
            val distributionPointsMap = distributionPoints.read().associateBy { it.distributionPointId }
            val shareOffersMap = shareOffers.read().associateBy { it.shareOfferId }
            val shareTypesMap = shareOffersMap.mapNotNull { it.key to it.value.shareType }.associateBy({it.first}) { it.second }
            val userProfilesMap = memberStorage.read().map{ member ->
                usersStorage.read().firstOrNull{ user -> user.id == member.memberId }?.profile
            }.mapNotNull { it }.associateBy { it.userProfileId }


            var allChecked by remember { mutableStateOf(false) }
            var filter by  remember { mutableStateOf(ShareSubscriptionFilter()) }
            var filteredCheckedSubscriptions by remember { mutableStateOf(checkedSubscriptions.read()) }

            LaunchedEffect(
                filter,
                allChecked,
                checkedMap.filter { it.value }.keys,
                checkedMap.keys.size,
                shareSubscriptions.read().joinToString("-") { "$it" },
            ) {
                filteredCheckedSubscriptions = checkedSubscriptions.filter { (_, subscription) ->
                    val matchesFiscalYears =
                        filter.fiscalYears?.let { FiscalYearId(subscription.fiscalYearId) in it } ?: true
                    val matchesShareTypes =
                        filter.shareTypes?.let { ShareTypeId(shareTypesMap[subscription.shareOfferId]!!.shareTypeId) in it }
                            ?: true
                    val matchesStatuses = filter.statuses?.let { subscription.status in it } ?: true
                    val matchesAhcAuthorized = filter.isAhcAuthorized?.let { subscription.ahcAuthorized == it } ?: true
                    val matchesUserProfiles = filter.userProfiles?.any { searchText ->
                        userProfilesMap[subscription.userProfileId]?.fullname()
                            ?.contains(searchText, ignoreCase = true) == true
                    } ?: true
                    val matchesDistributionPoints = filter.distributionPoints?.let { subscription.distributionPointId in it }?: true

                    matchesFiscalYears
                    && matchesShareTypes
                    && matchesStatuses
                    && matchesAhcAuthorized
                    && matchesUserProfiles
                    && matchesDistributionPoints
                }
                // Treat checks
                .map{
                    it.copy(checked = checkedMap[it.shareSubscription.shareSubscriptionId] ?: false)
                }
                // Keep checks on visible items only
                checkedMap.clear()
                checkedMap.putAll(filteredCheckedSubscriptions.associateBy({it.shareSubscription.shareSubscriptionId}) { it.checked })
            }

            ListWrapper({ defaultListStyles.listWrapper(this) }) {
                TitleWrapper(/*{width(90.percent)}*/) {
                    Title { H3 { Text("Share Subscriptions") } }
                    Horizontal({ JustifyContent.SpaceBetween }) {
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                            isDisabled = true
                        ) {

                        }
                    }
                    Horizontal({
                        width(80.percent)
                        alignSelf(AlignSelf.End)
                    }) {
                        val fiscalYearOptions: Map<String, List<FiscalYearId>> = fiscalYears.read().groupBy { it.format() }.map { (year, fiscalYears) ->
                            year to fiscalYears.map { FiscalYearId(it.fiscalYearId) }
                        }.associateBy ({ it.first }){it.second}
                            .let {
                            it.toMutableMap().apply {
                                this["All Years"] = emptyList()
                            }
                        }
                        var selectedFiscalYear by remember { mutableStateOf("All Years") }
                        Dropdown(
                            fiscalYearOptions,
                            selectedFiscalYear,
                            iconContent = {opened -> SimpleUpDown(opened) }
                        ) {
                            (key, value) ->
                            filter = filter.copy(
                                fiscalYears = when {
                                    value.isEmpty() -> null
                                    else -> value
                                }
                            )
                            selectedFiscalYear = key
                        }

                        var selectedShareType by remember { mutableStateOf("All Shares") }
                        val shareTypeOptions = shareTypes.read().groupBy{it.name}.let {
                            it.toMutableMap<String, List<ShareType>?>().apply{
                                this["All Shares"] = null
                            }
                        }
                        Dropdown(
                            shareTypeOptions,
                            selectedShareType,
                            iconContent = {opened -> SimpleUpDown(opened) }
                        ) { (key, shareTypes) ->
                            selectedShareType = key
                            filter = filter.copy(
                                shareTypes = when {
                                    shareTypes == null -> null
                                    else -> shareTypes.map { ShareTypeId(it.shareTypeId) }
                                }
                            )
                        }

                        var selectedStatus by remember { mutableStateOf("All Statuses") }
                        val statusOptions = mapOf(
                            "All Statuses" to null,
                            ShareStatus.ActivationRejected.value to ShareStatus.ActivationRejected,
                            ShareStatus.AwaitingAhcAuthorization.value to ShareStatus.AwaitingAhcAuthorization,
                            ShareStatus.Cancelled.value to ShareStatus.Cancelled,
                            ShareStatus.ClearedForAuction.value to ShareStatus.ClearedForAuction,
                            ShareStatus.Expired.value to ShareStatus.Expired,
                            ShareStatus.External.value to ShareStatus.External,
                            ShareStatus.Paused.value to ShareStatus.Paused,
                            ShareStatus.PaymentFailed.value to ShareStatus.PaymentFailed,
                            ShareStatus.PendingActivation.value to ShareStatus.PendingActivation,
                            ShareStatus.RolledOver.value to ShareStatus.RolledOver,
                            ShareStatus.RollingOver.value to ShareStatus.RollingOver,
                            ShareStatus.Subscribed.value to ShareStatus.Subscribed,
                            ShareStatus.Suspended.value to ShareStatus.Suspended,
                        )
                        Dropdown(
                            statusOptions,
                            selectedStatus,
                            iconContent = {opened -> SimpleUpDown(opened) }
                        ) { (key, status) ->
                            selectedStatus = key
                            filter = filter.copy(
                                statuses = when {
                                    status == null -> null
                                    else -> listOf(status)
                                }
                            )
                        }

                        var selectedAhcAuthorized by remember { mutableStateOf("All Auth") }
                        val ahcAuthorizedOptions = mapOf(
                            "All Authorized" to null,
                            CHECK_TRUE to true,
                            CHECK_FALSE to false,
                        )
                        Dropdown(
                            ahcAuthorizedOptions,
                            selectedAhcAuthorized,
                            iconContent = {opened -> SimpleUpDown(opened) }
                        ) {
                            (key, ahcAuthorized) ->
                            selectedAhcAuthorized = key
                            filter = filter.copy(
                                isAhcAuthorized = ahcAuthorized
                            )
                        }

                        var selectedDistributionPoint by remember { mutableStateOf("All Depots") }
                        val distributionPointOptions = distributionPoints.read().groupBy{it.name}.let{
                            it.toMutableMap<String, List<DistributionPoint>?>().apply{
                                this["All Deports"] = null
                            }
                        }
                        Dropdown(
                            distributionPointOptions,
                            selectedDistributionPoint,
                            iconContent = {opened -> SimpleUpDown(opened) }
                        ) {
                            (key, value) ->
                            selectedDistributionPoint = key
                            filter = filter.copy(distributionPoints = when{
                                value == null -> null
                                else -> value.map { it.distributionPointId }})
                        }


                        var searchText by remember {
                            mutableStateOf(filter.userProfiles?.joinToString(",") { it }.orEmpty())
                        }
                        SearchInput(searchText, SearchInputStyles()) {
                            text ->
                            searchText = text
                            filter = filter.copy(
                                userProfiles = when {
                                    text.isEmpty() -> null
                                    else -> text.split(",").map { it.trim() }
                                }
                            )
                        }
                        var bulksEditShareSubscriptionChanges by remember { mutableStateOf<BulkEditShareSubscriptionChanges>(
                            BulkEditShareSubscriptionChanges.None) }
                        var shareSubscriptionsState by remember { mutableStateOf<List<ShareSubscription>>(
                            filteredCheckedSubscriptions.map { it.shareSubscription }
                        ) }
                        LaunchedEffect(filter, checkedMap.filter { it.value }.keys, allChecked) {
                            shareSubscriptionsState = filteredCheckedSubscriptions.filter { it.checked }.map { it.shareSubscription }
                        }
                        BulkEditButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                            isDisabled = checkedSubscriptions.none { it.checked }
                        ) {
                            shareManagementModals.showBulkEditShareShareSubscriptionsModal(
                                storage = shareManagementStore,
                                texts = defaultBulkEditTexts() ,
                                device = deviceType,
                                modifier = ModifierId(currentUserId.read()),
                                fiscalYears = fiscalYears.read(),
                                shareOffers = shareOffers.read(),
                                distributionPoints = distributionPoints.read(),
                                shareSubscriptions = shareSubscriptionsState,
                                setChanges = {changes -> bulksEditShareSubscriptionChanges = changes}
                            ) {

                                shareManagementModals.showDialogModal(
                                    texts = dialogModalTexts("Are you sure you want to bulk edit share subscriptions?"),
                                    device = deviceType,
                                    onCancel = {},
                                ) {
                                scope.launch {
                                    when(val changes = bulksEditShareSubscriptionChanges) {
                                        is BulkEditShareSubscriptionChanges.None -> Unit
                                        is BulkEditShareSubscriptionChanges.Status ->
                                            shareSubscriptionsState.forEach { shareSubscription ->
                                                shareManagementActions dispatch updateShareStatus(
                                                    UpdateShareStatus(
                                                        providerId,
                                                        ShareSubscriptionId(shareSubscription.shareSubscriptionId),
                                                        changes.status.toApiType(),
                                                        changes.reason.toApiType(),
                                                        changes.changedBy.toApiType(),
                                                        changes.modifier,
                                                        changes.comment.orEmpty()
                                                    ),
                                                )
                                            }
                                        is BulkEditShareSubscriptionChanges.AddShareOffer ->
                                            shareSubscriptionsState.forEach { shareSubscription ->
                                                shareManagementActions dispatch createShareSubscription(
                                                    providerId.value,
                                                    changes.shareOfferId,
                                                    shareSubscription.userProfileId,
                                                    shareSubscription.distributionPointId,
                                                    changes.fiscalYearId,
                                                    changes.numberOfShares,
                                                    changes.pricePerShare,
                                                    changes.isAhcAuthorized,
                                                    emptyList(),
                                                )
                                            }
                                        is BulkEditShareSubscriptionChanges.Trivial -> when(val trChanges: BulkEditShareSubscriptionChanges.Trivial = changes) {
                                            is BulkEditShareSubscriptionChanges.Trivial.PricePerShare ->
                                                shareSubscriptionsState.forEach { shareSubscription ->
                                                    shareManagementActions dispatch updateShareSubscription(
                                                        shareSubscription.shareSubscriptionId,
                                                        providerId.value,
                                                        shareSubscription.shareOfferId,
                                                        shareSubscription.userProfileId,
                                                        shareSubscription.distributionPointId,
                                                        shareSubscription.fiscalYearId,
                                                        shareSubscription.numberOfShares,
                                                        trChanges.pricePerShare,
                                                        shareSubscription.ahcAuthorized,
                                                        shareSubscription.coSubscribers,
                                                    )

                                                }

                                            is BulkEditShareSubscriptionChanges.Trivial.NumberOfShares ->
                                                shareSubscriptionsState.forEach { shareSubscription ->
                                                    shareManagementActions dispatch updateShareSubscription(
                                                        shareSubscription.shareSubscriptionId,
                                                        providerId.value,
                                                        shareSubscription.shareOfferId,
                                                        shareSubscription.userProfileId,
                                                        shareSubscription.distributionPointId,
                                                        shareSubscription.fiscalYearId,
                                                        trChanges.numberOfShares,
                                                        shareSubscription.pricePerShare,
                                                        shareSubscription.ahcAuthorized,
                                                        shareSubscription.coSubscribers,
                                                    )
                                                }

                                            is BulkEditShareSubscriptionChanges.Trivial.DistributionPoint ->
                                                shareSubscriptionsState.forEach { shareSubscription ->
                                                    shareManagementActions dispatch updateShareSubscription(
                                                        shareSubscription.shareSubscriptionId,
                                                        providerId.value,
                                                        shareSubscription.shareOfferId,
                                                        shareSubscription.userProfileId,
                                                        trChanges.distributionPointId,
                                                        shareSubscription.fiscalYearId,
                                                        shareSubscription.numberOfShares,
                                                        shareSubscription.pricePerShare,
                                                        shareSubscription.ahcAuthorized,
                                                        shareSubscription.coSubscribers,
                                                    )
                                                }

                                            is BulkEditShareSubscriptionChanges.Trivial.AhcAuthorization ->
                                                shareSubscriptionsState.forEach { shareSubscription ->
                                                    shareManagementActions dispatch updateShareSubscription(
                                                        shareSubscription.shareSubscriptionId,
                                                        providerId.value,
                                                        shareSubscription.shareOfferId,
                                                        shareSubscription.userProfileId,
                                                        shareSubscription.distributionPointId,
                                                        shareSubscription.fiscalYearId,
                                                        shareSubscription.numberOfShares,
                                                        shareSubscription.pricePerShare,
                                                        trChanges.isAhcAuthorized,
                                                        shareSubscription.coSubscribers,
                                                    )
                                                }
                                        }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                HeaderWrapper {
                    Header {
                        CheckBoxCell({allChecked}, {width(2.percent)}) {
                            val newCheckedState = !allChecked
                            checkedMap.clear()
                            if(newCheckedState) {
                                checkedMap.putAll(filteredCheckedSubscriptions.associateBy({it.shareSubscription.shareSubscriptionId}) { true })
                            }
                            allChecked = newCheckedState
                        }
                        HeaderCell("Fiscal Year") { width(10.percent) }
                        HeaderCell("Share Type") { width(15.percent) }
                        HeaderCell("Status") { width(20.percent) }
                        HeaderCell("No") { width(5.percent) }
                        HeaderCell("Price") { width(5.percent) }
                        HeaderCell("SEPA") { width(5.percent) }
                        HeaderCell("Depot") { width(10.percent) }
                        HeaderCell("User") { width(20.percent) }
                    }
                }
                Scrollable(ScrollableStyles()
                    .modifyContainerStyle {
                        height(80.vh)
                        flexDirection(FlexDirection.RowReverse)
                    }
                    .modifyContentStyle {
                        flexGrow(1)
                    }
                ) {
                    val checksChanged = checkedMap.filter { it.value }.keys
                
                    ListItemsIndexed(filteredCheckedSubscriptions) { index, checkedSubscription ->
                        val checked = checkedSubscription.checked
                        val subscription = checkedSubscription.shareSubscription
                        key(index, allChecked, checksChanged, checked, subscription) {
                            ListItemWrapper({
                                listItemWrapperStyle(this, index)
                            }) {
                                DataWrapper() {
                                    var checkedState by remember { mutableStateOf(checked) }
                                    CheckBoxCell({ checkedState }, { width(2.percent) }) {
                                        checkedMap[subscription.shareSubscriptionId] = !checkedState
                                        checkedState = !checkedState
                                    }
                                    TextCell(
                                        fiscalYearsMap[subscription.fiscalYearId]?.format() ?: ""
                                    ) { width(10.percent) }
                                    TextCell(
                                        shareOffersMap[subscription.shareOfferId]?.shareType?.name ?: ""
                                    ) { width(15.percent) }
                                    TextCell(subscription.status.value) { width(20.percent) }
                                    NumberCell(subscription.numberOfShares) { width(5.percent) }
                                    NumberCell(subscription.pricePerShare?:0) { width(5.percent) }
                                    TextCell(subscription.ahcAuthorized.checkIcon("--")) { width(5.percent) }
                                    TextCell(
                                        distributionPointsMap[subscription.distributionPointId]?.name ?: ""
                                    ) { width(10.percent) }
                                    TextCell(
                                        userProfilesMap[subscription.userProfileId]?.fullname() ?: ""
                                    ) { width(20.percent) }
                                }
                                ActionsWrapper {
                                    EditButton(
                                        color = Color.black,
                                        bgColor = Color.white,
                                        deviceType = deviceType,
                                        isDisabled = true
                                    ) {}
                                    TrashCanButton(
                                        color = Color.black,
                                        bgColor = Color.white,
                                        deviceType = deviceType,
                                        isDisabled = true
                                    ) {}
                                }
                            }
                        }
                    }
                
                }
            }
        }
    }
}
