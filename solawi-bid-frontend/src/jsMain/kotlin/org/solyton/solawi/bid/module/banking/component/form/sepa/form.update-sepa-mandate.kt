package org.solyton.solawi.bid.module.banking.component.form.sepa

import androidx.compose.runtime.*
import kotlinx.datetime.LocalDateTime
import org.evoleq.change.data.Change
import org.evoleq.change.data.Keep
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.conditional.When
import org.evoleq.compose.date.format
import org.evoleq.compose.date.parse
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.kotlinx.date.now
import org.evoleq.kotlinx.date.toDateTime
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.language.texts
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.StyleScope
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.application.i18n.inputs
import org.solyton.solawi.bid.module.banking.data.BankAccountId
import org.solyton.solawi.bid.module.banking.data.MandateReference
import org.solyton.solawi.bid.module.banking.data.SepaCollectionId
import org.solyton.solawi.bid.module.banking.data.SepaMandateId
import org.solyton.solawi.bid.module.banking.data.bankaccount.BankAccount
import org.solyton.solawi.bid.module.banking.data.sepa.MandateStatus
import org.solyton.solawi.bid.module.banking.data.sepa.collection.SepaCollection
import org.solyton.solawi.bid.module.banking.data.sepa.mandate.SepaMandate
import org.solyton.solawi.bid.module.banking.i18n.*
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.SimpleUpDown
import org.solyton.solawi.bid.module.style.form.dateInputDesktopStyle
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.user.component.dropdown.dropdownStyles


