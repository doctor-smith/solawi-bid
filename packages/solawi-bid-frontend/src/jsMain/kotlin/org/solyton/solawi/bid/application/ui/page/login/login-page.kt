package org.solyton.solawi.bid.application.ui.page.login

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.Div
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.actions
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.ui.page.login.action.loginAction
import org.solyton.solawi.bid.application.ui.style.form.formPageStyle
import org.solyton.solawi.bid.module.authentication.component.LoginForm
import org.solyton.solawi.bid.application.data.authentication.LoginForm as LoginFormData

@Markup
@Composable
@Suppress("FunctionName")
fun LoginPage(storage: Storage<Application>) {
    val device = (storage * deviceData * mediaType).read()
    Div(
        attrs = { style { formPageStyle(device)() } }
    ) {
        LoginForm(storage * LoginFormData) {
            CoroutineScope(Job()).launch {
                val actions = (storage * actions).read()
                actions.emit(loginAction)
            }
        }
    }
}

