package org.solyton.solawi.bid.application.ui.page.banking

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.download.downloadCsv
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.kotlinx.date.now
import org.evoleq.language.Locale
import org.evoleq.language.extend
import org.evoleq.math.*
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.write
import org.evoleq.optics.transform.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.letsPlot.commons.intern.filterNotNullValues
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.managedUsers
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.action.*
import org.solyton.solawi.bid.module.banking.component.form.defaultBankAccountInputs
import org.solyton.solawi.bid.module.banking.component.modal.*
import org.solyton.solawi.bid.module.banking.component.modal.sepa.ManageCollectionPayments
import org.solyton.solawi.bid.module.banking.component.modal.sepa.UIState
import org.solyton.solawi.bid.module.banking.component.modal.sepa.showManagePaymentsOfSepaCollectionModal
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.api.CreateSepaPaymentsForCollection
import org.solyton.solawi.bid.module.banking.data.api.GenerateSepaMessageForCollection
import org.solyton.solawi.bid.module.banking.data.api.ImportBankAccounts
import org.solyton.solawi.bid.module.banking.data.application.*
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.bankingApplicationModals
import org.solyton.solawi.bid.module.banking.data.download.Download
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.banking.data.internal.Currency
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.message.message
import org.solyton.solawi.bid.module.banking.data.sepa.sepaCollections
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMessageString
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMessages
import org.solyton.solawi.bid.module.banking.service.download
import org.solyton.solawi.bid.module.constants.checkIcon
import org.solyton.solawi.bid.module.control.button.*
import org.solyton.solawi.bid.module.dialog.component.WarningSymbol
import org.solyton.solawi.bid.module.dialog.component.showDialogModal
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.control.dropdown.SimpleRightDown
import org.solyton.solawi.bid.module.page.component.Page
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.scrollable.ScrollableStyles
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.structure.s
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.SubTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.user.getUsers
import org.solyton.solawi.bid.module.user.action.user.readUserProfiles
import org.solyton.solawi.bid.module.user.data.userActions
import org.solyton.solawi.bid.module.values.AccessorId
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.ProviderId
import org.solyton.solawi.bid.module.values.UserId