@Composable
@Suppress("CyclomaticComplexMethod")
fun UpsertSepaMandateForm(
    texts: Source<Lang.Block> = updateSepaMandateFormTexts,
    collections: List<SepaCollection>,
    bankAccount: BankAccount?,
    sepaMandate: SepaMandate?,
    isReadOnly: Boolean = false,
    setSepaMandate: (SepaMandate) -> Unit,
) = Form(formDesktopStyle){

    val dropdownStyles = dropdownStyles.modifyContainerStyle {
        width(100.percent)
    }

    val inputs = texts * inputs

    val bankAccountId = sepaMandate?.debtorBankAccountId ?: bankAccount?.bankAccountId?: BankAccountId(NIL_UUID)
    var debtorNameState by remember { mutableStateOf(sepaMandate?.debtorName?:bankAccount?.bankAccountHolder) }
    var mandateReferenceState by remember { mutableStateOf(sepaMandate?.mandateReference) }
    var statusState by remember { mutableStateOf(sepaMandate?.status ?: MandateStatus.ACTIVE) }
    var signedAtState by remember { mutableStateOf(sepaMandate?.signedAt) }
    var validFromState by remember { mutableStateOf(sepaMandate?.validFrom) }
    var validUntilState by remember { mutableStateOf(sepaMandate?.validUntil) }
    var lastUsedAtState by remember { mutableStateOf(sepaMandate?.lastUsedAt) }
    var isActiveState by remember { mutableStateOf(sepaMandate?.isActive ?: true) }
    var amendmentOfState by remember { mutableStateOf(sepaMandate?.amendmentOf) }
    var collectionIdState by remember {
        mutableStateOf(sepaMandate?.collectionId ?: collections.firstOrNull()?.sepaCollectionId)
    }

    LaunchedEffect(sepaMandate) {
        debtorNameState = sepaMandate?.debtorName ?: bankAccount?.bankAccountHolder
        mandateReferenceState = sepaMandate?.mandateReference
        statusState = sepaMandate?.status ?: MandateStatus.ACTIVE
        signedAtState = sepaMandate?.signedAt
        validFromState = sepaMandate?.validFrom
        validUntilState = sepaMandate?.validUntil
        lastUsedAtState = sepaMandate?.lastUsedAt
        isActiveState = sepaMandate?.isActive ?: true
        amendmentOfState = sepaMandate?.amendmentOf
        collectionIdState = sepaMandate?.collectionId ?: collections.firstOrNull()?.sepaCollectionId
    }

    val fieldDesktopStyle: StyleScope.() -> Unit = {
        fieldDesktopStyle()
        width(100.percent)
    }

    // Debtor name
    Field(fieldDesktopStyle) {
        Label(
            text = (inputs * debtorName * label * title).emit(),
            id = "debtor-name",
            labelStyle = formLabelDesktopStyle,
            isRequired = true
        )
        TextInput(debtorNameState?: "") {
            style { width(100.percent) }
            disabled()
            onInput { event ->
                if (isReadOnly) return@onInput
                val newState = event.value
                update(SepaMandateChange(
                    sepaMandateId = sepaMandate?.sepaMandateId,
                    debtorBankAccountId = Keep(bankAccountId),
                    debtorName = Change(debtorNameState, newState){
                        debtorNameState = newState
                    },
                    mandateReference = Keep(mandateReferenceState),
                    signedAt = Keep(signedAtState),
                    status = Keep(statusState),
                    validFrom = Keep(validFromState),
                    validUntil = Keep(validUntilState),
                    lastUsedAt = Keep(lastUsedAtState),
                    isActive = Keep(isActiveState),
                    amendmentOf = Keep(amendmentOfState),
                    collectionId = Keep(collectionIdState)
                )) {
                        sepaMandate -> setSepaMandate(sepaMandate)
                }
            }
        }
    }
    When(sepaMandate != null){
        // Mandate Reference
        Field(fieldDesktopStyle) {
            Label(
                text = (inputs * mandateReference * label * title).emit(),
                id = "mandate-reference",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )
            TextInput(mandateReferenceState?.value?: "") {
                style { width(100.percent) }
                disabled()
                onInput { event ->
                    if (isReadOnly) return@onInput
                    update(SepaMandateChange(
                        sepaMandateId = sepaMandate?.sepaMandateId,
                        debtorBankAccountId = Keep(bankAccountId),
                        debtorName = Keep(debtorNameState),
                        mandateReference = Change(mandateReferenceState, MandateReference(event.value)){
                            mandateReferenceState = MandateReference(event.value)
                        },
                        signedAt = Keep(signedAtState),
                        status = Keep(statusState),
                        validFrom = Keep(validFromState),
                        validUntil = Keep(validUntilState),
                        lastUsedAt = Keep(lastUsedAtState),
                        isActive = Keep(isActiveState),
                        amendmentOf = Keep(amendmentOfState),
                        collectionId = Keep(collectionIdState)
                    )) {
                            sepaMandate -> setSepaMandate(sepaMandate)
                    }
                }
            }
        }
    }
    When(sepaMandate == null){
        // Choose Collection / Reference Prefix
        Field(fieldDesktopStyle) {
            Label(
                text = (inputs * collection * label * title).emit(),
                id = "collection-key-reference-prefix",
                labelStyle = formLabelDesktopStyle,
                isRequired = true
            )

            val options = collections.associateBy({"${it.collectionKey.value} / ${it.mandateReferencePrefix.value}"}) {it}
            val selectedOption = collections.firstOrNull { it.sepaCollectionId == collectionIdState }?.let {
                "${it.collectionKey.value} / ${it.mandateReferencePrefix.value}"
            } ?: options.keys.firstOrNull() ?: ""
            Dropdown(
                options = options,
                selected = selectedOption,
                isDisabled = { isReadOnly },
                iconContent = { open -> SimpleUpDown(open) },
                styles = dropdownStyles,
            ) { (_, value) ->
                if (isReadOnly) return@Dropdown
                update(
                    SepaMandateChange(
                        sepaMandateId = sepaMandate?.sepaMandateId,
                        debtorBankAccountId = Keep(bankAccountId),
                        debtorName = Keep(debtorNameState),
                        mandateReference = Keep(mandateReferenceState),
                        signedAt = Keep(signedAtState),
                        status = Keep(statusState),
                        validFrom = Keep(validFromState),
                        validUntil = Keep(validUntilState),
                        lastUsedAt = Keep(lastUsedAtState),
                        isActive = Keep(isActiveState),
                        amendmentOf = Keep(amendmentOfState),
                        collectionId = Change(
                            collectionIdState,
                            value.sepaCollectionId
                        ) {
                            collectionIdState = value.sepaCollectionId
                        }
                    )) { sepaMandate ->
                    setSepaMandate(sepaMandate)
                }
            }
        }
    }
    // Date Signed
    Field(fieldDesktopStyle) {
        Label(
            text = (inputs * dateSigned * label * title).emit(),
            id = "date-signed",
            labelStyle = formLabelDesktopStyle,
            isRequired = true
        )
        key(signedAtState, isReadOnly) {
            Input(InputType.Date) {
                id("signed-at")
                if (isReadOnly) disabled()
                value((signedAtState?:now()).date.format(Locale.Iso))
                style { dateInputDesktopStyle() }
                onInput {
                    if (isReadOnly) return@onInput
                    update(SepaMandateChange(
                        sepaMandateId = sepaMandate?.sepaMandateId,
                        debtorBankAccountId = Keep(bankAccountId),
                        debtorName = Keep(debtorNameState),
                        mandateReference = Keep(mandateReferenceState),
                        status = Keep(statusState),
                        signedAt = Change(
                            oldValue = signedAtState,
                            newValue = it.value.parse(Locale.Iso).toDateTime()
                        ) {
                            signedAtState = it.value.parse(Locale.Iso).toDateTime()
                        },
                        validFrom = Keep(validFromState),
                        validUntil = Keep(validUntilState),
                        lastUsedAt = Keep(lastUsedAtState),
                        isActive = Keep(isActiveState),
                        amendmentOf = Keep(amendmentOfState),
                        collectionId = Keep(collectionIdState)
                    )) {
                            sepaMandate -> setSepaMandate(sepaMandate)
                    }
                }
            }
        }
    }
    // Status
    Field(fieldDesktopStyle) {
        Label(
            text = (inputs * status * label * title).emit(),
            id = "status",
            labelStyle = formLabelDesktopStyle,
            isRequired = true
        )
        val statusOptions: Map<String, MandateStatus> = mapOf(
            MandateStatus.ACTIVE.name to MandateStatus.ACTIVE,
            MandateStatus.REVOKED.name to MandateStatus.REVOKED,
            MandateStatus.SUSPENDED.name to MandateStatus.SUSPENDED,
            MandateStatus.EXPIRED.name to MandateStatus.EXPIRED
        )
        Dropdown(
            options = statusOptions,
            selected = statusState.name,
            isDisabled = { isReadOnly },
            iconContent = {open -> SimpleUpDown(open) },
            styles = dropdownStyles,
        ) {
                (_, value) ->
            if (isReadOnly) return@Dropdown
            val newState = value
            update(SepaMandateChange(
                sepaMandateId = sepaMandate?.sepaMandateId,
                debtorBankAccountId = Keep(bankAccountId),
                debtorName = Keep(debtorNameState),
                mandateReference = Keep(mandateReferenceState),

                status = Change(
                    oldValue = statusState,
                    newValue = newState
                ) {
                    statusState = newState
                },
                signedAt = Keep(signedAtState),
                validFrom = Keep(validFromState),
                validUntil = Keep(validUntilState),
                lastUsedAt = Keep(lastUsedAtState),
                isActive = Keep(isActiveState),
                amendmentOf = Keep(amendmentOfState),
                collectionId = Keep(collectionIdState)
            )){
                    sepaMandate -> setSepaMandate(sepaMandate)
            }
        }
    }
}
// ... existing code ...
data class SepaMandateChange(
    val sepaMandateId: SepaMandateId? = null,
    val debtorBankAccountId: Change<BankAccountId>,
    val debtorName: Change<String>,
    val mandateReference: Change<MandateReference>,
    val signedAt: Change<LocalDateTime>,
    val validFrom: Change<LocalDateTime>,
    val validUntil: Change<LocalDateTime>,
    val lastUsedAt: Change<LocalDateTime>,
    val status: Change<MandateStatus>,
    val isActive: Change<Boolean>,
    val amendmentOf: Change<SepaMandateId>,
    val collectionId: Change<SepaCollectionId>
)

