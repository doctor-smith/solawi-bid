package org.solyton.solawi.bid.application.ui.page.banking

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
import org.evoleq.compose.routing.navigate
import org.evoleq.device.data.mediaType
import org.evoleq.language.extend
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.data.managedUsers
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.action.*
import org.solyton.solawi.bid.module.banking.component.form.defaultBankAccountInputs
import org.solyton.solawi.bid.module.banking.component.modal.showUpsertBankAccountModal
import org.solyton.solawi.bid.module.banking.component.modal.showUpsertBankAccountWithUserSearchModal
import org.solyton.solawi.bid.module.banking.component.modal.showUpsertFiscalYearsModal
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.application.creditorIdentifier
import org.solyton.solawi.bid.module.banking.data.application.deviceData
import org.solyton.solawi.bid.module.banking.data.application.fiscalYears
import org.solyton.solawi.bid.module.banking.data.application.legalEntity
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.bankingApplicationModals
import org.solyton.solawi.bid.module.banking.data.creditor.identifier.creditorId
import org.solyton.solawi.bid.module.banking.data.fiscalyear.FiscalYear
import org.solyton.solawi.bid.module.banking.data.fiscalyear.format
import org.solyton.solawi.bid.module.control.button.ArrowUpButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.navbar.component.SimpleRightDown
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
@Suppress("FunctionName","CognitiveComplexMethod")
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


    val customerBankAccountCandidates = managedUsers * FilterBy { customerBankAccounts.read().none { customer -> customer.userId.value == it.id } }

    val legalEntity = bankingApplicationStorage * legalEntity
    val creditorIdentifier = bankingApplicationStorage * creditorIdentifier

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
            println(legalEntity.read())
            Horizontal({
                width(100.percent)
                justifyContent(JustifyContent.SpaceBetween)
            }) {
                H3 { Text("Your data as Legal Entity:") }
                EditButton(
                    color = Color.black,
                    bgColor = Color.white,
                    texts = {"Edit "},
                    deviceType = deviceType
                ) {

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
                    SearchInput(customerBankAccountsSearchInput,
                        styles = SearchInputStyles()) {
                        customerBankAccountsSearchInput = it
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
                            HeaderCell("SEPA") { width(10.percent) }
                            HeaderCell("Start Date") { width(10.percent) }
                        }
                    }



                }
            }
        }
    }
}
