package org.solyton.solawi.bid.module.application.component.modal

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.attribute.disabled
import org.evoleq.compose.form.Form
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.modal.*
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.display
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.CheckboxInput
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.permissions.data.Role
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
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

    // val inputs = texts * inputs
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
