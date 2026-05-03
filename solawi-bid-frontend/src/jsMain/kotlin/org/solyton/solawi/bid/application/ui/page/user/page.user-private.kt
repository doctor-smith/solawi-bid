package org.solyton.solawi.bid.application.ui.page.user

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.guard.data.isLoading
import org.evoleq.compose.guard.data.onEmpty
import org.evoleq.compose.guard.data.onNullLaunch
import org.evoleq.compose.guard.data.withLoading
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.ReadOnlyProperties
import org.evoleq.compose.layout.ReadOnlyProperty
import org.evoleq.compose.layout.Vertical
import org.evoleq.device.data.mediaType
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.arrayOf
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.lens.FirstBy
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.lens.times
import org.evoleq.optics.prism.Either
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.dispatch
import org.evoleq.optics.storage.read
import org.evoleq.optics.transform.asPrism
import org.evoleq.optics.transform.firstByOrNull
import org.evoleq.optics.transform.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.env.i18nEnvironment
import org.solyton.solawi.bid.application.data.transform.user.userIso
import org.solyton.solawi.bid.application.ui.effect.LaunchComponentLookup
import org.solyton.solawi.bid.application.ui.page.user.effect.TriggerPasswordChange
import org.solyton.solawi.bid.application.ui.page.user.i18n.UserLangComponent
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.country.i18n.CountryLangComponent
import org.solyton.solawi.bid.module.i18n.data.language
import org.solyton.solawi.bid.module.i18n.guard.onMissing
import org.solyton.solawi.bid.module.loading.component.Loading
import org.solyton.solawi.bid.module.permissions.service.contextFromPath
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.page.PageTitle
import org.solyton.solawi.bid.module.style.page.verticalPageStyle
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.action.organization.readOrganizations
import org.solyton.solawi.bid.module.user.action.permission.readUserPermissionsAction
import org.solyton.solawi.bid.module.user.action.user.readPersonalUserProfile
import org.solyton.solawi.bid.module.user.action.user.updateUserProfile
import org.solyton.solawi.bid.module.user.component.modal.showChangePasswordModal
import org.solyton.solawi.bid.module.user.component.modal.showUpsertUserProfileModal
import org.solyton.solawi.bid.module.user.component.table.ListUserPermissions
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.user.data.address.addressLine1
import org.solyton.solawi.bid.module.user.data.address.addressLine2
import org.solyton.solawi.bid.module.user.data.address.city
import org.solyton.solawi.bid.module.user.data.address.countryCode
import org.solyton.solawi.bid.module.user.data.address.postalCode
import org.solyton.solawi.bid.module.user.data.address.stateOrProvince
import org.solyton.solawi.bid.module.user.data.api.ChangePassword
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.UpdateUserProfile
import org.solyton.solawi.bid.module.user.data.deviceData
import org.solyton.solawi.bid.module.user.data.i18n
import org.solyton.solawi.bid.module.user.data.profile.FullNameOrBlank
import org.solyton.solawi.bid.module.user.data.profile.addresses
import org.solyton.solawi.bid.module.user.data.profile.firstname
import org.solyton.solawi.bid.module.user.data.profile.lastname
import org.solyton.solawi.bid.module.user.data.profile.phoneNumber
import org.solyton.solawi.bid.module.user.data.profile.phoneNumber1
import org.solyton.solawi.bid.module.user.data.user.password
import org.solyton.solawi.bid.module.user.data.reader.*
import org.solyton.solawi.bid.module.user.data.user
import org.solyton.solawi.bid.module.user.data.user.organizations
import org.solyton.solawi.bid.module.user.data.user.profile
import org.solyton.solawi.bid.module.user.data.user.username
import org.solyton.solawi.bid.module.user.data.profile.title as userTitle
import org.solyton.solawi.bid.module.user.data.userActions
import org.solyton.solawi.bid.module.user.data.userModals
import org.solyton.solawi.bid.module.user.service.user.userIdFromToken
import org.solyton.solawi.bid.module.values.Firstname
import org.solyton.solawi.bid.module.values.Lastname
import org.solyton.solawi.bid.module.values.PhoneNumber
import org.solyton.solawi.bid.module.values.Title
import org.solyton.solawi.bid.module.values.UserId
import org.solyton.solawi.bid.module.values.UserProfileId

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
        onEmpty(
            storage * userIso * user * organizations.get
        ) {
            LaunchedEffect(Unit) {
                launch {
                    (storage * userIso * userActions).dispatch(readOrganizations())
                }
            }
        },/*
        onEmpty(
            storage * availableApplications.get
        ) {
            LaunchedEffect(Unit) {
                launch {
                    (storage * appIsl).dispatch(readOrganizations())
                }
            }
        }
        */
    ),
    onLoading = {Loading()}
) {

    // Data
    val deviceData = storage * userIso * deviceData * mediaType.get
    val userDataStorage = storage * userData

    // Data / I18N
    val texts = storage * i18N * language * component(UserLangComponent.UserPrivatePage)
    val buttons = texts * subComp("buttons")
    val dialogs = texts * subComp("dialogs")
    val permissions = texts * subComp("permissions")

    val countryTexts = storage * i18N * language * CountryLangComponent.Base.component

    LaunchedEffect((userDataStorage * userIdFromToken).emit()) {
        val userId = (userDataStorage * userIdFromToken).emit()
        if(userId != null) {
            (storage * userIso * userActions).dispatch(readPersonalUserProfile(UserId(userId)))
        }
    }

    // State
    var user by remember { mutableStateOf(ChangePassword("","")) }

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
            H2{ Text((texts * personalData * title).emit()) }

            // User Data
            H3{Text("Nutzerdaten")}
            Horizontal {
                Vertical({ width(40.percent) }) {
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
                Vertical({ width(20.percent) }) {
                    Horizontal({
                        justifyContent(JustifyContent.FlexEnd)
                    }) {
                        StdButton(
                            buttons * changePassword * title,
                            deviceData,
                            false
                        ) {
                            (storage * modals).showChangePasswordModal(
                                texts = dialogs * subComp("changePassword"),
                                device = deviceData,
                                styles = { dev -> auctionModalStyles(dev) },
                                storedPassword = (userDataStorage * password).read(),
                                setUserData = { password ->
                                    user = ChangePassword((userDataStorage * username).read(), password)
                                },
                                cancel = {}
                            ) {
                                TriggerPasswordChange(
                                    user = user,
                                    storage = storage * userIso
                                )
                            }
                        }
                    }
                }
            }


            // User Profile
            val userProfilePrism = (userData * profile).asPrism()
            val titlePrism = (userData * profile).asPrism() * userTitle
            // val firstnamePrism = (userData * profile).asPrism() * firstname
            // val lastnamePrism = (userData * profile).asPrism() * lastname
            val phoneNumberPrism = (userData * profile).asPrism() * phoneNumber
            val phoneNumber1NumberPrism = (userData * profile).asPrism() * phoneNumber1
            val addressesPrism = (userData * profile).asPrism() * addresses
            val firstAddressPrism = addressesPrism * Lens(
                get = {it.first()},
                set = {address ->{ list: List<Address> -> list + listOf(address) }}
            )
            val firstAddressLine1Prism = firstAddressPrism * addressLine1
            val firstAddressLine2Prism = firstAddressPrism * addressLine2
            val firstAddressPostalCodePrism = firstAddressPrism * postalCode
            val firstAddressCountryPrism = firstAddressPrism * countryCode
            val firstAddressStateOrProvincePrism = firstAddressPrism * stateOrProvince
            val firstAddressCityPrism = firstAddressPrism * city

            val userProfileStorage = storage * userProfilePrism
            val titleStorage = storage * titlePrism
            // val firstnameStorage = storage * firstnamePrism
            // val lastnameStorage = storage * lastnamePrism
            val phoneNumberStorage = storage * phoneNumberPrism
            val phoneNumber1NumberStorage = storage * phoneNumber1NumberPrism
            val firstAddressStorage = storage * firstAddressPrism
            val firstAddressLine1Storage = storage * firstAddressLine1Prism
            val firstAddressLine2Storage = storage * firstAddressLine2Prism
            val firstAddressPostalCodeStorage = storage * firstAddressPostalCodePrism
            val firstAddressCountryStorage = storage * firstAddressCountryPrism
            val firstAddressStateOrProvinceStorage = storage * firstAddressStateOrProvincePrism
            val firstAddressCityStorage = storage * firstAddressCityPrism

            //val userProfileTexts = texts * subComp("userProfile")

            var userProfileState by remember { mutableStateOf(userProfileStorage.read()) }

            val upsertUserProfileScope = rememberCoroutineScope()

            H3{ Text("Profil") }
            When(userProfileStorage.read() != null) {
                Horizontal {
                    Vertical({width(40.percent)}) {
                        ReadOnlyProperties(listOf(
                            Property("Title", titleStorage.read() ?: ""),
                            Property("Name", (userDataStorage * profile * FullNameOrBlank).emit())
                        ))
                     }
                    Vertical({width(40.percent)}) {
                        ReadOnlyProperties(listOf(
                            Property("Telefon", phoneNumberStorage.read() ?: ""),
                            Property("Mobil", phoneNumber1NumberStorage.read() ?: "")
                        ))
                    }
                    Vertical({width(20.percent)}) {
                        Horizontal({ justifyContent(JustifyContent.FlexEnd) }) {
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
                                    userProfile = userProfileStorage.read(),
                                    setUserProfile = { userProfile -> userProfileState = userProfile },
                                    cancel = {}
                                ) {
                                    if(userProfileState == null) return@showUpsertUserProfileModal
                                    val profile = requireNotNull(userProfileState) { "userProfileState is null" }
                                    val data = UpdateUserProfile(
                                        userProfileId = UserProfileId(profile.userProfileId),
                                        userId = profile.userId,
                                        firstname = Firstname(profile.firstname),
                                        lastname = Lastname(profile.lastname),
                                        title = profile.title?.let { Title(it) },
                                        phoneNumber = profile.phoneNumber?.let { PhoneNumber(it) },
                                        phoneNumber1 = profile.phoneNumber1?.let { PhoneNumber(it) },
                                        addresses = profile.addresses.map { UpdateAddress(
                                            it.addressId,
                                            it.recipientName,
                                            it.organizationName,
                                            it.addressLine1,
                                            it.addressLine2,
                                            it.city,
                                            it.stateOrProvince,
                                            it.postalCode,
                                            it.countryCode,
                                        ) },
                                    )
                                    upsertUserProfileScope.launch {
                                        (storage * userIso * userActions) dispatch updateUserProfile(data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            When(userProfileStorage.read() == null) {
                Horizontal({ justifyContent(JustifyContent.SpaceBetween) }) {
                    Text("Keine Profildaten angegeben")
                    // Create Profile Button
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = { "Edit" },
                        deviceType = deviceData,
                    ) {
                        // open create dialog
                        // open update dialog
                        (storage * userIso * userModals).showUpsertUserProfileModal(
                            styles = { dev -> commonModalStyles(dev) },
                            device = deviceData,
                            texts = texts,
                            countryTexts = countryTexts,
                            userProfile = null,
                            setUserProfile = { userProfile -> userProfileState = userProfile },
                            cancel = {}
                        ) {
                            if(userProfileState == null) return@showUpsertUserProfileModal
                            val profile = requireNotNull(userProfileState) { "userProfileState is null" }
                            val data = UpdateUserProfile(
                                userProfileId = UserProfileId(profile.userProfileId),
                                userId = profile.userId,
                                firstname = Firstname(profile.firstname),
                                lastname = Lastname(profile.lastname),
                                title = profile.title?.let { Title(it) },
                                phoneNumber = profile.phoneNumber?.let { PhoneNumber(it) },
                                phoneNumber1 = profile.phoneNumber1?.let { PhoneNumber(it) },
                                addresses = profile.addresses.map { UpdateAddress(
                                    it.addressId,
                                    it.recipientName,
                                    it.organizationName,
                                    it.addressLine1,
                                    it.addressLine2,
                                    it.city,
                                    it.stateOrProvince,
                                    it.postalCode,
                                    it.countryCode,
                                ) },
                            )
                            upsertUserProfileScope.launch {
                                (storage * userIso * userActions) dispatch updateUserProfile(data)
                            }
                        }
                    }
                }
            }
            H3{ Text("Adresse") }
            When(firstAddressStorage.read() != null) {
                Horizontal {
                    Vertical({width(40.percent)}) {
                        ReadOnlyProperty(Property("Straße, Nr.", firstAddressLine1Storage.read() ?: ""))
                        ReadOnlyProperty(Property("PLZ", firstAddressPostalCodeStorage.read() ?: ""))
                        ReadOnlyProperty(Property("Stadt", firstAddressCityStorage.read() ?: ""))
                    }
                    Vertical({width(40.percent)}) {
                        ReadOnlyProperty(Property("Adress Zusatz", firstAddressLine2Storage.read() ?: ""))
                        ReadOnlyProperty(Property("Bundesland", firstAddressStateOrProvinceStorage.read() ?: ""))
                        ReadOnlyProperty(Property("Land", firstAddressCountryStorage.read() ?: ""))
                    }
                    Vertical({
                        width(20.percent)
                    }){
                        Horizontal({justifyContent(JustifyContent.FlexEnd)}) {
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                texts = { "Edit" },
                                deviceType = deviceData,
                            ) {
                                // open update dialog
                                // open update dialog
                                (storage * userIso * userModals).showUpsertUserProfileModal(
                                    styles = { dev -> commonModalStyles(dev) },
                                    device = deviceData,
                                    texts = texts,
                                    countryTexts = countryTexts,
                                    userProfile = userProfileStorage.read(),
                                    setUserProfile = { userProfile -> userProfileState = userProfile },
                                    cancel = {}
                                ) {
                                    if(userProfileState == null) return@showUpsertUserProfileModal
                                    val profile = requireNotNull(userProfileState) { "userProfileState is null" }
                                    val data = UpdateUserProfile(
                                        userProfileId = UserProfileId(profile.userProfileId),
                                        userId = profile.userId,
                                        firstname = Firstname(profile.firstname),
                                        lastname = Lastname(profile.lastname),
                                        title = profile.title?.let { Title(it) },
                                        phoneNumber = profile.phoneNumber?.let { PhoneNumber(it) },
                                        phoneNumber1 = profile.phoneNumber1?.let { PhoneNumber(it) },
                                        addresses = profile.addresses.map { UpdateAddress(
                                            it.addressId,
                                            it.recipientName,
                                            it.organizationName,
                                            it.addressLine1,
                                            it.addressLine2,
                                            it.city,
                                            it.stateOrProvince,
                                            it.postalCode,
                                            it.countryCode,
                                        ) },
                                    )
                                    upsertUserProfileScope.launch {
                                        (storage * userIso * userActions) dispatch updateUserProfile(data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            When(firstAddressStorage.read() == null && userProfileStorage.read() != null) {
                Horizontal({ justifyContent(JustifyContent.SpaceBetween) }) {
                    Text("Keine Adresse angegeben")
                    // Add Update Profile behind Create Button
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = { "Edit" },
                        deviceType = deviceData,
                    ) {
                        // open update user profile dialog
                        // open update dialog
                        (storage * userIso * userModals).showUpsertUserProfileModal(
                            styles = { dev -> commonModalStyles(dev) },
                            device = deviceData,
                            texts = texts,
                            countryTexts = countryTexts,
                            userProfile = userProfileStorage.read(),
                            setUserProfile = { userProfile -> userProfileState = userProfile },
                            cancel = {}
                        ) {
                            if(userProfileState == null) return@showUpsertUserProfileModal
                            val profile = requireNotNull(userProfileState) { "userProfileState is null" }
                            val data = UpdateUserProfile(
                                userProfileId = UserProfileId(profile.userProfileId),
                                userId = profile.userId,
                                firstname = Firstname(profile.firstname),
                                lastname = Lastname(profile.lastname),
                                title = profile.title?.let { Title(it) },
                                phoneNumber = profile.phoneNumber?.let { PhoneNumber(it) },
                                phoneNumber1 = profile.phoneNumber1?.let { PhoneNumber(it) },
                                addresses = profile.addresses.map { UpdateAddress(
                                    it.addressId,
                                    it.recipientName,
                                    it.organizationName,
                                    it.addressLine1,
                                    it.addressLine2,
                                    it.city,
                                    it.stateOrProvince,
                                    it.postalCode,
                                    it.countryCode,
                                ) },
                            )
                            upsertUserProfileScope.launch {
                                (storage * userIso * userActions) dispatch updateUserProfile(data)
                            }
                        }
                    }
                }
            }


            // Banking
            val userId = (userDataStorage * userIdFromToken).emit()
            require(userId != null){ "User id is null -> WTF" }
            val bankAccountPrism = (storage * bankAccounts).firstByOrNull()
            val bA = bankAccountPrism.match{ it.userId == UserId(userId) }
            H3{ Text("Bank") }
            When(bA is Either.Right) {
                val bankAccount = (bA as Either.Right).value
                Text("Bank Account: ${bankAccount.iban}")

                Horizontal({width(40.percent)}) {
                    Vertical {
                        ReadOnlyProperties(
                            listOf(
                                Property("Name", bankAccount.bankAccountHolder),
                                Property("IBAN", bankAccount.iban),
                                Property("BIC", bankAccount.bic),
                            )
                        )
                    }
                    Vertical{
                        Horizontal({justifyContent(JustifyContent.FlexEnd)}) {
                            EditButton(
                                color = Color.black,
                                bgColor = Color.white,
                                texts = { "Edit Bank Account" },
                                deviceType = deviceData,
                            ) {

                            }
                        }
                    }
                }
            }
            When(bA is Either.Left) {
                Horizontal({justifyContent(JustifyContent.SpaceBetween)}) {
                    Text("Keinen Bank Account angegeben")
                    PlusButton(
                        color = Color.black,
                        bgColor = Color.white,
                        texts = { "Create Bank Account" },
                        deviceType = deviceData,
                    ) {
                        // Create Bank Account
                    }
                }
            }


            When(false) {
                H2 { Text((permissions * title).emit()) }

                ListUserPermissions(storage * userIso, permissions * table)
            }
        }
    }
}
