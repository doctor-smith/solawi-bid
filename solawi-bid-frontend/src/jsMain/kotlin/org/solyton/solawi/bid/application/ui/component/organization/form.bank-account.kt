package org.solyton.solawi.bid.application.ui.component.organization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.serializationx.ZeroUUID
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.BIC
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.IBAN
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.values.UserId


@Markup
@Composable
@Suppress("FunctionName")
fun BankAccountForm(
    inputs: Source<Lang.Block>,
    userId: UserId,
    bankAccount: BankAccount?,
    setBankAccount: (BankAccount) -> Unit,
) {
    Form(formDesktopStyle) {
        // Bank account
        val bankAccountInputs = inputs * subComp("bankAccount")
        var ibanState by remember { mutableStateOf(bankAccount?.iban?.value) }
        var bicState by remember { mutableStateOf(bankAccount?.bic?.value) }

        H3{Text((bankAccountInputs * title).emit())}
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
                    try {
                        val newBankAccount = bankAccount?.copy(iban = IBAN(it.value))?: BankAccount(
                            userId = userId,
                            bankAccountId = BankAccountId(NIL_UUID),
                            iban = IBAN(it.value),
                            bic = BIC(bicState?:""),
                        )
                        setBankAccount(newBankAccount)
                    } catch (exception: Exception) {
                        // validation stuff
                    } finally {
                        ibanState = it.value
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
                    try {
                        val newBankAccount = bankAccount?.copy(bic = BIC(it.value))?: BankAccount(
                            userId = userId,
                            bankAccountId = BankAccountId(NIL_UUID),
                            iban = IBAN(ibanState?:""),
                            bic = BIC(it.value),
                        )
                        setBankAccount(newBankAccount)
                    } catch (exception: Exception) {
                        // validation stuff
                    } finally {
                        bicState = it.value
                    }
                }
            }
        }
    }
}