fun update(sepaMandateChange: SepaMandateChange, onChange: (SepaMandate) -> Unit) = try{
    val sepaMandate = SepaMandate(
        sepaMandateId = sepaMandateChange.sepaMandateId?: SepaMandateId(NIL_UUID),
        debtorBankAccountId = requireNotNull(sepaMandateChange.debtorBankAccountId.new) {
            "debtor bank account id is null"
        },
        debtorName = requireNotNull(sepaMandateChange.debtorName.new) {
            "debtor name is null"
        },
        mandateReference = sepaMandateChange.mandateReference.new?: MandateReference("NOT_SET"),
        signedAt = requireNotNull(sepaMandateChange.signedAt.new) {
            "signed at is null"
        },
        status = requireNotNull(sepaMandateChange.status.new) {
            "status is null"
        },
        validFrom = requireNotNull(sepaMandateChange.validFrom.new?: sepaMandateChange.signedAt.new!!) {
            "valid from is null"
        },
        validUntil = sepaMandateChange.validUntil.new,
        lastUsedAt = sepaMandateChange.lastUsedAt.new,
        isActive = requireNotNull(sepaMandateChange.isActive.new) {
            "is active is null"
        },
        amendmentOf = sepaMandateChange.amendmentOf.new,
        collectionId = sepaMandateChange.collectionId.new
    )
    onChange(sepaMandate)
} catch (e: Exception) {
    println(e)
} finally {
    with(sepaMandateChange) {
        debtorBankAccountId.onChange()
        debtorName.onChange()
        mandateReference.onChange()
        signedAt.onChange()
        status.onChange()
        validFrom.onChange()
        validUntil.onChange()
        lastUsedAt.onChange()
        isActive.onChange()
        amendmentOf.onChange()
        collectionId.onChange()
    }
}
val updateSepaMandateFormTexts = Source {
    "inputs" texts {
        "debtorName" block {
            "label" block {
                "title" colon "DebtorName"
            }
        }
        "mandateReference" block {
            "label" block {
                "title" colon "MandateReference"
            }
        }
        "collection" block {
            "label" block {
                "title" colon "Collection Key / Reference Prefix"
            }
        }
        "dateSigned" block {
            "label" block {
                "title" colon "Date Signed"
            }
        }
        "status" block {
            "label" block {
                "title" colon "Status"
            }
        }
    }
}
