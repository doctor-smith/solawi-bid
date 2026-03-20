package org.solyton.solawi.bid.application.ui.component.organization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.country.i18n.CountryLangComponent
import org.solyton.solawi.bid.module.country.i18n.CountryLangComponent.Companion.names
import org.solyton.solawi.bid.module.i18n.data.variables
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.user.data.address.addressLine1
import org.solyton.solawi.bid.module.user.data.address.addressLine2
import org.solyton.solawi.bid.module.user.data.address.city
import org.solyton.solawi.bid.module.user.data.address.countryCode
import org.solyton.solawi.bid.module.user.data.address.organizationName
import org.solyton.solawi.bid.module.user.data.address.postalCode
import org.solyton.solawi.bid.module.user.data.address.recipientName
import org.solyton.solawi.bid.module.user.data.address.stateOrProvince
import org.solyton.solawi.bid.module.user.data.api.userprofile.CreateAddress
import org.solyton.solawi.bid.module.user.data.api.userprofile.UserProfileToImport
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.user.data.profile.addresses
import org.solyton.solawi.bid.module.values.Username

@Markup
@Composable
@Suppress("FunctionName", "CyclomaticComplexMethod")
fun UserProfileForm(
    device: Source<DeviceType>,
    inputs: Source<Lang.Block>,
    countryTexts: Source<Lang.Block>,
    username: Username?,
    setUsername: (Username) -> Unit,
    userProfile: UserProfile?,
    setUserProfile: (UserProfile) -> Unit,
    importUserProfile: (UserProfileToImport) -> Unit,
){
    val scope = rememberCoroutineScope()

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
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    TextInput(usernameState ?: "") {
                        required()
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
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    TextInput(userProfileState?.firstname ?: "") {
                        required()
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
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    TextInput(userProfileState?.lastname ?: "") {
                        required()
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
                Field(fieldDesktopStyle) {
                    Label(
                        (userProfileInputs * subComp("phoneNumber") * title).emit(),
                        id = "phoneNumber",
                        labelStyle = formLabelDesktopStyle
                    )
                    TextInput(userProfileState?.phoneNumber ?: "") {
                        id("phoneNumber")
                        style { textInputDesktopStyle() }
                        onInput {
                            val newUserProfile =
                                userProfileState?.copy(phoneNumber = it.value) ?: UserProfile("", "","", "", it.value)
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

                H3 { Text((addressInputs * title).emit()) }
                Horizontal {
                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("recipientName") * title).emit(),
                            id = "recipientName",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        TextInput(address?.recipientName ?: "") {
                            required()
                            id("recipientName")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.recipientName { it.value } ?: Address.default().recipientName { it.value }
                                    )
                                } ?: UserProfile.default()
                                    .addresses { listOf(Address.default().recipientName { it.value }) }
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
                        TextInput(address?.organizationName ?: "") {
                            id("organizationName")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.organizationName { it.value } ?: Address.default()
                                            .organizationName { it.value }
                                    )
                                } ?: UserProfile.default()
                                    .addresses { listOf(Address.default().organizationName { it.value }) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }
                }

                Field(fieldDesktopStyle) {
                    Label(
                        (addressInputs * subComp("addressLine1") * title).emit(),
                        id = "addressLine1",
                        labelStyle = formLabelDesktopStyle,
                        isRequired = true
                    )
                    TextInput(address?.addressLine1 ?: "") {
                        required()
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
                Horizontal {
                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("postalCode") * title).emit(),
                            id = "postalCode",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        TextInput(address?.postalCode ?: "") {
                            required()
                            id("postalCode")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.postalCode { it.value } ?: Address.default().postalCode { it.value }
                                    )
                                } ?: UserProfile.default()
                                    .addresses { listOf(Address.default().postalCode { it.value }) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }
                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("city") * title).emit(),
                            id = "city",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        TextInput(address?.city ?: "") {
                            required()
                            id("city")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.city { it.value } ?: Address.default().city { it.value }
                                    )
                                } ?: UserProfile.default().addresses { listOf(Address.default().city { it.value }) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }
                }
                Horizontal {

                    val countries = (countryTexts * names * variables).emit().filter { it.key in listOf("DE", "AT", "CH") }
                    val countriesMap = countries.associateBy ({
                        it.value
                    }){it.key}
                    val countryStates = (countryTexts * with(CountryLangComponent){
                        statesOrProvinces(address?.countryCode ?: "DE")
                    } * variables).emit()
                    val countryStatesMap = countryStates.associateBy ({
                        it.value
                    }) {it.key}

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("countryCode") * title).emit(),
                            id = "countryCode",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        /*
                        TextInput(address?.countryCode ?: "DE") {
                            required()
                            id("countryCode")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.countryCode { it.value } ?: Address.default().countryCode { it.value }
                                    )
                                } ?: UserProfile.default()
                                    .addresses { listOf(Address.default().countryCode { it.value }) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }

                         */

                        Dropdown(
                            countriesMap,
                            countries.firstOrNull{it.key == (address?.countryCode?:"DE")}?.value,
                        ) { (_, value) ->
                            val newUserProfile = userProfileState?.addresses {
                                val address = userProfileState?.addresses?.firstOrNull()
                                listOf(
                                    address?.countryCode { value } ?: Address.default().countryCode { value }
                                )
                            } ?: UserProfile.default()
                                .addresses { listOf(Address.default().countryCode { value }) }
                            userProfileState = newUserProfile
                            setUserProfile(newUserProfile)
                        }


                    }
                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("stateOrProvince") * title).emit(),
                            id = "stateOrProvince",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        /*
                        TextInput(address?.stateOrProvince ?: "DE-BW") {
                            required()
                            id("stateOrProvince")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = userProfileState?.addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.stateOrProvince { it.value } ?: Address.default()
                                            .stateOrProvince { it.value }
                                    )
                                } ?: UserProfile.default()
                                    .addresses { listOf(Address.default().stateOrProvince { it.value }) }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                        */
                        Dropdown(
                            countryStatesMap,
                            countryStates.firstOrNull{it.key == (address?.stateOrProvince?:"DE-BW")}?.value,
                        ) { (_, value) ->
                            val newUserProfile = userProfileState?.addresses {
                                val address = userProfileState?.addresses?.firstOrNull()
                                listOf(
                                    address?.stateOrProvince { value } ?: Address.default().stateOrProvince { value }
                                )
                            } ?: UserProfile.default()
                                .addresses { listOf(Address.default().stateOrProvince { value }) }
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
                }
            }
        }
    }
}
