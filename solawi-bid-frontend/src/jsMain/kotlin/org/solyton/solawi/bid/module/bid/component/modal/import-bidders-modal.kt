package org.solyton.solawi.bid.module.bid.component.modal

import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.evoleq.compose.Markup
import org.evoleq.compose.dnd.Dropzone
import org.evoleq.compose.dnd.readFileContent
import org.evoleq.compose.modal.Modal
import org.evoleq.compose.modal.ModalData
import org.evoleq.compose.modal.ModalType
import org.evoleq.compose.modal.Modals
import org.evoleq.compose.style.data.device.DeviceType
import org.evoleq.csv.parseCsv
import org.evoleq.language.Lang
import org.evoleq.math.Source
import org.evoleq.optics.storage.Action
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.storage.nextId
import org.evoleq.optics.storage.put
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.module.bid.action.importBiddersFromOrganization
import org.solyton.solawi.bid.module.bid.component.styles.auctionModalStyles
import org.solyton.solawi.bid.module.bid.data.BidApplication
import org.solyton.solawi.bid.module.bid.data.api.*
import org.solyton.solawi.bid.module.bid.data.values.AuctionId
import org.solyton.solawi.bid.module.control.button.UploadButton
import org.solyton.solawi.bid.module.values.ProviderId
import org.w3c.dom.HTMLElement

enum class ImportBiddersSource {
    CSV,
    ORGANIZATION_MEMBERS
}

val importBiddersContentStyle: StyleScope.() -> Unit = {
    height(280.px)
    width(100.percent)
    property("box-sizing", "border-box")
}

