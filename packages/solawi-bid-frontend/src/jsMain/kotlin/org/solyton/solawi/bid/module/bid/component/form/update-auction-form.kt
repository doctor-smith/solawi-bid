package org.solyton.solawi.bid.module.bid.component.form

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.date.format
import org.evoleq.compose.date.parse
import org.evoleq.compose.label.Label
import org.evoleq.compose.modal.*
import org.evoleq.language.Lang
import org.evoleq.language.Locale
import org.evoleq.language.component
import org.evoleq.language.get
import org.evoleq.math.Source
import org.evoleq.math.emit
import org.evoleq.math.onIsDouble
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.height
import org.jetbrains.compose.web.css.vh
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.form.*
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalContainerStyle
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.*
import org.solyton.solawi.bid.module.bid.service.onNullEmpty
import org.w3c.dom.HTMLElement

@Markup
@Suppress("FunctionName")
fun UpdateAuctionModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    auction: Storage<Auction>,
    device: Source<DeviceType>,
    cancel: ()->Unit,
    update: ()->Unit
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
    texts = texts,
    styles = auctionModalStyles(device),
) {
    // input texts
    val inputs: Lang.Block = texts.component("inputs")

    Div(attrs = {style { formDesktopStyle() }}) {

        Div(attrs = {style { fieldDesktopStyle() }}) {
            Label(inputs["title"], id = "name" , labelStyle = formLabelDesktopStyle)
            TextInput((auction * name).read()) {
                id("name")
                style { textInputDesktopStyle() }
                onInput { (auction * name).write(it.value) }
            }
        }
        Div(attrs = {style { fieldDesktopStyle() }}) {
            // State
            val initDate = (auction * date).read().format(Locale.Iso)
            var dateString by remember{ mutableStateOf( initDate ) }

            Label(inputs["date"], id = "date" , labelStyle = formLabelDesktopStyle)
            Input(InputType.Date) {
                id("date")
                value(dateString)
                style { dateInputDesktopStyle() }
                onInput {
                    dateString = it.value
                    (auction * date).write(it.value.parse(Locale.Iso))
                }
            }
        }

        Div(attrs = {style { fieldDesktopStyle() }}) {
            Label(inputs["benchmark"], id = "benchmark" , labelStyle = formLabelDesktopStyle)
            TextInput (onNullEmpty((auction * auctionDetails * benchmark).read()){it}) {
                id("benchmark")
                style { numberInputDesktopStyle() }
                onInput {
                    onIsDouble(it.value) {
                        (auction * auctionDetails * benchmark).write(toDouble())
                    }
                }
            }
        }

        Div(attrs = {style { fieldDesktopStyle() }}) {
            Label(inputs["targetAmount"], id = "targetAmount" , labelStyle = formLabelDesktopStyle)
            TextInput(onNullEmpty((auction * auctionDetails * targetAmount).read()){it}) {
                id("targetAmount")
                style { numberInputDesktopStyle() }
                onInput {
                    onIsDouble(it.value) {
                        (auction * auctionDetails * targetAmount).write(toDouble())
                    }
                }
            }
        }
        Div(attrs = {style { fieldDesktopStyle() }}) {
            Label(inputs["solidarityContribution"], id = "solidarityContribution" , labelStyle = formLabelDesktopStyle)
            TextInput(onNullEmpty((auction * auctionDetails * solidarityContribution).read()){it}) {
                id("solidarityContribution")
                style { numberInputDesktopStyle() }
                onInput {
                    onIsDouble(it.value) {
                        (auction * auctionDetails * solidarityContribution).write(toDouble())
                    }
                }
            }
        }
        /*
        Div(attrs = {style { fieldDesktopStyle() }}) {
            Label("Minimal Bid", id = "minimalBid" , labelStyle = formLabelDesktopStyle)
            TextInput(onNullEmpty((auction * auctionDetails * minimalBid).read()){it}) {
                id("minimalBid")
                style { numberInputDesktopStyle() }
                onInput {
                    onIsDouble(it.value) {
                        (auction * auctionDetails * minimalBid ).write(toDouble())
                    }
                }
            }
        }

         */
    }
}

@Markup
fun Storage<Modals<Int>>.showUpdateAuctionModal(
    auction: Storage<Auction>,
    texts: Lang.Block,
    device: Source<DeviceType>,
    cancel: ()->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        UpdateAuctionModal(
            this,
            texts,
            this@showUpdateAuctionModal,
            auction,
            device,
            cancel,
            update
        )
    )
    )
}
