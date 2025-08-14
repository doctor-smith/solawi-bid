package org.solyton.solawi.bid.module.bid.component

import androidx.compose.runtime.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Property
import org.evoleq.compose.layout.PropertyStyles
import org.evoleq.compose.layout.ReadOnlyProperty
import org.evoleq.optics.lens.Lens
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.frontend.JsFrontendUtil
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.intern.Plot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDiscrete
import org.jetbrains.letsPlot.scale.scaleYContinuous
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.bidround.Round
import org.solyton.solawi.bid.module.bid.data.bidround.bidRoundEvaluation
import org.solyton.solawi.bid.module.bid.service.roundTo
import org.solyton.solawi.bid.module.separator.LineSeparator
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import kotlin.math.roundToInt

fun createHistogram(
    bidData: List<Double>,
    standardBid: Double,
    showStandardBids: Boolean = true,
    xLabel: String = ""): Plot? {

    val filteredBids = if (showStandardBids) bidData else bidData.filter { it != standardBid }
    if (filteredBids.isEmpty()) {
        return null
    }
    val bids = filteredBids.groupingBy { it }.eachCount().toList().sortedBy { it.first }
    val bidValues = bids.map { it.first }
    val bidCounts = bids.map { it.second }
    val plotData = mapOf(
        "bidValue" to bidValues,
        "count" to bidCounts
    )
    val barWidth = .1
    return letsPlot(plotData) + geomBar(
        stat = Stat.identity,
        width = barWidth,
        fill = "lightblue",
        color = "black",
        alpha = 0.7,
    ) {
        x = "bidValue"
        y = "count"
    } + labs(
        x = xLabel,
        y = "Anzahl der Gebote"
    ) + scaleXDiscrete() + scaleYContinuous(trans = "sqrt")
}