@Markup
@Suppress("FunctionName")
fun ImportBiddersModal(
    id: Int,
    texts: Lang.Block,
    modals: Storage<Modals<Int>>,
    device: Source<DeviceType>,
    dispatch: suspend (Action<BidApplication, ImportBiddersFromOrganization, ApiAuction>)->Unit,
    organizationId: ProviderId,
    auctionId: AuctionId,
    // fiscalYearId: FiscalYearId,
    setBidders: (List<NewBidder>)->Unit,
    addBidders: (AddBidders)->Unit,
    isOkButtonDisabled: ()->Boolean,
    cancel: ()->Unit,
    update: ()->Unit
): @Composable ElementScope<HTMLElement>.()->Unit = Modal(
    type = ModalType.Dialog,
    id = id,
    modals = modals,
    device = device,
    onOk = {
        update()
    },
    onCancel = {
        cancel()
    },
    isOkButtonDisabled = isOkButtonDisabled,
    texts = texts,
    styles = auctionModalStyles(device).compact(),
) {

    var isProcessingFileContent by remember {
        mutableStateOf(false)
    }

    var importBiddersSource by remember {
        mutableStateOf(ImportBiddersSource.CSV)
    }

    val scope = rememberCoroutineScope()


        // Header
        Div({
            style {
                marginBottom(24.px)
            }
        }) {

            H3({
                style {
                    margin(0.px)
                    marginBottom(8.px)
                    fontSize(22.px)
                    fontWeight("600")
                    color(Color("#243B2C"))
                }
            }) {
                Text("Teilnehmer importieren")
            }

            P({
                style {
                    margin(0.px)
                    fontSize(14.px)
                    color(Color("#718078"))
                    lineHeight("1.6")
                }
            }) {
                Text(
                    "Wähle aus, woher du die Teilnehmer " +
                            "für diese Bieterrunde importieren möchtest."
                )
            }
        }

        // Importquelle
        ImportSourceSelector(
            selected = importBiddersSource,
            onSelect = {
                importBiddersSource = it
            }
        )

        // Inhalt
        Div({
            style {
                importBiddersContentStyle()
            }
        }) {
        when (importBiddersSource) {

            ImportBiddersSource.CSV -> {

                Div({
                    style {
                        padding(24.px)
                        borderRadius(12.px)
                        backgroundColor(Color("#F8FBF8"))
                    }
                }) {

                    H4({
                        style {
                            marginTop(0.px)
                            marginBottom(12.px)
                            color(Color("#243B2C"))
                        }
                    }) {
                        Text("CSV-Datei hochladen")
                    }

                    Dropzone(
                        onProcessingStarted = {
                            isProcessingFileContent = true
                        },
                        onProcessingStopped = {
                            isProcessingFileContent = false
                        }
                    ) { files ->

                        files
                            .filter {
                                it.name.endsWith(
                                    ".csv",
                                    ignoreCase = true
                                )
                            }
                            .forEach { file ->

                                readFileContent(file) { content ->

                                    val parsed = parseCsv(content)

                                    if (!parsed.hasData(
                                            "Vorname",
                                            "Nachname",
                                            "Email",
                                            "Anteile",
                                            "Eier-Anteile",
                                            "Emails",
                                            "Data"
                                        )
                                    ) {
                                        return@readFileContent
                                    }

                                    setBidders(
                                        parsed.map {
                                            NewBidder(
                                                it["Email"]!!,
                                                0,
                                                it["Anteile"]!!.toInt()
                                            )
                                        }
                                    )

                                    addBidders(
                                        AddBidders(
                                            parsed.map {
                                                BidderData(
                                                    it["Vorname"]!!,
                                                    it["Nachname"]!!,
                                                    it["Email"]!!,
                                                    it["Anteile"]!!.toInt(),
                                                    it["Eier-Anteile"]!!.toInt(),
                                                    it["Emails"]!!
                                                        .split(",")
                                                        .map(String::trim),
                                                    it["Data"]!!
                                                        .split(",")
                                                        .map(String::trim)
                                                )
                                            }
                                        )
                                    )
                                }
                            }
                    }

                    if (isProcessingFileContent) {
                        P({
                            style {
                                color(Color("#438A63"))
                                fontSize(13.px)
                            }
                        }) {
                            Text("Datei wird verarbeitet ...")
                        }
                    }
                }

                // Hinweis
                Div({
                    style {
                        marginTop(16.px)
                        padding(14.px)
                        borderRadius(8.px)
                        backgroundColor(Color("#EFF6F0"))
                        fontSize(13.px)
                        color(Color("#52715D"))
                        lineHeight("1.5")
                    }
                }) {
                    Text(
                        "ⓘ Die CSV-Datei muss die erforderlichen " +
                                "Spalten für Teilnehmer und Anteile enthalten."
                    )
                }
            }

            ImportBiddersSource.ORGANIZATION_MEMBERS -> {

                Div({
                    style {
                        padding(24.px)
                        borderRadius(12.px)
                        backgroundColor(Color("#F8FBF8"))
                    }
                }) {

                    H4({
                        style {
                            marginTop(0.px)
                            color(Color("#243B2C"))
                        }
                    }) {
                        Text("Mitglieder importieren")
                    }

                    P({
                        style {
                            color(Color("#718078"))
                            fontSize(14.px)
                            lineHeight("1.6")
                            marginBottom(24.px)
                        }
                    }) {
                        Text(
                            "Übernimm die Mitglieder deiner " +
                                    "Organisation als Teilnehmer " +
                                    "für diese Bieterrunde."
                        )
                    }

                    UploadButton(
                        color = Color.white,
                        bgColor = Color("#438A63"),
                        texts = {
                            "Mitglieder importieren"
                        },
                        deviceType = device,
                        isDisabled = false
                    ) {
                        scope.launch {
                            dispatch(
                                importBiddersFromOrganization(
                                    ImportBiddersFromOrganization(
                                        organizationId,
                                        auctionId = auctionId
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
 }







    /*
    var isProcessingFileContent by remember { mutableStateOf(false) }
    var importBiddersSource by remember { mutableStateOf(ImportBiddersSource.CSV) }

    Div {
        ImportBiddersSource.entries.forEach { source ->
            Input(type = InputType.Radio) {
                name("import-bidders-source")
                value(source.name)
                checked(importBiddersSource == source)
                onChange { importBiddersSource = source }
            }
            Span { Text(source.name) }
        }
    }
    if (importBiddersSource == ImportBiddersSource.CSV) {
        Dropzone(
            onProcessingStarted = { isProcessingFileContent = true },
            onProcessingStopped = { isProcessingFileContent = true },
        ) { files ->
            files.filter { it.name.endsWith("csv") }.map {
                readFileContent(it) { content ->

                    val parsed = parseCsv(content)

                    setBidders(parsed.map {
                        NewBidder(it["Email"]!!, 0, it["Anteile"]!!.toInt())
                    })
                    if (!parsed.hasData(
                            "Vorname",
                            "Nachname",
                            "Email",
                            "Anteile",
                            "Eier-Anteile",
                            "Emails",
                            "Data"
                        )
                    ) return@readFileContent
                    // todo:i18n
                    addBidders(AddBidders(parsed.map {
                        BidderData(
                            it["Vorname"]!!,
                            it["Nachname"]!!,
                            it["Email"]!!,
                            it["Anteile"]!!.toInt(),
                            it["Eier-Anteile"]!!.toInt(),
                            it["Emails"]!!.split(",").map { it.trim() },
                            it["Data"]!!.split(",").map { it.trim() },
                        )
                    }))
                }
            }
        }
    } else {
        val scope = rememberCoroutineScope()
        UploadButton(
            color = Color.black,
            bgColor = Color.white,
            {"Import bidders from organization"},
            device,
            false,
        ) {
            scope.launch {
                dispatch(importBiddersFromOrganization(
                    ImportBiddersFromOrganization(
                        organizationId,
                        auctionId = auctionId
                    )
                ))
            }
        }
    }
    */



@Composable
private fun ImportSourceSelector(
    selected: ImportBiddersSource,
    onSelect: (ImportBiddersSource) -> Unit
) {
    Div({
        style {
            display(DisplayStyle.Flex)
            gap(16.px)
            marginBottom(24.px)
        }
    }) {
        ImportSourceCard(
            title = "CSV-Datei",
            description = "Teilnehmer aus einer CSV-Datei importieren",
            selected = selected == ImportBiddersSource.CSV,
            onClick = {
                onSelect(ImportBiddersSource.CSV)
            }
        )

        ImportSourceCard(
            title = "Organisation",
            description = "Mitglieder der Organisation übernehmen",
            selected = selected == ImportBiddersSource.ORGANIZATION_MEMBERS,
            onClick = {
                onSelect(ImportBiddersSource.ORGANIZATION_MEMBERS)
            }
        )
    }
}

@Composable
private fun ImportSourceCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Div({
        onClick { onClick() }

        style {
            flex(1)
            padding(20.px)

            borderRadius(12.px)

            border {
                width(2.px)
                style(LineStyle.Solid)
                color(
                    if (selected) Color("#438A63")
                    else Color("#E2E8E4")
                )
            }

            backgroundColor(
                if (selected) Color("#F0F8F2")
                else Color.white
            )

            cursor("pointer")
            property("transition", "all 0.2s ease")
        }
    }) {

        Div({
            style {
                display(DisplayStyle.Flex)
                alignItems(AlignItems.Center)
                gap(10.px)
                marginBottom(10.px)
            }
        }) {

            Span({
                style {
                    color(Color("#438A63"))
                    fontSize(20.px)
                }
            }) {
                Text(if (selected) "◉" else "○")
            }

            Span({
                style {
                    fontWeight("600")
                    fontSize(16.px)
                    color(Color("#243B2C"))
                }
            }) {
                Text(title)
            }
        }

        Div({
            style {
                fontSize(13.px)
                color(Color("#718078"))
                lineHeight("1.5")
            }
        }) {
            Text(description)
        }
    }
}

@Composable
private fun CsvUploadArea(
    isProcessing: Boolean,
    onFilesDropped: (List<org.w3c.files.File>) -> Unit
) {
    Div({
        style {
            marginTop(12.px)
        }
    }) {

        H4({
            style {
                marginBottom(12.px)
                color(Color("#243B2C"))
                fontSize(15.px)
                fontWeight("600")
            }
        }) {
            Text("CSV-Datei hochladen")
        }

        Div({
            style {
                padding(32.px)
                borderRadius(12.px)

                border {
                    width(2.px)
                    style(LineStyle.Dashed)
                    color(Color("#A9C9B3"))
                }

                backgroundColor(Color("#F8FBF8"))
                textAlign("center")
            }
        }) {

            Div({
                style {
                    fontSize(36.px)
                    color(Color("#438A63"))
                    marginBottom(12.px)
                }
            }) {
                Text("↑")
            }

            Div({
                style {
                    fontSize(16.px)
                    fontWeight("600")
                    color(Color("#243B2C"))
                    marginBottom(8.px)
                }
            }) {
                Text(
                    if (isProcessing)
                        "Datei wird verarbeitet ..."
                    else
                        "CSV-Datei hier ablegen"
                )
            }

            Div({
                style {
                    fontSize(13.px)
                    color(Color("#718078"))
                }
            }) {
                Text("Unterstütztes Format: .csv")
            }
        }
    }
}

fun List<Map<String, String>>.hasData(vararg keys: String): Boolean = all {
    keys.all { key ->
        it.containsKey(key)
    }
}

@Markup
fun Storage<Modals<Int>>.showImportBiddersModal(
    texts: Lang.Block,
    device: Source<DeviceType>,
    dispatch: suspend (Action<BidApplication, ImportBiddersFromOrganization, ApiAuction>)->Unit,
    organizationId: ProviderId,
    auctionId: AuctionId,
    setBidders: (List<NewBidder>)->Unit,
    addBidders: (AddBidders)->Unit,
    isOkButtonDisabled: ()->Boolean,
    cancel: ()->Unit,
    update: ()->Unit
) = with(nextId()) {
    put(this to ModalData(this,
        ModalType.Dialog,
        ImportBiddersModal(
            this,
            texts,
            this@showImportBiddersModal,
            device,
            dispatch,
            organizationId,
            auctionId,
            setBidders,
            addBidders,
            isOkButtonDisabled,
            cancel,
            update
        )
    ) )
}
