package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.label.Label
import org.evoleq.compose.layout.*
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H4
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.dom.TextInput
import org.solyton.solawi.bid.application.data.Application
import org.evoleq.compose.style.data.device.DeviceType
import org.solyton.solawi.bid.application.ui.style.form.formLabelStyle
import org.solyton.solawi.bid.application.ui.style.form.textInputStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.bid.data.Round
import org.solyton.solawi.bid.module.bid.data.bidRoundEvaluation
import org.solyton.solawi.bid.module.separator.LineSeparator
import org.solyton.solawi.bid.module.statistics.component.Distribution
import org.solyton.solawi.bid.module.statistics.data.DistributionConfiguration

@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundEvaluation(storage: Storage<Application>, round: Lens<Application, Round>) {

    val evaluation = (storage * round * bidRoundEvaluation).read()
    // todo:i18n

    Vertical(scrollableStyle){
        Wrap {
            H4{Text("Ziel erreicht")}
            ReadOnlyProperty(Property("Zielsumme", evaluation.auctionDetails.targetAmount))
            ReadOnlyProperty(Property("Erreichte Summe", evaluation.totalSumOfWeightedBids),)
            LineSeparator()
            val difference = evaluation.totalSumOfWeightedBids - evaluation.auctionDetails.targetAmount!!
            ReadOnlyProperty(
                Property(
                    "Differenz",
                    difference
                ),
                PropertyStyles().copy (
                    valueStyle = {
                        when {
                            difference >= 0 -> color(Color.seagreen)
                            else -> color(Color.crimson)
                        }
                    }
                )
            )
        }
        Wrap {
            H4{Text("Verhältnis max / min")}

            val minimalBid = evaluation.weightedBids.minBy { bid -> bid.bid }
            val maximalBid = evaluation.weightedBids.maxBy { bid -> bid.bid }

            ReadOnlyProperty(Property("Minimal Gebot (Gebot / Anteile)", "${minimalBid.bid} / ${minimalBid.weight}"))
            ReadOnlyProperty(Property("Maximal Gebot (Gebot / Anteile)", "${maximalBid.bid} / ${maximalBid.weight}"))
            ReadOnlyProperty(Property("Verhältnis max / min", maximalBid.bid / minimalBid.bid))
        }

        Wrap {
            H4{Text("Verteilung")}
            var max by remember { mutableStateOf(310.0) }
            var min by remember { mutableStateOf(0.0) }
            var resolution by remember{ mutableStateOf(30) }
            Horizontal({marginTop(10.px)}) {
                Vertical {
                    Vertical {
                        Label("Minimum", id = "min", labelStyle = formLabelStyle(DeviceType.Desktop))
                        TextInput(min.toString()) {
                            id("min")
                            style { textInputStyle(DeviceType.Desktop)() }
                            onInput {
                                try{min = it.value.toDouble()} catch(_: Exception){if(it.value.isBlank()) {min = 0.0}}
                                //emailValid = it.value.isEmail()
                            }
                        }
                    }
                    Vertical {
                        Label("Maximum", id = "max", labelStyle = formLabelStyle(DeviceType.Desktop))
                        TextInput(max.toString()) {
                            id("max")
                            style { textInputStyle(DeviceType.Desktop)() }
                            onInput {
                                try{max = it.value.toDouble()} catch(_: Exception){if(it.value.isBlank()){ max = 0.0}}
                                //emailValid = it.value.isEmail()
                            }
                        }
                    }
                    Vertical {
                        Label("Auflösung", id = "resolution", labelStyle = formLabelStyle(DeviceType.Desktop))
                        TextInput(resolution.toString()) {
                            id("resolution")
                            style { textInputStyle(DeviceType.Desktop)() }
                            onInput {
                                try{resolution = it.value.toInt()} catch(_: Exception){if(it.value.isBlank()) resolution = 1}
                                //emailValid = it.value.isEmail()
                            }
                        }
                    }
                }


                Div({
                    style {
                        marginTop(10.px)
                        height(200.px)
                        width(50.percent)
                    }
                }) {
                    Distribution(
                        evaluation.weightedBids.map { it.bid },
                        DistributionConfiguration(
                            min, max, resolution
                        )
                    )
                }
            }
        }
    }
}
