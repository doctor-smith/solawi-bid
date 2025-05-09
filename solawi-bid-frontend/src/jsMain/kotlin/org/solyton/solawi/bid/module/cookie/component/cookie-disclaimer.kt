package org.solyton.solawi.bid.module.cookie.component

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.language.Lang
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.math.x
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.module.cookie.data.CookieDisclaimer
import org.solyton.solawi.bid.module.cookie.data.isConfirmed
import org.solyton.solawi.bid.module.cookie.data.isShown

@Markup
@Composable
@Suppress("FunctionName")
fun CookieDisclaimer(
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    cookieDisclaimer: Storage<CookieDisclaimer>,
    excluded: Boolean = false
) = Div {
    if (
        !excluded && !((cookieDisclaimer * isConfirmed).read() || (cookieDisclaimer * isShown).read())
    ) {

        with(modals.nextId()) {
            val id = this
            modals.put(
                id x CookieDisclaimerModal(
                    id,
                    texts,
                    modals,
                    cookieDisclaimer,
                    device,
                )
            )
        }
        (cookieDisclaimer * isShown).write(true)
    }
}

@Markup
@Suppress("FunctionName")
fun CookieDisclaimerModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    cookieDisclaimer: Storage<CookieDisclaimer>,
    device: Source<DeviceType>,
): ModalData/*@Composable ElementScope<HTMLElement>.()->Unit*/
= ModalData(
    ModalType.CookieDisclaimer ,
    Modal(
        id,
        modals,
        device,
        dataId = "cookie-disclaimer",
        onOk = {
            (cookieDisclaimer * isConfirmed).write(true)
            (cookieDisclaimer * isShown).write(false)
        },
        onCancel = null,
        texts = texts
    ) {
        Div {
            with(texts.component("content")) {
                Text(this["hint"])
            }
        }
    })
