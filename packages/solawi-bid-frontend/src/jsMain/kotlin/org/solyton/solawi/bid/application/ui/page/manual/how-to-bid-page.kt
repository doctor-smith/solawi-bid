package org.solyton.solawi.bid.application.ui.page.manual

import androidx.compose.runtime.*
import org.evoleq.compose.Markup
import org.evoleq.compose.layout.Horizontal
import org.evoleq.compose.layout.Space
import org.evoleq.compose.layout.Vertical
import org.evoleq.compose.layout.scrollableStyle
import org.evoleq.compose.routing.navigate
import org.evoleq.math.Reader
import org.evoleq.optics.storage.Storage
import org.evoleq.optics.transform.times
import org.evoleq.parser.Whitespace
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.solyton.solawi.bid.application.data.Application
import org.solyton.solawi.bid.application.data.device.Device
import org.solyton.solawi.bid.application.data.device.DeviceType
import org.solyton.solawi.bid.application.data.device.mediaType
import org.solyton.solawi.bid.application.data.deviceData
import org.solyton.solawi.bid.application.data.env.Environment
import org.solyton.solawi.bid.application.ui.style.page.verticalPageStyle
import org.solyton.solawi.bid.application.ui.style.wrap.Wrap
import org.solyton.solawi.bid.module.bid.component.form.SendBidForm
import org.solyton.solawi.bid.module.control.button.StdButton
import org.solyton.solawi.bid.module.mobile.component.MobileDevice
import org.solyton.solawi.bid.module.qrcode.QRCodeSvg

@Markup
@Composable
@Suppress("FunctionName")
fun HowToBidPage(application: Storage<Application>) {
    val scale = 1.0
    val link = "demo-link"
    val storage = Storage(
        read = {Application(
            environment = Environment(),
            deviceData = Device(mediaType =  DeviceType.Tablet),
        )},
        write = {{}}
    )

    var email by remember { mutableStateOf("") }
    var bid by remember { mutableStateOf(0.0) }
    var numberOfShares by remember { mutableStateOf(2) }

    Vertical({
        verticalPageStyle()
        scrollableStyle()
        height(96.vh)
        //flexGrow(1) // Allow it to take available space
        minHeight(0.px) // Required in some cases to enable scrolling


    }) {
        Wrap {
            H1 { Text("Wie man bietet") }
        }
        Wrap {
            H2 { Text("1. Scanne den QR-Code ") }
            Horizontal {
                Div({/*style { width(210.px) }*/}) {
                    MobileDevice(scale) {
                        Vertical {
                            QRCodeSvg(
                                id = "#qr-code-id-0",
                                size = 90.percent,
                                data = "fjkdajfkdajkdajfkdjaödj",
                                download = false
                            )
                            Space()
                            StdButton({ "Zur Website" }, DeviceType.Mobile) {}
                        }
                    }
                }
                Div({style {
                    padding(20.px)
                }}) {
                    P{Text("Scanne den QR-Code mit einem Scanner deiner Wahl und navigiere anschließend zur Seite auf der Du dein Gebot abgeben kannst")}
                }
            }
        }
        Wrap {
            H2 { Text("2. Biete") }
            Horizontal {
                MobileDevice(scale) {
                    Vertical({padding(5.percent)}) {
                        SendBidForm((storage * deviceData * mediaType).read()) {
                            newBid ->
                                bid = newBid.amount
                                email = newBid.username
                        }
                        Div({ style { flexGrow(1) } }) {}
                        Wrap {
                            A (href = "#qr-code-id-0"){
                                StdButton({ "QR Code" }, storage * deviceData * mediaType.get) {
                                    navigate("#qr-code-id-0")
                                }
                            }
                        }
                    }
                }
                Div({style {
                    padding(20.px)
                }}) {
                        P{Text("Gib bei Email die Email Adresse ein, unter der Du bei der Solawi registriert bist")}
                        P{Text("Unter Gebot gibst Du bitte dein Gebot ein, das du für einen Anteil bezahlen möchtest. Wenn du mehrere Anteile gebucht hast, wird dein Gebot durch die Software entsprechend gewertet.")}
                        P{Text("Sende dein Gebot ab, indem du auf den Button \"Gebot Senden\" drückst")}
                        P{Text("Du erhälts dann ein Feedback, ob dein Gebot angenommen wurde oder nicht. Falls dein Gebot nicht angenommen werden kann, überprüfe bitte deine Angaben. Wenn es dennoch nicht funktioniert, dann wende dich bitte ans Personal")}
                }
            }
        }
        Wrap{
            H2{ Text("3. Überprüfe das Ergebnis ")}
            Horizontal {

                MobileDevice(scale) {
                    Vertical({padding(10.px)}) {
                        Wrap {
                            P { Text("Ihr Gebot wurde erfolgreich gesendet") }
                        }
                        Wrap {
                            Vertical {
                                Wrap{
                                    P{Text("Betrag:")}
                                    P{Text("$bid")}
                                }
                                Wrap{
                                    P{Text("Anteile:")}

                                    P{Text("$numberOfShares")}
                                }
                                Wrap{
                                    P{Text("Monatlicher Gesamtbetrag:")}

                                    P{Text("${numberOfShares * bid}")}
                                }
                            }
                        }
                        Space()
                        StdButton(
                            texts = Reader{"Ok"},
                            deviceType = DeviceType.Mobile,
                        ) {}

                    }


                }
                Div({style {
                    padding(20.px)
                    marginLeft(20.px)
                }}){
                        P { Text("Wenn dein Gebot erfolgreich übermittelt wurde, erhält du eine Rückmeldung, die folgende Daten enthält:") }
                        Ul({style { marginLeft(20.px) }}) {
                            Li { Text("  1. Das Gebot") }
                            Li { Text("  2. Die Anzahl deiner gebuchten Anteile") }
                            Li { Text("  3. Den Gesamtbetrag (Anzahl Anteile x Gebot)") }
                        }
                        P { Text("Bitte überprüfe diese Werte genau.") }
                }
            }
        }
        Wrap {
            H2 { Text("4. Teile den QR-Code") }
            Vertical {
                Wrap {
                    Horizontal {
                        MobileDevice(scale) {
                            Vertical {
                                SendBidForm((storage * deviceData * mediaType).read()) {}
                                Div({ style { flexGrow(1) } }) {}
                                Wrap {
                                    A (href = "#qr-code-id-1"){
                                        StdButton({ "QR Code" }, storage * deviceData * mediaType.get) {
                                            navigate("#qr-code-id-1")
                                        }
                                    }
                                }
                            }
                        }

                        Div({style {
                            padding(20.px)
                        }}){
                            P{Text("Clicke den Button \"QR-Code\". Es erscheint der Zuganscode für die aktuell laufende Runde.")}
                            P{Text("Teile diesen QR-Code mit deinen \"Nachbarn\", um den Biete Vorgang voranzutreiben")}
                        }
                    }
                }
                Wrap {
                    Horizontal {
                        MobileDevice(scale) {
                            QRCodeSvg(
                                id = "qr-code-id-0",
                                size = 90.percent,
                                data = "fjkdajfkdajkdajfkdjaödj",
                                download = false
                            )
                        }

                        Div({style {
                            padding(20.px)
                        }}){
                                P { Text("Clicke den Button \"QR-Code\". Es erscheint der Zuganscode für die aktuell laufende Runde.") }
                                P { Text("Teile diesen QR-Code mit deinen \"Nachbarn\", um den Biete Vorgang voranzutreiben") }

                        }
                    }
                }
            }
        }
    }
}
