package org.solyton.solawi.bid.application.ui.page.banking

import androidx.compose.runtime.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.download.downloadCsv
import org.evoleq.compose.effect.LaunchedEffectOnSource
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
import org.evoleq.compose.routing.navigate
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.days
import org.evoleq.kotlinx.date.now
import org.evoleq.kotlinx.date.today
import org.evoleq.language.*
import org.evoleq.math.*
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.evoleq.symbols.INFO
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.letsPlot.commons.intern.filterNotNullValues
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.context
import org.solyton.solawi.bid.application.data.managedUsers
import org.solyton.solawi.bid.application.data.transform.application.management.applicationManagementModule
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.effect.ForceContext
import org.solyton.solawi.bid.application.service.organizationApplicationContextId
import org.solyton.solawi.bid.application.service.useI18nTransform
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.banking.i18n.BankingLangComponent
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.application.data.ApplicationName
import org.solyton.solawi.bid.module.banking.action.*
import org.solyton.solawi.bid.module.banking.action.sepa.*
import org.solyton.solawi.bid.module.banking.application.BANKING_APPLICATION_NAME
import org.solyton.solawi.bid.module.banking.component.form.defaultBankAccountInputs
import org.solyton.solawi.bid.module.banking.component.form.sepa.FormConfiguration
import org.solyton.solawi.bid.module.banking.component.form.sepa.PartialSepaCollection
import org.solyton.solawi.bid.module.banking.component.modal.*
import org.solyton.solawi.bid.module.banking.component.modal.sepa.*
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccounts
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaCollection
import org.solyton.solawi.bid.module.banking.data.api.UpdateSepaMandate
import org.solyton.solawi.bid.module.banking.data.application.*
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.download.Download
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.banking.data.internal.Currency
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntity
import org.solyton.solawi.bid.module.banking.data.legalentity.legalEntityId
import org.solyton.solawi.bid.module.banking.data.sepa.*
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.message.message
import org.solyton.solawi.bid.module.banking.service.download
import org.solyton.solawi.bid.module.constants.checkIcon
import org.solyton.solawi.bid.module.context.data.isEmpty
import org.solyton.solawi.bid.module.control.button.*
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.permission.data.ContextId
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.style.card.cardStyle
import org.solyton.solawi.bid.module.style.list.cardListStyles
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.user.getUsers
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
import org.solyton.solawi.bid.module.user.data.api.OrganizationId
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.user.data.userActions
import org.solyton.solawi.bid.module.values.AccessorId
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.banking.data.sepa.message.download as downloadLens

@Markup
@Composable
@Suppress("FunctionName","CognitiveComplexMethod", "CyclomaticComplexMethod")
fun BankingApplicationForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    storage.ForceContext(
        ApplicationName(BANKING_APPLICATION_NAME),
        OrganizationId(providerId.value)
    )

    if((storage * context * isEmpty()).emit() ) return@BankingApplicationForOrganizationsPage

    val scope = rememberCoroutineScope()

    val bankingApplicationContextId = storage * applicationManagementModule * organizationApplicationContextId(
        BANKING_APPLICATION_NAME,
        providerId.value
    ) map { id -> requireNotNull(id) {
        error("Banking application context id is null")
    } }

    val managedUsers = storage * managedUsers
    val bankingApplicationStorage = storage * bankingApplicationIso
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val deviceType = bankingApplicationStorage * deviceData * mediaType.get

    val legalEntity = bankingApplicationStorage * legalEntity

    withLoading(
        isLoading = isLoading(
            onMissing(
                BankingLangComponent.BankingForOrganizationsPage,
                bankingApplicationStorage * i18N.get
            ) {
                LaunchComponentLookup(
                    langComponent = BankingLangComponent.BankingForOrganizationsPage,
                    environment = Read(storage) map {it.environment.useI18nTransform()},
                    i18n = (bankingApplicationStorage * i18N)
                )
            }
        ),
        onLoading = { Loading() }
    ) {

    val texts = (bankingApplicationStorage * i18N * language * component(BankingLangComponent.BankingForOrganizationsPage))

    LaunchedEffect(providerId) {
        launch {
            storage * userIso * userActions dispatch getUsers(providerId.value)
        }
        launch {
            bankingApplicationActions dispatch readPersonalLegalEntity(
                partyId = providerId.value,
                onError = {(statusCode, _) -> {
                    when(statusCode) {
                        HttpStatusCode.NotFound -> it
                        else -> it
                    }
                }},
            )
        }
        launch {
           bankingApplicationActions dispatch readFiscalYears(providerId.value)
        }
        launch {
            bankingApplicationActions dispatch readBankAccounts(LegalEntityId(providerId.value))
        }
        launch {
            bankingApplicationActions dispatch readSepaMessagesByLegalEntity(LegalEntityId(providerId.value))
        }
        launch {
            bankingApplicationActions dispatch readSepaPaymentLInksByLegalEntity(LegalEntityId(providerId.value))
        }
    }
    LaunchedEffectOnSource(Read(managedUsers)) {
        if(Read(managedUsers).emit().isNotEmpty()) {
            storage * userIso * userActions dispatch readUserProfiles(managedUsers.read().map { it.id })
        }
    }
    LaunchedEffectOnSource(Read(legalEntity)) {
        val legalEntityId = legalEntity.read().legalEntityId
        if(legalEntityId.value != NIL_UUID) {
            launch {
                bankingApplicationActions dispatch readPersonalCreditorIdentifier(
                    legalEntityId,
                    onError = {(statusCode, _) -> {
                        when(statusCode) {
                            HttpStatusCode.NotFound -> it
                            else -> it
                        }
                    }}
                )
            }
            launch {
                bankingApplicationActions dispatch readSepaMandatesByCreditorsLegalEntity(LegalEntityId(providerId.value))
            }
        }
    }

    Page(verticalPageStyle) {
        Wrap {
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle((texts * title).emit())
                Horizontal {
                    ArrowUpButton(
                        Color.black,
                        Color.white,
                        { (texts * subComp("actions") * subComp("navToUp") * tooltip).emit() },
                        deviceType,
                        false,
                    ) {
                        navigate(up)
                    }
                }
            }
            SubTitle(texts * valueOf("subTitle"))
        }

        // TODO i18n: Untergeordnete Composables (LegalEntity, CreditorBankAccounts, CustomerBankAccounts, FiscalYears, SepaCollections)
        //           enthalten weiterhin hardcoded Strings. Sie sollen in Folge-Iterationen umverkabelt werden.
        //           Alle Texte sind bereits in de/en.solyton.banking.bankingForOrganizationsPage angelegt.
        LegalEntity(
            bankingApplicationStorage,
            providerId,
            scope,
            deviceType
        )

        CreditorBankAccounts(
            bankingApplicationContextId,
            bankingApplicationStorage,
            providerId,
            scope,
            deviceType
        )

        CustomerBankAccounts(
            bankingApplicationContextId,
            bankingApplicationStorage,
            managedUsers,
            providerId,
            scope,
            deviceType
        )

        FiscalYears(
            bankingApplicationStorage,
            providerId,
            scope,
            deviceType
        )

        SepaCollections(
            bankingApplicationStorage,
            providerId,
            scope,
            deviceType
        )
    }
    }
}

