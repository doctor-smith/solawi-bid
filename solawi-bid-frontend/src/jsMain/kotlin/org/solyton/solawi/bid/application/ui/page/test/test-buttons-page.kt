package org.solyton.solawi.bid.application.ui.page.test

import androidx.compose.runtime.Composable
import org.evoleq.compose.Markup
import org.evoleq.compose.style.data.device.DeviceType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.control.button.DetailsButton
import org.solyton.solawi.bid.module.control.button.EditButton
import org.solyton.solawi.bid.module.control.button.TrashCanButton

@Markup
@Composable
@Suppress("FunctionName")
fun TestButtonsPage() {
    H1{Text("Buttons")}

    EditButton(Color.seagreen, Color.transparent, {"Edit Button"}, {DeviceType.Mobile}, false) {
        console.log("Edit me, please fucking edit me!")
    }
    DetailsButton(Color.seagreen, Color.transparent, {"Details Button"}, {DeviceType.Mobile}, false) {
        console.log("Edit me, please fucking edit me!")
    }

    TrashCanButton(Color.seagreen, Color.transparent, {"delete resource"}, {DeviceType.Mobile}, false) {
        console.log("Edit me, please fucking edit me!")
    }
}
