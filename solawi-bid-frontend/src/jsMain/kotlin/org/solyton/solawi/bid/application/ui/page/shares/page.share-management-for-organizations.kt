package org.solyton.solawi.bid.application.ui.page.shares

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.now
import org.evoleq.language.Lang
import org.evoleq.language.texts
import org.evoleq.math.FirstOrNull
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.lens.BiMap
import org.evoleq.optics.lens.DeepSearch
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.filter
import org.evoleq.optics.storage.none
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.letsPlot.commons.intern.filterNotNullValues
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.distribution.distributionManagementIso
import org.solyton.solawi.bid.application.data.transform.shares.shareManagementIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.action.*
import org.solyton.solawi.bid.module.banking.component.form.PartialSepaCollection
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaCollection
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaMandate
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaCollection
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.application.creditorIdentifier
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.bankaccount.AccountType
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.creditor.identifier.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.banking.data.sepa.SepaSequenceType
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.service.generateReference
import org.solyton.solawi.bid.module.constants.CHECK_FALSE
import org.solyton.solawi.bid.module.constants.CHECK_TRUE
import org.solyton.solawi.bid.module.constants.checkIcon
import org.solyton.solawi.bid.module.control.button.*
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.dialog.component.WarningSymbol
import org.solyton.solawi.bid.module.dialog.component.showDialogModal
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.distribution.action.readDistributionPoints
import org.solyton.solawi.bid.module.distribution.data.distributionManagementActions
import org.solyton.solawi.bid.module.distribution.data.distributionpoint.DistributionPoint
import org.solyton.solawi.bid.module.distribution.data.management.distributionPoints
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.list.style.defaultListStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.shares.action.*
import org.solyton.solawi.bid.module.shares.component.modal.*
import org.solyton.solawi.bid.module.shares.data.api.UpdateShareStatus
import org.solyton.solawi.bid.module.shares.data.internal.ShareStatus
import org.solyton.solawi.bid.module.shares.data.management.deviceData
import org.solyton.solawi.bid.module.shares.data.management.shareOffers
import org.solyton.solawi.bid.module.shares.data.management.shareSubscriptions
import org.solyton.solawi.bid.module.shares.data.management.shareTypes
import org.solyton.solawi.bid.module.shares.data.offers.ShareOffer
import org.solyton.solawi.bid.module.shares.data.offers.fiscalYear
import org.solyton.solawi.bid.module.shares.data.shareManagementActions
import org.solyton.solawi.bid.module.shares.data.shareManagementModals
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.shares.data.toApiType
import org.solyton.solawi.bid.module.shares.data.types.ShareType
import org.solyton.solawi.bid.module.shares.data.values.ShareSubscriptionId
import org.solyton.solawi.bid.module.shares.data.values.ShareTypeId
import org.solyton.solawi.bid.module.shares.service.refersTo
import org.solyton.solawi.bid.module.shares.service.relatedBankAccountExists
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflow
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
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.ModifierId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.Username