@Markup
@Composable
@Suppress("FunctionName")
fun BidRoundEvaluation(
    storage: Storage<BidApplication>,
    round: Lens<BidApplication, Round>
) {
    // Checkbox Variables
    var showStandardBidsTotal by remember { mutableStateOf(true) }
    var showStandardBidsPerShare by remember { mutableStateOf(true) }

    val evaluation = (storage * round * bidRoundEvaluation).read()
    val standardBid = (evaluation.auctionDetails.benchmark ?: 0.0) +
            (evaluation.auctionDetails.solidarityContribution ?: 0.0)
    val avgSign = "\u2205"
    val headingText = if (evaluation.totalSumOfWeightedBids >= evaluation.auctionDetails.targetAmount!!) {
        "Ziel erreicht!"
    } else {
        "Ziel nicht erreicht"
    }
    val bidAmounts = evaluation.weightedBids.map { it.weight * it.bid }
    val totalAmount = bidAmounts.sum()
    val bidsCount = evaluation.weightedBids.size
    val totalShares = evaluation.weightedBids.sumOf { it.weight }
    val avgBidPerShare = (totalAmount / totalShares).asDynamic().toFixed(2) as String
    val noneStandardBidsAmount = bidAmounts.filter { it != standardBid }.size
    val pctMadeBid = (noneStandardBidsAmount.toDouble() / evaluation.weightedBids.size.toDouble()) * 100
    val pctMadeBidDisplay = pctMadeBid.asDynamic().toFixed(2) as String
    val avgSharesPerBid = (evaluation.weightedBids.sumOf { it.weight }.toDouble() / evaluation.weightedBids.size.toDouble())
    val avgSharesPerBidDisplay = avgSharesPerBid.asDynamic().toFixed(3) as String
    val lowBidsAmount = evaluation.weightedBids.map { it.bid }.filter { it < standardBid }.size
    val lowBidsAmountPct = ((lowBidsAmount.toDouble() / bidsCount.toDouble()) * 100).asDynamic().toFixed(2) as String

    fun updateHistogram(data: List<Double>, showStandardData: Boolean, label: String, objectId: String) {
        val histogram: Plot? = createHistogram(
            data,
            standardBid,
            showStandardData,
            label
        )
        val containerDiv = document.getElementById("dialogContainer") as? HTMLElement
        val prevScrollTop = containerDiv?.scrollTop ?: 0.0
        val contentDiv = document.getElementById(objectId)
        contentDiv?.innerHTML = ""
        val content = if (histogram != null) {
            JsFrontendUtil.createPlotDiv(histogram)
        } else {
            val textContent = document.createElement("p") as HTMLParagraphElement
            textContent.textContent = "Leerer Graph. Keine Daten mit dieser Auswahl verfügbar."
            textContent.style.color = "red"
            textContent.style.fontSize = "16px"
            textContent.style.marginBottom = "15px"
            textContent // returns the value
        }
        contentDiv?.appendChild(content)
        // Prevent Scroll-Position from changing, when toggling checkbox
        window.requestAnimationFrame {
            containerDiv?.scrollTop = prevScrollTop
        }
    }
    LaunchedEffect(evaluation, showStandardBidsTotal) {
        val totalBids = evaluation.weightedBids.map { it.weight * it.bid }
        updateHistogram(totalBids, showStandardBidsTotal,"Höhe des Gebots (gesamt) (in €)", "bidHistogramContainer")
    }
    LaunchedEffect(evaluation, showStandardBidsPerShare) {
        val totalBidsPerShare = evaluation.weightedBids.map { it.bid }
        updateHistogram(totalBidsPerShare, showStandardBidsPerShare,"Höhe des Gebots pro Anteil (in €)", "bidPerShareHistogramContainer")
    }
    LaunchedEffect(evaluation) {
        val totalSharesAmount = evaluation.weightedBids.map { it.weight.toDouble() }
        updateHistogram(totalSharesAmount, true,"Anzahl Anteile pro Gebot", "sharesHistogramContainer")
    }

    val pStyle: AttrBuilderContext<HTMLParagraphElement> = {
        style {
            property("font-size", "16px")
            property("color", "black")
            property("margin", "4px")
        }
    }

    Div(attrs = {
        style {
            overflowX("auto")
            overflowY("auto")
            padding(25.px, 50.px)
            height(100.percent)
            alignItems(AlignItems.Center)
            property("margin", "auto")
        }
        id("dialogContainer")
    }) {
        Wrap {
            H4 { Text(headingText) }
            ReadOnlyProperty(Property("Zielsumme", evaluation.auctionDetails.targetAmount))
            ReadOnlyProperty(Property("Erreichte Summe", evaluation.totalSumOfWeightedBids))
            Div(attrs = { style {
                width(50.percent)
                padding(0.px)
                margin(0.px) } }) { LineSeparator() }
            val difference = evaluation.totalSumOfWeightedBids - evaluation.auctionDetails.targetAmount
            val formattedDifference = if (difference >= 0) "+${difference}" else "$difference" // difference is already negative when < 0
            val percentChange = (difference / evaluation.auctionDetails.targetAmount.toDouble()) * 100
            val formattedPercentage = if (percentChange >= 0)
                "+${percentChange.roundToInt()}%"
            else
                "${percentChange.roundToInt()}%"
            ReadOnlyProperty(
                Property(
                    "Differenz",
                    "$formattedDifference (${formattedPercentage})"
                ),
                PropertyStyles().copy(
                    valueStyle = {
                        when {
                            difference >= 0 -> color(Color.seagreen)
                            else -> color(Color.crimson)
                        }
                    }
                )
            )
            P { Text("Gesamtanzahl gekaufter Anteile: ${evaluation.totalNumberOfShares}") }
        }
        Wrap {
            H4 { Text("Verhältnis Max / Min")}
            val minimalBid = evaluation.weightedBids.minBy { bid -> bid.bid }
            val maximalBid = evaluation.weightedBids.maxBy { bid -> bid.bid }
            val quotient = (maximalBid.bid / minimalBid.bid).asDynamic().toFixed(2) as String
            Div(attrs = { style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                alignItems(AlignItems.Stretch)
                gap(20.px)
            }}) {
                Wrap {
                    P(attrs = pStyle) { Text("Minimal Gebot (Gebot / Anteile)")}
                    P(attrs = pStyle) { Text("Maximal Gebot (Gebot / Anteile)")}
                    P(attrs = pStyle) { Text("Verhältnis max / min")}
                }
                Wrap {
                    P(attrs = pStyle) { Text("${minimalBid.bid} / ${minimalBid.weight}")}
                    P(attrs = pStyle) { Text("${maximalBid.bid} / ${maximalBid.weight}")}
                    P(attrs = pStyle) { Text(quotient)}
                }
            }
        }
        Wrap {
            H4 { Text("Automatische Gebote")}
            Div(attrs = { style {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
                alignItems(AlignItems.Stretch)
                gap(20.px)
            }}) {
                Wrap {
                    P(attrs = pStyle) { Text("Autom. Gebotshöhe pro Anteil (Richtwert + Solibeitrag):")}
                    P(attrs = pStyle) { Text("% der Bieter, die aktiv ein Gebot abgegeben haben:")}
                    P(attrs = pStyle) { Text("% der Gebote, deren Höhe unter der automatischen liegt:")}
                }
                Wrap {
                    P(attrs = pStyle) { Text("$standardBid €")}
                    P(attrs = pStyle) { Text("$pctMadeBidDisplay% ($noneStandardBidsAmount von ${evaluation.weightedBids.size})") }
                    P(attrs = pStyle) { Text("$lowBidsAmountPct% ($lowBidsAmount von $bidsCount)")}
                }
            }
        }
        Wrap {
            H4 { Text("Graphische Darstellung")}
            Div(attrs = {
                style { width(100.percent) }
                id("bidHistogramContainer")
            })
            Label(forId = "plotCheckbox") {
                CheckboxInput(
                    checked = showStandardBidsTotal,
                    attrs = {
                        id("plotCheckbox")
                        onInput { event -> showStandardBidsTotal = event.value }
                    }
                )
                Span(attrs = {
                        style { marginLeft(10.px) }
                    }
                ) {
                    Text("Standard-Gebote anzeigen ?")
                }
            }
            val avgBid = bidAmounts.average().roundTo(2)
            P {
                Span({ style { fontSize(150.percent) } }) {
                    Text(avgSign)
                }
                Text(" Gebotshöhe (gesamt): $avgBid €")
            }
        }
        Div(attrs = {
            style { width(100.percent) }
            id("bidPerShareHistogramContainer")
        })
        Label(forId = "plotCheckbox2") {
            CheckboxInput(
                checked = showStandardBidsPerShare,
                attrs = {
                    id("plotCheckbox2")
                    onInput { event -> showStandardBidsPerShare = event.value }
                }
            )
            Span(attrs = {
                    style { marginLeft(10.px) }
                }
            ) {
                Text("Standard-Gebote anzeigen ?")
            }
        }
        P {
            Span({ style { fontSize(150.percent) } }) {
                Text(avgSign)
            }
            Text(" Gebotshöhe (pro Anteil): $avgBidPerShare €")
        }
        Div(attrs = {
            style { width(100.percent) }
            id("sharesHistogramContainer")
        })
        P {
            Span({ style { fontSize(150.percent) } }) {
                Text(avgSign)
            }
            Text(" Anzahl Anteile pro Gebot: $avgSharesPerBidDisplay")
        }
    }
}

