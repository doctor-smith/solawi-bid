package org.solyton.solawi.bid.module.user.component.modal

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.*
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.user.component.form.UpdateUserForm
import org.solyton.solawi.bid.module.user.data.api.UpdateUser
import org.solyton.solawi.bid.module.user.data.user.User
import org.w3c.dom.HTMLElement


@Markup
fun Storage<Modals<Int>>.showUpdateUserModal(
    texts: Source<Lang.Block>,
    device: Source<DeviceType>,
    isOkButtonDisabled: ()->Boolean = {false},
    user: User,
    setUser: (user: UpdateUser) -> Unit,
    cancel: ()->Unit = {},
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        this,
        ModalType.Dialog,
        UpdateUserModal(
            this,
            texts,
            this@showUpdateUserModal,
            device,
            {commonModalStyles(it)},
            isOkButtonDisabled,
            user,
            setUser,
            cancel,
            update
        )
    ) )
}


@Markup
@Suppress("FunctionName")
fun UpdateUserModal(
    id: Int,
    texts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    isOkButtonDisabled: ()->Boolean,
    user: User,
    setUser: (user: UpdateUser) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
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
    isOkButtonDisabled = isOkButtonDisabled,
) {
    UpdateUserForm(
        texts,
        user
    ) {
        setUser(it)
    }
}