@Markup
@Composable
@Suppress("FunctionName","CognitiveComplexMethod", "CyclomaticComplexMethod")
fun BankingApplicationForOrganizationsPage(storage: Storage<Application>, providerId: ProviderId, up: String) {
    val scope = rememberCoroutineScope()

    val managedUsers = storage * managedUsers
    val bankingApplicationStorage = storage * bankingApplicationIso
    val bankingApplicationActions = bankingApplicationStorage * bankingApplicationActions
    val bankingApplicationModals = bankingApplicationStorage * bankingApplicationModals
    val deviceType = bankingApplicationStorage * deviceData * mediaType.get

    val fiscalYears = bankingApplicationStorage * fiscalYears
    val creditorBankAccounts = bankingApplicationStorage * bankAccounts * FilterBy { it.userId == UserId(providerId.value) }
    val customerBankAccounts = bankingApplicationStorage * bankAccounts * FilterBy { it.userId != UserId(providerId.value) }

    val bankAccountToUserMap = bankingApplicationStorage * bankAccounts * Reader{ bankAccounts: List<BankAccount> ->
        bankAccounts.associateBy({it.bankAccountId}) {
            (managedUsers * FirstOrNull { user -> user.id == it.userId.value }).emit()
        }.filterNotNullValues()
    }
    val customerBankAccountCandidates = managedUsers * FilterBy { customerBankAccounts.read().none { customer -> customer.userId.value == it.id } }

    val legalEntity = bankingApplicationStorage * legalEntity
    val creditorIdentifier = bankingApplicationStorage * creditorIdentifier

    val sepaModule = bankingApplicationStorage * sepaModule
    val sepaCollections = sepaModule * sepaCollections
    val sepaMessages = sepaModule * sepaMessages
    val collectionToBankAccountMap = sepaCollections * Reader<List<SepaCollection>, Map<SepaCollectionId, BankAccount>> {
        collections: List<SepaCollection> -> collections.associateBy({it.sepaCollectionId}) {
            (creditorBankAccounts * FirstOrNull { bankAccount -> bankAccount.bankAccountId == it.creditorBankAccountId }).emit()
        }.filterNotNullValues()
    }

    LaunchedEffect(providerId) {
        launch {
            storage * userIso * userActions dispatch getUsers(providerId.value)
        }
        launch {
            bankingApplicationActions dispatch readPersonalLegalEntity(providerId.value)
        }
        launch {
           bankingApplicationActions dispatch readFiscalYears(providerId.value)
        }
        launch {
            bankingApplicationActions dispatch readBankAccounts(LegalEntityId(providerId.value))
        }
        launch{
            bankingApplicationActions dispatch readPersonalSepaCollections(LegalEntityId(providerId.value))
        }
        launch {
            bankingApplicationActions dispatch readSepaMessagesByLegalEntity(LegalEntityId(providerId.value))
        }
    }
    LaunchedEffect(managedUsers.read()) {
        storage * userIso * userActions dispatch readUserProfiles(managedUsers.read().map { it.id })
    }
    LaunchedEffect(legalEntity.read()) {
        val legalEntityId = legalEntity.read().legalEntityId
        if(legalEntityId.value != NIL_UUID) {
            bankingApplicationActions dispatch readPersonalCreditorIdentifier(legalEntityId)
        }
    }

    Page(verticalPageStyle) {
        Wrap {
            Horizontal(styles = {
                justifyContent(JustifyContent.SpaceBetween)
                alignItems(AlignItems.Center)
                width(100.percent)
            }) {
                PageTitle("Banking for Organizations")
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
            SubTitle("Manage your banking for Organizations")
        }

        Wrap{key(legalEntity.read()){
            Horizontal({
                width(100.percent)
                justifyContent(JustifyContent.SpaceBetween)
            }) {
                H3 { Text("Your data as Legal Entity:") }

                var legalEntityState by remember{ mutableStateOf(legalEntity.read()) }
                var creditorIdentifierState by remember { mutableStateOf(creditorIdentifier.read())}
                EditButton(
                    color = Color.black,
                    bgColor = Color.white,
                    texts = {"Edit "},
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
                        {l, c ->
                            legalEntityState = l
                            creditorIdentifierState = c
                        }
                    ) {

                    }
                }
            }
            ReadOnlyProperties(listOf(
                Property("Name", legalEntity.read().name){ it.toString() },
                Property("Legal Form", legalEntity.read().legalForm){it?.toString()?:""},
                Property("Type", legalEntity.read().legalEntityType.name){it.toString()},
                Property("Creditor Id", creditorIdentifier.read()?.creditorId?.value ?: "---"){it.toString()},
            ))
        }}




        Wrap {
            ListWrapper {
                TitleWrapper {
                    SimpleRightDown(true) {}
                    Title { H3{ Text("Creditor Bank Accounts") } }
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
                            setBankAccount = {bA -> bankAccountState = bA},
                            isOkButtonDisabled = {bankAccountState == null},
                            hasDescription = true
                        ) {
                            if(bankAccountState != null ) {
                                val newBankAccount = requireNotNull(bankAccountState)
                                scope.launch {
                                    bankingApplicationActions dispatch createBankAccount(
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
                HeaderWrapper {
                    Header{
                        HeaderCell("Account Holder") { width(30.percent) }
                        HeaderCell("Description") {  width(15.percent)}
                        HeaderCell("IBAN") { width(30.percent) }
                        HeaderCell("BIC") { width(20.percent) }
                        HeaderCell("Active"){ width(5.percent) }
                    }
                }
                ListItemsIndexed(creditorBankAccounts.read().let{
                    it.sortedByDescending { bankAccount -> bankAccount.bic.value  }
                }) {index,  bankAccount ->
                    ListItemWrapper({ listItemWrapperStyle(index) }) {
                        DataWrapper {
                            TextCell(bankAccount.bankAccountHolder) {width(30.percent)}
                            TextCell(bankAccount.description?:"") {width(15.percent)}
                            TextCell(bankAccount.iban.value) {width(30.percent)}
                            TextCell(bankAccount.bic.value) {width(20.percent)}
                            TextCell(bankAccount.isActive.toString()) {width(5.percent)}
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
                                    setBankAccount = {bA -> bankAccountState = bA},
                                    isOkButtonDisabled = {bankAccountState == null},
                                    hasDescription = true
                                ) {
                                    if(bankAccountState != null ) {
                                        val newBankAccount = requireNotNull(bankAccountState)
                                        scope.launch {
                                            bankingApplicationActions dispatch updateBankAccount(
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

        Wrap {
            ListWrapper { var opened by remember { mutableStateOf(false) }
                TitleWrapper {

                    SimpleRightDown(opened) {opened = !opened}
                    Title(onClick = {opened = !opened}) { H3{ Text("Customer Bank Accounts") } }

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
                        var importBankAccountsState by remember { mutableStateOf<ImportBankAccounts?>(null)}
                        UploadButton(
                            color = Color.black,
                            bgColor = Color.white,
                            deviceType = deviceType,
                        ) {
                            bankingApplicationModals.showImportBankAccountsModal(
                                texts = dialogModalTexts("Import Bank Accounts"),
                                device = deviceType,
                                accessorId = AccessorId(providerId.value),
                                bankAccounts= customerBankAccounts.read(),
                                users = managedUsers.read(),
                                setImportBankAccounts = {
                                    importBankAccountsState = it
                                }
                            ) {
                                if(importBankAccountsState == null) return@showImportBankAccountsModal
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
                            texts = { "Download as CSV" },
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
                                val (bankAccountId, userId,  iban, bic, bankAccountHolder,  isActive, bankAccountType, description,) = account
                                val user = bankAccountToUserMap.emit()[bankAccountId]
                                when{
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
                }

                When(opened) {
                    var customerBankAccountsSearchInput by remember { mutableStateOf("") }
                    var customerBankAccountsFilter by remember { mutableStateOf<(BankAccount)->Boolean>({true}) }
                    LaunchedEffect(customerBankAccountsSearchInput) {
                        customerBankAccountsFilter = { bankAccount ->
                            bankAccount.bankAccountHolder.contains(customerBankAccountsSearchInput, ignoreCase = true)
                            || bankAccount.iban.value.contains(customerBankAccountsSearchInput, ignoreCase = true)
                            || bankAccount.bic.value.contains(customerBankAccountsSearchInput, ignoreCase = true)
                        }
                    }
                    HeaderWrapper {
                        Header {
                            HeaderCell("Number: ${customerBankAccounts.read().size}") {width(15.percent)}
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
                        Header {
                            HeaderCell("Account Holder") { width(30.percent) }
                            HeaderCell("IBAN") { width(30.percent) }
                            HeaderCell("BIC") { width(20.percent) }
                            HeaderCell("Active") { width(10.percent) }
                        }
                    }
                    Scrollable(ScrollableStyles
                        .modifyContainerStyle { height(80.vh) }
                        .modifyContentStyle { width(100.percent) }
                    ) {
                        ListItemsIndexed(customerBankAccounts.read()
                            .filter(customerBankAccountsFilter)
                            .let {
                            it.sortedByDescending { bankAccount -> bankAccount.bic.value }
                        }) { index, bankAccount ->
                            ListItemWrapper({
                                listItemWrapperStyle(index)
                                width(98.percent)
                            }) {
                                DataWrapper {
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
    s("Fiscal Years") {
        Wrap {
            ListWrapper {
                var opened by remember { mutableStateOf(false) }
                TitleWrapper {
                    SimpleRightDown(opened) {opened = !opened}
                    Title { H3 { Text("Fiscal Years") } }
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
                }
                When(opened) {
                    HeaderWrapper {
                        Header {
                            HeaderCell("Fiscal Year") { width(10.percent) }
                            HeaderCell("Start Date") { width(10.percent) }
                            HeaderCell("End Date") { width(10.percent) }
                        }
                    }
                    ListItemsIndexed(fiscalYears.read().let {
                        it.sortedByDescending { fiscalYear -> fiscalYear.format() }
                    }) { index, fiscalYear ->
                        ListItemWrapper({ listItemWrapperStyle(index) }) {
                            fun LocalDate.format(): String = "$year-$monthNumber-$dayOfMonth"
                            DataWrapper {
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

    s("SEPA") {

        LaunchedEffect((sepaModule * sepaMessageString).read()) {
            val sepaMessageString = sepaModule * sepaMessageString
            val downloadStatus = sepaMessageString.read().download
            if(downloadStatus == Download.Start) {
                download((sepaMessageString * message).read(), "PAIN_${now().format(Locale.Iso)}.xml")
                scope.launch {
                    downloadStatus.write(Download.Done)
                }
            }
        }


        Wrap {
            ListWrapper {
                var opened by remember { mutableStateOf(false) }
                TitleWrapper {
                    SimpleRightDown(opened) {opened = !opened}
                    Title { H3 { Text("SEPA") } }
                }
                When(opened) {
                    HeaderWrapper {
                        Header {
                            HeaderCell("Bank Account") { width(20.percent) }
                            HeaderCell("Mandate Ref Prefix"){ width(15.percent) }
                            HeaderCell("Remittance Info"){ width(20.percent) }
                            HeaderCell("Active"){ width(5.percent) }
                            HeaderCell("Seq. Type") { width(10.percent) }
                            HeaderCell("L-Time"){ width(5.percent) }
                            HeaderCell("C-Day"){ width(5.percent) }
                            // HeaderCell("Next Payment"){width(10.percent)}
                            HeaderCell("Amount"){width(10.percent)}
                        }
                    }

                    ListItemsIndexed(sepaCollections.read()) { index , collection ->

                        val bankAccount = collectionToBankAccountMap.emit()[collection.sepaCollectionId]
                        val latestExecutionDate = collection.sepaPayments.maxOfOrNull{payment -> payment.executionDate}
                        val cumulatedAmount = collection.sepaPayments
                            .filter { it.executionDate == latestExecutionDate }
                            .sumOf { payment -> payment.amount }
                            .round(2)

                        var uiState by remember { mutableStateOf(UIState()) }
                        ListItemWrapper({listItemWrapperStyle(index)}) {
                            DataWrapper {
                                TextCell(bankAccount?.iban?.value?: ""){ width(20.percent) }
                                TextCell(collection.mandateReferencePrefix.value){  width(15.percent)}
                                TextCell(collection.remittanceInformation.value) { width(20.percent) }
                                TextCell(collection.isActive.checkIcon("--")){ width(5.percent) }
                                TextCell(collection.sepaSequenceType.name){  width(10.percent)}
                                NumberCell(collection.leadTimesDays){  width(5.percent)}
                                NumberCell(collection.requestedCollectionDay?:-1){  width(5.percent)}
                                PriceCell(cumulatedAmount, Currency.EUR) { width(10.percent) }
                                // TextCell(collection.){}
                            }
                            ActionsWrapper {

                                var manageCollectionPaymentsState by remember() { mutableStateOf<ManageCollectionPayments?>(null)}
                                key(collection) {
                                CreditCardButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    deviceType = deviceType,
                                    texts = {"Manage Mandates, Payments and Sepa Messages"},
                                    isDisabled = collection.sepaMandates.isEmpty()
                                ) {

                                    bankingApplicationModals.showManagePaymentsOfSepaCollectionModal(
                                        storage = bankingApplicationStorage,
                                        texts = dialogModalTexts("hahaha"),
                                        device = deviceType,
                                        uiState = uiState,
                                        setUiState = {data -> uiState = data},
                                        sepaCollection = sepaCollections * Reader{list: List<SepaCollection> -> list.first { it.sepaCollectionId == collection.sepaCollectionId }},
                                        sepaMessages = Source { sepaMessages.read() },
                                        executionDate = null,
                                        setManageCollectionPayments = {data -> manageCollectionPaymentsState = data}
                                    ) {
                                        bankingApplicationModals.showDialogModal(
                                            texts = dialogModalTexts("Are you sure you want to bulk edit share subscriptions?"),
                                            device = deviceType,
                                            symbol = { WarningSymbol(deviceType = deviceType.emit()) },
                                            onCancel = {}
                                        ) {

                                                scope.launch {
                                                    when (val state = manageCollectionPaymentsState) {
                                                        null -> Unit
                                                        is ManageCollectionPayments.AttachPayments -> bankingApplicationActions dispatch createSepaPaymentsForCollection(
                                                            CreateSepaPaymentsForCollection(
                                                                collection.sepaCollectionId,
                                                                state.executionDate,
                                                                state.remittanceInformation,
                                                            ),
                                                            collection.sepaCollectionId
                                                        )

                                                        is ManageCollectionPayments.CreateMessage -> bankingApplicationActions dispatch generateSepaMessageForCollection(
                                                            GenerateSepaMessageForCollection(
                                                                collection.sepaCollectionId,
                                                                state.executionDate,
                                                                listOf(),
                                                                state.remittanceInformation
                                                            )
                                                        )
                                                    }
                                                }
                                            }

                                    } }
                                }
                                EditButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    texts = {"Edit Sepa Collection"},
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
}
