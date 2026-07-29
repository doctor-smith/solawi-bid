package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.*
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.mediaType
import org.evoleq.language.*
import org.evoleq.math.*
import org.evoleq.optics.lens.FilterBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.prism.Either
import org.evoleq.optics.storage.Read
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.transform.asPrism
import org.evoleq.optics.transform.firstByOrNull
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.banking.bankingApplicationIso
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.i18n.UserLangComponent
import org.solyton.solawi.bid.module.banking.action.createBankAccount
import org.solyton.solawi.bid.module.banking.action.readPersonalBankAccounts
import org.solyton.solawi.bid.module.banking.action.updateBankAccount
import org.solyton.solawi.bid.module.banking.component.form.defaultBankAccountInputs
import org.solyton.solawi.bid.module.banking.component.modal.showUpsertBankAccountModal
import org.solyton.solawi.bid.module.banking.data.application.BankingApplication
import org.solyton.solawi.bid.module.banking.data.application.bankAccounts
import org.solyton.solawi.bid.module.banking.data.application.sepaModule
import org.solyton.solawi.bid.module.banking.data.bankaccount.AccountType
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.bankingApplicationActions
import org.solyton.solawi.bid.module.banking.data.bankingApplicationModals
import org.solyton.solawi.bid.module.banking.data.sepa.sepaMandates
import org.solyton.solawi.bid.module.control.button.CardChevrons
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.country.i18n.CountryLangComponent
import org.solyton.solawi.bid.module.dialog.i18n.dialogModalTexts
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.card.cardStyle
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.action.user.createUserProfile
import org.solyton.solawi.bid.module.user.action.user.readPersonalUserProfile
import org.solyton.solawi.bid.module.user.action.user.updateUser
import org.solyton.solawi.bid.module.user.action.user.updateUserProfile
import org.solyton.solawi.bid.module.user.component.modal.showUpdateUserModal
import org.solyton.solawi.bid.module.user.component.modal.showUpsertUserProfileModal
import org.solyton.solawi.bid.module.user.component.table.ListUserPermissions
import org.solyton.solawi.bid.module.user.data.address.*
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateUserProfile
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateUserProfile
import org.solyton.solawi.bid.module.user.data.deviceData
import org.solyton.solawi.bid.module.user.data.i18n
import org.solyton.solawi.bid.module.user.data.profile.FullNameOrBlank
import org.solyton.solawi.bid.module.user.data.profile.addresses
import org.solyton.solawi.bid.module.user.data.profile.phoneNumber
import org.solyton.solawi.bid.module.user.data.profile.phoneNumber1
import org.solyton.solawi.bid.module.user.data.reader.personalData
import org.solyton.solawi.bid.module.user.data.reader.properties
import org.solyton.solawi.bid.module.user.data.reader.table
import org.solyton.solawi.bid.module.user.data.reader.value
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.User
import org.solyton.solawi.bid.module.user.data.user.profile
import org.solyton.solawi.bid.module.user.data.user.username
import org.solyton.solawi.bid.module.user.data.userActions
import org.solyton.solawi.bid.module.user.data.userModals
import org.solyton.solawi.bid.module.user.service.user.userIdFromToken
import org.solyton.solawi.bid.module.values.*
import org.solyton.solawi.bid.module.user.data.profile.title as userTitle

