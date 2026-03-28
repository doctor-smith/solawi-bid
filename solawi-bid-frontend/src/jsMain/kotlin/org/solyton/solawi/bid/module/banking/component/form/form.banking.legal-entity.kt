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
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.invert
import org.evoleq.math.times
import org.evoleq.uuid.NIL_UUID
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntity
import org.solyton.solawi.bid.module.banking.data.legalentity.LegalEntityType
import org.solyton.solawi.bid.module.control.dropdown.Dropdown
import org.solyton.solawi.bid.module.control.dropdown.DropdownStyles
import org.solyton.solawi.bid.module.navbar.component.SimpleUpDown
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.data.address.Address
import org.solyton.solawi.bid.module.values.LegalEntityId

@Markup
@Composable
@Suppress("FunctionName")
fun LegalEntityForm(
    inputs: Source<Lang.Block> = defaultInputs,
    partyId: LegalEntityId,
    legalEntity: LegalEntity?,
    setLegalEntity: (LegalEntity) -> Unit,
) {

    val dropdownStyles = DropdownStyles.modifyContainerStyle { width(100.percent) }

    Form(formDesktopStyle) {
        var nameState by remember { mutableStateOf(legalEntity?.name ?: "") }
        var legalFormState by remember { mutableStateOf(legalEntity?.legalForm ?: "") }
        var legalEntityTypeState by remember { mutableStateOf(legalEntity?.legalEntityType ) }
        var addressState by remember { mutableStateOf(legalEntity?.address ?: Address.default()) }

        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("name") * title).emit(),
                id = "name",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(nameState) {
                id("name")
                style { textInputDesktopStyle() }
                onInput {
                    update(
                        LegalEntityChange(
                            legalEntityId = legalEntity?.legalEntityId ?: LegalEntityId(NIL_UUID),
                            partyId = partyId,
                            name = Change(nameState, it.value) {
                                nameState = it.value
                            },
                            legalForm = Keep(legalFormState),
                            legalEntityType = Keep(legalEntityTypeState),
                            address = Keep(addressState)
                        )
                    ) { legalEntity ->
                        setLegalEntity(legalEntity)
                    }
                }
            }
        }
        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("legalForm") * title).emit(),
                id = "legal-form",
                labelStyle = formLabelDesktopStyle
            )
            TextInput(legalFormState) {
                id("legal-form")
                style { textInputDesktopStyle() }
                onInput {
                    update(LegalEntityChange(
                        legalEntityId = legalEntity?.legalEntityId ?: LegalEntityId(NIL_UUID),
                        partyId = partyId,
                        name = Keep(nameState),
                        legalForm = Change(legalFormState, it.value){
                            legalFormState = it.value
                        },
                        legalEntityType = Keep(legalEntityTypeState),
                        address = Keep(addressState)
                    )){
                            legalEntity -> setLegalEntity(legalEntity)
                    }
                }
            }
        }
        Field(fieldDesktopStyle) {
            Label(
                (inputs * subComp("legalEntityType") * title).emit(),
                id = "legal-entity-type",
                labelStyle = formLabelDesktopStyle
            )
            val legalEntityTypeOptions: Map<String, LegalEntityType> = mapOf(
                "Natürliche Person" to LegalEntityType.HUMAN,
                "Organisation" to LegalEntityType.ORGANIZATION
            )
            val inverted = legalEntityTypeOptions.invert()
            Dropdown(
                options = legalEntityTypeOptions,
                selected = inverted[legalEntityTypeState?: LegalEntityType.HUMAN],
                iconContent = {open -> SimpleUpDown(open) },
                styles = dropdownStyles
            ) {
                (_, value) -> update(LegalEntityChange(
                    legalEntityId = legalEntity?.legalEntityId ?: LegalEntityId(NIL_UUID),
                    partyId = partyId,
                    name = Keep(nameState),
                    legalForm = Keep(legalFormState),
                    legalEntityType = Change(legalEntityTypeState, value) {
                        legalEntityTypeState = value
                    },
                    address = Keep(addressState)
                )) { legalEntity -> setLegalEntity(legalEntity) }
            }
        }
    }
}

fun update(change: LegalEntityChange, onChange: (LegalEntity) -> Unit) {
    try{
        val newLegalEntity = LegalEntity(
            legalEntityId = change.legalEntityId,
            partyId = change.partyId,
            name = change.name.new!!,
            legalForm =change.legalForm.new!!,
            legalEntityType = change.legalEntityType.new!!,
            address = change.address.new!!
        )
        onChange(newLegalEntity)

    } catch (exception: Exception) {
        println(exception)
    } finally {
        change.name.onChange()
        change.legalForm.onChange()
        change.legalEntityType.onChange()
        change.address.onChange()
    }
}

data class LegalEntityChange(
    val legalEntityId: LegalEntityId,
    val partyId: LegalEntityId,
    val name: Change<String>,
    val legalForm: Change<String>,
    val legalEntityType: Change<LegalEntityType>,
    val address: Change<Address>
)
