package org.solyton.solawi.bid.module.user.component.form

import androidx.compose.runtime.*
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.texts
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.jetbrains.compose.web.attributes.required
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.data.address.*

@Composable
fun UpsertAddressForm(
    texts: Source<Lang.Block> = Source { upsertAddressFormTexts },
    address: Address?,
    setAddress: (Address) -> Unit
) {
    Vertical({ width(50.percent) }) {
        // I18n
        // val formTexts = texts * subComp("upsertAddressForm")
        val addressInputs = texts * subComp("inputs")

        var addressState by remember { mutableStateOf<Address>(address?: Address.default()) }

        val horizontalFieldsStyle: StyleScope.() -> Unit = {
            width(98.percent)
        }

        H3 { Text((texts * title).emit()) }
        Horizontal(horizontalFieldsStyle) {
            Field(fieldDesktopStyle) {
                Label(
                    (addressInputs * subComp("recipientName") * title).emit(),
                    id = "recipientName",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                TextInput(addressState.recipientName ?: "") {
                    required()
                    id("recipientName")
                    style { textInputDesktopStyle() }
                    onInput {
                        addressState = addressState.recipientName { it.value }
                        setAddress(addressState)
                    }
                }
            }

            Field(fieldDesktopStyle) {
                Label(
                    (addressInputs * subComp("organizationName") * title).emit(),
                    id = "organizationName",
                    labelStyle = formLabelDesktopStyle
                )
                TextInput(addressState.organizationName ?: "") {
                    id("organizationName")
                    style { textInputDesktopStyle() }
                    onInput {
                        addressState = addressState.organizationName { it.value }
                        setAddress(addressState)
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
            TextInput(addressState.addressLine1 ?: "") {
                required()
                id("addressLine1")
                style { textInputDesktopStyle() }
                onInput {
                    addressState = addressState.addressLine1 { it.value }
                    setAddress(addressState)
                }
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                (addressInputs * subComp("addressLine2") * title).emit(),
                id = "addressLine2",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(addressState.addressLine2 ?: "") {
                id("addressLine2")
                style { textInputDesktopStyle() }
                onInput {
                    addressState = addressState.addressLine2 { it.value }
                    setAddress(addressState)
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
                TextInput(addressState.postalCode ?: "") {
                    required()
                    id("postalCode")
                    style { textInputDesktopStyle() }
                    onInput {
                        addressState = addressState.postalCode { it.value }
                        setAddress(addressState)
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
                TextInput(addressState.city ?: "") {
                    required()
                    id("city")
                    style { textInputDesktopStyle() }
                    onInput {
                        addressState = addressState.city { it.value }
                        setAddress(addressState)
                    }
                }
            }
        }
        /*
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

         */
    }
}

val upsertAddressFormTexts by lazy {
    "upsertAddressForm" texts {
        "title" colon "Address"
        "inputs" block {
            "recipientName" block {
                "title" colon "Recipient Name"
            }
            "organizationName" block {
                "title" colon "Name of Organization"
            }
            "addressLine1" block {
                "title" colon "Address Line 1"
            }
            "addressLine2" block {
                "title" colon "Address Line 2"
            }
            "postalCode" block {
                "title" colon "Postal Code"
            }
            "city" block {
                "title" colon "City"
            }
            "countyCode" block {
                "title" colon "County Code"
            }
            "stateOrProvince" block {
                "title" colon "State or Province"
            }
        }
    }
}
