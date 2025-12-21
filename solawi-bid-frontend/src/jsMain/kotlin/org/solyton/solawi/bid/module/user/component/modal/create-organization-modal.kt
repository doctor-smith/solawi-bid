package org.solyton.solawi.bid.module.user.component.modal

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
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
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.module.style.form.fieldDesktopStyle
import org.solyton.solawi.bid.module.style.form.formDesktopStyle
import org.solyton.solawi.bid.module.style.form.formLabelDesktopStyle
import org.solyton.solawi.bid.module.style.form.textInputDesktopStyle
import org.solyton.solawi.bid.module.user.data.reader.inputs
import org.solyton.solawi.bid.module.user.data.reader.name
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun CreateOrganizationModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
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
    var organizationName by remember{ mutableStateOf("") }

    val inputs = texts * inputs

    Vertical {
        Div(attrs = { style { formDesktopStyle() } }) {
            Div(attrs = { style { fieldDesktopStyle() } }) {
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
    }
}

@Markup
fun Storage<Modals<Int>>.showCreateOrganizationModal(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    setOrganizationData: (organizationName: String /* ,...*/) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        CreateOrganizationModal(
            this,
            texts,
            this@showCreateOrganizationModal,
            device,
            styles,
            setOrganizationData,
            cancel,
            update,
        )
    ) )
}