data class CheckedShareSubscription(val checked: Boolean, val shareSubscription: ShareSubscription)
data class ShareSubscriptionFilter(
    val fiscalYears: List<FiscalYearId>? = null,
    val shareTypes: List<ShareTypeId>? = null,
    val statuses: List<ShareStatus>? = null,
    val isAhcAuthorized: Boolean? = null,
    val userProfiles: List<String>? = null,
    val distributionPoints: List<String>? = null,
)

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
    val debtorBankAccounts = bankingApplicationStorage * bankAccounts * FilterBy { it.bankAccountType == AccountType.DEBTOR }
    val fiscalYears = bankingApplicationStorage * fiscalYears
    val creditorIdentifier = bankingApplicationStorage * creditorIdentifier
    val creditorBankAccounts = bankingApplicationStorage * bankAccounts * FilterBy { it.bankAccountType == AccountType.CREDITOR }
    val sepaModule = bankingApplicationStorage * sepaModule
    val sepaCollections = sepaModule * sepaCollections


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
            bankingApplicationActions dispatch readBankAccounts(LegalEntityId(providerId.value))
        }
        launch{
            bankingApplicationActions dispatch readPersonalCreditorIdentifier(LegalEntityId(providerId.value))
        }
        launch {
            bankingApplicationActions dispatch readPersonalSepaCollections(LegalEntityId(providerId.value))
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
                        HeaderCell("Share Type") { width(20.percent) }
                        HeaderCell("Price") { width(10.percent) }
                        HeaderCell("PricingType") { width(10.percent) }
                        HeaderCell("SEPA required") { width(10.percent) }
                        HeaderCell("SEPA - Assoc Collections") { width(40.percent) }
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
                        val sepaCollections = sepaCollections * FilterBy { shareOffer.shareOfferId in it.referenceIds.map{ ref -> ref.value} }

                        DataWrapper() {
                            TextCell(shareOffer.fiscalYear.format()) { width(10.percent) }
                            TextCell(shareOffer.shareType.name) { width(20.percent) }
                            TextCell("${shareOffer.price ?: "--"}") { width(10.percent) }
                            TextCell(shareOffer.pricingType.name) { width(10.percent) }
                            TextCell(getKeyOf(shareOffer.ahcAuthorizationRequired)) { width(10.percent) }
                            TextCell(sepaCollections.read().joinToString(", ") { sC -> sC.mandateReferencePrefix.value }) {
                                width(40.percent)
                                overflow(Overflow.Hidden)
                            }
                        }
                        ActionsWrapper {
                            var shareOfferState by remember { mutableStateOf<ShareOffer>(shareOffer) }
                            var creditorIdentifierState by remember { mutableStateOf<CreditorIdentifier?>(creditorIdentifier.read()) }

                            When(creditorIdentifierState != null ) {
                                val finalCreditorIdentifier = requireNotNull(creditorIdentifierState) {"Cannot be null "}
                                val sepaCollectionReader = sepaCollections * FirstOrNull { shareOffer.shareOfferId in it.referenceIds.map { id -> id.value } }
                                var sepaCollectionState by remember {
                                    mutableStateOf(PartialSepaCollection(
                                        sepaCollectionId = sepaCollectionReader.emit()?.sepaCollectionId,
                                        creditorIdentifierId = finalCreditorIdentifier.creditorIdentifierId,
                                        creditorBankAccountId = sepaCollectionReader.emit()?.creditorBankAccountId,
                                        mandateReferencePrefix = sepaCollectionReader.emit()?.mandateReferencePrefix,
                                        remittanceInformation = sepaCollectionReader.emit()?.remittanceInformation,
                                        sepaSequenceType = sepaCollectionReader.emit()?.sepaSequenceType?: SepaSequenceType.FRST,
                                        localInstrument = sepaCollectionReader.emit()?.localInstrument,
                                        isActive = sepaCollectionReader.emit()?.isActive?:true,
                                        leadTimesDays = sepaCollectionReader.emit()?.leadTimesDays?:5,
                                        requestedCollectionDay = sepaCollectionReader.emit()?.requestedCollectionDay?:2,
                                        chargeBearer = sepaCollectionReader.emit()?.chargeBearer?: ChargeBearer("SLEV")
                                    ))
                                }
                                CreditCardButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    deviceType = deviceType,
                                ) {

                                    shareManagementModals.showAttachSepaCollectionModal(
                                        bankingApplicationStorage,
                                        texts = dialogModalTexts("Attach Sepa Collection"),
                                        device = deviceType,
                                        bankAccounts = creditorBankAccounts.read(),
                                        sepaCollection = sepaCollectionState,
                                        isOkButtonDisabled = {with(sepaCollectionState){
                                            creditorBankAccountId == null
                                            || creditorIdentifierId == null
                                            || mandateReferencePrefix == null
                                            || remittanceInformation == null
                                            || sepaSequenceType == null
                                            || leadTimesDays == null
                                            || chargeBearer == null
                                            || isActive == null
                                        } },
                                        setSepaCollection = {collection -> sepaCollectionState = collection}
                                    ) {
                                        when(val sepaCollectionId = sepaCollectionState.sepaCollectionId) {
                                            null -> {
                                                val data = with(sepaCollectionState) {
                                                    CreateSepaCollection(
                                                        creditorIdentifierId = creditorIdentifierId!!,
                                                        creditorBankAccountId = creditorBankAccountId!!,
                                                        mandateReferencePrefix = mandateReferencePrefix!!,
                                                        remittanceInformation = remittanceInformation!!,
                                                        sepaSequenceType = sepaSequenceType!!.toApiType(),
                                                        localInstrument = localInstrument,
                                                        chargeBearer = chargeBearer!!,
                                                        requestedCollectionDay = requestedCollectionDay,
                                                        leadTimeDays = leadTimesDays!!,
                                                        purposeCode = purposeCode,
                                                        isActive = isActive!!,
                                                        sepaMandates = null,
                                                        sepaPayments = null,
                                                        referenceIds = listOf(SepaCollectionReferenceId(shareOffer.shareOfferId)),
                                                    )
                                                }

                                                scope.launch {
                                                    bankingApplicationActions dispatch createSepaCollection(
                                                        data
                                                    )
                                                }
                                            }
                                            else -> {
                                                val data = with(sepaCollectionState) {
                                                    UpdateSepaCollection(
                                                        sepaCollectionId,
                                                        creditorIdentifierId = creditorIdentifierId!!,
                                                        creditorAccountId = creditorBankAccountId!!,
                                                        mandateReferencePrefix = mandateReferencePrefix!!,
                                                        remittanceInformation = remittanceInformation!!,
                                                        sepaSequenceType = sepaSequenceType!!.toApiType(),
                                                        localInstrument = localInstrument,
                                                        chargeBearer = chargeBearer!!,
                                                        requestedCollectionDay = requestedCollectionDay,
                                                        leadTimeDays = leadTimesDays!!,
                                                        purposeCode = purposeCode,
                                                        isActive = isActive!!,
                                                        referenceIds = listOf(SepaCollectionReferenceId(shareOffer.shareOfferId)),
                                                    )
                                                }

                                                scope.launch {
                                                    bankingApplicationActions dispatch updateSepaCollection(
                                                        data
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            When(creditorIdentifierState == null){
                                CreditCardButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    deviceType = deviceType,
                                    isDisabled = true,
                                    texts = {"No creditor identifiers defined"}
                                ) {

                                }
                            }
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
            val userProfileToUserMap = memberStorage.read().mapNotNull { member ->
                usersStorage.read().firstOrNull { user -> user.id == member.memberId }
            }.associateBy { it.profile?.userProfileId }
            val userProfileToBankAccountMap = usersStorage.read().map { user ->
                val bankAccount = debtorBankAccounts * FirstOrNull { it.userId.value == user.id }
                user to bankAccount.emit()
            }.associateBy({it.first.profile?.userProfileId}) { it.second  }.filterNotNullValues()
            // val sepaCollectionsMap = sepaCollections.read().associateBy { it.sepaCollectionId }
            // val sepaCollectionIdsByReference = sepaCollections.read().map{
            //     collection -> collection.referenceIds.map{ it to collection.sepaCollectionId }
            // }.flatten().distinct().groupBy ({ it.first }){it.second}

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
                }
                HeaderWrapper {
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
                                sepaCollections = sepaCollections.read(),
                                creditorIdentifier = creditorIdentifier.read(),
                                setChanges = {changes -> bulksEditShareSubscriptionChanges = changes}
                            ) {

                                shareManagementModals.showDialogModal(
                                    texts = dialogModalTexts("Are you sure you want to bulk edit share subscriptions?"),
                                    device = deviceType,
                                    symbol = { WarningSymbol(deviceType = deviceType.emit()) },
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
                                        is BulkEditShareSubscriptionChanges.AddSepaMandate -> {
                                            shareSubscriptionsState.forEachIndexed {index,  shareSubscription ->
                                                val debtorBankAccount = userProfileToBankAccountMap[shareSubscription.userProfileId]
                                                // Leave out subscriptions of users without bank account
                                                if(debtorBankAccount == null) return@forEachIndexed
                                                bankingApplicationActions dispatch createSepaMandate(
                                                    CreateSepaMandate(
                                                        creditorId = changes.creditorIdentifier.creditorId,
                                                        debtorBankAccountId = debtorBankAccount.bankAccountId,
                                                        debtorName = debtorBankAccount.bankAccountHolder,
                                                        mandateReference = changes.sepaCollection.mandateReferencePrefix.generateReference(
                                                            changes.mandateReferencePadStart, 8, index
                                                        ),
                                                        mandateReferencePrefix = null,
                                                        signedAt = now(),
                                                        validFrom = now(),
                                                        validUntil = null,
                                                        status = changes.status.toApyType(),
                                                        isActive = changes.isActive,
                                                        amendmentOf = null,
                                                        collectionId = changes.sepaCollection.sepaCollectionId,
                                                    ),
                                                    targetCollectionId = changes.sepaCollection.sepaCollectionId,
                                                )
                                            }
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
                        var bulkUpdateShareSubscriptionsState by remember{ mutableStateOf<ShareSubscriptions?> (null) }
                        val initialShareSubscriptions = {
                            filteredCheckedSubscriptions.filter{
                                it.checked
                            }.map{ s ->
                                val username =
                                    userProfileToUserMap[s.shareSubscription.userProfileId]!!.username
                                Username(username) to s.shareSubscription
                            }.associateBy ({
                                it.first
                            }){it.second}
                        }
                        UploadButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                            isDisabled = false
                        ) {
                            shareManagementModals.showBulkUpdateShareDataByFileImportModal(
                                texts = dialogModalTexts("BulkEdit by import"),
                                device = deviceType,
                                shareSubscriptions = initialShareSubscriptions(),
                                setShareSubscriptions = {
                                    bulkUpdateShareSubscriptionsState = it
                                }
                            ) {
                                shareManagementModals.showDialogModal(
                                    texts = dialogModalTexts("Are you sure you want to bulk edit share subscriptions?"),
                                    device = deviceType,
                                    symbol = { WarningSymbol(deviceType = deviceType.emit()) },
                                    onCancel = {},
                                ) {
                                    scope.launch {
                                        bulkUpdateShareSubscriptionsState?.all?.forEach { shareSubscription ->
                                            shareManagementActions dispatch updateShareSubscription(
                                                shareSubscription.shareSubscriptionId,
                                                providerId.value,
                                                shareSubscription.shareOfferId,
                                                shareSubscription.userProfileId,
                                                shareSubscription.distributionPointId,
                                                shareSubscription.fiscalYearId,
                                                shareSubscription.numberOfShares,
                                                shareSubscription.pricePerShare,
                                                shareSubscription.ahcAuthorized,
                                                shareSubscription.coSubscribers,
                                            )
                                        }
                                        bulkUpdateShareSubscriptionsState = null
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
                        HeaderCell("Depot") { width(10.percent) }
                        HeaderCell("User") { width(20.percent) }
                        HeaderCell("SEPA") { width(5.percent) }
                        HeaderCell("SEPA - Assoc Collection") { width(20.percent) }
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

                                    TextCell(
                                        distributionPointsMap[subscription.distributionPointId]?.name ?: ""
                                    ) { width(10.percent) }
                                    TextCell(
                                        userProfilesMap[subscription.userProfileId]?.fullname() ?: ""
                                    ) { width(20.percent) }
                                    TextCell(subscription.ahcAuthorized.checkIcon("--")) { width(5.percent) }
                                    TextCell(sepaCollections.read().filter{
                                        coll -> coll.refersTo(subscription) && subscription.relatedBankAccountExists(
                                            coll,
                                            userProfileToBankAccountMap
                                        )
                                    }.joinToString(", ") { sC -> sC.mandateReferencePrefix.value }) {
                                        width(20.percent)
                                        overflow(Overflow.Hidden)
                                    }
                                }
                                ActionsWrapper {
                                    CreditCardButton(
                                        color = Color.black,
                                        bgColor = Color.white,
                                        deviceType = deviceType,
                                        isDisabled = true
                                    ) {}
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

fun shareManagementForOrganizationsTexts(): Source<Lang.Block> = {
    "shareManagementForOrganizations" texts{
        "shareOffersList" block {
            "item" block {
                "actions" block {
                    "attachSepaCollection" block {
                        "hints" block {
                            "noCreditorsDefined" colon "No Creditors defined"
                        }
                        "tooltip" block {
                            "title" colon "Attach or update Sepa Collection"
                        }
                    }
                }
            }
        }

        "dialogs" block {
            "attachSepaCollectionModal" block {
                "x" colon "Y"
            }
        }
    }
}
