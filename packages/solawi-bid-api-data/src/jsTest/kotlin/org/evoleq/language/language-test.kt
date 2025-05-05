package org.evoleq.language

import org.evoleq.parser.ParsingResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LanguageTest {


    @Test fun merge1() {
        val base = texts {
            key = "base"
            variable {
                key = "1"
                value = "1"
            }
        }
        val new = texts {
            key = "base"
            variable {
                key = "2"
                value = "2"
            }
        }
        val merged = base.merge(new)
        val expected = texts {
            key = "base"
            variable {
                key = "1"
                value = "1"
            }
            variable {
                key = "2"
                value = "2"
            }
        }
        assertEquals(expected, merged)
    }

    @Test fun merge2() {
        val base = texts {
            key = "base"
            variable {
                key = "1"
                value = "1"
            }
        }
        val new = texts {
            key = "base"
            variable {
                key = "1"
                value = "2"
            }
        }
        val merged = base.merge(new)
        val expected = texts {
            key = "base"
            variable {
                key = "1"
                value = "2"
            }

        }
        assertEquals(expected, merged)
    }




    @Test fun merge5() {
        val langBase = texts{
            key = "base"
            block{
                key = "block"
                variable {
                    key = "1"
                    value = "1"
                }
            }
        }

        val newLang = texts{
            key = "base"
            block{
                key = "block"
                variable {
                    key = "2"
                    value = "2"
                }
            }
        }

        val merged = langBase.merge(newLang)

        val expected = texts{
            key = "base"
            block{
                key = "block"
                variable {
                    key = "1"
                    value = "1"
                }
                variable {
                    key = "2"
                    value = "2"
                }
            }
        }
        assertEquals(expected, merged)
    }
    @Test fun mergeComponents() {
        val c1 = """
            |de{
            |   module{
                |   component1{
                |       name: "c1"
                |   }
            |   }
            |}
        """.trimMargin()
        val c2 = """
            |de{
            |   module{
                |   component2{
                |       name: "c2"
                |   }
            |   }
            |}
        """.trimMargin()
        val b1Result = LanguageP().run(c1)
        assertTrue{b1Result.result != null}
        val b1 = b1Result.result!!
        val b2Result = LanguageP().run(c2)
        assertTrue{b2Result.result != null}
        val b2 = b2Result.result!!

        val merged = b1.merge(b2)

        println(merged)

        val components = (merged as Block).component(
            "module"
        )
        assertEquals(2, components.value.size)

        val comp1 = components.component("component1")
        val comp2 = components.component("component2")

        assertEquals("c1", comp1["name"])
        assertEquals("c2", comp2["name"])
    }


    @Test fun realExample() {
        val c1 = "de{\n" +
            "    solyton{\n" +
            "        authentication{\n" +
            "            login{\n" +
            "                fields{\n" +
            "                    username: \"Nutzername\"\n" +
            "                    password: \"Passwort\"\n" +
            "                }\n" +
            "                buttons{\n" +
            "                    ok: \"Login\"\n" +
            "                    password-reset: \"Passwort zurücksetzen\"\n" +
            "                    sign-up: \"Registrieren\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        solawi{\n" +
            "            mainPage{\n" +
            "                title: \"Solawi Bieter App\"\n" +
            "            }\n" +
            "        }\n" +
            "        cookieDisclaimer {\n" +
            "            title: \"Cookie Richtlinie\"\n" +
            "            content {\n" +
            "                hint: \"Wir verwenden Cookies um das User Experience auf unserer Website kontinuierlich zu verbessern. Dazu speichern wir die bevorzugte Sprache des Nutzers.\"\n" +
            "            }\n" +
            "            okButton {\n" +
            "                title: \"Ok\"\n" +
            "            }\n" +
            "            cancelButton {\n" +
            "                title: \"Abbrechen\"\n" +
            "            }\n" +
            "        }\n" +
            "        dashboardPage {\n" +
            "            title: \"Herzlich Willkommen auf Ihrem Dashboard\"\n" +
            "            auctionsCard {\n" +
            "                navButton {\n" +
            "                    title: \"Auktionen\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        auction {\n" +
            "            createDialog {\n" +
            "                title: \"Auktion erstellen\"\n" +
            "                inputs {\n" +
            "                    title: \"Name\"\n" +
            "                    date: \"Datum\"\n" +
            "                }\n" +
            "                okButton {\n" +
            "                    title: \"Ok\"\n" +
            "                }\n" +
            "                cancelButton {\n" +
            "                    title: \"Abbrechen\"\n" +
            "                }\n" +
            "            }\n" +
            "            updateDialog {\n" +
            "                title: \"Auktion bearbeiten\"\n" +
            "                inputs {\n" +
            "                    title: \"Name\"\n" +
            "                    date: \"Datum\"\n" +
            "                    targetAmount: \"Zielbetrag\"\n" +
            "                    solidarityContribution: \"Solibeitrag\"\n" +
            "                    minimalBid: \"Minimal Gebot\"\n" +
            "                    benchmark: \"Richtwert\"\n" +
            "                }\n" +
            "                okButton {\n" +
            "                    title: \"Ok\"\n" +
            "                }\n" +
            "                cancelButton {\n" +
            "                    title: \"Abbrechen\"\n" +
            "                }\n" +
            "            }\n" +
            "            importBiddersDialog {\n" +
            "                title: \"Bieter importieren\"\n" +
            "                okButton {\n" +
            "                    title: \"Ok\"\n" +
            "                }\n" +
            "                cancelButton {\n" +
            "                    title: \"Abbrechen\"\n" +
            "                }\n" +
            "            }\n" +
            "            round {\n" +
            "                bidRoundEvaluationModal {\n" +
            "                    title: \"Evaluation\"\n" +
            "                    okButton {\n" +
            "                        title: \"Annehmen\"\n" +
            "                    }\n" +
            "                    cancelButton {\n" +
            "                        title: \"Ablehnen\"\n" +
            "                    }\n" +
            "                }\n" +
            "                successfulBidInformationModal {\n" +
            "                    title: \"Gebot erfolgreich gesendet\"\n" +
            "                    message: \"Ihr Gebot wurde erfolgreich gesendet\"\n" +
            "                    amount: \"Betrag\"\n" +
            "                    numberOfShares: \"Anteile\"\n" +
            "                    totalAmountPerMonth: \"Monatlicher Gesamtbetrag\"\n" +
            "                    okButton {\n" +
            "                        title: \"Ok\"\n" +
            "                    }\n" +
            "                    cancelButton {\n" +
            "                        title: \"Abbrechen\"\n" +
            "                    }\n" +
            "                }\n" +
            "                states {\n" +
            "                    opened: \"Eröffnet\"\n" +
            "                    started: \"Gestartet\"\n" +
            "                    stopped: \"Beendet\"\n" +
            "                    evaluated: \"Ausgewertet\"\n" +
            "                    closed: \"Geschlossen\"\n" +
            "                    frozen: \"Eingefroren\"\n" +
            "                }\n" +
            "                commands {\n" +
            "                    start: \"Start\"\n" +
            "                    stop: \"Stop\"\n" +
            "                    evaluate: \"Auswerten\"\n" +
            "                    close: \"Schließen\"\n" +
            "                    freeze: \"Einfrieren\"\n" +
            "                    null: \"-\"\n" +
            "                }\n" +
            "                bidRoundList {\n" +
            "                    rounds: \"Runden\"\n" +
            "                    item {\n" +
            "                        buttons {\n" +
            "                            exportResults {\n" +
            "                                title: \"Export\"\n" +
            "                                tooltip: \"Ergebnisse der Bieterunde exportieren\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "\n" +
            "            }\n" +
            "            auctionPage {\n" +
            "                title: \"Auktion\"\n" +
            "                details {\n" +
            "                    title: \"Details\"\n" +
            "                    targetAmount: \"Zielbetrag\"\n" +
            "                    solidarityContribution: \"Solibeitrag\"\n" +
            "                    date: \"Datum\"\n" +
            "                    minimalBid: \"Minimal Gebot\"\n" +
            "                    benchmark: \"Richtwert\"\n" +
            "                    numberOfBidders: \"Anzahl der Prosumenten\"\n" +
            "                    numberOfShares: \"Anzahl der Anteile\"\n" +
            "                }\n" +
            "                buttons {\n" +
            "                    updateAuction {\n" +
            "                        text: \"Konfiguration\"\n" +
            "                    }\n" +
            "                    importBidders {\n" +
            "                        text: \"Bieter Import\"\n" +
            "                    }\n" +
            "                    createRound {\n" +
            "                        text: \"Neue Runde\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "            auctionsPage {\n" +
            "                title: \"Auktionen\"\n" +
            "                buttons {\n" +
            "                    createAuction {\n" +
            "                        title: \"Auktion Erstellen\"\n" +
            "                    }\n" +
            "                }\n" +
            "                auctionList {\n" +
            "                    items {\n" +
            "                        buttons {\n" +
            "                            details {\n" +
            "                                title: \"Details\"\n" +
            "                            }\n" +
            "                            edit {\n" +
            "                                title: \"Bearbeiten\"\n" +
            "                            }\n" +
            "                            delete {\n" +
            "                                title: \"Loschen\"\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "            searchBiddersPage{\n" +
            "                title: \"Bieter durchsuchen\"\n" +
            "                buttons {\n" +
            "                    search {\n" +
            "                        title: \"Suchen\"\n" +
            "                    }\n" +
            "                }\n" +
            "                inputs {\n" +
            "                    firstname: \"Vorname\"\n" +
            "                    lastname: \"Nachname\"\n" +
            "                    emailAddress: \"Email Adresse\"\n" +
            "                    relatedEmailAddresses: \"Weitere Email Adressen\"\n" +
            "                    relatedData: \"Weitere Daten\"\n" +
            "                }\n" +
            "                results {\n" +
            "                    title: \"Email Adressen\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        errors {\n" +
            "            title: \"Fehler\"\n" +
            "            okButton {\n" +
            "                title: \"Ok\"\n" +
            "            }\n" +
            "            cancelButton {\n" +
            "                title: \"Abbrechen\"\n" +
            "            }\n" +
            "            messages {\n" +
            "                loginFailed: \"Login fehlgeschlagen\"\n" +
            "            }\n" +
            "        }\n" +
            "        locales{\n" +
            "            de: \"Deutsch\"\n" +
            "            en: \"Englisch\"\n" +
            "        }\n" +
            "    }\n" +
            "}"

        val c2 = "de{\n" +
            "    solyton{\n" +
            "        user {\n" +
            "            managementPage {\n" +
            "                title: \"\"\n" +
            "            }\n" +
            "            privatePage {\n" +
            "                title: \"\"\n" +
            "                buttons {\n" +
            "                    changePassword {\n" +
            "                        title: \"Passwort ändern\"\n" +
            "                    }\n" +
            "                }\n" +
            "                dialogs {\n" +
            "                    changePassword {\n" +
            "                        title: \"Passwort ändern\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}"

        val lang = LanguageP().run(c1).result!!
        val l = LanguageP().run(c2).result!!

        val r = lang.merge(l)
    }
}