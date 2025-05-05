package org.solyton.solawi.bid.application.ui.page.test

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.ui.effect.LaunchSetDeviceData
import org.solyton.solawi.bid.module.bid.component.form.SendBidForm

@Markup
@Composable
@Suppress("FunctionName")
fun MobileTestPage(storage: Storage<Application>) {
    val device = (storage * deviceData).read()

    H1 { Text("Mobile Test Page") }
    LaunchSetDeviceData(storage * deviceData)
    SendBidForm(device.mediaType) {

    }
}