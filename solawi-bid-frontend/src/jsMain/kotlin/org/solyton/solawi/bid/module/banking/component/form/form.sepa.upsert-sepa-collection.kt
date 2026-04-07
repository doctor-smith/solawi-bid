package org.solyton.solawi.bid.module.banking.component.form

import androidx.compose.runtime.*
import kotlinx.serialization.json.internal.decodeByReader
import org.evoleq.change.data.Change
import org.evoleq.change.data.Keep
import org.evoleq.compose.Markup
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.texts
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.bankaccount.bankAccountHolder
import org.solyton.solawi.bid.module.banking.data.bankaccount.isActive
import org.solyton.solawi.bid.module.banking.data.creditor.identifier.CreditorIdentifier
import org.solyton.solawi.bid.module.banking.data.sepa.SepaSequenceType
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.collection.chargeBearer
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle

fun defaultSepaCollectionInputs(): Lang.Block = texts{
    key = "inputs"
    block{
        key = "mandateReferencePrefix"
        variable {
            key = "title"
            value = "Mandate Reference Prefix"
        }
    }
    block {
        key = "remittanceInformation"
        variable {
            key = "title"
            value = "Remittance Information"
        }
    }
    "requestedCollectionDay" block {
        "title" colon "Requested Collection Day"
    }
    "bankAccount" block {
        "title" colon "Creditor Bank Account"
    }
}

data class PartialSepaCollection(
    val sepaCollectionId: SepaCollectionId? = null,
    val creditorBankAccountId: BankAccountId? = null,
    val creditorIdentifierId: CreditorIdentifierId? = null,
    val mandateReferencePrefix: MandateReferencePrefix? = null,
    val remittanceInformation: RemittanceInformation? = null,
    val sepaSequenceType: SepaSequenceType? = null,
    val localInstrument: LocalInstrument? = null,
    val chargeBearer: ChargeBearer? = null,
    val requestedCollectionDay: Int? = null,
    val isActive: Boolean? = null,
    val leadTimesDays: Int? = null,
    val purposeCode: PurposeCode? = null
)

data class SepaCollectionChange(
    val sepaCollectionId: SepaCollectionId?,
    val creditorIdentifierId: Change<CreditorIdentifierId>,
    val creditorBankAccountId: Change<BankAccountId>,
    val mandateReferencePrefix: Change<MandateReferencePrefix>,
    val remittanceInformation: Change<RemittanceInformation>,
    val sepaSequenceType: Change<SepaSequenceType> = Keep(SepaSequenceType.FRST),
    val localInstrument: Change<LocalInstrument> = Keep(null),
    val leadTimesDays: Change<Int> = Keep(5),
    val chargeBearer: Change<ChargeBearer> = Keep(ChargeBearer("SLEV")),
    val purposeCode: Change<PurposeCode> = Keep(null),
    val isActive: Change<Boolean> = Keep(true),
    val requestedCollectionDay: Change<Int>,
)