@Markup
@Composable
@Suppress("FunctionName", "CyclomaticComplexMethod", "CognitiveComplexMethod")
fun PrivateUserPage(storage: Storage<Application>) = withLoading(
    isLoading = isLoading(
        onMissing(
            UserLangComponent.UserPrivatePage,
            storage * i18N.get
        ) {
            LaunchComponentLookup(
                langComponent = UserLangComponent.UserPrivatePage,
                environment = storage * environment * i18nEnvironment,
                i18n = (storage * i18N)
            )
        },
        *arrayOf("DE", "AT", "CH").map { countryCode ->
            onMissing(
                CountryLangComponent.StateOrProvince(countryCode),
                storage * userIso * i18n.get
            ) {
                LaunchComponentLookup(
                    langComponent = CountryLangComponent.StateOrProvince(countryCode),
                    environment = storage * environment * i18nEnvironment,
                    i18n = (storage * userIso * i18n)
                )
            }
        }.toBooleanArray(),
        onMissing(
            CountryLangComponent.Countries,
            storage * userIso * i18n.get
        ) {
            LaunchComponentLookup(
                langComponent = CountryLangComponent.Countries,
                environment = storage * environment * i18nEnvironment,
                i18n = (storage * userIso * i18n)
            )
        },
        onNullLaunch(
            storage * availablePermissions * contextFromPath("APPLICATION"),
        ) {
            CoroutineScope(Job()).launch {
                (storage * userIso * userActions).dispatch(readUserPermissionsAction())
            }
        },
    ),
    onLoading = {Loading()}
) {

    // Data
    val deviceData = storage * userIso * deviceData * mediaType.get
    val userDataStorage = storage * userData

    // Data / I18N
    val texts = storage * i18N * language * component(UserLangComponent.UserPrivatePage)

    val permissions = texts * subComp("permissions")

/*
    LaunchedEffect((userDataStorage * userIdFromToken).emit()) {
        val userId = (userDataStorage * userIdFromToken).emit()
        if(userId != null) {
            (storage * userIso * userActions).dispatch(readPersonalUserProfile(UserId(userId)))
        }
    }
    */
    LaunchedEffect((userDataStorage * userIdFromToken).emit()) {
        val userId = (userDataStorage * userIdFromToken).emit()
        if(userId != null) {
            (storage * userIso * userActions).dispatch(readOrganizations())
        }
    }

    // Markup
    Vertical(verticalPageStyle) {
        Wrap {
            Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {
                PageTitle(texts * title)
                Horizontal {
                    // General Actions here !!!
                }
            }
        }

        Wrap {
            H2 { Text((texts * personalData * title).emit()) }
        }


        UserData(
            storage = storage,
            userDataStorage = userDataStorage,
            texts = texts,
            deviceData = deviceData,
        )

        UserProfile(
            storage = storage,
            texts = texts,
            deviceData = deviceData,
        )

        Banking(
            userDataStorage =userDataStorage,
            bankingStorage = storage * bankingApplicationIso,
            deviceData = deviceData,
        )

        /*
        ShareManagement(
            userDataStorage = userDataStorage,
            shareManagementStorage = storage * shareManagementIso,
            bankingStorage = storage * bankingApplicationIso,

            deviceData = deviceData,
        )

         */

        // User permissions
        When(false) {
            H2 { Text((permissions * title).emit()) }

            ListUserPermissions(storage * userIso, permissions * table)
        }
    }
}

@Composable
fun UserData(
    storage: Storage<Application>,
    userDataStorage: Storage<User>,
    texts: Source<Lang.Block>,
    deviceData: Source<DeviceType>,
) {
    val scope = rememberCoroutineScope()

    val dialogs = texts * subComp("dialogs")

    Wrap(cardStyle) {
        var opened by remember { mutableStateOf(true) }
        // User Data
        Horizontal({
            justifyContent(JustifyContent.SpaceBetween);
            width(100.percent);
        }) {
            H3 { Text("Nutzerdaten") }
            Horizontal {
                When(opened) {
                    var storedUser by remember { mutableStateOf( userDataStorage.read()) }
                    var userState by remember { mutableStateOf(UpdateUser(
                        oldUsername = Username(storedUser.username ),
                        newUsername = Username(storedUser.username),
                        oldPassword = Password(""),
                        newPassword = Password("")
                    )) }
                    EditButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = { "Edit" },
                        deviceType = deviceData,
                    ) {
                        (storage * modals).showUpdateUserModal(
                            texts = dialogs * subComp("updateUser"),
                            device = deviceData,
                            isOkButtonDisabled = {
                                userState.oldPassword.value.isBlank()
                            },
                            user = storedUser,
                            setUser = {userState = it},
                        ) {
                            scope.launch {
                                val newUserData = when {
                                    userState.newPassword.value.isBlank() -> userState.copy(
                                        newPassword = Password(userState.oldPassword.value)
                                    )
                                    else -> userState
                                }
                               (storage * userIso * userActions) dispatch updateUser(
                                   newUserData,
                               )
                            }
                        }
                    }
                }
                CardChevrons(
                    deviceType = deviceData,
                    opened = opened,
                    setOpened = { opened = it }
                )
            }
        }
        When(opened) {
            ReadOnlyProperties(
                listOf(
                    Property(
                        (texts * personalData * properties * org.solyton.solawi.bid.module.user.data.reader.username * value).emit(),
                        (userDataStorage * username).read()
                    ),
                    Property("Passwort", "...")
                )
            )
        }
    }
}


