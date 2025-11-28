package org.solyton.solawi.bid.module.application.component.modal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.label.Label
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
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.ElementScope
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
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun ConnectApplicationToOrganizationModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    application: Application,
    organizations: List<Organization>,
    setOrganizationId: (organizationId: String /*, ... */) -> Unit,
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
    var organizationId by remember{ mutableStateOf("") }

    val inputs = texts * inputs

    Vertical {
        Div(attrs = { style { formDesktopStyle() } }) {
            Div(attrs = { style { fieldDesktopStyle() } }) {
                Text (application.name )
                Label("organization-id"/*(inputs * name * title).emit()*/, id = "organization-id", labelStyle = formLabelDesktopStyle)

                Ul { organizations.forEach {
                    organization ->
                    Li { Text("${organization.name}: ${organization.organizationId}") }
                } }

                // Dropdown
                TextInput(organizationId) {
                    id("organization-id")
                    style { textInputDesktopStyle() }
                    onInput {
                        organizationId = it.value
                        setOrganizationId(it.value)
                    }
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showConnectApplicationToOrganizationModule(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    application: Application,
    organizations: List<Organization>,
    setOrganizationId: (organizationId: String /* ,...*/) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        ConnectApplicationToOrganizationModal(
            this,
            texts,
            this@showConnectApplicationToOrganizationModule,
            device,
            styles,
            application,
            organizations,
            setOrganizationId,
            cancel,
            update,
        )
    ) )
}
