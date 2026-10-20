package org.solyton.solawi.bid.module.user.component.form

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.dataId
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
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.country.i18n.CountryLangComponent
import org.solyton.solawi.bid.module.country.i18n.CountryLangComponent.Companion.names
import org.solyton.solawi.bid.module.i18n.data.variables
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.data.address.*
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.solyton.solawi.bid.module.user.data.profile.addresses

@Markup
@Composable
@Suppress("FunctionName", "CyclomaticComplexMethod", "UnusedPrivateProperty", "UnusedParameter")
fun UserProfileForm(
    device: Source<DeviceType>,
    inputs: Source<Lang.Block>,
    countryTexts: Source<Lang.Block>,
    userProfile: UserProfile?,
    setUserProfile: (UserProfile) -> Unit,
){
    // val scope = rememberCoroutineScope()

    var userProfileState by remember { mutableStateOf<UserProfile?>(userProfile) }
    Form(formDesktopStyle) {
        val horizontalFieldsStyle: StyleScope.() -> Unit = {
            width(98.percent)
        }

        // User Profile is used everywhere
        val userProfileInputs = inputs * subComp("userProfile")


        Horizontal {

            Vertical({ width(50.percent) }) {
                H3 { Text((userProfileInputs * Reader { lang -> lang["formTitle"] }).emit()) }

                Field(fieldDesktopStyle) {
                    Label(
                        (userProfileInputs * subComp("title") * title).emit(),
                        id = "title",
                        labelStyle = formLabelDesktopStyle
                    )
                    TextInput(userProfileState?.title ?: "") {
                        id("title")
                        dataId("user-profile.form.title-input")
                        style { textInputDesktopStyle() }
                        onInput {
                            val newUserProfile =
                                (userProfileState ?: UserProfile.default).copy(title = it.value)
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
                        dataId("user-profile.form.firstname-input")
                        style { textInputDesktopStyle() }
                        onInput {
                            val newUserProfile =
                                (userProfileState ?: UserProfile.default).copy(firstname = it.value)
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
                        dataId("user-profile.form.lastname-input")
                        id("lastname")
                        style { textInputDesktopStyle() }
                        onInput {
                            val newUserProfile =
                                (userProfileState ?: UserProfile.default).copy(lastname = it.value)
                            userProfileState = newUserProfile
                            setUserProfile(newUserProfile)
                        }
                    }
                }
                Horizontal(horizontalFieldsStyle) {
                    Field({ fieldDesktopStyle() }) {
                        Label(
                            (userProfileInputs * subComp("phoneNumber") * title).emit(),
                            id = "phoneNumber",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(userProfileState?.phoneNumber ?: "") {
                            id("phoneNumber")
                            dataId("user-profile.form.phone-number-input")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile =
                                    (userProfileState ?: UserProfile.default).copy(phoneNumber = it.value)
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }

                    Field(fieldDesktopStyle) {
                        Label(
                            (userProfileInputs * subComp("cellPhoneNumber") * title).emit(),
                            id = "phoneNumber1",
                            labelStyle = formLabelDesktopStyle
                        )
                        TextInput(userProfileState?.phoneNumber1 ?: "") {
                            id("phoneNumber1")
                            dataId("user-profile.form.cell-phone-number-input")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile =
                                    (userProfileState ?: UserProfile.default).copy(phoneNumber1 = it.value)
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }
                }
            }

            Vertical({ width(50.percent) }) {
                // Address
                val addressInputs = userProfileInputs * subComp("address")
                val address = userProfileState?.addresses?.firstOrNull()

                H3 { Text((addressInputs * title).emit()) }
                Horizontal(horizontalFieldsStyle) {
                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("recipientName") * title).emit(),
                            id = "recipientName",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        TextInput(address?.recipientName ?: "") {
                            required()
                            dataId("user-profile.form.address.recipient-name-input")
                            id("recipientName")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.recipientName { it.value } ?: Address.default()
                                            .recipientName { it.value }
                                    )
                                }
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
                            dataId("user-profile.form.address.organization-name-input")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.organizationName { it.value } ?: Address.default()
                                            .organizationName { it.value }
                                    )
                                }
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
                        dataId("user-profile.form.address.address-line-1-input")
                        id("addressLine1")
                        style { textInputDesktopStyle() }
                        onInput {
                            val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                val address = userProfileState?.addresses?.firstOrNull()
                                listOf(
                                    address?.addressLine1 { it.value } ?: Address.default().addressLine1 { it.value }
                                )
                            }
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
                        dataId("user-profile.form.address.address-line-2-input")
                        style { textInputDesktopStyle() }
                        onInput {
                            val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                val address = userProfileState?.addresses?.firstOrNull()
                                listOf(
                                    address?.addressLine2 { it.value } ?: Address.default().addressLine2 { it.value }
                                )
                            }
                            userProfileState = newUserProfile
                            setUserProfile(newUserProfile)
                        }
                    }
                }
                Horizontal(horizontalFieldsStyle) {
                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("postalCode") * title).emit(),
                            id = "postalCode",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        TextInput(address?.postalCode ?: "") {
                            required()
                            dataId("user-profile.form.address.postal-code-input")
                            id("postalCode")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.postalCode { it.value } ?: Address.default().postalCode { it.value }
                                    )
                                }
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
                            dataId("user-profile.form.address.city-input")
                            style { textInputDesktopStyle() }
                            onInput {
                                val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                    val address = userProfileState?.addresses?.firstOrNull()
                                    listOf(
                                        address?.city { it.value } ?: Address.default().city { it.value }
                                    )
                                }
                                userProfileState = newUserProfile
                                setUserProfile(newUserProfile)
                            }
                        }
                    }
                }
                Horizontal(horizontalFieldsStyle) {

                    val countries =
                        (countryTexts * names * variables).emit().filter { it.key in listOf("DE", "AT", "CH") }
                    val countriesMap = countries.associateBy({
                        it.value
                    }) { it.key }
                    val countryStates = (countryTexts * with(CountryLangComponent) {
                        statesOrProvinces(address?.countryCode ?: "DE")
                    } * variables).emit()
                    val countryStatesMap = countryStates.associateBy({
                        it.value
                    }) { it.key }

                    val dropdownStyles = DropdownStyles()
                        .modifyContainerStyle {
                            marginTop(5.px)
                            width(100.percent)
                        }

                    Field(fieldDesktopStyle) {
                        Label(
                            (addressInputs * subComp("countryCode") * title).emit(),
                            id = "countryCode",
                            labelStyle = formLabelDesktopStyle,
                            isRequired = true
                        )
                        Dropdown(
                            options = countriesMap,
                            selected = countries.firstOrNull { it.key == (address?.countryCode ?: "DE") }?.value,
                            styles = dropdownStyles,
                            dataId = "user-profile.form.address.country-code-input",
                            iconContent = { expanded -> SimpleUpDown(expanded) }
                        ) { (_, value) ->
                            val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                val address = userProfileState?.addresses?.firstOrNull()
                                listOf(
                                    address?.countryCode { value } ?: Address.default().countryCode { value }
                                )
                            }
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
                        Dropdown(
                            options = countryStatesMap,
                            selected = countryStates.firstOrNull {
                                it.key == (address?.stateOrProvince ?: "DE-BW")
                            }?.value,
                            styles = dropdownStyles,
                            dataId = "user-profile.form.address.state-or-province-input",
                            iconContent = { expanded -> SimpleUpDown(expanded) }
                        ) { (_, value) ->
                            val newUserProfile = (userProfileState ?: UserProfile.default).addresses {
                                val address = userProfileState?.addresses?.firstOrNull()
                                listOf(
                                    address?.stateOrProvince { value } ?: Address.default().stateOrProvince { value }
                                )
                            }
                            userProfileState = newUserProfile
                            setUserProfile(newUserProfile)
                        }
                    }
                }
            }
        }
    }
}