@Composable
@Suppress("FunctionName", "CyclomaticComplexMethod", "CognitiveComplexMethod")
fun UserProfile(
    storage: Storage<Application>,
    texts: Source<Lang.Block>,
    deviceData: Source<DeviceType>,
) {

    val scope = rememberCoroutineScope()

    val userDataStorage = storage * userIso * user
    // User Profile
    val userProfilePrism = profile.asPrism()
    val titlePrism = profile.asPrism() * userTitle
    val phoneNumberPrism = profile.asPrism() * phoneNumber
    val phoneNumber1NumberPrism = profile.asPrism() * phoneNumber1
    val addressesPrism = profile.asPrism() * addresses
    val firstAddressPrism = addressesPrism * Lens(
        get = { it.first() },
        set = { address -> { list: List<Address> -> list + listOf(address) } }
    )
    val firstAddressLine1Prism = firstAddressPrism * addressLine1
    val firstAddressLine2Prism = firstAddressPrism * addressLine2
    val firstAddressPostalCodePrism = firstAddressPrism * postalCode
    val firstAddressCountryPrism = firstAddressPrism * countryCode
    val firstAddressStateOrProvincePrism = firstAddressPrism * stateOrProvince
    val firstAddressCityPrism = firstAddressPrism * city

    val userProfileStorage = userDataStorage * userProfilePrism
    val titleStorage = userDataStorage * titlePrism
    val phoneNumberStorage = userDataStorage * phoneNumberPrism
    val phoneNumber1NumberStorage = userDataStorage * phoneNumber1NumberPrism
    val firstAddressStorage = userDataStorage * firstAddressPrism
    val firstAddressLine1Storage = userDataStorage * firstAddressLine1Prism
    val firstAddressLine2Storage = userDataStorage* firstAddressLine2Prism
    val firstAddressPostalCodeStorage = userDataStorage * firstAddressPostalCodePrism
    val firstAddressCountryStorage = userDataStorage * firstAddressCountryPrism
    val firstAddressStateOrProvinceStorage = userDataStorage * firstAddressStateOrProvincePrism
    val firstAddressCityStorage = userDataStorage * firstAddressCityPrism

    val countryTexts = storage * i18N * language * CountryLangComponent.Base.component

    var userProfileState by remember { mutableStateOf(userProfileStorage.read()) }
    var opened by remember { mutableStateOf(false) }


    val userId = userDataStorage * userIdFromToken map { it?.let{UserId(it)} }

    LaunchedEffect(userId.emit()) {
        val id = userId.emit()
        if(id != null) {
            (storage * userIso * userActions).dispatch(readPersonalUserProfile(id))
        }
    }
    LaunchedEffect(userProfileStorage.read()) {
        userProfileState = userProfileStorage.read()
    }

    // User Profile
    Wrap(cardStyle) {
        Horizontal({
            justifyContent(JustifyContent.SpaceBetween);
            width(100.percent);
        }) {
            H3 { Text("Profil") }
            Horizontal {
                When(opened) {
                    When(userProfileStorage.read() == null) {
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "Edit" },
                            deviceType = deviceData,
                        ) {
                            // open create dialog
                            (storage * userIso * userModals).showUpsertUserProfileModal(
                                styles = { dev -> commonModalStyles(dev) },
                                device = deviceData,
                                texts = texts * subComp("dialogs.updateUserProfile"),
                                countryTexts = countryTexts,
                                userProfile = null,
                                setUserProfile = { userProfile -> userProfileState = userProfile },
                                cancel = {}
                            ) {
                                if (userProfileState == null) return@showUpsertUserProfileModal
                                val profile = requireNotNull(userProfileState) { "userProfileState is null" }
                                val userId = requireNotNull(userId.emit()) { "userId is null" }
                                val data = CreateUserProfile(
                                    userId = userId,
                                    firstname = Firstname(profile.firstname),
                                    lastname = Lastname(profile.lastname),
                                    title = profile.title?.let { Title(it) },
                                    phoneNumber = profile.phoneNumber?.let { PhoneNumber(it) },
                                    phoneNumber1 = profile.phoneNumber1?.let { PhoneNumber(it) },
                                    address = profile.addresses.firstOrNull()?.let{CreateAddress(
                                        it.recipientName,
                                        it.organizationName,
                                        it.addressLine1,
                                        it.addressLine2,
                                        it.city,
                                        it.stateOrProvince,
                                        it.postalCode,
                                        it.countryCode,
                                    )}?: CreateAddress.empty
                                    ,
                                )
                                scope.launch {
                                    (storage * userIso * userActions) dispatch createUserProfile(data)
                                }
                            }
                        }
                    }
                    When(userProfileStorage.read() != null) {
                        EditButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "Edit" },
                            deviceType = deviceData,
                        ) {
                            // open update dialog
                            (storage * userIso * userModals).showUpsertUserProfileModal(
                                styles = { dev -> commonModalStyles(dev) },
                                device = deviceData,
                                texts = texts * subComp("dialogs.updateUserProfile"),
                                countryTexts = countryTexts,
                                userProfile = userProfileState,
                                setUserProfile = { userProfile -> userProfileState = userProfile },
                                cancel = {}
                            ) {
                                if (userProfileState == null) return@showUpsertUserProfileModal
                                val profile = requireNotNull(userProfileState) { "userProfileState is null" }
                                val data = UpdateUserProfile(
                                    userProfileId = UserProfileId(profile.userProfileId),
                                    userId = profile.userId,
                                    firstname = Firstname(profile.firstname),
                                    lastname = Lastname(profile.lastname),
                                    title = profile.title?.let { Title(it) },
                                    phoneNumber = profile.phoneNumber?.let { PhoneNumber(it) },
                                    phoneNumber1 = profile.phoneNumber1?.let { PhoneNumber(it) },
                                    addresses = profile.addresses.map {
                                        UpdateAddress(
                                            it.addressId,
                                            it.recipientName,
                                            it.organizationName,
                                            it.addressLine1,
                                            it.addressLine2,
                                            it.city,
                                            it.stateOrProvince,
                                            it.postalCode,
                                            it.countryCode,
                                        )
                                    },
                                )
                                scope.launch {
                                    (storage * userIso * userActions) dispatch updateUserProfile(data)
                                }
                            }
                        }
                    }
                }

                CardChevrons(
                    deviceType = deviceData,
                    opened = opened,
                    setOpened = { opened = it }
                )
            }
        }
        When(opened) {
            When(userProfileStorage.read() != null) {
                Horizontal({
                    width(80.percent);
                }) {
                    Vertical({ width(50.percent) }) {
                        ReadOnlyProperties(
                            listOf(
                                Property("Title", titleStorage.read() ?: ""),
                                Property("Name", (userDataStorage * profile * FullNameOrBlank).emit())
                            )
                        )
                    }
                    Vertical({ width(50.percent) }) {
                        ReadOnlyProperties(
                            listOf(
                                Property("Telefon", phoneNumberStorage.read() ?: ""),
                                Property("Mobil", phoneNumber1NumberStorage.read() ?: "")
                            )
                        )
                    }
                }
            }

            When(userProfileStorage.read() == null) {
                Horizontal({ justifyContent(JustifyContent.SpaceBetween) }) {
                    Text("Keine Profildaten angegeben")
                }
            }
            H3 { Text("Adresse") }
            When(firstAddressStorage.read() != null) {
                Horizontal({
                    width(80.percent);
                }) {
                    Vertical({ width(50.percent) }) {
                        ReadOnlyProperty(Property("Straße, Nr.", firstAddressLine1Storage.read() ?: ""))
                        ReadOnlyProperty(Property("PLZ", firstAddressPostalCodeStorage.read() ?: ""))
                        ReadOnlyProperty(Property("Stadt", firstAddressCityStorage.read() ?: ""))
                    }
                    Vertical({ width(50.percent) }) {
                        ReadOnlyProperty(Property("Adress Zusatz", firstAddressLine2Storage.read() ?: ""))
                        ReadOnlyProperty(Property("Bundesland", firstAddressStateOrProvinceStorage.read() ?: ""))
                        ReadOnlyProperty(Property("Land", firstAddressCountryStorage.read() ?: ""))
                    }
                }
            }
            When(firstAddressStorage.read() == null && userProfileStorage.read() != null) {
                Horizontal({ justifyContent(JustifyContent.SpaceBetween) }) {
                    Text("Keine Adresse angegeben")
                }
            }

        }
    }
}