@Markup
@Composable
@Suppress("FunctionName")
fun SepaCollectionForm(
    inputs: Source<Lang.Block> = Source{defaultSepaCollectionInputs()},
    bankAccounts: List<BankAccount>,
    sepaCollection: PartialSepaCollection?,
    setSepaCollection: (PartialSepaCollection) -> Unit
) {
    Form(formDesktopStyle) {

        var mandateReferencePrefixState by remember{
            mutableStateOf<MandateReferencePrefix?>(sepaCollection?.mandateReferencePrefix)
        }
        var requestedCollectionDayState by remember{
            mutableStateOf<Int?>(sepaCollection?.requestedCollectionDay)
        }
        var remittanceInformationState by remember{
            mutableStateOf<RemittanceInformation?>(sepaCollection?.remittanceInformation)
        }
        val bankAccount = sepaCollection?.let{ sC -> bankAccounts.firstOrNull { it.bankAccountId == sC.creditorBankAccountId } }
        var bankAccountState by remember{mutableStateOf<BankAccount?>( bankAccount )}

        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("mandateReferencePrefix") * title).emit(),
                id = "mandate-reference-prefix",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(mandateReferencePrefixState?.value?: "") {
                id("mandate-reference-prefix")
                style { textInputDesktopStyle() }
                onInput {
                    val newValue = MandateReferencePrefix(it.value)
                    update( SepaCollectionChange(
                        sepaCollectionId = sepaCollection?.sepaCollectionId,
                        creditorIdentifierId = Keep(sepaCollection?.creditorIdentifierId),
                        creditorBankAccountId = Keep(bankAccountState?.bankAccountId),
                        mandateReferencePrefix = Change(mandateReferencePrefixState, newValue) {
                            mandateReferencePrefixState = newValue
                        }  ,
                        remittanceInformation = Keep(remittanceInformationState),
                        requestedCollectionDay = Keep(requestedCollectionDayState),
                        sepaSequenceType = Keep(sepaCollection?.sepaSequenceType),
                        localInstrument = Keep(sepaCollection?.localInstrument),
                        chargeBearer = Keep(sepaCollection?.chargeBearer),
                        isActive = Keep(sepaCollection?.isActive)
                    ) ){
                        value -> setSepaCollection(value)
                    }
                }
            }
        }


        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("remittanceInformation") * title).emit(),
                id = "remittance-information",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(remittanceInformationState?.value?: "") {
                id("remittance-information")
                style { textInputDesktopStyle() }
                onInput {
                    val newValue = RemittanceInformation(it.value)
                    update( SepaCollectionChange(
                        sepaCollectionId = sepaCollection?.sepaCollectionId,
                        creditorIdentifierId = Keep(sepaCollection?.creditorIdentifierId),
                        creditorBankAccountId = Keep(bankAccountState?.bankAccountId),
                        mandateReferencePrefix = Keep(mandateReferencePrefixState),
                        remittanceInformation = Change(remittanceInformationState, newValue) {
                            remittanceInformationState = newValue
                        },
                        requestedCollectionDay = Keep(requestedCollectionDayState),
                        sepaSequenceType = Keep(sepaCollection?.sepaSequenceType),
                        localInstrument = Keep(sepaCollection?.localInstrument),
                        chargeBearer = Keep(sepaCollection?.chargeBearer),
                        isActive = Keep(sepaCollection?.isActive)
                    ) ){
                        value -> setSepaCollection(value)
                    }
                }
            }
        }


        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("requestedCollectionDay") * title).emit(),
                id = "requested-collection-day",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(requestedCollectionDayState?.toString()?: "") {
                id("requested-collection-day")
                style { textInputDesktopStyle() }
                onInput {
                    val newValue = it.value.toIntOrNull()
                    update( SepaCollectionChange(
                        sepaCollectionId = sepaCollection?.sepaCollectionId,
                        creditorIdentifierId = Keep(sepaCollection?.creditorIdentifierId),
                        creditorBankAccountId = Keep(bankAccountState?.bankAccountId),
                        mandateReferencePrefix = Keep(mandateReferencePrefixState),
                        remittanceInformation = Keep(remittanceInformationState),
                        requestedCollectionDay = Change(requestedCollectionDayState, newValue) {
                            requestedCollectionDayState = newValue
                        },
                        sepaSequenceType = Keep(sepaCollection?.sepaSequenceType),
                        localInstrument = Keep(sepaCollection?.localInstrument),
                        chargeBearer = Keep(sepaCollection?.chargeBearer),
                        isActive = Keep(sepaCollection?.isActive)
                    ) ){
                            value -> setSepaCollection(value)
                    }
                }
            }
        }
        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("bankAccount") * title).emit(),
                id = "bank-account",
                labelStyle = formLabelDesktopStyle
            )
            val bankAccountOptions = bankAccounts.associateBy {
                bankAccount -> "${bankAccount.bankAccountHolder} / ${bankAccount.description}"
            }

            val dropdownStyles = DropdownStyles().modifyContainerStyle { width(100.percent) }
            Dropdown(
                options = bankAccountOptions,
                selected = bankAccountState?.let{ "${it.bankAccountHolder} / ${it.description}" }?: "Select Bank Account",
                styles = dropdownStyles,
                iconContent = {opened -> SimpleUpDown(opened) }
            ) { (_, newValue) ->
                update( SepaCollectionChange(
                    sepaCollectionId = sepaCollection?.sepaCollectionId,
                    creditorIdentifierId = Keep(sepaCollection?.creditorIdentifierId),
                    creditorBankAccountId = Change(bankAccountState?.bankAccountId, newValue.bankAccountId) {
                        bankAccountState = newValue
                    },
                    mandateReferencePrefix = Keep(mandateReferencePrefixState),
                    remittanceInformation = Keep(remittanceInformationState),
                    requestedCollectionDay = Keep(requestedCollectionDayState),
                    sepaSequenceType = Keep(sepaCollection?.sepaSequenceType),
                    localInstrument = Keep(sepaCollection?.localInstrument),
                    chargeBearer = Keep(sepaCollection?.chargeBearer),
                    isActive = Keep(sepaCollection?.isActive)
                ) ){
                        value -> setSepaCollection(value)
                }
            }
        }
    }
}

fun update(change: SepaCollectionChange, onChange: (PartialSepaCollection)-> Unit): Unit = try {
    with(change) {
        val collection = PartialSepaCollection(
            sepaCollectionId = change.sepaCollectionId,
            creditorIdentifierId = creditorIdentifierId.new,
            creditorBankAccountId = creditorBankAccountId.new,
            mandateReferencePrefix = mandateReferencePrefix.new,
            remittanceInformation = remittanceInformation.new,
            sepaSequenceType = sepaSequenceType.new,
            localInstrument = localInstrument.new,
            chargeBearer = chargeBearer.new,
            purposeCode = purposeCode.new,
            isActive = isActive.new,
            leadTimesDays = leadTimesDays.new,
            requestedCollectionDay = requestedCollectionDay.new
        )
        println(collection)
        onChange(collection)
    }
} catch(_ : Exception) {

} finally {
    with(change){
        creditorIdentifierId.onChange()
        creditorBankAccountId.onChange()
        mandateReferencePrefix.onChange()
        remittanceInformation.onChange()
        requestedCollectionDay.onChange()
    }
}
