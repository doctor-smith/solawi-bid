package org.solyton.solawi.bid.application.ui

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Container
import org.evoleq.compose.modal.ModalLayer
import org.evoleq.language.Block
import org.evoleq.language.component
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.cookieDisclaimer
import org.solyton.solawi.bid.application.data.i18N
import org.solyton.solawi.bid.application.data.modals
import org.solyton.solawi.bid.application.routing.Routing
import org.solyton.solawi.bid.module.cookie.component.CookieDisclaimer
import org.solyton.solawi.bid.module.i18n.data.language

@Markup
@Suppress("FunctionName")
@Composable fun UI(storage: Storage<Application>) {
    val texts = (storage * i18N * language).read() as Block
    // The whole UI needs to be wrapped in a component
    // which is able to handle the interactive control flow of the application,
    // namely: dialogs, cookie-disclaimers errors, etc
    // Note: Routing is done in the main container just below the navigation section
    ModalLayer<Int>(
        1000,
        storage * modals,
    ) {
        // The Cookie disclaimer pops up, whenever as user
        // visits the page for the first time or cleared the cookies
        CookieDisclaimer(
            texts.component("solyton.cookieDisclaimer"),
            storage * modals,
            storage * cookieDisclaimer
        )
        // All pages shall be wrapped in a container
        Container{
            // Routing
            Routing(storage)
        }
    }
}