@Composable
fun Banking(
    userDataStorage: Storage<User>,
    bankingStorage: Storage<BankingApplication>,
    deviceData: Source<DeviceType>,
)   {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            (bankingStorage * bankingApplicationActions) dispatch readPersonalBankAccounts()
        }
        /*
        scope.launch {
            (bankingStorage * bankingApplicationActions) dispatch readPersonalSepaMandates()
        }

         */
    }

    val sepaMandates = bankingStorage *  sepaModule * sepaMandates * FilterBy {
        it.debtorBankAccountId in Read(bankingStorage * bankAccounts)
    }

    // val organizations = userDataStorage * organizations * FilterBy {  }

    // Banking
    Wrap(cardStyle) {
        // Banking
        val userId = (userDataStorage * userIdFromToken).emit()
        require(userId != null){ "User id is null -> WTF" }
        val bankAccountPrism = (bankingStorage * bankAccounts).firstByOrNull()
        val bA = bankAccountPrism.match{ it.userId == UserId(userId) }

        var opened by remember { mutableStateOf(false) }
        Horizontal({
            justifyContent(JustifyContent.SpaceBetween);
            width(100.percent);
        }) {
            H3 { Text("Bank") }
            Horizontal({
                justifyContent(JustifyContent.FlexEnd)
            }) {
                When(opened) {
                    When(bA is Either.Right) {
                        require(bA is Either.Right)

                        val bankAccountTexts = dialogModalTexts("BankAccounts").extend {
                            add(defaultBankAccountInputs())
                        }

                        var bankAccountState by remember { mutableStateOf(bA.value) }
                        EditButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "Edit Bank Account" },
                            deviceType = deviceData,
                        ) {
                            (bankingStorage * bankingApplicationModals ).showUpsertBankAccountModal(
                                storage = bankingStorage,
                                texts = bankAccountTexts,
                                device = deviceData,
                                legalEntityId = LegalEntityId(userId),
                                bankAccount = bankAccountState,
                                setBankAccount = {bankAccountState = it}
                            ) {
                                scope.launch {
                                    (bankingStorage * bankingApplicationActions) dispatch updateBankAccount(
                                        bankAccountState.bankAccountId,
                                        UserId(userId),
                                        bankAccountState.iban,
                                        bankAccountState.bic,
                                        bankAccountState.bankAccountHolder,
                                        bankAccountState.isActive,
                                        AccountType.DEBTOR,
                                    )
                                }
                            }
                        }
                    }
                    When(bA is Either.Left) {
                        val bankAccountTexts = dialogModalTexts("BankAccounts").extend {
                            add(defaultBankAccountInputs())
                        }
                        var bankAccountState by remember { mutableStateOf<BankAccount?>(null) }
                        PlusButton(
                            color = Color.black,
                            bgColor = Color.white,
                            texts = { "Create Bank Account" },
                            deviceType = deviceData,
                        ) {
                            // Create Bank Account
                            (bankingStorage * bankingApplicationModals ).showUpsertBankAccountModal(
                                storage = bankingStorage,
                                texts = bankAccountTexts,
                                device = deviceData,
                                legalEntityId = LegalEntityId(userId),
                                bankAccount = bankAccountState,
                                setBankAccount = {bankAccountState = it},
                                isOkButtonDisabled = {
                                    bankAccountState == null
                                },
                            ) {
                                scope.launch {
                                    val bankAccount = requireNotNull(bankAccountState) { "bankAccountState is null" }
                                    (bankingStorage * bankingApplicationActions) dispatch createBankAccount(
                                        UserId(userId),
                                        bankAccount.iban,
                                        bankAccount.bic,
                                        bankAccount.bankAccountHolder,
                                        bankAccount.isActive,
                                        AccountType.DEBTOR,
                                    )
                                }
                            }
                        }
                    }
                }
                CardChevrons(
                    deviceType = deviceData,
                    opened = opened,
                    setOpened = { opened = it }
                )
            }
        }
        When(opened ) {
            When(bA is Either.Right) {
                val bankAccount = (bA as Either.Right).value

                Horizontal({ width(40.percent) }) {
                    Vertical {
                        ReadOnlyProperties(
                            listOf(
                                Property("Name", bankAccount.bankAccountHolder),
                                Property("IBAN", bankAccount.iban.value),
                                Property("BIC", bankAccount.bic.value),
                            )
                        )
                    }
                }
            }
            When(bA is Either.Left) {
                Horizontal({ justifyContent(JustifyContent.SpaceBetween) }) {
                    Text("Keinen Bank Account angegeben")
                }
            }
        }
    }
}

@Composable
fun ShareManagement(
    userDataStorage: Storage<User>,
    // shareManagementStorage: Storage<ShareManagement>,
    bankingStorage: Storage<BankingApplication>,
    deviceData: Source<DeviceType>,
) {

    // Banking
    Wrap(cardStyle) {
        // ShareManagement
        val userId = (userDataStorage * userIdFromToken).emit()
        require(userId != null){ "User id is null -> WTF" }
        val bankAccountPrism = (bankingStorage * bankAccounts).firstByOrNull()
        val bA = bankAccountPrism.match{ it.userId == UserId(userId) }

        var opened by remember { mutableStateOf(false) }
        Horizontal({
            justifyContent(JustifyContent.SpaceBetween);
            width(100.percent);
        }) {
            H3 { Text("Anteile Verwalten") }
            Horizontal({
                justifyContent(JustifyContent.FlexEnd)
            }) {
                When(opened) {
                    Text("Actions TBD")
                }
                CardChevrons(
                    deviceType = deviceData,
                    opened = opened,
                    setOpened = { opened = it }
                )
            }
        }
        When(opened ) {

            Text("Content TBD")

        }
    }
}
