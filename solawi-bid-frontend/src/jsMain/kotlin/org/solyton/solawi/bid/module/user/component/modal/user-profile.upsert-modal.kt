package org.solyton.solawi.bid.module.user.component.modal

import org.solyton.solawi.bid.module.user.component.form.UserProfileForm
import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.*
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.subComp
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.dom.ElementScope
import org.solyton.solawi.bid.module.user.data.profile.UserProfile
import org.w3c.dom.HTMLElement


@Markup
@Suppress("FunctionName")
fun UpsertUserProfileModal(
    id: Int,
    texts: Source<Lang.Block>,
    countryTexts: Source<Lang.Block>,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    userProfile: UserProfile?,
    setUserProfile: (userProfile: UserProfile) -> Unit,
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
    val inputs = texts * subComp("inputs")
    UserProfileForm(
        device,
        inputs,
        countryTexts,
        userProfile,
        setUserProfile
    )
}

@Markup
fun Storage<Modals<Int>>.showUpsertUserProfileModal(
    texts: Source<Lang.Block>,
    countryTexts: Source<Lang.Block>,
    device: Source<DeviceType>,
    styles: (Source<DeviceType>)-> ModalStyles,
    userProfile: UserProfile?,
    setUserProfile: (userProfile: UserProfile) -> Unit,
    cancel: ()->Unit,
    update: ()->Unit,
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpsertUserProfileModal(
            this,
            texts,
            countryTexts,
            this@showUpsertUserProfileModal,
            device,
            styles,
            userProfile,
            setUserProfile,
            cancel,
            update,
        )
    )
    )
}
