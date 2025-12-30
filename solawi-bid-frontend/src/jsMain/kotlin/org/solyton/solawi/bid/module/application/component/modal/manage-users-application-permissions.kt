package org.solyton.solawi.bid.module.application.component.modal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalStyles
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.CheckboxInput
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Li
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.jetbrains.compose.web.dom.Ul
import org.solyton.solawi.bid.module.application.data.application.Application
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.application.data.organization.Organization
import org.solyton.solawi.bid.module.application.i18n.inputs
import org.solyton.solawi.bid.module.permissions.data.Role
import org.w3c.dom.HTMLElement

data class CheckedUserRole(val role: Role, val checked: Boolean)

@Markup
@Suppress("FunctionName")
fun ManageUserPermissionsModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    checkedRoles: List<CheckedUserRole>,
    setCheckedRoles: (List<CheckedUserRole>) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id = id,
    modals = modals,
    device = device,

    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    texts = texts.emit(),
    styles = styles(device),
) {

    val inputs = texts * inputs
    val userRoles = checkedRoles.associateBy { checkedUserRole -> checkedUserRole.role.roleId }.toMutableMap()

    Vertical {
        Form(formDesktopStyle){
            userRoles.forEach { (id, checkedRole) ->
                Div(attrs = { style {
                    display(DisplayStyle.Flex)
                    gap(2.px)
                } }) {
                    var checked by remember{ mutableStateOf(checkedRole.checked) }
                    CheckboxInput(checked = checked) {
                        if(checkedRole.role.roleName == "OWNER") disabled()
                        onInput { event ->
                            println("checked: ${event.value}")
                            userRoles[id] = checkedRole.copy(checked = event.value)
                            setCheckedRoles(userRoles.values.toList())
                            checked = event.value
                    }}
                    Text(checkedRole.role.roleName)
                    // Text(checkedRole.role.rights.joinToString(", ") { it.rightName })
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showManageUserPermissionsModule(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    checkedRoles: List<CheckedUserRole>,
    setCheckedRoles: (List<CheckedUserRole>) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        ManageUserPermissionsModal(
            this,
            texts,
            this@showManageUserPermissionsModule,
            device,
            styles,
            checkedRoles,
            setCheckedRoles,
            cancel,
            update,
        )
    ) )
}
