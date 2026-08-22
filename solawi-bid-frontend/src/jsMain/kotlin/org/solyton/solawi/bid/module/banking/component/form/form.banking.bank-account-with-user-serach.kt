package org.solyton.solawi.bid.module.banking.component.form

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.list.component.*
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.user.data.managed.ManagedUser
import org.solyton.solawi.bid.module.values.LegalEntityId

@Markup
@Composable
@Suppress("FunctionName")
fun BankAccountFormWithUserSearch(
    inputs: Source<Lang.Block> = defaultInputs,
    legalEntities: List<ManagedUser> = emptyList(),
    legalEntityId: LegalEntityId?,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount) -> Unit,
    hasDescription: Boolean = false,
) {
    /*
    val listStyles = ListStyles().modifyDataWrapper {
        overflowY(Overflow.Auto)
    }

     */

    var legalEntityIdState by remember { mutableStateOf<LegalEntityId>(legalEntityId?: LegalEntityId(NIL_UUID)) }
    Wrap {
        ListWrapper {
            var searchInput by remember { mutableStateOf("") }
            var searching by remember { mutableStateOf(false) }
            var filteredLegalEntities by remember { mutableStateOf(legalEntities) }
            LaunchedEffect(searchInput, legalEntityIdState, searching, legalEntities) {
                filteredLegalEntities = when {
                    legalEntityIdState.value != NIL_UUID && !searching -> listOf(legalEntities.first { it.id == legalEntityIdState.value })
                    searchInput.isBlank() -> legalEntities
                    else -> legalEntities.filter { (id, username, _, _,_, profile) ->
                        id == legalEntityIdState.value ||
                        username.contains(searchInput, ignoreCase = true)
                                || profile?.firstname?.contains(searchInput, ignoreCase = true) ?: false
                                || profile?.lastname?.contains(searchInput, ignoreCase = true) ?: false
                    }
                }
            }
            TitleWrapper {
                Title { H3 { Text("Legal Entities Search") } }
            }
            SearchInput(searchInput, SearchInputStyles()) {
                searching = true
                searchInput = it
            }
            HeaderWrapper {
                Header {
                    HeaderCell("Username"){ width(20.percent)}
                    HeaderCell("Firstname"){ width(10.percent)}
                    HeaderCell("Lastname"){ width(10.percent)}
                }
            }
            Scrollable {
                ListItemsIndexed(filteredLegalEntities.sortedBy {
                    if (it.id == legalEntityIdState.value) 0 else 1
                }) { index, (id, username, _, _,_, profile) ->
                    ListItemWrapper({
                        listItemWrapperStyle(index)
                    }) {
                        DataWrapper(
                            onClick = {
                                legalEntityIdState = LegalEntityId(id)
                                searching = false
                            }
                        ) {
                            TextCell(username) {
                                maxHeight(60.px)
                                minWidth(100.px)
                                width(20.percent); cursor(Cursor.Pointer)
                            }
                            TextCell(profile?.firstname ?: "N/A"){
                                maxHeight(60.px)
                                minWidth(100.px)
                                width(10.percent)
                            }
                            TextCell(profile?.lastname ?: "N/A"){
                                maxHeight(60.px)
                                minWidth(100.px)
                                width(10.percent)
                            }
                        }
                        ActionsWrapper {
                            When(id == legalEntityIdState.value) {
                                TrashCanButton(
                                    color = Color.black,
                                    bgColor = Color.white,
                                    deviceType = { DeviceType.Desktop },
                                    texts = { "Deselect" },
                                    isDisabled = true
                                ) {
                                    legalEntityIdState = LegalEntityId(NIL_UUID)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    BankAccountForm(inputs, legalEntityIdState, bankAccount, setBankAccount,hasDescription = hasDescription)
}
