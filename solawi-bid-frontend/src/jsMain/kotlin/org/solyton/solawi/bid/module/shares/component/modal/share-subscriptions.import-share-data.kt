package org.solyton.solawi.bid.module.shares.component.modal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.evoleq.compose.Markup
import org.evoleq.compose.conditional.When
import org.evoleq.compose.dnd.Dropzone
import org.evoleq.compose.dnd.readFileContent
import org.evoleq.compose.download.downloadCsv
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.csv.parseCsv
import org.evoleq.kotlinx.date.now
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.color
import org.jetbrains.compose.web.css.gap
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.ElementScope
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text
import org.solyton.solawi.bid.module.control.button.FileExportButton
import org.solyton.solawi.bid.module.control.checkbox.CheckBox
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscription
import org.solyton.solawi.bid.module.shares.data.subscriptions.ShareSubscriptions
import org.solyton.solawi.bid.module.style.modal.commonModalStyles
import org.solyton.solawi.bid.module.style.wrap.Wrap
import org.solyton.solawi.bid.module.values.Username
import org.w3c.dom.HTMLElement
import kotlin.math.max


@Markup
@Suppress("FunctionName")
fun BulkUpdateShareDataByFileImportModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    shareSubscriptions: Map<Username, ShareSubscription>,
    setShareSubscriptions: (ShareSubscriptions)->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    id = id,
    modals = modals,
    device = device,
    onOk = { update() },
    onCancel = {},
    texts = texts,
    styles = commonModalStyles(device),
) {
    // All subscriptions need to belong to the same share offer
    val subscriptionsAreValid = shareSubscriptions.map{
        it.value.shareOfferId
    }.distinct().size == 1

    Wrap {
        When(!subscriptionsAreValid) {
            Span({
                style {
                    color(Color.red)
                }
            }){
                // todo:i18n
                Text("All subscriptions need to belong to the same share offer")
            }
        }
        When(subscriptionsAreValid) {
            var checkCount by remember { mutableStateOf(0) }
            fun updateCheckCount(boolean: Boolean) {
                checkCount = if (boolean) checkCount + 1 else max(0, checkCount - 1)
            }
            var exportPricePerShare by remember { mutableStateOf(false) }
            var exportNumberOfShares by remember { mutableStateOf(false) }
            var exportAhcAuthorized by remember { mutableStateOf(false) }
            // var exportDistributionPoint by remember { mutableStateOf(false) }
            var exportCoSubscribers by remember { mutableStateOf(false) }

            Horizontal({gap(10.px)}) {
                CheckBox(
                    exportPricePerShare,
                ) { bool ->
                    exportPricePerShare = bool
                    updateCheckCount(bool)
                }
                Text("Export price per share")
            }
            Horizontal({gap(10.px)}) {
                CheckBox(
                    exportNumberOfShares,
                ) { bool ->
                    exportNumberOfShares = bool
                    updateCheckCount(bool)
                }
                Text("Export number of shares")
            }
            Horizontal({gap(10.px)}) {
                CheckBox(
                    exportAhcAuthorized,
                ) { bool ->
                    exportAhcAuthorized = bool
                    updateCheckCount(bool)
                }
                Text("Export sepa authorized")
            }
            /*
            Horizontal({gap(10.px)}) {
                CheckBox(
                    exportDistributionPoint,
                ) { bool ->
                    exportDistributionPoint = bool
                    updateCheckCount(bool)
                }
                Text("Export distribution point")
            }

             */

            FileExportButton(
                color = Color.black,
                bgColor = Color.white,
                {"Download template"},
                device,
            ) {
                val checked = mapOf(
                    "username" to true,
                    "pricePerShare" to exportPricePerShare,
                    "numberOfShares" to exportNumberOfShares,
                    "ahcAuthorized" to exportAhcAuthorized,
                    "coSubscribers" to exportCoSubscribers,
                    // "distributionPoint" to exportDistributionPoint,
                )

                val headers = checked.filterValues { it }.keys.joinToString(";")
                val numberOfCols = checked.filterValues { it }.size
                val semiColons = ";".repeat(numberOfCols - 1)
                val csvLines: String = shareSubscriptions.map { (username, _) ->
                    username.value + semiColons
                }.joinToString("\n")

                val csv = """
                    |$headers
                    |$csvLines
                """.trimMargin()

                downloadCsv(csv, "share-subscriptions_${now()}.csv")
            }
            Dropzone() { files ->
                files.filter { it.name.endsWith("csv") }.map {
                    readFileContent(it) { content ->
                        val parsed = parseCsv(content)
                        val data = parsed.map { map ->
                            val username = Username(map["username"]!!)
                            val shareSubscription = shareSubscriptions[username]!!
                            shareSubscription.copy(
                                pricePerShare = map["pricePerShare"]?.toDoubleOrNull()
                                    ?: shareSubscription.pricePerShare,
                                numberOfShares = map["numberOfShares"]?.toIntOrNull()
                                    ?: shareSubscription.numberOfShares,
                                ahcAuthorized = map["ahcAuthorized"]?.toBooleanStrictOrNull()
                                    ?: shareSubscription.ahcAuthorized,

                            )
                        }.let { subs -> ShareSubscriptions(subs) }
                        setShareSubscriptions(data)
                    }
                }
            }
        }
    }
}

@Markup
fun Storage<Modals<Int>>.showBulkUpdateShareDataByFileImportModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    shareSubscriptions: Map<Username, ShareSubscription>,
    setShareSubscriptions: (ShareSubscriptions)->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(
        ModalType.Dialog,
        BulkUpdateShareDataByFileImportModal(
            id = this,
            texts = texts,
            modals = this@showBulkUpdateShareDataByFileImportModal,
            device = device,
            shareSubscriptions = shareSubscriptions,
            setShareSubscriptions = setShareSubscriptions,
            update = update
        )
    ) )
}
