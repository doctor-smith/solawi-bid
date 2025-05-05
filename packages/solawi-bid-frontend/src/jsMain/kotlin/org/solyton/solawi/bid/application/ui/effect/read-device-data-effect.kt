package org.solyton.solawi.bid.application.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.solyton.solawi.bid.application.data.device.Device
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.device.screenWidth

@Markup
@Composable
@Suppress("FunctionName")
fun LaunchSetDeviceData(deviceData: Storage<Device>) {
    LaunchedEffect(Unit) {
        launch {
            if((deviceData * mediaType).read() is DeviceType.Empty) {
                (deviceData * screenWidth).write(window.innerWidth.toDouble())
                (deviceData * mediaType).write(
                    DeviceType.from(
                    window.innerWidth.toDouble(),
                    window.devicePixelRatio,
                    js("('ontouchstart' in window)") as Boolean,
                    window.navigator.userAgent.lowercase() )
                )
            }
            window.addEventListener("resize", {
                (deviceData * screenWidth).write(window.innerWidth.toDouble())
                (deviceData * mediaType).write(
                    DeviceType.from(
                    window.innerWidth.toDouble(),
                    window.devicePixelRatio,
                    js("('ontouchstart' in window)") as Boolean,
                    window.navigator.userAgent.lowercase()
                ))
            })
        }
    }
}