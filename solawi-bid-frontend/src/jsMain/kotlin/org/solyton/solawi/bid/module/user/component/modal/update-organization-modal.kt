package org.solyton.solawi.bid.module.user.component.modal

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.form.Form
import org.evoleq.compose.form.field.Field
import org.evoleq.compose.form.label.Label
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.modal.*
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.title
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.width
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.module.control.button.PlusButton
import org.solyton.solawi.bid.module.list.component.ActionCell
import org.solyton.solawi.bid.module.list.component.ActionCellItem
import org.solyton.solawi.bid.module.list.component.DataWrapper
import org.solyton.solawi.bid.module.list.component.Header
import org.solyton.solawi.bid.module.list.component.HeaderCell
import org.solyton.solawi.bid.module.list.component.HeaderWrapper
import org.solyton.solawi.bid.module.list.component.ListItemWrapper
import org.solyton.solawi.bid.module.list.component.ListWrapper
import org.solyton.solawi.bid.module.list.component.TextCell
import org.solyton.solawi.bid.module.list.component.Title
import org.solyton.solawi.bid.module.list.component.TitleWrapper
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.action.organization.addMember
import org.solyton.solawi.bid.module.user.data.organization.Organization
import org.solyton.solawi.bid.module.user.data.reader.inputs
import org.solyton.solawi.bid.module.user.data.reader.name
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpdateOrganizationModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    organization: Organization,
    setOrganizationData: (organizationName: String /*, ... */) -> Unit,
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
    var organizationName by remember{ mutableStateOf(organization.name) }

    val inputs = texts * inputs

    Vertical {
        Form(formDesktopStyle) {
            Field(fieldDesktopStyle) {
                Label((inputs * name * title).emit(), id = "organization-name", labelStyle = formLabelDesktopStyle)
                TextInput(organizationName) {
                    id("organization-name")
                    style { textInputDesktopStyle() }
                    onInput {
                        organizationName = it.value
                        setOrganizationData(it.value)
                    }
                }
            }



        }
        // Members
        // list of members
        // username | roles (+.-)| +
        ListWrapper {
            TitleWrapper {
                Title{
                    Text("Members")
                }
                PlusButton(
                    color = Color.black,
                    bgColor = Color.white,
                    texts= {"Add Member"},
                    deviceType = device,
                    onClick = {
                        console.log("add member to organization ${organization.name}")
                    }
                )
            }
            HeaderWrapper { Header{
                HeaderCell( "Username" ){ width(20.percent) }
                HeaderCell( "Roles" ){ width(80.percent) }
            } }
            ListItemWrapper {
                DataWrapper { organization.members.map { member ->
                    TextCell(member.username){ width(20.percent) }
                    ActionCell(style = {width(100.percent)}) {
                        member.roles.forEach { role ->
                            ActionCellItem(
                                role.roleName,
                                onClick = {
                                    console.log("remove role $role from member ${member.username}")
                                }
                            ) {
                                Text("-")
                            }
                        }
                        ActionCell() { Text ("+") }
                    }
                } }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showUpdateOrganizationModal(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    organization: Organization,
    setOrganizationData: (organizationName: String /* ,...*/) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpdateOrganizationModal(
            this,
            texts,
            this@showUpdateOrganizationModal,
            device,
            styles,
            organization,
            setOrganizationData,
            cancel,
            update,
        )
    ) )
}