@Composable
@Suppress("CognitiveComplexMethod")
fun LegalEntity(
    bankingApplicationStorage: Storage<BankingApplication>,
    providerId: ProviderId,
    scope: CoroutineScope,
    deviceType: Source<DeviceType>
) {
    val bankingApplicationModals = bankingApplicationStorage * bankingApplicationModals

    val legalEntity = bankingApplicationStorage * legalEntity
    val isLegalEntityDefined = Read(legalEntity * legalEntityId) map { it.value != NIL_UUID }
    val creditorIdentifier = bankingApplicationStorage * creditorIdentifier

    // i18n
    val texts = bankingApplicationStorage * i18N * language * component(BankingLangComponent.BankingForOrganizationsPage) * subComp("legalEntity")
    val actions = texts * subComp("actions")
    val properties = texts * subComp("properties")

    Wrap(cardStyle) {
        var opened by remember { mutableStateOf(true) }
        key(legalEntity.read(), creditorIdentifier.read() ) {
            Horizontal({
                width(100.percent)
                justifyContent(JustifyContent.SpaceBetween)
            }) {
                H3 { Text((texts * title).emit()) }

                val storedCreditorIdentifier = creditorIdentifier.read()
                var creditorIdentifierState by remember { mutableStateOf(storedCreditorIdentifier) }
                ActionsWrapper({
                    with(cardListStyles){actionsWrapper()}
                    flexGrow(1.0)
                    justifyContent(JustifyContent.End)
                }) {
                    When(opened) {
                        When(isLegalEntityDefined * negate) {
                            var legalEntityState by remember { mutableStateOf<LegalEntity?>(null) }
                            PlusButton(
                                color = Color.black,
                                bgColor = Color.white,
                                texts = actions * subComp("create") * tooltip,
                                deviceType = deviceType
                            ) {
                                bankingApplicationModals.showUpsertLegalEntityModal(
                                    bankingApplicationStorage,
                                    dialogModalTexts("Create Legal Entity") extend {
                                        "inputs" block {
                                            "name" block {
                                                "title" colon "Name"
                                            }
                                            "legalForm" block {
                                                "title" colon "Legal From"
                                            }
                                            "legalEntityType" block {
                                                "title" colon "Type"
                                            }
                                            "creditorId" block {
                                                "title" colon "Creditor ID"
                                            }
                                        }
                                    },
                                    deviceType,
                                    LegalEntityId(providerId.value),
                                    creditorIdentifierState,
                                    null,
                                    { l, c ->
                                        legalEntityState = l
                                        creditorIdentifierState = c
                                    }
                                ) {
                                    if(legalEntityState != null  ) scope.launch {
                                        val legalEntity = requireNotNull(legalEntityState) {"legal entity should not be null"}
                                        bankingApplicationStorage * bankingApplicationActions dispatch createLegalEntity(
                                            partyId = providerId.value,
                                            legalEntity.name,
                                            legalEntity.legalForm!!,
                                            legalEntity.legalEntityType,
                                            legalEntity.address,
                                        )
                                    }
                                }
                            }
                        }
                        When(isLegalEntityDefined) {

                            var legalEntityState by remember { mutableStateOf(legalEntity.read()) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                texts = actions * subComp("edit") * tooltip,
                                deviceType = deviceType
                            ) {
                                bankingApplicationModals.showUpsertLegalEntityModal(
                                    bankingApplicationStorage,
                                    dialogModalTexts("Upsert Legal Entity") extend {
                                        "inputs" block {
                                            "name" block {
                                                "title" colon "Name"
                                            }
                                            "legalForm" block {
                                                "title" colon "Legal From"
                                            }
                                            "legalEntityType" block {
                                                "title" colon "Type"
                                            }
                                            "creditorId" block {
                                                "title" colon "Creditor ID"
                                            }
                                        }
                                    },
                                    deviceType,
                                    LegalEntityId(providerId.value),
                                    creditorIdentifierState,
                                    legalEntityState,
                                    { l, c ->
                                        legalEntityState = l
                                        creditorIdentifierState = c
                                    }
                                ) {
                                    scope.launch {
                                        bankingApplicationStorage * bankingApplicationActions dispatch updateLegalEntity(
                                            legalEntityId = legalEntityState.legalEntityId,
                                            partyId = providerId.value,
                                            name = legalEntityState.name,
                                            legalForm = legalEntityState.legalForm!!,
                                            legalEntityType = legalEntityState.legalEntityType,
                                            address = legalEntityState.address,
                                        )
                                    }
                                    if(creditorIdentifierState != null) {
                                        scope.launch {
                                            val creditorIdentifier = requireNotNull(creditorIdentifierState) {"creditor identifier state should not be null"}
                                            val actionDispatcher = bankingApplicationStorage * bankingApplicationActions
                                            when (storedCreditorIdentifier) {
                                                null -> actionDispatcher dispatch createCreditorIdentifier(
                                                    legalEntityId = legalEntityState.legalEntityId,
                                                    creditorId = creditorIdentifier.creditorId,
                                                    validFrom = creditorIdentifier.validFrom,
                                                    validUntil = creditorIdentifier.validUntil,
                                                    isActive = true
                                                )
                                                else -> actionDispatcher dispatch updateCreditorIdentifier(
                                                    creditorIdentifierId = creditorIdentifier.creditorIdentifierId,
                                                    legalEntityId = legalEntityState.legalEntityId,
                                                    creditorId = creditorIdentifier.creditorId,
                                                    validFrom = creditorIdentifier.validFrom,
                                                    validUntil = creditorIdentifier.validUntil,
                                                    isActive = true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    CardChevrons(
                        texts = Source{ cardChevronTexts() },
                        deviceType = deviceType,
                        opened = opened
                    ) {
                        opened = it
                    }
                }
            }
            When(opened) {
                ReadOnlyProperties(
                    listOf(
                        Property((properties * subComp("name") * title).emit(), legalEntity.read().name) { it.toString() },
                        Property((properties * subComp("legalForm") * title).emit(), legalEntity.read().legalForm) { it?.toString() ?: "" },
                        Property((properties * subComp("type") * title).emit(), legalEntity.read().legalEntityType.name) { it.toString() },
                        Property(
                            (properties * subComp("creditorId") * title).emit(),
                            creditorIdentifier.read()?.creditorId?.value ?: "---"
                        ) { it.toString() },
                    )
                )
            }
        }
    }
}


@Composable
@Suppress("CognitiveComplexMethod")
fun CreditorBankAccounts(
    contextId: Source<String>,
    bankingApplicationStorage: Storage<BankingApplication>,
    providerId: ProviderId,
    scope: CoroutineScope,
    deviceType: Source<DeviceType>
) {

    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val bankingApplicationModals = bankingApplicationStorage * bankingApplicationModals
    val creditorBankAccounts = bankingApplicationStorage * bankAccounts * FilterBy { it.userId == UserId(providerId.value) }

    // i18n
    val texts = bankingApplicationStorage * i18N * language * component(BankingLangComponent.BankingForOrganizationsPage) * subComp("creditorBankAccounts")
    val headers = texts * subComp("headers")

    Wrap(cardStyle) {
        var opened by remember { mutableStateOf(false) }
        ListWrapper(cardListStyles.listWrapper,) {
            TitleWrapper(cardListStyles.titleWrapper,) {
                Title { H3 { Text((texts * title).emit()) } }
                ActionsWrapper({
                    with(cardListStyles){actionsWrapper()}
                    flexGrow(1.0)
                }) {
                    When(opened) {
                        var bankAccountState by remember { mutableStateOf<BankAccount?>(null) }
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                        ) {
                            val bankAccountTexts = dialogModalTexts("BankAccounts").extend {
                                add(defaultBankAccountInputs())
                            }

                            bankingApplicationModals.showUpsertBankAccountModal(
                                storage = bankingApplicationStorage,
                                texts = bankAccountTexts,
                                device = deviceType,
                                legalEntityId = LegalEntityId(providerId.value),
                                bankAccount = null,
                                setBankAccount = { bA -> bankAccountState = bA },
                                isOkButtonDisabled = { bankAccountState == null },
                                hasDescription = true
                            ) {
                                if (bankAccountState != null) {
                                    val newBankAccount = requireNotNull(bankAccountState)
                                    scope.launch {
                                        bankingApplicationActions dispatch createBankAccount(
                                            contextId.emit(),
                                            newBankAccount.userId,
                                            newBankAccount.iban,
                                            newBankAccount.bic,
                                            newBankAccount.bankAccountHolder,
                                            newBankAccount.isActive,
                                            newBankAccount.bankAccountType,
                                            newBankAccount.description
                                        )
                                        bankAccountState = null
                                    }
                                }
                            }
                        }
                    }
                    CardChevrons(
                        texts = Source{ cardChevronTexts() },
                        deviceType = deviceType,
                        opened = opened
                    ) {
                        opened = it
                    }
                }
            }
            When(opened) {
                HeaderWrapper {
                    Header(cardListStyles.header) {
                        HeaderCell((headers * subComp("accountHolder") * title).emit()) { width(30.percent) }
                        HeaderCell((headers * subComp("description") * title).emit()) { width(15.percent) }
                        HeaderCell((headers * subComp("iban") * title).emit()) { width(30.percent) }
                        HeaderCell((headers * subComp("bic") * title).emit()) { width(20.percent) }
                        HeaderCell((headers * subComp("active") * title).emit()) { width(5.percent) }
                    }
                }
                ListItemsIndexed(creditorBankAccounts.read().let {
                    it.sortedByDescending { bankAccount -> bankAccount.bic.value }
                }) { index, bankAccount ->
                    ListItemWrapper({ listItemWrapperStyle(index) }) {
                        DataWrapper(cardListStyles.dataWrapper) {
                            TextCell(bankAccount.bankAccountHolder) { width(30.percent) }
                            TextCell(bankAccount.description ?: "") { width(15.percent) }
                            TextCell(bankAccount.iban.value) { width(30.percent) }
                            TextCell(bankAccount.bic.value) { width(20.percent) }
                            TextCell(bankAccount.isActive.toString()) { width(5.percent) }
                        }
                        ActionsWrapper {
                            var bankAccountState by remember { mutableStateOf<BankAccount?>(null) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                                isDisabled = false
                            ) {
                                val bankAccountTexts = dialogModalTexts("BankAccounts").extend {
                                    add(defaultBankAccountInputs())
                                }

                                bankingApplicationModals.showUpsertBankAccountModal(
                                    storage = bankingApplicationStorage,
                                    texts = bankAccountTexts,
                                    device = deviceType,
                                    legalEntityId = LegalEntityId(providerId.value),
                                    bankAccount = bankAccount,
                                    setBankAccount = { bA -> bankAccountState = bA },
                                    isOkButtonDisabled = { bankAccountState == null },
                                    hasDescription = true
                                ) {
                                    if (bankAccountState != null) {
                                        val newBankAccount = requireNotNull(bankAccountState)
                                        scope.launch {
                                            bankingApplicationActions dispatch updateBankAccount(
                                                ContextId(contextId.emit()),
                                                bankAccount.bankAccountId,
                                                newBankAccount.userId,
                                                newBankAccount.iban,
                                                newBankAccount.bic,
                                                newBankAccount.bankAccountHolder,
                                                newBankAccount.isActive,
                                                newBankAccount.bankAccountType,
                                                newBankAccount.description
                                            )
                                            bankAccountState = null
                                        }
                                    }
                                }
                            }
                            TrashCanButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                                isDisabled = false
                            ) {
                                scope.launch {
                                    bankingApplicationActions dispatch deleteBankAccount(bankAccount.bankAccountId)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Suppress("CognitiveComplexMethod")
fun CustomerBankAccounts(
    contextId: Source<String>,
    bankingApplicationStorage: Storage<BankingApplication>,
    managedUsers: Storage<List<ManagedUser>>,
    providerId: ProviderId,
    scope: CoroutineScope,
    deviceType: Source<DeviceType>
) {
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val bankingApplicationModals = bankingApplicationStorage * bankingApplicationModals

    val creditorIdentifier = bankingApplicationStorage * creditorIdentifier
    val customerBankAccounts = bankingApplicationStorage * bankAccounts * FilterBy { it.userId != UserId(providerId.value) }

    // i18n
    val texts = bankingApplicationStorage * i18N * language * component(BankingLangComponent.BankingForOrganizationsPage) * subComp("customerBankAccounts")
    val headers = texts * subComp("headers")
    val actions = texts * subComp("actions")

    val bankAccountToUserMap: Source<Map<BankAccountId, ManagedUser?>> = bankingApplicationStorage * bankAccounts * Reader{ bankAccounts: List<BankAccount> ->
        bankAccounts.associateBy<BankAccount, BankAccountId, ManagedUser?>({it.bankAccountId}) {
            (managedUsers * FirstOrNull<ManagedUser> { user: ManagedUser -> user.id == it.userId.value }).emit()
        }.filterNotNullValues()
    }
    val customerBankAccountCandidates = managedUsers * FilterBy { customerBankAccounts.read().none { customer -> customer.userId.value == it.id } }

    val sepaModule = bankingApplicationStorage * sepaModule
    val sepaMandates = sepaModule * sepaMandates
    val sepaCollections = sepaModule * sepaCollections

    Wrap(cardStyle) {
        ListWrapper(cardListStyles.listWrapper) {
            var opened by remember { mutableStateOf(false) }
            TitleWrapper(cardListStyles.titleWrapper,) {
                Title(onClick = { opened = !opened }) { H3 { Text((texts * title).emit()) } }
                ActionsWrapper({
                    with(cardListStyles){actionsWrapper()}
                    flexGrow(1.0)
                }) {
                    When(opened) {
                        var bankAccountState by remember { mutableStateOf<BankAccount?>(null) }
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                        ) {
                            val bankAccountTexts = dialogModalTexts("BankAccounts").extend {
                                add(defaultBankAccountInputs())
                            }

                            bankingApplicationModals.showUpsertBankAccountWithUserSearchModal(
                                storage = bankingApplicationStorage,
                                texts = bankAccountTexts,
                                device = deviceType,
                                legalEntities = customerBankAccountCandidates.read(),
                                legalEntityId = null,
                                bankAccount = null,
                                setBankAccount = { bA -> bankAccountState = bA },
                                isOkButtonDisabled = {
                                    bankAccountState == null || bankAccountState?.userId?.value == NIL_UUID
                                }
                            ) {
                                if (bankAccountState != null) {
                                    val newBankAccount = requireNotNull(bankAccountState)
                                    scope.launch {
                                        bankingApplicationActions dispatch createBankAccount(
                                            contextId.emit(),
                                            newBankAccount.userId,
                                            newBankAccount.iban,
                                            newBankAccount.bic,
                                            newBankAccount.bankAccountHolder,
                                            newBankAccount.isActive,
                                            newBankAccount.bankAccountType,
                                            null,
                                            listOf(AccessorId(providerId.value))
                                        )
                                        bankAccountState = null
                                    }
                                }
                            }
                        }
                        var importBankAccountsState by remember { mutableStateOf<ImportBankAccounts?>(null) }
                        UploadButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                        ) {
                            bankingApplicationModals.showImportBankAccountsModal(
                                texts = dialogModalTexts("Import Bank Accounts"),
                                device = deviceType,
                                accessorId = AccessorId(providerId.value),
                                bankAccounts = customerBankAccounts.read(),
                                users = managedUsers.read(),
                                setImportBankAccounts = {
                                    importBankAccountsState = it
                                }
                            ) {
                                if (importBankAccountsState == null) return@showImportBankAccountsModal
                                val state = requireNotNull(importBankAccountsState)
                                scope.launch {
                                    bankingApplicationActions dispatch importBankAccounts(
                                        state.override,
                                        state.accessorId,
                                        state.bankAccounts
                                    )
                                }
                            }
                        }
                        DownloadButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = actions * subComp("downloadCsv") * tooltip,
                            deviceType = deviceType,
                        ) {
                            val checked = listOf(
                                "username",
                                "bank_account_holder",
                                "iban",
                                "bic",
                                "description",
                                "is_active"
                            )

                            val headers = checked.joinToString(";")
                            // val numberOfCols = checked.size
                            //val semiColons = ";".repeat(numberOfCols - 1)
                            val csvLines: String = customerBankAccounts.read().joinToString("\n") { account ->
                                val (bankAccountId, userId, iban, bic, bankAccountHolder, isActive, bankAccountType, description, ) = account
                                val user = bankAccountToUserMap.emit()[bankAccountId]
                                when {
                                    user == null -> ""
                                    else -> "${user.username};$bankAccountHolder;${iban.value};${bic.value ?: ""};$description;$isActive"
                                }
                            }

                            val csv = """
                                |$headers
                                |$csvLines
                            """.trimMargin()

                            downloadCsv(csv, "bank_accounts_${now()}.csv")
                        }
                    }
                    CardChevrons(
                        texts = Source{ cardChevronTexts() },
                        deviceType = deviceType,
                        opened = opened
                    ) {
                        opened = it
                    }
                }
            }

            When(opened) {
                var customerBankAccountsSearchInput by remember { mutableStateOf("") }
                var customerBankAccountsFilter by remember { mutableStateOf<(BankAccount) -> Boolean>({ true }) }
                LaunchedEffect(customerBankAccountsSearchInput) {
                    customerBankAccountsFilter = { bankAccount ->
                        bankAccount.bankAccountHolder.contains(customerBankAccountsSearchInput, ignoreCase = true)
                                || bankAccount.iban.value.contains(
                            customerBankAccountsSearchInput,
                            ignoreCase = true
                        )
                                || bankAccount.bic.value.contains(
                            customerBankAccountsSearchInput,
                            ignoreCase = true
                        )
                    }
                }
                HeaderWrapper {
                    Header(cardListStyles.header) {
                        HeaderCell("${(headers * subComp("number") * title).emit()}: ${customerBankAccounts.read().size}") { width(15.percent) }
                        SearchInput(
                            customerBankAccountsSearchInput,
                            styles = SearchInputStyles()
                        ) {
                            customerBankAccountsSearchInput = it
                        }
                    }
                }
                HeaderWrapper({
                    width(98.percent)
                }) {
                    Header(cardListStyles.header) {
                        HeaderCell((headers * subComp("accountHolder") * title).emit()) { width(30.percent) }
                        HeaderCell((headers * subComp("iban") * title).emit()) { width(30.percent) }
                        HeaderCell((headers * subComp("bic") * title).emit()) { width(20.percent) }
                        HeaderCell((headers * subComp("active") * title).emit()) { width(10.percent) }
                    }
                }
                Scrollable(
                    ScrollableStyles
                        .modifyContainerStyle { height(80.vh) }
                        .modifyContentStyle { width(100.percent) }
                ) {
                    ListItemsIndexed(
                        customerBankAccounts.read()
                            .filter(customerBankAccountsFilter)
                            .let {
                                it.sortedByDescending { bankAccount -> bankAccount.bic.value }
                            }) { index, bankAccount ->
                        ListItemWrapper({
                            listItemWrapperStyle(index)
                            width(98.percent)
                        }) {
                            DataWrapper(cardListStyles.dataWrapper) {
                                TextCell(bankAccount.bankAccountHolder) { width(30.percent) }
                                TextCell(bankAccount.iban.value) { width(30.percent) }
                                TextCell(bankAccount.bic.value) { width(20.percent) }
                                TextCell(bankAccount.isActive.toString()) { width(10.percent) }
                            }
                            ActionsWrapper {
                                var bankAccountState by remember { mutableStateOf<BankAccount?>(null) }
                                EditButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    deviceType = deviceType,
                                    isDisabled = false
                                ) {
                                    val bankAccountTexts = dialogModalTexts("BankAccounts").extend {
                                        add(defaultBankAccountInputs())
                                    }

                                    bankingApplicationModals.showUpsertBankAccountModal(
                                        bankingApplicationStorage,
                                        bankAccountTexts,
                                        deviceType,
                                        LegalEntityId(bankAccount.userId.value),
                                        bankAccount,
                                        { bA -> bankAccountState = bA }
                                    ) {
                                        if (bankAccountState != null) {
                                            val newBankAccount = requireNotNull(bankAccountState)
                                            scope.launch {
                                                bankingApplicationActions dispatch updateBankAccount(
                                                    ContextId(contextId.emit()),
                                                    bankAccount.bankAccountId,
                                                    newBankAccount.userId,
                                                    newBankAccount.iban,
                                                    newBankAccount.bic,
                                                    newBankAccount.bankAccountHolder,
                                                    newBankAccount.isActive,
                                                    newBankAccount.bankAccountType
                                                )
                                                bankAccountState = null
                                            }
                                        }
                                    }
                                }
                                val usersSepaMandates =
                                    sepaMandates * FilterBy { it.debtorBankAccountId == bankAccount.bankAccountId }
                                val usersSepaMandateIds =
                                    Read(usersSepaMandates).emit().map { mandate -> mandate.sepaMandateId }
                                val usersCollections = sepaCollections * FilterBy {
                                    it.sepaMandates.map { mandate -> mandate.sepaMandateId }.any { id ->
                                        id in usersSepaMandateIds
                                    }
                                }
                                var usersSepaMandatesState by remember { mutableStateOf(usersSepaMandates.read()) }
                                val creditorId = creditorIdentifier.read()?.creditorId
                                CreditCardButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = actions * subComp("manageSepaMandates") * tooltip,
                                    deviceType = deviceType,
                                    isDisabled = creditorId == null
                                ) {
                                    scope.launch {
                                        bankingApplicationModals.showUpsertSepaMandatesModal(
                                            storage = bankingApplicationStorage,
                                            texts = upsertSepaMandatesModalTexts,
                                            device = deviceType,
                                            bankAccount = bankAccount,
                                            sepaCollections = usersCollections.read(),
                                            sepaMandates = usersSepaMandates.read(),
                                            setSepaMandates = { mandates -> usersSepaMandatesState = mandates },
                                        ) {
                                            scope.launch {

                                                usersSepaMandatesState.forEach {
                                                    bankingApplicationActions dispatch updateSepaMandate(
                                                        UpdateSepaMandate(
                                                            it.sepaMandateId,
                                                            it.debtorBankAccountId,
                                                            requireNotNull(creditorId) {
                                                                "Creditor ID is null"
                                                            },
                                                            it.debtorName,
                                                            it.mandateReference,
                                                            it.signedAt,
                                                            it.validFrom,
                                                            it.validUntil,
                                                            it.lastUsedAt,
                                                            it.status.toApyType(),
                                                            it.isActive,
                                                            it.amendmentOf
                                                        ),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                TrashCanButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    deviceType = deviceType,
                                    isDisabled = false
                                ) {
                                    scope.launch {
                                        bankingApplicationActions dispatch deleteBankAccount(bankAccount.bankAccountId)
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


@Composable
fun FiscalYears(
    bankingApplicationStorage: Storage<BankingApplication>,
    providerId: ProviderId,
    scope: CoroutineScope,
    deviceType: Source<DeviceType>
) {
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val bankingApplicationModals = bankingApplicationStorage * bankingApplicationModals

    val fiscalYears = bankingApplicationStorage * fiscalYears

    // i18n
    val texts = bankingApplicationStorage * i18N * language * component(BankingLangComponent.BankingForOrganizationsPage) * subComp("fiscalYears")
    val headers = texts * subComp("headers")

    Wrap(cardStyle) {
        ListWrapper(cardListStyles.listWrapper) {
            var opened by remember { mutableStateOf(false) }
            TitleWrapper(cardListStyles.titleWrapper) {
                Title { H3 { Text((texts * title).emit()) } }
                ActionsWrapper({
                    with(cardListStyles) { actionsWrapper() }
                    flexGrow(1.0)
                }) {
                    When(opened) {
                        var fiscalYearState by remember { mutableStateOf<FiscalYear?>(null) }
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                        ) {
                            bankingApplicationModals.showUpsertFiscalYearsModal(
                                bankingApplicationStorage,
                                dialogModalTexts("Message"),
                                deviceType,
                                fiscalYears.read(),
                                fiscalYearState,
                                { fiscalYear -> fiscalYearState = fiscalYear },
                            ) {
                                val state = fiscalYearState
                                requireNotNull(state)
                                scope.launch {
                                    bankingApplicationActions dispatch createFiscalYear(
                                        providerId.value,
                                        state.start,
                                        state.end
                                    )
                                }
                            }
                        }
                    }
                    CardChevrons(
                        texts = Source{cardChevronTexts()},
                        deviceType = deviceType,
                        opened = opened
                    ) {
                        opened = it
                    }
                }
            }
            When(opened) {
                HeaderWrapper {
                    Header(cardListStyles.header) {
                        HeaderCell((headers * subComp("fiscalYear") * title).emit()) { width(10.percent) }
                        HeaderCell((headers * subComp("startDate") * title).emit()) { width(10.percent) }
                        HeaderCell((headers * subComp("endDate") * title).emit()) { width(10.percent) }
                    }
                }
                ListItemsIndexed(fiscalYears.read().let {
                    it.sortedByDescending { fiscalYear -> fiscalYear.format() }
                }) { index, fiscalYear ->
                    ListItemWrapper({ listItemWrapperStyle(index) }) {
                        fun LocalDate.format(): String = "$year-$monthNumber-$dayOfMonth"
                        DataWrapper(cardListStyles.dataWrapper) {
                            TextCell(fiscalYear.format()) { width(10.percent) }
                            TextCell(fiscalYear.start.toString()) { width(10.percent) }
                            TextCell(fiscalYear.end.toString()) { width(10.percent) }
                        }
                        ActionsWrapper {
                            var fiscalYearState by remember { mutableStateOf<FiscalYear?>(fiscalYear) }
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                deviceType = deviceType,
                            ) {
                                bankingApplicationModals.showUpsertFiscalYearsModal(
                                    bankingApplicationStorage,
                                    dialogModalTexts("Message"),
                                    deviceType,
                                    fiscalYears.read(),
                                    fiscalYearState,
                                    { fiscalYear -> fiscalYearState = fiscalYear },
                                ) {
                                    val state = fiscalYearState
                                    requireNotNull(state)
                                    scope.launch {
                                        bankingApplicationActions dispatch updateFiscalYear(
                                            fiscalYear.fiscalYearId,
                                            providerId.value,
                                            state.start,
                                            state.end
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
    }
}


@Composable
@Suppress("CognitiveComplexMethod")
fun SepaCollections(
    bankingApplicationStorage: Storage<BankingApplication>,
    providerId: ProviderId,
    scope: CoroutineScope,
    deviceType: Source<DeviceType>
) {
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val bankingApplicationModals = bankingApplicationStorage * bankingApplicationModals

    val legalEntity = bankingApplicationStorage * legalEntity
    val isLegalEntityDefined = Read(legalEntity * legalEntityId) map { it.value != NIL_UUID }

    val creditorBankAccounts = bankingApplicationStorage * bankAccounts * FilterBy { it.userId == UserId(providerId.value) }

    val sepaModule = bankingApplicationStorage * sepaModule
    val sepaCollections = sepaModule * sepaCollections
    val sepaMessages = sepaModule * sepaMessages
    val collectionToBankAccountMap = sepaCollections * Reader<List<SepaCollection>, Map<SepaCollectionId, BankAccount>> {
            collections: List<SepaCollection> -> collections.associateBy({it.sepaCollectionId}) {
        (creditorBankAccounts * FirstOrNull { bankAccount -> bankAccount.bankAccountId == it.creditorBankAccountId }).emit()
    }.filterNotNullValues()
    }
    val sepaMandates = sepaModule * sepaMandates
    val sepaPaymentLinks = sepaModule * sepaPaymentLinks

    // i18n
    val texts = bankingApplicationStorage * i18N * language * component(BankingLangComponent.BankingForOrganizationsPage) * subComp("sepaCollections")
    val headers = texts * subComp("headers")
    val actions = texts * subComp("actions")
    val errors = texts * subComp("errors")

    LaunchedEffect(
        (sepaModule * sepaMessageString).read(),
        (sepaModule * sepaMessageString * downloadLens).read()
    ) {
        val sepaMessageString = sepaModule * sepaMessageString
        val downloadStatus = sepaMessageString * downloadLens
        if(downloadStatus.read() == Download.Start) {
            scope.launch{
                bankingApplicationActions dispatch readPersonalSepaCollections(LegalEntityId(providerId.value))
            }.join()
            download((sepaMessageString * message).read(), "PAIN_${now().format(Locale.Iso)}.xml")
            downloadStatus.write(Download.None)
        }
    }
    LaunchedEffectOnSource(Read(sepaMandates), Read(sepaMessages)) {
        if(isLegalEntityDefined.emit()) launch{
            bankingApplicationActions dispatch readPersonalSepaCollections(LegalEntityId(providerId.value))
        }
    }
    LaunchedEffectOnSource(Read(sepaCollections)) {
        if(isLegalEntityDefined.emit()) {
            launch {
                bankingApplicationActions dispatch readSepaPaymentLInksByLegalEntity(LegalEntityId(providerId.value))
            }
            launch{
                bankingApplicationActions dispatch readSepaMessagesByLegalEntity(LegalEntityId(providerId.value))
            }
        }

    }

    Wrap(cardStyle) {
        ListWrapper(cardListStyles.listWrapper) {
            var opened by remember { mutableStateOf(false) }
            TitleWrapper(cardListStyles.titleWrapper) {
                Title { H3 { Text((texts * title).emit()) } }
                ActionsWrapper({
                    with(cardListStyles) { actionsWrapper() }
                    flexGrow(1.0)

                }) {
                    CardChevrons(
                        texts = Source{cardChevronTexts()},
                        deviceType = deviceType,
                        opened = opened
                    ) {
                        opened = it
                    }
                }
            }
            When(opened) {
                When(isLegalEntityDefined) {
                    HeaderWrapper {
                        Header(cardListStyles.header) {
                            HeaderCell((headers * subComp("bankAccount") * title).emit()) { width(20.percent) }
                            HeaderCell((headers * subComp("collectionKey") * title).emit()) { width(10.percent) }
                            HeaderCell((headers * subComp("mandateRefPrefix") * title).emit()) { width(15.percent) }
                            HeaderCell((headers * subComp("remittanceInfo") * title).emit()) { width(20.percent) }
                            HeaderCell((headers * subComp("active") * title).emit()) { width(5.percent) }
                            // HeaderCell("Seq. Type") { width(10.percent) }

                            HeaderCell(
                                (headers * subComp("leadTime") * title).emit(),
                                "$INFO ${(headers * subComp("leadTime") * tooltip).emit()}"
                            ) { width(5.percent) }

                            // HeaderCell("C-Day", "$INFO Collection Day - "){ width(5.percent) }
                            
                            // HeaderCell("Next Payment"){width(10.percent)}
                            HeaderCell((headers * subComp("totalAmount") * title).emit(), "$INFO ${(headers * subComp("totalAmount") * tooltip).emit()}") { width(10.percent) }
                        }
                    }

                    ListItemsIndexed(sepaCollections.read()) { index, collection ->

                        val bankAccount = collectionToBankAccountMap.emit()[collection.sepaCollectionId]
                        val cumulatedAmount = collection.sepaPayments
                            .filter { it.status in listOf(PaymentExecutionStatus.CONFIRMED, PaymentExecutionStatus.PAYED_MANUALLY) }
                            .sumOf { payment -> payment.amount }
                            .round(2)

                        var uiState by remember { mutableStateOf(UIState()) }
                        var manageCollectionPaymentsState by remember() {
                            mutableStateOf<ManageCollectionPaymentsState>(
                                ManageCollectionPaymentsState(today() + collection.leadTimesDays.days(), )
                            )
                        }

                        ListItemWrapper({ listItemWrapperStyle(index) }) {
                            DataWrapper(cardListStyles.dataWrapper) {
                                TextCell(bankAccount?.iban?.value ?: "") { width(20.percent) }
                                TextCell(collection.collectionKey.value) { width(10.percent) }
                                TextCell(collection.mandateReferencePrefix.value) { width(15.percent) }
                                TextCell(collection.remittanceInformation.value) { width(20.percent) }
                                TextCell(collection.isActive.checkIcon("--")) { width(5.percent) }
                                // TextCell(collection.sepaSequenceType.name) { width(10.percent) }

                                DaysCell(collection.leadTimesDays){  width(5.percent)}
                            
                                // NumberCell(collection.requestedCollectionDay?:-1){  width(5.percent)}

                            
                                PriceCell(cumulatedAmount, Currency.EUR) { width(10.percent) }
                                // TextCell(collection.){}
                            }
                            ActionsWrapper {

                                key(collection) {
                                    CreditCardButton(
                                        color = Color.black,
                                        bgColor = Color.white,
                                        deviceType = deviceType,
                                        texts = actions * subComp("manage") * tooltip,
                                        isDisabled = collection.sepaMandates.isEmpty()
                                    ) {

                                        bankingApplicationModals.showManagePaymentsOfSepaCollectionModal(
                                            storage = bankingApplicationStorage,
                                            texts = dialogModalTexts("hahaha"),
                                            device = deviceType,
                                            uiState = uiState,
                                            setUiState = { data -> uiState = data },
                                            sepaCollection = sepaCollections * Reader { list: List<SepaCollection> -> list.first { it.sepaCollectionId == collection.sepaCollectionId } },
                                            sepaMessages = Source { sepaMessages.read() },
                                            sepaPaymentLinks = Source { sepaPaymentLinks.read() },
                                            manageCollectionPaymentsState = manageCollectionPaymentsState,
                                            setManageCollectionPaymentsState = { data ->
                                                manageCollectionPaymentsState = data
                                            }
                                        ) {
                                            // Nothing to do here
                                        }
                                    }
                                }
                                var partialSepaCollectionState by remember { mutableStateOf(PartialSepaCollection.from(collection)) }
                                EditButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = actions * subComp("edit") * tooltip,
                                    deviceType = deviceType,
                                    isDisabled = false
                                ) {

                                    bankingApplicationModals.showUpsertSepaCollectionModal(
                                        bankingApplicationStorage,
                                        texts = defaultUpsertSepaCollectionModalTexts,
                                        device = deviceType,
                                        configuration = FormConfiguration(),
                                        creditorBankAccounts = creditorBankAccounts.read(),
                                        partialSepaCollection = partialSepaCollectionState,
                                        setPartialSepaCollection = {partialSepaCollectionState = it}
                                    ) {


                                        val collectionData = partialSepaCollectionState.toSepaCollection()
                                        if (collectionData != null) {
                                            scope.launch {
                                                bankingApplicationActions dispatch updateSepaCollection(
                                                    with(
                                                        collectionData
                                                    ) {
                                                        UpdateSepaCollection(
                                                            sepaCollectionId,
                                                            creditorIdentifierId,
                                                            creditorBankAccountId,
                                                            mandateReferencePrefix,
                                                            collectionKey,
                                                            remittanceInformation,
                                                            sepaSequenceType.toApiType(),
                                                            localInstrument,
                                                            chargeBearer,
                                                            requestedCollectionDay,
                                                            leadTimesDays,
                                                            purposeCode,
                                                            isActive,
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                When(isLegalEntityDefined * negate) {
                    Text((errors * valueOf("noLegalEntity")).emit())
                }
            }
        }
    }
}
