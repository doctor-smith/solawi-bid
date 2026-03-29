package org.solyton.solawi.bid.module.banking.component.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.layout.Flex
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.device.data.Device
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.FlexDirection
import org.jetbrains.compose.web.css.backgroundColor
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.flexDirection
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.maxHeight
import org.jetbrains.compose.web.css.minHeight
import org.jetbrains.compose.web.css.minWidth
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.ui.page.application.style.listItemWrapperStyle
import org.solyton.solawi.bid.application.ui.page.user.style.listItemWrapperStyle
import org.solyton.solawi.bid.module.authentication.data.deviceType
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.control.button.TrashCanButton
import org.solyton.solawi.bid.module.list.component.ActionsWrapper
import org.solyton.solawi.bid.module.list.component.DataWrapper
import org.solyton.solawi.bid.module.list.component.Header
import org.solyton.solawi.bid.module.list.component.HeaderCell
import org.solyton.solawi.bid.module.list.component.HeaderWrapper
import org.solyton.solawi.bid.module.list.component.ListItemWrapper
import org.solyton.solawi.bid.module.list.component.ListItemsIndexed
import org.solyton.solawi.bid.module.list.component.ListWrapper
import org.solyton.solawi.bid.module.list.component.TextCell
import org.solyton.solawi.bid.module.list.component.Title
import org.solyton.solawi.bid.module.list.component.TitleWrapper
import org.solyton.solawi.bid.module.list.style.ListStyles
import org.solyton.solawi.bid.module.scrollable.Scrollable
import org.solyton.solawi.bid.module.search.component.SearchInput
import org.solyton.solawi.bid.module.search.component.SearchInputStyles
import org.solyton.solawi.bid.module.style.cursor.Cursor
import org.solyton.solawi.bid.module.style.cursor.cursor
import org.solyton.solawi.bid.module.style.overflow.Overflow
import org.solyton.solawi.bid.module.style.overflow.overflowY
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
                    else -> legalEntities.filter { (id, username, _, _, profile) ->
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
                }) { index, (id, username, _, _, profile) ->
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
