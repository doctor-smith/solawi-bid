package org.solyton.solawi.bid.module.banking.component.form.sepa

import androidx.compose.runtime.*
import org.evoleq.change.data.Change
import org.evoleq.change.data.Keep
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
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
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.*
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.sepa.SepaSequenceType
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle

fun defaultSepaCollectionInputs(): Lang.Block = "inputs" texts {
    "mandateReferencePrefix" block {
        "title" colon  "Mandate Reference Prefix"
    }
    "collectionKey" block {
         "title" colon  "Collection Key"
    }
    "remittanceInformation" block {
        "title" colon  "Remittance Information"
    }
    "leadTimesDays" block {
        "title" colon  "Lead Time (Days)"
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
    val collectionKey: SepaCollectionKey? = null,
    val remittanceInformation: RemittanceInformation? = null,
    val sepaSequenceType: SepaSequenceType? = null,
    val localInstrument: LocalInstrument? = null,
    val chargeBearer: ChargeBearer? = null,
    val requestedCollectionDay: Int? = null,
    val isActive: Boolean? = null,
    val leadTimesDays: Int? = null,
    val purposeCode: PurposeCode? = null
) {

    fun toSepaCollection(): SepaCollection? = try {
        with(this) {
            SepaCollection(
                sepaCollectionId!!,
                creditorIdentifierId!!,
                creditorBankAccountId!!,
                mandateReferencePrefix!!,
                collectionKey!!,
                remittanceInformation!!,
                sepaSequenceType!!,
                localInstrument,
                chargeBearer!!,
                requestedCollectionDay,
                leadTimesDays!!,
                purposeCode,
                isActive!!,
            )
        }
    } catch (_: Exception) {null}

    companion object {
        fun from(collection: SepaCollection): PartialSepaCollection {
            return with(collection) {
                PartialSepaCollection(
                    sepaCollectionId,
                    creditorBankAccountId,
                    creditorIdentifierId,
                    mandateReferencePrefix,
                    collectionKey,
                    remittanceInformation,
                    sepaSequenceType,
                    localInstrument,
                    chargeBearer,
                    requestedCollectionDay,
                    isActive,
                    leadTimesDays,
                    purposeCode
                )
            }
        }

    }
}

data class SepaCollectionChange(
    val sepaCollectionId: SepaCollectionId?,
    val creditorIdentifierId: Change<CreditorIdentifierId>,
    val creditorBankAccountId: Change<BankAccountId>,
    val mandateReferencePrefix: Change<MandateReferencePrefix>,
    val collectionKey: Change<SepaCollectionKey>,
    val remittanceInformation: Change<RemittanceInformation>,
    val sepaSequenceType: Change<SepaSequenceType> = Keep(SepaSequenceType.FRST),
    val localInstrument: Change<LocalInstrument> = Keep(null),
    val leadTimesDays: Change<Int> = Keep(5),
    val chargeBearer: Change<ChargeBearer> = Keep(ChargeBearer("SLEV")),
    val purposeCode: Change<PurposeCode> = Keep(null),
    val isActive: Change<Boolean> = Keep(true),
    val requestedCollectionDay: Change<Int>,
)

data class FormConfiguration(
    // Show only when automated bank-account management is enabled (not present yet)
    val showRequestedCollectionDay: Boolean = false,
    val showLeadTimesDays: Boolean = true,
    val showLocalInstrument: Boolean = false,
)

@Markup
@Composable
@Suppress("FunctionName")
fun UpsertSepaCollectionForm(
    inputs: Source<Lang.Block> = Source{defaultSepaCollectionInputs()},
    configuration: FormConfiguration = FormConfiguration(),
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
        var collectionKeyState by remember{
            mutableStateOf<SepaCollectionKey?>(sepaCollection?.collectionKey)
        }
        var remittanceInformationState by remember{
            mutableStateOf<RemittanceInformation?>(sepaCollection?.remittanceInformation)
        }
        val bankAccount = sepaCollection?.let{ sC -> bankAccounts.firstOrNull { it.bankAccountId == sC.creditorBankAccountId } }
        var bankAccountState by remember{mutableStateOf<BankAccount?>( bankAccount )}

        var leadTimesDaysState by remember{
            mutableStateOf<Int?>(sepaCollection?.leadTimesDays?: 5)
        }


        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("bankAccount") * title).emit(),
                id = "bank-account",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
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
                    collectionKey = Keep(collectionKeyState),
                    mandateReferencePrefix = Keep(mandateReferencePrefixState),
                    remittanceInformation = Keep(remittanceInformationState),
                    requestedCollectionDay = Keep(requestedCollectionDayState),
                    leadTimesDays = Keep(leadTimesDaysState),
                    sepaSequenceType = Keep(sepaCollection?.sepaSequenceType),
                    localInstrument = Keep(sepaCollection?.localInstrument),
                    chargeBearer = Keep(sepaCollection?.chargeBearer),
                    isActive = Keep(sepaCollection?.isActive)
                ) ){
                        value -> setSepaCollection(value)
                }
            }
        }

        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("collectionKey") * title).emit(),
                id = "collection-key-label",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput(collectionKeyState?.value?: "") {
                id("collection-key-input")
                style { textInputDesktopStyle() }
                onInput {
                    val newValue = SepaCollectionKey(it.value)
                    update( SepaCollectionChange(
                        sepaCollectionId = sepaCollection?.sepaCollectionId,
                        creditorIdentifierId = Keep(sepaCollection?.creditorIdentifierId),
                        creditorBankAccountId = Keep(bankAccountState?.bankAccountId),
                        mandateReferencePrefix = Keep(mandateReferencePrefixState),
                        collectionKey = Change(collectionKeyState, newValue) {
                            collectionKeyState = newValue
                        },
                        remittanceInformation = Keep(remittanceInformationState),
                        leadTimesDays = Keep(leadTimesDaysState),
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
                (inputs * subComp("mandateReferencePrefix") * title).emit(),
                id = "mandate-reference-prefix",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
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
                        collectionKey = Keep(collectionKeyState),
                        remittanceInformation = Keep(remittanceInformationState),
                        requestedCollectionDay = Keep(requestedCollectionDayState),
                        leadTimesDays = Keep(leadTimesDaysState),
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
                labelStyle = formLabelDesktopStyle,
                isRequired = true
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
                        collectionKey = Keep(collectionKeyState),
                        requestedCollectionDay = Keep(requestedCollectionDayState),
                        leadTimesDays = Keep(leadTimesDaysState),
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

        When(configuration.showLeadTimesDays) {
            Field(fieldDesktopStyle) {
                Label(
                    (inputs * subComp("leadTimesDays") * title).emit(),
                    id = "label.lead-times-days",
                    labelStyle = formLabelDesktopStyle,
                    isRequired = true
                )
                TextInput(leadTimesDaysState?.toString() ?: "") {
                    id("input.lead-times-days")
                    style { textInputDesktopStyle() }
                    onInput {
                        val newValue = it.value.toIntOrNull()
                        update(
                            SepaCollectionChange(
                                sepaCollectionId = sepaCollection?.sepaCollectionId,
                                creditorIdentifierId = Keep(sepaCollection?.creditorIdentifierId),
                                creditorBankAccountId = Keep(bankAccountState?.bankAccountId),
                                mandateReferencePrefix = Keep(mandateReferencePrefixState),
                                remittanceInformation = Keep(remittanceInformationState),
                                requestedCollectionDay = Keep(requestedCollectionDayState),
                                leadTimesDays = Change(leadTimesDaysState, newValue) {
                                    leadTimesDaysState = newValue
                                },
                                collectionKey = Keep(collectionKeyState),
                                sepaSequenceType = Keep(sepaCollection?.sepaSequenceType),
                                localInstrument = Keep(sepaCollection?.localInstrument),
                                chargeBearer = Keep(sepaCollection?.chargeBearer),
                                isActive = Keep(sepaCollection?.isActive)
                            )
                        ) { value ->
                            setSepaCollection(value)
                        }
                    }
                }
            }
        }

        When(configuration.showRequestedCollectionDay) {
            Field(fieldDesktopStyle) {
                Label(
                    (inputs * subComp("requestedCollectionDay") * title).emit(),
                    id = "requested-collection-day",
                    labelStyle = formLabelDesktopStyle
                )
                TextInput(requestedCollectionDayState?.toString() ?: "") {
                    id("requested-collection-day")
                    style { textInputDesktopStyle() }
                    onInput {
                        val newValue = it.value.toIntOrNull()
                        update(
                            SepaCollectionChange(
                                sepaCollectionId = sepaCollection?.sepaCollectionId,
                                creditorIdentifierId = Keep(sepaCollection?.creditorIdentifierId),
                                creditorBankAccountId = Keep(bankAccountState?.bankAccountId),
                                mandateReferencePrefix = Keep(mandateReferencePrefixState),
                                remittanceInformation = Keep(remittanceInformationState),
                                requestedCollectionDay = Change(requestedCollectionDayState, newValue) {
                                    requestedCollectionDayState = newValue
                                },
                                leadTimesDays = Keep(leadTimesDaysState),
                                collectionKey = Keep(collectionKeyState),
                                sepaSequenceType = Keep(sepaCollection?.sepaSequenceType),
                                localInstrument = Keep(sepaCollection?.localInstrument),
                                chargeBearer = Keep(sepaCollection?.chargeBearer),
                                isActive = Keep(sepaCollection?.isActive)
                            )
                        ) { value ->
                            setSepaCollection(value)
                        }
                    }
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
            collectionKey = collectionKey.new,
            remittanceInformation = remittanceInformation.new,
            sepaSequenceType = sepaSequenceType.new,
            localInstrument = localInstrument.new,
            chargeBearer = chargeBearer.new,
            purposeCode = purposeCode.new,
            isActive = isActive.new,
            leadTimesDays = leadTimesDays.new,
            requestedCollectionDay = requestedCollectionDay.new
        )
        onChange(collection)
    }
} catch(_ : Exception) {

} finally {
    with(change){
        creditorIdentifierId.onChange()
        creditorBankAccountId.onChange()
        mandateReferencePrefix.onChange()
        collectionKey.onChange()
        remittanceInformation.onChange()
        requestedCollectionDay.onChange()
        leadTimesDays.onChange()
    }
}
