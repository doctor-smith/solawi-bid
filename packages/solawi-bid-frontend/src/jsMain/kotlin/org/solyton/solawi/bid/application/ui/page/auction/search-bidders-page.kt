package org.solyton.solawi.bid.application.ui.page.auction

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Vertical
import org.evoleq.language.component
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.JustifyContent
import org.jetbrains.compose.web.css.justifyContent
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.*
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.ui.page.auction.action.searchUsernameOfBidder
import org.solyton.solawi.bid.application.ui.style.form.fieldStyle
import org.solyton.solawi.bid.application.ui.style.form.formLabelStyle
import org.solyton.solawi.bid.application.ui.style.form.formStyle
import org.solyton.solawi.bid.application.ui.style.form.textInputStyle
import org.solyton.solawi.bid.application.ui.style.page.verticalPageStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.bid.data.api.SearchBidderData
import org.solyton.solawi.bid.module.bid.data.reader.*
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Composable
@Suppress("FunctionName")
fun SearchBiddersPage(storage: Storage<Application>) = Div {

    // State
    var fstname by remember{ mutableStateOf("") }
    var lstname by remember{ mutableStateOf("") }
    var email by remember{ mutableStateOf("") }
    var emails by remember{ mutableStateOf("") }
    var data by remember{ mutableStateOf("") }

    // Data
    val device = storage * deviceData * mediaType.get
    val texts = storage * i18N * language * component(BidComponent.SearchBiddersPage)
    val searchButton = texts * subComp("buttons") * subComp("search")
    val inputs = texts * subComp("inputs")
    val results =texts * subComp("results")

    // Markup
    Vertical(verticalPageStyle) {
        Wrap { Horizontal(styles = { justifyContent(JustifyContent.SpaceBetween); width(100.percent) }) {

            H1 { Text((texts * title).emit()) }
            Horizontal {
                StdButton(searchButton * title, device) {
                    val searchBidderData = SearchBidderData(
                        firstname = fstname,
                        lastname = lstname,
                        email = email,
                        relatedEmails = emails.split(",").map{it.trim()}.filter { it.isNotBlank() },
                        relatedNames = data.split(",").map{it.trim()}.filter { it.isNotBlank() }
                    )
                    CoroutineScope(Job()).launch {
                        (storage * actions).read().emit(searchUsernameOfBidder(searchBidderData))
                    }
                }
            }
        } }
        Wrap {
            Div(attrs = {
                style { formStyle(device.emit())() }
            }) {
                Div(attrs = { style { fieldStyle(device.emit())() } }) {
                    Label((inputs * firstname).emit(), id = "firstname", labelStyle = formLabelStyle(device.emit()))
                    TextInput(fstname) {
                        style { textInputStyle(device.emit())() }
                        id("firstname")
                        onInput {
                            fstname = it.value
                        }
                    }
                }

                Div(attrs = { style { fieldStyle(device.emit())() } }) {
                    Label((inputs * lastname).emit(), id = "lastname", labelStyle = formLabelStyle(device.emit()))
                    TextInput(lstname) {
                        style { textInputStyle(device.emit())() }
                        id("lastname")
                        onInput {
                            lstname = it.value
                        }
                    }
                }

                Div(attrs = { style { fieldStyle(device.emit())() } }) {
                    Label((inputs * emailAddress).emit(), id = "email", labelStyle = formLabelStyle(device.emit()))
                    TextInput(email) {
                        style { textInputStyle(device.emit())() }
                        id("email")
                        onInput {
                            email = it.value
                        }
                    }
                }

                Div(attrs = { style { fieldStyle(device.emit())() } }) {
                    Label((inputs * relatedEmailAddresses).emit(), id = "emails", labelStyle = formLabelStyle(device.emit()))
                    TextInput(emails) {
                        style { textInputStyle(device.emit())() }
                        id("emails")
                        onInput {
                            emails = it.value
                        }
                    }
                }

                Div(attrs = { style { fieldStyle(device.emit())() } }) {
                    Label((inputs * relatedData).emit(), id = "data", labelStyle = formLabelStyle(device.emit()))
                    TextInput(data) {
                        style { textInputStyle(device.emit())() }
                        id("data")
                        onInput {
                            data = it.value
                        }
                    }
                }
            }
        }
        Wrap{
            H2{Text((results * title).emit())}
            (storage * bidderMailAddresses.get).emit().emails.forEach {
                P{ Text(it) }
            }
        }
    }
}
