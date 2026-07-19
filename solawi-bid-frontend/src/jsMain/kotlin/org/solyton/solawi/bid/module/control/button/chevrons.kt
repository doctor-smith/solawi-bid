package org.solyton.solawi.bid.module.control.button

import androidx.compose.runtime.Composable
import org.evoleq.compose.conditional.When
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.language.Lang
import org.evoleq.language.get
import org.evoleq.language.texts
import org.evoleq.math.Reader
import org.evoleq.math.Source
import org.evoleq.math.times
import org.evoleq.optics.storage.Storage
import org.jetbrains.compose.web.css.AlignSelf
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.alignSelf
import org.jetbrains.compose.web.dom.Div


fun cardChevronTexts(): Lang.Block = "cardChevron" texts {
    "open" colon "Open"
    "close" colon "Close"
}

val open: Reader<Lang.Block, String> = Reader {block -> block["open"]}
val close: Reader<Lang.Block, String> = Reader {block -> block["open"]}

@Composable
fun CardChevrons(
    texts: Source<Lang.Block> = Source{cardChevronTexts()},
    deviceType: Source<DeviceType>,
    opened: Storage<Boolean>
) = CardChevrons(texts, deviceType, opened.read()) {
    opened.write(it)
}

@Composable
fun CardChevrons(
    texts: Source<Lang.Block> = Source{cardChevronTexts()},
    deviceType: Source<DeviceType>,
    opened: Boolean,
    setOpened: (Boolean) -> Unit
) = Div({
    style {
        alignSelf(AlignSelf.FlexEnd)
    }
}) {
    When(!opened) {
        ChevronDownButton(
            Color.black,
            Color.white,
            texts * open,
            deviceType,
        ) {
            setOpened(true)
        }

    }
    When(opened) {
        ChevronLeftButton(
            Color.black,
            Color.white,
            texts * close,
            deviceType,
        ) {
            setOpened(false)
        }
    }
}
