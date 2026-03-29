package org.solyton.solawi.bid.module.banking.component.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.change.data.Change
import org.evoleq.change.data.Keep
import org.evoleq.compose.Markup
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Horizontal
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.texts
import org.evoleq.compose.conditional.When
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.invert
import org.evoleq.math.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.bankaccount.AccountType
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccountChange
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.values.LegalEntityId
import org.solyton.solawi.bid.module.values.UserId


@Markup
@Composable
@Suppress("FunctionName")
fun BankAccountForm(
    inputs: Source<Lang.Block> = defaultInputs,
    legalEntityId: LegalEntityId,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount) -> Unit,
    hasDescription: Boolean = false,
) {

    Form(formDesktopStyle) {
        // Bank account
        val bankAccountInputs = inputs * subComp("bankAccount")
       // var legalEntityIdState by remember { mutableStateOf(legalEntityId) }
        var bankAccountIdState by remember { mutableStateOf(bankAccount?.bankAccountId?: BankAccountId(NIL_UUID)) }
        var ibanState by remember { mutableStateOf(bankAccount?.iban?.value) }
        var bicState by remember { mutableStateOf(bankAccount?.bic?.value?:"") }
        var accountHolderState by remember { mutableStateOf(bankAccount?.bankAccountHolder) }
        var isActiveState by remember { mutableStateOf(bankAccount?.isActive) }
        var accountTypeState by remember { mutableStateOf(bankAccount?.bankAccountType?: AccountType.DEBTOR) }
        var descriptionState by remember { mutableStateOf(bankAccount?.description) }
        H3{Text((bankAccountInputs * title).emit())}

        Horizontal {
            val dropdownStyle = DropdownStyles.modifyContainerStyle { width(100.percent) }

            Field(fieldDesktopStyle) {
                Label(
                    (bankAccountInputs * subComp("accountType") * title).emit(),
                    id = "bank-account-type",
                    labelStyle = formLabelDesktopStyle
                )
                val accountTypesMap: Map<String, AccountType> = mapOf(
                    "CREDITOR" to AccountType.CREDITOR,
                    "DEBTOR" to AccountType.DEBTOR,
                )
                val invertedAccountTypesMap: Map<AccountType, String> = accountTypesMap.invert()
                Dropdown(
                    options = accountTypesMap,
                    selected = invertedAccountTypesMap[accountTypeState?: AccountType.DEBTOR],
                    styles = dropdownStyle,
                    iconContent = {open -> SimpleUpDown(open) }
                ) { (_, value) ->
                    update(BankAccountChange(
                        bankAccountIdState,
                        Keep(legalEntityId),
                        Keep(ibanState),
                        Keep(bicState),
                        Keep(accountHolderState),
                        Keep(isActiveState),
                        Change(accountTypeState, value) {
                            accountTypeState = value
                        },
                        Keep(descriptionState)
                    )) {
                        bankAccount -> setBankAccount(bankAccount)
                    }
                }
            }


            Field(fieldDesktopStyle) {
                Label(
                    (bankAccountInputs * subComp("isActive") * title).emit(),
                    id = "bank-account-is-active",
                    labelStyle = formLabelDesktopStyle
                )
                val accountTypesMap: Map<String, Boolean> = mapOf(
                    "TRUE" to true,
                    "FALSE" to false,
                )
                val invertedAccountTypesMap: Map<Boolean, String> = accountTypesMap.invert()
                Dropdown(
                    options = accountTypesMap,
                    selected = invertedAccountTypesMap[isActiveState],
                    styles = dropdownStyle,
                    iconContent = {open -> SimpleUpDown(open) }
                ) { (_, value) ->
                    update(BankAccountChange(
                        bankAccountIdState,
                        Keep(legalEntityId),
                        Keep(ibanState),
                        Keep(bicState),
                        Keep(accountHolderState),
                        Change(isActiveState, value) {
                            isActiveState = value
                        },
                        Keep(accountTypeState),
                        Keep(descriptionState)
                    )) {
                        bankAccount -> setBankAccount(bankAccount)
                    }
                }
            }
        }


        Field(fieldDesktopStyle) {

            Label(
                (bankAccountInputs * subComp("accountHolder") * title).emit(),
                id = "account-holder",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(accountHolderState ?: "") {
                id("account-holder")
                style { textInputDesktopStyle() }
                onInput {
                    update(BankAccountChange(
                        bankAccountIdState,
                        Keep(legalEntityId),
                        Keep(ibanState),
                        Keep(bicState),
                        Change(accountHolderState, it.value) {
                            accountHolderState = it.value
                        },
                        Keep(isActiveState),
                        Keep( accountTypeState),
                        Keep(descriptionState)
                    )) {
                        bankAccount -> setBankAccount(bankAccount)
                    }
                }
            }
        }
        When(hasDescription) {
            Field(fieldDesktopStyle) {

                Label(
                    (bankAccountInputs * subComp("description") * title).emit(),
                    id = "description",
                    labelStyle = formLabelDesktopStyle
                )
                TextInput(descriptionState ?: "") {
                    id("description")
                    style { textInputDesktopStyle() }
                    onInput {
                        update(BankAccountChange(
                            bankAccountIdState,
                            Keep(legalEntityId),
                            Keep(ibanState),
                            Keep(bicState),
                            Keep(accountHolderState),
                            Keep(isActiveState),
                            Keep(accountTypeState),
                            Change(descriptionState,it.value){
                                descriptionState = it.value
                            }
                        )) {
                                bankAccount -> setBankAccount(bankAccount)
                        }
                    }
                }
            }
        }

        Field(fieldDesktopStyle) {

            Label(
                (bankAccountInputs * subComp("iban") * title).emit(),
                id = "iban",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(ibanState ?: "") {
                id("iban")
                style { textInputDesktopStyle() }
                onInput {
                    update(BankAccountChange(
                        bankAccountIdState,
                        Keep(legalEntityId),
                        Change(ibanState, it.value) {
                            ibanState = it.value
                        },
                       Keep(bicState),
                        Keep(accountHolderState),
                        Keep(isActiveState),
                        Keep(accountTypeState),
                        Keep(descriptionState)
                    )) {
                            bankAccount -> setBankAccount(bankAccount)
                    }
                }
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                (bankAccountInputs * subComp("bic") * title).emit(),
                id = "bic",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(bicState ?: "") {
                id("bic")
                style { textInputDesktopStyle() }
                onInput {
                    update(BankAccountChange(
                        bankAccountIdState,
                        Keep(legalEntityId),
                        Keep(ibanState),
                        Change(bicState, it.value){
                            bicState = it.value
                        },
                        Keep(accountHolderState),
                        Keep(isActiveState),
                        Keep(accountTypeState),
                        Keep(descriptionState)
                    )) {
                            bankAccount -> setBankAccount(bankAccount)
                    }
                }
            }
        }

    }
}

fun defaultBankAccountInputs() = texts {
    key = "bankAccount"
    variable{
        key = "title"
        value = "Bank Account"
    }
    block {
        key = "accountHolder"
        variable {
            key = "title"
            value = "Account Holder"
        }
    }
    block {
        key = "iban"
        variable {
            key = "title"
            value = "IBAN"
        }
    }
    block {
        key = "bic"
        variable {
            key = "title"
            value = "BIC"
        }
    }
    block {
        key = "accountType"
        variable {
            key = "title"
            value = "Account Type"
        }
    }
    block {
        key = "isActive"
        variable {
            key = "title"
            value = "Is Active"
        }
    }
    block {
        key = "description"
        variable {
            key = "title"
            value = "Description"
        }
    }
}


val defaultInputs by lazy {   Source{ defaultBankAccountInputs() }}


fun update(change: BankAccountChange, onChange: (BankAccount) -> Unit ) {
    try{
        val bankAccount = BankAccount(
            bankAccountId = change.bankAccountId,
            userId = UserId(change.legalEntityId.new!!.value),
            iban = IBAN(change.iban.new!!),
            bic = BIC(change.bic.new!!),
            bankAccountHolder = change.bankAccountHolder.new!!,
            isActive = change.isActive.new!!,
            bankAccountType = change.bankAccountType.new!!,
            description = change.description.new!!
        )
        onChange(bankAccount)
    }catch(exception: Exception){
        println(exception)
    } finally {
        change.iban.onChange()
        change.bic.onChange()
        change.bankAccountHolder.onChange()
        change.isActive.onChange()
        change.bankAccountType.onChange()
        change.description.onChange()
    }
}
