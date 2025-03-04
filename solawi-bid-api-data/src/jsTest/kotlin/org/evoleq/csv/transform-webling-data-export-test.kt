package org.evoleq.csv

import org.solyton.solawi.bid.module.bid.data.api.BidderData
import kotlin.test.Test

class TransformWeblingDataExportTest {

    // @Test
    fun transformTest() {

        val parsed = parseCsv(text)

        val keys = parsed.first().keys
        println(keys)

        val searchData = parsed.filter{
            try{
                it["Anzahl Anteile"]!!.toInt()
                it["E-Mail*"]!!.isNotBlank()
            } catch (e:Exception){
                false
            }
        }.map{
            BidderData(
                it["Vorname*"]!!,
                it["Name*"]!!,
                it["E-Mail*"]!!,
                it["Anzahl Anteile"]!!.toInt(),
                try{it["Eieranteile je 15,86/Monat (61cent*6/Woche)"]!!.toInt()}catch(e:Exception){0},
                listOf(
                    it["E-Mail2"]!!,
                    it["E-Mail3"]!!,
                    it["E-Mail4"]!!,
                    it["E-Mail5"]!!,
                    it["E-Mail6"]!!,
                    it["E-Mail7"]!!,
                    it["E-Mail8"]!!,
                ).filter { it.isNotBlank() },
                listOf(it["Vorname (Mitanteilsnehmer*)"]!!).filter { it.isNotBlank() },
                listOf(it["Nachname (Mitanteilsnehmer*)"]!!).filter { it.isNotBlank() },
            )
        }

        println(searchData.first())

        val newCsv = """k
            |Vorname;Nachname;Email;Anteile;Eier-Anteile;Emails;Data
            |${searchData.joinToString("\n") { 
                "${it.firstname};${it.lastname};${it.email};${it.numberOfShares};${it.numberOfEggShares};${it.relatedEmails.joinToString(",") { it }};${listOf(*it.relatedFirstnames.toTypedArray(), *it.relatedLastnames.toTypedArray()).joinToString(",") { it }}"
        }}
        """.trimMargin()

        println(newCsv)

       // console.log(keys)

    }

}

val text by lazy{
    "Mitglied;Vorname*;Name*;E-Mail*;Eieranteile je 15,86/Monat (61cent*6/Woche);Datum der Erst-Bestellung Eieranteil;Datum der Erst-Bestellung Eieranteil (Jahre);Datum der Erst-Bestellung Eieranteil (Tag, Monat);Bemerkungen zur Bestellung;Datum der Abbestellung Eieranteil;Datum der Abbestellung Eieranteil (Jahre);Datum der Abbestellung Eieranteil (Tag, Monat);Depot;Anzahl Anteile;Eintrittsdatum;Eintrittsdatum (Jahre);Eintrittsdatum (Tag, Monat);Austrittsdatum;Austrittsdatum (Jahre);Austrittsdatum (Tag, Monat);Vorname (Mitanteilsnehmer*);Nachname (Mitanteilsnehmer*);Mobile*;Telefon*;Webseite;Strasse*;Ort*;PLZ*;E-Mail2;E-Mail3;E-Mail4;E-Mail5;E-Mail6;E-Mail7;E-Mail8;Kommentare;Gruppen\n" +
            "Michaela Allard;Michaela;Allard;michaela.allard@t-online.de;;;0;;;;0;;4 (Dussl);1;23.03.24;0;23.03.;;0;;;;;015120015413;;Kriegswiesenstraße 4;Ofterdingen;72131;;;;;;;;0;Depot 4 (DS)\n" +
            "Sebastian Arndt;Sebastian;Arndt;sebastian_arndt@aol.com;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;;;;0176 66894592;;Hanna-Bernheim-Straße 12;Tübingen;72072;;;;;;;;0;Depot 3 (S)\n" +
            "Hans-Peter und Frauke Auer;Hans-Peter und Frauke ;Auer;a.reichenauer@imara.gmbh;;;0;;;;0;;2 (Der.Wa);1;;0;;;0;;Andrea Ruth u. Valentin;Reichenauer-Feil u. Feil;;;;;;;;;;;;;;0;Depot 2 (WS)\n" +
            "Eckehard Baeurle;Eckehard;Baeurle;baeurle-biolandhof@web.de;;;0;;;;0;;;;;0;;;0;;;;0157-36541228;07072 – 6578;;;;;bethgepau@gmail.com;markus@bulling-online.de;;;;;;0;Erzeuger (DU), Solawikreis (SK)\n" +
            "Johanna Bätge;Johanna;Bätge;johanna.baetge@gmx.de;1;02.03.23;1;02.03.;auf neuem Konto überwiesen;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;0179-7511592;0179-7511592;;Gartenstraße 91;Tübingen;72074;;;;;;;;0;Depot 0 (WH)\n" +
            "Ulrike Bauhofer;Ulrike;Bauhofer;carlo.schmid@web.de;;;0;;;;0;;3 (Speicher);1;08.12.20;4;08.12.;31.03.25;0;31.03.;Carlo;Schmid;;;;Goethestrasse 14;Tübingen;72076;carlo.schmid@web.de;;;;;;;0;Depot 3 (S)\n" +
            "Florian Baumann;Florian;Baumann ;annika.gromes@posteo.de;;;0;;;;0;;0 (WH);1;01.07.19;5;01.07.;;0;;Annika;Gromes;017662351564;070717770821;;Im Winkelrain 12a;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Patrick und Miriam Baumhof;Patrick und Miriam;Baumhof;miriam.baumhof@gmail.com;;;0;;;;0;;4 (Dussl);1;01.04.23;1;01.04.;;0;;;;;07072  12 69 34;;Erlenstrasse, 28       ;Dusslingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Verena Becher;Verena;Becher;luegge@posteo.de;;;0;;;;0;;5 (Rott);1;;0;;;0;;;;;07472/280870;;Katharinenweg 1;Rottenburg Wendelsheim;72108;;;;;;;;0;Depot 5 (R)\n" +
            "Andreas Bechthold;Andreas;Bechthold;Andreas.Bechthold@web.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;;;;;;;;;;;;;0;Depot 0 (WH)\n" +
            "Anke Beck;Anke;Beck;aanke1312@aol.com;;;0;;;;0;;2 (Der.Wa);1;01.04.23;1;01.04.;31.03.25;0;31.03.;Susanne;Hagelberg-Hübel;015784668943;07071360339;;Wennfelder Garten 15;Tübingen ;72072;susa@hagelberg-huebel.de;;;;;;;0;Depot 2 (WS)\n" +
            "Judith Beier;Judith;Beier;JudithBeier@gmx.net;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;Hannes;Rathmann;0160 95256688 - Hannes;0176 96814759 - Judith;;Ammergasse 28;72070;Tübingen;rathmann.hannes@gmail.com;;;;;;;0;Depot 3 (S)\n" +
            "Manuel Beigang;Manuel;Beigang;manuel-b@online.de;;;0;;;;0;;3 (Speicher);1;22.02.21;3;22.02.;;0;;;;;+4915775248016;;Gartenstr. 37/1;Tübingen;72074;dexter.frueh@student.uni-tuebingen.de;;;;;;;0;Depot 3 (S)\n" +
            "Michel Kalle Bendias;Michel Kalle ;Bendias;kalle.bendias@your.company;;;0;;;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;;;0176-24896043;0176-24896043;;Bei den Pferdeställen 10;Tübingen;72072;kallebendias@gmx.de;;;;;;;0;Depot 3 (S)\n" +
            "Claudia Bendisch;Claudia;Bendisch;clume@posteo.de;;;0;;;;0;;2 (Der.Wa);1;26.04.24;0;26.04.;;0;;;;;;;Lange Furche 44;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Barbara Benzing;Barbara;Benzing;barbarabenzing@posteo.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;017634903876;010711358794;;Blaihofstr.43 ;Tübingen;72074;;;;;;;;0;Depot 0 (WH)\n" +
            "Walburga Biesinger;Walburga;Biesinger;posteo@posteo.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;;;Sophienstraße14;Tübingen;;;;;;;;;0;Depot 0 (WH)\n" +
            "Susanne Birk;Susanne ;Birk;s.holkenbrink@gmx.net;;;0;;;;0;;2 (Der.Wa);1;;0;;;0;;;;;017630440081;;Sieben-Höfe Straße 64;Tübingen ;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Cornelia Blume;Cornelia;Blume;corn.blume@freenet.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;Friederike;Kuschnitzki;01703019094;07071360286;;;;;nele.blume@freenet.de;to.tuebingen@gmx.de;;;;;;0;Depot 2 (WS)\n" +
            "Franziska Boege;Franziska ;Boege;franziwolf@web.de;;;0;;;;0;;5 (Rott);1;;0;;;0;;Marcus ;Wolf;0176-81662071;;;Hallstattstr. 23;Dußlingen;72144;f-h.auer@gmx.de;;;;;;;0;Depot 5 (R)\n" +
            "Sonja Borchers;Sonja;Borchers;sonja.borchers@gmail.com;;;0;;;;0;;3 (Speicher);1;01.07.23;1;01.07.;;0;;;;;017684058944;;Kornhausstr. 14;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Sandra Briem;Sandra;Briem;sandra.briem@posteo.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;;;;0176 96048464;;Stiffurtstraße 18;Tübingen;72074;svenkobelt@gmx.com;;;;;;;0;Depot 3 (S)\n" +
            "Tyler Brownlow;Tyler ;Brownlow ;Shalynn.Crawford@gmail.com;3;04.10.24;0;04.10.;;;0;;0 (WH);1;;0;;;0;;Shalynn;Crawford; ;017684502782;;Payerstraße ;Tübingen ;72074 ;Tyler.crawford@gmail.com;;;;;;;0;Depot 0 (WH)\n" +
            "Ruben Burkard;Ruben;Burkard;rubenburkard@gmx.net;1;23.02.23;1;23.02.;auf neuem Konto überwiesen;;0;;3 (Speicher);1;;0;;;0;;Claudio;Stumpf;;0176 30119866;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Oliver Burkhardt;Oliver;Burkhardt;burkhardtom@yahoo.de;;;0;;;;0;;5 (Rott);1;;0;;;0;;Irina;Burkhardt;;07472 42563;;Hinterer Bühlweg 31;Rottenburg;72108;;;;;;;;0;Depot 5 (R)\n" +
            "Silvia Bürth;Silvia;Bürth;silviabuerth@googlemail.com;;;0;;;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;;;;017643628438;;Mühlstr.;Tübingen ;72074;felix.aupperle@googlemail.com;;;;;;;0;Depot 3 (S)\n" +
            "Christiane Busch;Christiane;Busch;christianeundgert-hauke@outlook.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;;;;015203381078;;Friedrich-Dannenmann-Straße 13;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Patrick Butterweck;Patrick;Butterweck;patrick.butterweck@posteo.de;;;0;;;;0;;0 (WH);1;01.04.23;1;01.04.;;0;;;;0176 344 85 164 ;07071 60 58 707;;Haußerstr. 87;Tübingen;72076 ;;;;;;;;0;Depot 0 (WH), Solawikreis (SK)\n" +
            "Kornelia u Fabio Cani;Kornelia u Fabio;Cani;Fabio-cani@web.de;;;0;;;;0;;4 (Dussl);1;21.01.21;4;21.01.;;0;;Kornelia;;;0176-62918372;;Lichtensteinstraße 28/1;Reutlingen;72770;mail@konicani.com;;;;;;;0;Depot 4 (DS)\n" +
            "Misuk Choi;Misuk;Choi;misukchoi@posteo.de;1;31.03.23;1;31.03.;auf neuem Konto überwiesen;;0;;1 (Der.Ka);1;01.04.18;6;01.04.;;0;;;;01639295666;;;Hechinger Str. 12;Tübingen;72072;artifexzero@gmx.de;alexpepler@gmail.com;;;;;;0;Depot 1 (K)\n" +
            "Hanek Christina;Hanek ;Christina;christina.hanek@gmx.de;;;0;;;;0;;4 (Dussl);1;16.01.25;0;16.01.;;0;;;;;074739505331;;Brunnenstraße 13;Nehren;72147;;;;;;;;0;Depot 4 (DS)\n" +
            "Marta Chrzanowska;Marta ;Chrzanowska ;marta.chrz@gmail.com;;;0;;;;0;;1 (Der.Ka);1;01.01.25;0;01.01.;;0;;;;01752324852;;;Wennfelder Garten 7  ;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Markus Claus;Markus;Claus;einkaufen.markus@gmail.com;;;0;;;;0;;0 (WH);1;22.04.24;0;22.04.;;0;;;;;01733202573;;Eduard-Spranger-Straße 57/4;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Antony Cole;Antony;Cole;tonycole89@gmail.com;2;01.05.24;0;01.05.;Ich hatte schon 1 Anteil und würde gern ab Mai 2 Anteile haben. ;;0;;3 (Speicher);1;13.04.23;1;13.04.;;0;;Tony;Cole;017684470759;017684470759;;Hölderlinstraße 19;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Philip Copony;Philip;Copony;Knoti@mtmedia.org;2;20.03.23;1;20.03.;auf neuem Konto überwiesen, bezahlt durch Susanne Jahn, Keskin auf altem?;;0;;2 (Der.Wa);1;01.05.20;4;01.05.;;0;;Philip;Copony;015757043611;015757043611;;Schellingstr. 6/3;Tübingen;;herr.lamprecht@gmail.com;yasmina@imi-online.de;;;;;;0;Depot 2 (WS)\n" +
            "Claudia und  Daniel Dann;Claudia und  Daniel;Dann;danieldann@hotmail.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;Claudia;Dann;;0162 6666125;;Breslauerstr. 16;Tübingen;72072;claudia@krickes.de;;;;;;;0;Depot 3 (S)\n" +
            "Joy Leonie Defant;Joy Leonie;Defant ;joydefant@protonmail.com;;;0;;;;0;;3 (Speicher);1;21.02.21;3;21.02.;;0;;Andreas;;;015739074891;;Gartenstrasse 9/1;Tübingen;72074;andreas.wagner01@protonmail.com;;;;;;;0;Depot 3 (S)\n" +
            "Antonia Degenhardt;Antonia ;Degenhardt;deg.antonia@gmail.com;;;0;;;;0;;3 (Speicher);1;01.12.21;3;01.12.;;0;;;;;;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Frieder Dehlinger;Frieder ;Dehlinger;dehlinger@yahoo.de;;;0;;;;0;;4 (Dussl);1;11.02.21;4;11.02.;;0;;Victoria ;;+49 174 8568585;07072 9139929;;Rottenburger Str. 14;Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Astrid Denk;Astrid;Denk;d.nolden@yahoo.de;2;16.02.23;1;16.02.;2 Eieranteile am 17.4. eingetragen, immer noch altes Konto;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;Dagmar ;Nolden;017682110112;017623175651;;Astrid Denk;Astrid Denk;Astrid Denk;adenk@gmx.net;;;;;;;0;Depot 0 (WH)\n" +
            "Katharina Diestelmeier;Katharina ;Diestelmeier;kdiestel@gmx.net;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;070717781321;;Erlenweg 2;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Yannic Sören Drabesch;Yannic Sören ;Drabesch;soeren.drabesch@ifg.uni-tuebingen.de;;;0;;;;0;;3 (Speicher);1;;0;;31.03.25;0;31.03.;;;017689233934;;;Steinbößstraße 45;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Theresa Dressler;Theresa ;Dressler;theresadressler@web.de;1;01.05.24;0;01.05.;;;0;;0 (WH);1;01.04.24;0;01.04.;;0;;Theresa ;Dreßler ;015155653722;015155653722;;Haußerstraße 140;Tübingen ;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Christiane Emmerich;Christiane;Emmerich;cemmerich@web.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;01747895997;;Bohnenbergerstr. 29;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Anna Engel;Anna ;Engel;anna.engel@posteo.de;;;0;;;;0;;3 (Speicher);1;09.02.21;4;09.02.;;0;;Anna;Engel;0176-61117934;0176-61117934;;Kelternstraße 10;Tübingen;72070;noemi.spano@posteo.de;giulia.luttenberger@gmx.at;;;;;;0;Depot 3 (S)\n" +
            "Helmut Enslin;Helmut;Enslin ;helmutenslin@web.de;2;27.07.23;1;27.07.;auf neuem Konto überwiesen;;0;;0 (WH);1;01.04.23;1;01.04.;;0;;;;01637182393;0707144427;;Eduard-Spranger-Straße 98;Tübingen Wanne;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Martín Fink;Martín ;Fink;leonie.rosenbauer@web.de;;;0;;;;0;;3 (Speicher);1;01.04.18;6;01.04.;31.03.25;0;31.03.;Leonie;Rosenbauer;0179-2842241;0177-788 6664;;Mallestr 17;Tübingen;72072;m.f.ink@mtmedia.org;;;;;;;0;Depot 3 (S)\n" +
            "Lennart Fischer;Lennart;Fischer;lennartfischer01@yahoo.de;;;0;;;;0;;3 (Speicher);1;01.05.23;1;01.05.;;0;;Lennart;Fischer;;015902248905;;Metzgergasse 8;Tübingen;72070;lennartfischer01@yahoo.de;;;;;;;0;Depot 3 (S)\n" +
            "Andreas und Nadja Flad;\"Andreas und Nadja\t\t\";Flad;nadja.wisniewski@gmx.de;;;0;;;;0;;2 (Der.Wa);1;01.04.20;4;01.04.;;0;;;;;01638908132;;\"Marienstraße 14\t\t\t\t\";Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Doppelt Flick;Doppelt;Flick;;;;0;;;;0;;3 (Speicher);;;0;;;0;;;;;;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Lena Flik;Lena;Flik;lena.flik@web.de;1;03.03.23;1;03.03.;immer noch altes Konto;;0;;3 (Speicher);2;01.04.18;6;01.04.;;0;;Anna;Igel;017661176502;017661176502;;Albrechtstr. 34;Tübingen ;72072;anna.igel@gmail.com;moritz.igel@gmail.com;;;;;;0;Depot 3 (S)\n" +
            "Jule Fohrer;Jule ;Fohrer;jule.fohrer@posteo.de;;;0;;;;0;;0 (WH);1;01.10.22;2;01.10.;;0;;;;;;;;;;;;;;;;;0;Depot 0 (WH)\n" +
            "Stefan Fortunat;Stefan;Fortunat;sfortunat@yahoo.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;;;;0173 4937556;;;;;sebastianblaes@gmail.com;;;;;;;0;Depot 3 (S)\n" +
            "Sabine Frantz;Sabine;Frantz;sfrantz@online.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;Flora;Frantz;0176 578 848 29;07071/40355;;Berliner Ring 45;Tübingen;72076;ffrantz@online.de;;;;;;;0;Depot 0 (WH), Solawikreis (SK)\n" +
            "Manuela Franz;Manuela;Franz;umeko@mailbox.org;;;0;;;;0;;5 (Rott);1;01.04.24;0;01.04.;;0;;;;;01708554366;;Paradeisstraße 8;Rottenburg;72108;;;;;;;;0;Depot 5 (R)\n" +
            "Joscha Friedmann;Joscha  ;Friedmann;joschafriedmann@gmail.com;;;0;;;;0;;3 (Speicher);1;;0;;;0;;Anja;Stevenson;0176-23598834;;;Derendinger Straße 73;Tübingen;72072;;;;;;;;0;Depot 3 (S)\n" +
            "Christine Fröhlich;Christine;Fröhlich;tine.j.bus@gmail.com;;;0;;;;0;;0 (WH);1;01.04.22;2;01.04.;31.03.25;0;31.03.;Christine;Fröhlich;;01773875573;;Philipp von Heck Straße 1;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Nora Fröhlich;Nora;Fröhlich;nora.froehlich@posteo.de;;;0;;;;0;;3 (Speicher);1;01.11.24;0;01.11.;;0;;;;;;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Kristin Funcke;Kristin;Funcke;kristinfuncke@hotmail.com;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;;;;01603870927;;Lilli-Zapf-Str. 3;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Jonathan Gaukesbrink;Jonathan ;Gaukesbrink;jonathang99@gmx.de;;;0;;;;0;;3 (Speicher);1;13.02.21;4;13.02.;;0;;Marc;Rosenfelder;;;;Kastellweg 18/1;Tübingen;72072;marcenfelder@gmail.com;;;;;;;0;Depot 3 (S)\n" +
            "Christian & Antonia Geibel & Schnell;Christian & Antonia;Geibel & Schnell;christiangeibel@aol.com;;;0;;;;0;;3 (Speicher);1;01.04.21;3;01.04.;;0;;;;0178-8900022;;;Marktgasse 14;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Susann Geisler;Susann;Geisler;su-zan@live.de;;;0;;;;0;;0 (WH);1;01.04.23;1;01.04.;;0;;;;;07071856123;;Mistralweg 2;Tübingen;72072 ;;;;;;;;0;Depot 0 (WH)\n" +
            "Heike Gerlach;Heike;Gerlach;heike.gerlach@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;;;01788018192;;;Raichbergstraße 38A;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Harald u Michaela Gerstberger;Harald u Michaela ;Gerstberger;harald.gerstberger@web.de;;;0;;;;0;;5 (Rott);1;27.02.23;1;27.02.;;0;;Sarah ;Gerstberger;015784195867;0747242497;;Wittenberger Str. 55;Rottenburg;72108 ;michaela.gerstberger@web.de;sarah.gerstberger@web.de;;;;;;0;Depot 5 (R)\n" +
            "Miriam Gerstberger;Miriam;Gerstberger;miriam.gerstberger@posteo.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;Lilo;Brisslinger;0157-87932377;;;Roßbergstraße 34;Tübingen;72072;filo.miafilo@gmail.com;lilo.brisslinger@posteo.de;;;;;;0;Depot 3 (S)\n" +
            "Andrej Girod;Andrej;Girod;andrejgirod@gmx.de;;;0;;;;0;;3 (Speicher);1;01.04.24;0;01.04.;;0;;Britta;Lasch;;+491723986041;;Sofienstraße ;Tübingen;72070;britta-lasch@t-online.de;;;;;;;0;Depot 3 (S)\n" +
            "Björn und Anna Gorder;Björn und Anna ;Gorder;agoerder@posteo.de;1;07.06.24;0;07.06.;;;0;;0 (WH);1;01.07.23;1;01.07.;;0;;;;;07071 5666602;;Eschenweg 30;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Mila Gorecki;Mila;Gorecki;milasophia@googlemail.com;;;0;;;;0;;0 (WH);1;05.02.21;4;05.02.;;0;;Kornelius ;Raeth;;;;;;;;;;;;;;0;Depot 0 (WH)\n" +
            "Kathrin Görz;Kathrin;Görz ;kathringoerz@yahoo.de;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;;;;017621790472;;Paul Dietz-Straße 6 ;Tübingen ;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Arko Graf-Burk;Arko;Graf-Burk;arkograf01@gmail.com;;;0;;;;0;;3 (Speicher);1;01.04.23;1;01.04.;;0;;;;;015735187980;;Im Winkelrain 29;Tübingen;72076;;;;;;;;0;Depot 3 (S)\n" +
            "Anaim Gräff;Anaim;Gräff;anaim@posteo.de;;;0;;;;0;;;1;;0;;;0;;;;;;;;;;;;;;;;;0;Koordinatoren (KD)\n" +
            "Klaus Gräff;Klaus;Gräff;klaus@posteo.de;;;0;;;;0;;;1;;0;;;0;;;;;;;;;;;;;;;;;0;Koordinatoren (KD), Solawikreis (SK)\n" +
            "Doppelt Greiner;Doppelt;Greiner;;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;31.03.25;0;31.03.;;;;070719914404;;Hainbuchenweg 25;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Judith und Samuel Greiner;Judith und Samuel;Greiner;sammy@posteo.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;070719914404;;Hainbuchenweg 25;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Julika Grimm;Julika;Grimm;jul.martin@gmx.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;2 (Der.Wa);1;;0;;;0;;;;015737850821;;;Lilli-Zapf-Str 15;Tübingen ;72072;charlotte.quest@yahoo.de;;;;;;;0;Depot 2 (WS)\n" +
            "Alina Grobel;Alina ;Grobel;alina.grobel@web.de;;;0;;;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;;;015789596782;;;Moltkestraße 15;Tübingen;72072;alina.grobel@web.de;;;;;;;0;Depot 3 (S)\n" +
            "Christine Haaser;Christine ;Haaser;christine.haaser@rechtsanwaltskanzlei-rottenburg.de;;;0;;;;0;;5 (Rott);1;01.11.18;6;01.11.;;0;;;;;0160-94463617;;;;;goelz@steuerberatung-goelz.de;;;;;;;0;Depot 5 (R)\n" +
            "Verena Haberer;Verena;Haberer;v.haberer@web.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;3 (Speicher);1;;0;;;0;;Andreas;Lachner;017699277852;070715498384;;Friedrich-Dannenmann-Straße 18/2;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Cornelia Hack;Cornelia;Hack;corneliahack@posteo.de;1;17.03.23;1;17.03.;immer noch altes Konto;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;Ralf;Wenzel ;;07071-5668798;;Hechingerstrasse 20;Tübingen;72072 ;j.lerchenmueller@yahoo.com;sabine_besenfelder@yahoo.com;ralfwenzel@posteo.de;;;;;0;Depot 3 (S)\n" +
            "Paula Hack;Paula;Hack;paula.hack@gmx.de;;;0;;;;0;;3 (Speicher);1;26.04.24;0;26.04.;;0;;Hack, Elhaus, Lenz, Schniewind;Pia Elhaus;;015223080532;;Hallstattstraße 7;Tübingen;72070;pia.elhaus@posteo.de;;;;;;;0;Depot 3 (S)\n" +
            "Mona Haddada;Mona ;Haddada;mona.haddada@web.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;;;+491607632503;;;Schwabstraße 6 ;Tübingen ;72074;dsdiederich@posteo.org;;;;;;;0;Depot 3 (S)\n" +
            "Anna-Lena Hajosch;Anna-Lena;Hajosch;annahajosch@gmail.com;1;02.03.23;1;02.03.;immer noch altes Konto;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;;;;0151-17615968;;Reutlingerstr 35;Tübingen;72072;mawi@posteo.de;;;;;;;0;Depot 3 (S)\n" +
            "Sarah Hänsch;Sarah;Hänsch;sarah.haensch@studio-diagonal.de;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;;;;017632992240;;Heinlenstrasse  27;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Klaus Hantke;Klaus;Hantke;hantke@uni-tuebingen.de;;;0;;;;0;;0 (WH);1;01.02.19;6;01.02.;;0;;Angelika;Hantke;;07071-640964;;Beckmannweg 7/1;Tübingen;72076 ;;;;;;;;0;Depot 0 (WH)\n" +
            "Zita Hartel;Zita ;Hartel;zita.hartel@yahoo.de;;;0;;;;0;;0 (WH);1;;0;;;0;;;;;0157-87625597;;;;;;;;;;;;0;Depot 0 (WH)\n" +
            "Conrad Hartter;Conrad;Hartter ;hartter@t-online.de;;;0;;;;0;;3 (Speicher);1;26.04.24;0;26.04.;;0;;;;;07071-552515;;Pfalzhaldenweg 13;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Andrea Hauff;Andrea;Hauff;Andrea.hauff@web.de;;;0;;;;0;;0 (WH);1;01.04.23;1;01.04.;;0;;;;015152261738;070717709452;;Aischbachstrasse 26;Tübingen ;72070;;;;;;;;0;Depot 0 (WH)\n" +
            "Sylvia Haug;Sylvia;Haug;g_haug@web.de;;;0;;;;0;;1 (Der.Ka);2;01.04.18;6;01.04.;;0;;;;0157 84504801;;;Kanalstr. 9;Tübingen;72072;sylvia.haug@gmx.de;;;;;;;0;Depot 1 (K), Depotverantwortliche (DV)\n" +
            "Doppelt Haug1;Doppelt;Haug1;;;;0;;;;0;;1 (Der.Ka);;01.04.18;6;01.04.;;0;;;;;;;Kanalstr. 9;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Martin Haußmann;Martin;Haußmann;amhaus@posteo.de;1;01.05.24;0;01.05.;;;0;;0 (WH);1;01.04.24;0;01.04.;;0;;Martin;Haußmann;015126875064;015126875064;;Liststraße 21;Tübingen;72074;;;;;;;;0;Depot 0 (WH)\n" +
            "Julius Heinzmann;Julius;Heinzmann;daniela.dannert@gmx.de;;;0;;;;0;;0 (WH);1;01.11.21;3;01.11.;;0;;Dannert;Daniela;0173-163 0878 ;;;Beethovenweg 20;Tübingen ;72076;julius.heinzmann@gmail.com;;;;;;;0;Depot 0 (WH)\n" +
            "Anna Helle;Anna ;Helle;anna.helle@gmx.de;;;0;;;;0;;3 (Speicher);1;01.11.24;0;01.11.;;0;;;;0157-56185489;015756185489;;Hölderlinstraße 17;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Julia Hellmig;Julia;Hellmig;juliahe03@gmail.com;;19.02.24;0;19.02.;;28.07.24;0;28.07.;3 (Speicher);1;;0;;;0;;Julia;Hellmig;01624692574;01624692574;;Belthlestr. 4;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Johann-Martin Hempel;Johann-Martin;Hempel;prozesse@posteo.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;Claudia;Burghart;01738101556;01738101556;;Niethammerstr. 8;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Lisa Henke;Lisa;Henke;lisa.zachrich@posteo.de;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;Tobias;Zachrich;;01778235627;;Saibenstraße 3;Tübingen;72072;Magdalenahelm@hotmail.com;;;;;;;0;Depot 1 (K)\n" +
            "Agnes Henschen;Agnes ;Henschen;agnes_henschen@web.de;1;05.04.24;0;05.04.;;;0;;2 (Der.Wa);1;01.10.19;5;01.10.;;0;;;;;+4917622621008;;Hechinger Strasse 40;Tübingen;72072;wiebke.mollik@gmail.com;kathrin.singer@posteo.de;;;;;;0;Depot 2 (WS)\n" +
            "Laura Celina & Matthias Hermenau & Gather;Laura Celina & Matthias;Hermenau & Gather;Laura.non.ce@web.de;;;0;;;;0;;1 (Der.Ka);1;01.04.18;6;01.04.;;0;;;;;0176 83 02 0118;;Heinlenstr. 4;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Miriam Heusel;Miriam;Heusel;miriamheusel@gmx.de;;31.03.23;1;31.03.;Eieranteil Ende August 23 beendet;27.08.23;1;27.08.;3 (Speicher);1;;0;;;0;;;;;;;Mathildenstraße 27;Tübingen ;72072;michael@nikosami.de;mgihr@gmx.de;;;;;;0;Depot 3 (S)\n" +
            "H. Heyworth;H.  ;Heyworth ;moritz@talktalk.net;2;28.07.23;1;28.07.;auf neuem Konto überwiesen;;0;;3 (Speicher);1;;0;;;0;;M.;Kaiser;;017647778620;;Moerikestr. 7;Tuebingen;72076;;;;;;;;0;Depot 3 (S)\n" +
            "Theda Himpel;Theda ;Himpel;michahimpel@web.de;;;0;;;;0;;0 (WH);1;01.04.19;5;01.04.;;0;;Micha;Himpel;0171-1604413;;;Weizsäckerstr 7;Tübingen;72074;;;;;;;;0;Depot 0 (WH)\n" +
            "Lars Hofmann;Lars;Hofmann;kristina.lieres@gmail.com;1;07.06.24;0;07.06.;;;0;;3 (Speicher);1;01.04.24;0;01.04.;;0;;Kristina ;von Lieres;;015784938994;;Schleifmühleweg 57;Tübingen;72070;julia.hoevelmann@posteo.de;lars_hofmann@icloud.com;kristina.lieres@googlemail.com;jilschwender@gmx.de;;;;0;Depot 3 (S)\n" +
            "Birgit Hoinle;Birgit;Hoinle;bhoinle@gmx.net;1;02.03.23;1;02.03.;auf neuem Konto überwiesen;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;D;Albi;01772764010;07071/5667980;;Autenriethstraße 15;Tübingen;72072;albi@mtmedia.org;;;;;;;0;Depot 2 (WS)\n" +
            "Karin Homberger;Karin;Homberger;a-kolb@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;Anne;Kolb;;;;17.9.2017;;;homkarin1954@gmail.com;;;;;;;0;Depot 2 (WS)\n" +
            "Luisa Huber;Luisa;Huber;huber-luisa@gmx.net;;;0;;;;0;;2 (Der.Wa);1;01.04.20;4;01.04.;;0;;;;;01577-7764866;;Schleifmühleweg 32;Tübingen;72070;julia5.maeder@gmail.com;huber-luisa@gmx.net;;;;;;0;Depot 2 (WS)\n" +
            "Hedwig Huster;Hedwig;Huster;hedwig.huster@web.de;;;0;;;;0;;0 (WH);1;13.03.24;0;13.03.;;0;;;;;01605555757;;Haußerstraße 84;Tübingen;72076 ;;;;;;;;0;Depot 0 (WH)\n" +
            "Swen Idahl;\"Swen \t\t\t\";Idahl;Swen.idahl@web.de;;;0;;;;0;;1 (Der.Ka);1;01.04.20;4;01.04.;;0;;;;015755440788;070719986868;;Sieben Höfe str 67-1; Tübingen ;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Antonia Indlekofer;Antonia;Indlekofer;toni@indlekofer-klettgau.de;;;0;;;;0;;3 (Speicher);1;01.11.22;2;01.11.;;0;;;;+491625291525;;;Hölderlinstr. 17;Tübingen;72074;sarahfodor@posteo.de;linda.indlekofer@web.de;;;;;;0;Depot 3 (S)\n" +
            "Hans Peter und Gabi Kaiser;Hans Peter und Gabi ;Kaiser;kaiser-peter@t-online.de;;;0;;;;0;;4 (Dussl);1;01.11.20;4;01.11.;;0;;;;;;;Mühlstr. 26;Ofterdingen;72131 ;;;;;;;;0;Depot 4 (DS)\n" +
            "Patrick Kaiser;Patrick;Kaiser;patrickkaiser@posteo.de;;;0;;;;0;;;1;;0;;;0;;;;;;;;;;;;;;;;;0;Fördermitglieder\n" +
            "Dorothee Kapp;Dorothee ;Kapp;dorothee.kapp@posteo.de;;;0;;;;0;;3 (Speicher);1;09.08.24;0;09.08.;;0;;;;;015755584271;;Haaggasse 27;Tübingen ;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Anneke Karthe;Anneke;Karthe;anneke.karthe@zaehlwerk.de;2;30.01.24;1;30.01.;Anzahl der Eier wurde am 30.1.24 geändert ;;0;;3 (Speicher);1;01.04.22;2;01.04.;;0;;;;;01774830944;;Galgenbergstraße 24;Tübingen;72072;Lisa.Goldbach@gmx.de;;;;;;;0;Depot 3 (S)\n" +
            "Michael Kaschek;Michael;Kaschek;michael.kaschek@web.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;015224008083;;Beethovenweg 16;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Jana Kielbassa;Jana;Kielbassa;Janakielbassa@web.de;;;0;;;;0;;3 (Speicher);1;01.11.24;0;01.11.;;0;;;;01639782423;;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Hanna Kiepke;Hanna;Kiepke;hanna.kiepke@gmx.net;1;02.09.24;0;02.09.;Wir möchten einen Eieranteil ab September.;;0;;0 (WH);1;01.07.24;0;01.07.;;0;;Hanna;Kiepke;;017626577389;;Im Rotbad 34;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Melanie Kirch;Melanie ;Kirch;melli.kirch@gmail.com;;;0;;;;0;;3 (Speicher);1;;0;;;0;;Nora u. Melanie;Kirch + Streit;0157 56551352;0157 56551352;;Doblerstraße 6;Tübingen;72074;norastreit7@gmail.com;;;;;;;0;Depot 3 (S)\n" +
            "Hermann Kley;Hermann;Kley;obstsolawi@posteo.de;;;0;;;;0;;0 (WH);1;01.04.13;11;01.04.;;0;;;;;;;;;;;;;;;;;0;Depot 0 (WH)\n" +
            "Paula Klusmann;Paula;Klusmann;paula-klussmann@web.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;;;0178-3174408 ;;;ReutlingerStr.35;Tübingen;72072;;;;;;;;0;Depot 3 (S)\n" +
            "Thomas Knöller;Thomas;Knöller;magdalena.knoeller@posteo.de;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;Magdalena;Knöller;;017660971281;;Primus-Truber-Straße 48;Tübingen-Derendingen;72072;t.knoeller@posteo.de;;;;;;;0;Depot 1 (K)\n" +
            "Yvonne Knorre;Yvonne ;Knorre;vonnitzschi@gmail.com;;;0;;;;0;;4 (Dussl);1;01.04.20;4;01.04.;;0;;;;;digitaler Vertrag am 28.3.;;Mühlackerstr. 19 ;Dußlingen ;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Daniel und Rahel Kocher;Daniel und Rahel ;Kocher;RahelKocher@gmx.de;;;0;;;;0;;4 (Dussl);1;01.11.20;4;01.11.;;0;;;;07072/1262070 ;;;;;;danielkocher@gmx.de;;;;;;;0;Depot 4 (DS)\n" +
            "Doppelt Konold;Doppelt;Konold;;;;0;;;;0;;;;;0;;;0;;;;;;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Silke Konold;Silke;Konold;silke@konold.de;;;0;;;;0;;3 (Speicher);2;01.04.24;0;01.04.;31.03.25;0;31.03.;Silke;Konold;01633917214;070715667908;;Wächterstraße;Tübingen;72074;martin@konold.de;;;;;;;0;Depot 3 (S)\n" +
            "Lena Korn;Lena;Korn;lena.korn@posteo.de;;;0;;;;0;;3 (Speicher);1;01.02.25;0;01.02.;;0;;;;0157 88876074 ;;;Collegiumsgasse 4;Tübingen;72070 ;;;;;;;;0;Depot 3 (S)\n" +
            "Hale Kosaloglu;Hale ;Kosaloglu ;halekosaloglu@icloud.com;;;0;;;;0;;;1;01.04.24;0;01.04.;;0;;;;;01731793657;;Hanna- Bernheim Str 22;Tübingen ;72072;;;;;;;;0;Depot 6 (NEU)\n" +
            "Stephanie Krahn;Stephanie;Krahn;stkrahn@web.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;015773933424;;Niethammerstraße4;Tübingen;;;;;;;;;0;Depot 0 (WH)\n" +
            "Rosa Kramp;Rosa;Kramp;rosa.kramp@web.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;;;0176-95843780;;;Geissweg 21;Tübingen;72076;;;;;;;;0;Depot 3 (S)\n" +
            "Kilian Krebs;Kilian ;Krebs;kilian.krebs@yahoo.de;2;16.02.23;1;16.02.;2 Eieranteile am 03.05. eingetragen, auf neuem Konto überwiesen;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;;;;0176-23178306;;;;;Manuela.thukral@posteo.de;;;;;;;0;Depot 3 (S)\n" +
            "Iris Kronenbitter;Iris;Kronenbitter;kronenbitter-bga@gmx.net;1;02.03.23;1;02.03.;immer noch altes Konto;;0;;1 (Der.Ka);1;01.04.18;6;01.04.;;0;;;;;;;;;;;;;;;;;0;Depot 1 (K)\n" +
            "Marco Krüger;Marco;Krüger;marco.krueger@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.11.20;4;01.11.;;0;;;;0163 4560411;;;Galgenbergstraße 34;Tübingen ;72072;bmvdb@aol.com;;;;;;;0;Depot 2 (WS)\n" +
            "Jonas Kübler;Jonas;Kübler;jmk010624@gmail.com;1;20.03.23;1;20.03.;auf neuem Konto überwiesen;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;015737308054;015737308054;;Hartmeyerstraße 66;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Doppelt Kuhn u. Bell;Doppelt;Kuhn u. Bell;;;;0;;;;0;;;;;0;;;0;;;;;;;;;;;;;;;;;0;Depot 2 (WS)\n" +
            "Torsten u. Patricia (WG) Kuhn u. Bell;Torsten u. Patricia (WG);Kuhn u. Bell;patricia.bell@posteo.de;;;0;;;;0;;2 (Der.Wa);2;;0;;;0;;Amelie ;Moedlinger;;0157/79588891;;Obere Dorfstraße 24 / Hegelstrasse 7;Rottenburg Wendelsheim;72108;wagnerjuli@posteo.de;;;;;;;0;Depot 2 (WS)\n" +
            "Anett Kuntz;Anett;Kuntz;anett.kuntz@gmx.de;;;0;;;;0;;5 (Rott);1;01.04.18;6;01.04.;;0;;Doris;Deppe;0151/56324237;07472/9379427;;Kantstraße 41;Rottenburg;72108;garten-art-deppe@gmx.de;;;;;;;0;Depot 5 (R)\n" +
            "Lukas Ladurner;Lukas;Ladurner;mimamela@posteo.de;;;0;;;;0;;4 (Dussl);1;09.02.21;4;09.02.;;0;;Lena;Vester;;;;Pulvermühle 5;Dußlingen;72144;Lena.vester@posteo.de;Johannes-soellner@web.de;;;;;;0;Depot 4 (DS)\n" +
            "Ulrich u Andra Lambrecht;Ulrich u Andra ;Lambrecht;andralambrecht@yahoo.de;;;0;;;;0;;0 (WH);1;01.12.23;1;01.12.;;0;;;;015771430823;07071640155;;Raichbergstrasse 72;Tübingen;72072;ulrichlambrecht@yahoo.de;;;;;;;0;Depot 0 (WH)\n" +
            "Immanuel und Mareike Lang;Immanuel und Mareike;Lang;mareike.web@web.de;;;0;;;;0;;0 (WH);1;01.06.21;3;01.06.;;0;;Immanuel;;0176-57713042 ;;;Brombergstraße 14;Tübingen;72070;;;;;;;;0;Depot 0 (WH)\n" +
            "Martin Lang;Martin;Lang;martin_lang@posteo.net;;;0;;;;0;;0 (WH);1;19.04.24;0;19.04.;;0;;;;;017670001930;;Philosophenweg 45;Tübingen ;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Saskia + Jürgen Lange;Saskia + Jürgen;Lange;buffy-67@gmx.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;1 (Der.Ka);1;01.04.18;6;01.04.;;0;;;;;0176 23456846;;Rutenweg 6;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Ursula Lansche;Ursula;Lansche;urslansche@gmail.com;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;Philipp Wolfgang;;;01707868091;;Weisdornweg 14/268;;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Anna Katharina Lehmann;Anna Katharina;Lehmann;katharina_lehmann@yahoo.de;1;18.10.24;0;18.10.;ich würde gerne ab dem 18.10. 6 Eier in der Woche bestellen. Muss ich dann den ganzen Monat, also 15,86 € zahlen? Wenn ja dann erst ab November.? wenn;;0;;0 (WH);1;01.04.19;5;01.04.;31.05.24;0;31.05.;Katharina ;Lehmann;07073-832434;;;Alemannenweg 10;Tübingen;72070;;;;;;;;0;Depot 0 (WH)\n" +
            "Marius Lemm;Marius;Lemm;mariusclemm@gmail.com;;;0;;;;0;;3 (Speicher);1;01.04.24;0;01.04.;;0;;Kelsey;Houston-Edwards;01713303214;01713303214;;Hauffstraße 8;Tübingen;72074;kelsey.houston.edwards@gmail.com;;;;;;;0;Depot 3 (S)\n" +
            "Ruomin Li;Ruomin;Li;ruomin.li2121@gmail.com;;;0;;;;0;;3 (Speicher);1;01.04.23;1;01.04.;;0;;;;;01749254598;;Schwabstraße 6;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Ilona Liesche;Ilona;Liesche;Ilona.Liesche@gmx.net;;;0;;;;0;;3 (Speicher);1;01.11.24;0;01.11.;31.03.25;0;31.03.;;;;015786240473;;Brunnenstraße 12/1;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Jan-Timo Liewald;Jan-Timo;Liewald ;timo.liewald@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;;;;01715841476;;Holzwiesenstraße 6;Kusterdingen;72127;Kristin.Ewald@gmx.de;;;;;;;0;Depot 2 (WS)\n" +
            "Andreas Linder;Andreas;Linder ;inger.einfeldt@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.24;0;01.04.;;0;;Inger;Einfeldt;;07071360865;;Provenceweg 3;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Pierre Lorenz;Pierre;Lorenz;Pierre.Lorenz@rwth-aachen.de;;;0;;;;0;;4 (Dussl);1;21.02.21;3;21.02.;31.03.25;0;31.03.;Dominique;Stärr;;;;Austraße 55;Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Lorenz Löwe;Lorenz;Löwe;l_loewe@hotmail.de;;;0;;;;0;;2 (Der.Wa);1;01.04.23;1;01.04.;;0;;;;;017697911919;;Primus-Truber-Str. 10;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Janosch Ludwig;Janosch;Ludwig;ja-ludwig@posteo.de;;;0;;;;0;;3 (Speicher);1;01.02.22;3;01.02.;;0;;;;015156116643;015156116643;;Mathildenstraße 27;Tübingen;72070 ;julia-bremser@t-online.de;;;;;;;0;Depot 3 (S)\n" +
            "Rainer Mack;Rainer;Mack;rainer.mack1@gmx.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;0 (WH);1;01.04.19;5;01.04.;;0;;Sibylle ;Rothm.;;;;;;;;;;;;;;0;Depot 0 (WH)\n" +
            "Lydia Maidl;Lydia;Maidl ;lydia2023@t-online.de;;;0;;;;0;;;1;;0;;;0;;;;;;;;;;;;;;;;;0;Solawikreis (SK)\n" +
            "Hanspeter Mallot;Hanspeter;Mallot ;baerbel.foesemallot@web.de;;;0;;;;0;;0 (WH);1;01.04.23;1;01.04.;;0;;Barbara;Foese-Mallot;;+49707167432;;Stäudach 35;Tübingen;72074;;;;;;;;0;Depot 0 (WH)\n" +
            "Friederike Manegold;Friederike ;Manegold;rikejo2@gmail.com;;;0;;;;0;;3 (Speicher);1;01.04.24;0;01.04.;;0;;;;;;;Gottlob Braeuning Straße ;Tübingen ;72072;jm.mane@t-online.de;;;;;;;0;Depot 3 (S)\n" +
            "Laura Mannan;\"Laura\t\";Mannan;laura.mannan@gmx.de;;;0;;;;0;;2 (Der.Wa);1;01.04.20;4;01.04.;;0;;;;015118919612;;;\"Lembergstraße 31/1\t\t\t\t\";Tübingen;\"72072\t\";hannahkaelber@web.de;;;;;;;0;Depot 2 (WS)\n" +
            "Timo Manz;Timo;Manz;nora.zeller@gmx.de;;;0;;;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;Nora;Zeller;;;;;;;timo.manz89@gmail.com;;;;;;;0;Depot 3 (S)\n" +
            "Ralph Maurer;Ralph;Maurer;heike.u.maurer@gmail.com;1;24.03.23;1;24.03.;immer noch altes Konto einzeln;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;Heike;Maurer;015154405891;;;Vöchtingstrasse 27;Tübingen;72076;heike.u.maurer@gmail.com;;;;;;;0;Depot 0 (WH)\n" +
            "Regina Corinna Mayer;Regina Corinna;Mayer;raphael.mayer@student.uni-tuebingen.de;3;23.02.23;1;23.02.;auf neuem Konto überwiesen;;0;;0 (WH);1;01.09.21;3;01.09.;;0;;Raphael;Mayer;+49 15732536007;;;;;;regina-markwardt@web.de;kathi.naumann@t-online.de;davwoj@gmx.de;;;;;0;Depot 0 (WH)\n" +
            "Tamara Merk;Tamara;Merk;tamara-merk@gmx.de;;;0;;;;0;;5 (Rott);1;01.04.24;0;01.04.;;0;;;;;;;;;;;;;;;;;0;Depot 5 (R)\n" +
            "Lilli Metelmann;Lilli;Metelmann;Lilli.Metelmann@web.de;;;0;;;;0;;3 (Speicher);1;06.02.21;4;06.02.;;0;;Lilli;Metelmann;01729587202;01729587202;;Mühlstr. 6;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Lion Michelberger;Lion;Michelberger;lion.michelberger@t-online.de;;;0;;;;0;;3 (Speicher);1;01.04.24;0;01.04.;;0;;;;;01729299106;;Kreuzstraße 15;Tübingen;72074;lion.michelberger@student.uni-tuebingen.de;;;;;;;0;Depot 3 (S)\n" +
            "Jana Moehrle;Jana;Moehrle;max-julius.meier@web.de;;;0;;;;0;;3 (Speicher);1;05.01.21;4;05.01.;;0;;Ida;Steinacker;01577947078;015779470787;;Brühlstraße 4;Ammerbuch;72119;idasteinacker@posteo.de;jana@kuenstle-moehrle.de;nicolas-friedrich@web.de;;;;;0;Depot 3 (S)\n" +
            "Claudia Mosebach;Claudia;Mosebach;Cla.mosebach@gmail.com;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;;;;015759553402;;Raichbergstraße;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Jeanette Müller;Jeanette;Müller;jeanettemuller@hotmail.co.uk;;;0;;;;0;;0 (WH);1;01.04.24;0;01.04.;;0;;;;;015732597626;;Hainbuchenweg 1;Tübingen ;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Sandra Müller;Sandra;Müller;rosmarinsgarden@posteo.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;31.03.25;0;31.03.;;;;01766256620;;Weißdornweg 14;Tübingen;;;;;;;;;0;Depot 0 (WH)\n" +
            "Carsten und Christine Müller-Czepull;Carsten und Christine ;Müller-Czepull;mueller.christine@gmx.de;1;24.03.23;1;24.03.;immer noch altes Konto;;0;;3 (Speicher);1;09.02.21;4;09.02.;;0;;;;0178-5567835;01785567835;;Hirschaue Straße 33;72070;Tübingen;;;;;;;;0;Depot 3 (S)\n" +
            "Maria Müller-Lindenlauf;Maria;Müller-Lindenlauf;Maria.Mueller-Lindenlauf@hfwu.de;;;0;;;;0;;1 (Der.Ka);1;01.04.18;6;01.04.;;0;;Uwe  und  Gertrud ;Liebe-Harkort und Scheuberth;;07071-72329;;;;;uwe@liebe-harkort.com;;;;;;;0;Depot 1 (K)\n" +
            "Doppelt Münze 13;Doppelt;Münze 13;marie.graef@posteo.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;;;;;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Münze 13 Münze 13 e.V.;Münze 13;Münze 13 e.V.;muenze13@riseup.net;1;23.02.23;1;23.02.;auf neuem Konto überwiesen;;0;;3 (Speicher);2;01.01.20;5;01.01.;;0;;Münze 13;;;;;;;;umruteg@gmail.com;;;;noah.vohrer@mtmedia.org;lena.bode@posteo.de;;0;Depot 3 (S)\n" +
            "Johanna Neuffer;Johanna;Neuffer;johanna.neuffer@boden-web.de;;;0;;;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;;;0178 32 13 130;;;;;;;;;;;;;0;Depot 3 (S)\n" +
            "Alexander Neuger;Alexander;Neuger;alexander-neuger@web.de;;;0;;;;0;;2 (Der.Wa);1;;0;;;0;;Ulrike;Österle;;;;;;;ulrike.oesterle@web.de;;;;;;;0;Depot 2 (WS)\n" +
            "Matthias Niedenfuehr;Matthias;Niedenfuehr;valerie.niedenfuehr@gmail.com;2;04.03.23;1;04.03.;auf neuem Konto überwiesen;;0;;3 (Speicher);1;;0;;;0;;Valerie;;017670853178;070718599567;;Hundskapfklinge 28;Tübingen;72074;matthias.niedenfuehr@gmx.de;;;;;;;0;Depot 3 (S)\n" +
            "Marlis Niesen;Marlis;Niesen;marlisniessen@gmx.com;;;0;;;;0;;2 (Der.Wa);1;08.04.24;0;08.04.;31.03.25;0;31.03.;;;;07071-76812;;Sieben-Höfe-Str. 85/1;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Alexander Nolte;Alexander;Nolte;info@speicher-tuebingen.de;;;0;;;;0;;3 (Speicher);1;01.07.24;0;01.07.;;0;;;;;015753711269;;Beim Nonnenhaus 3;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Beate Nüssle;Beate;Nüssle;bea.nuessle@gmail.com;;;0;;;;0;;3 (Speicher);1;01.11.24;0;01.11.;;0;;;;;00 49 173 1601600;;Rappenberghalde 51/6;Tübingen;72070;feemembarth@web.de;mmoehrle@t-online.de;nils.nuessle@hotmail.com;;;;;0;Depot 3 (S)\n" +
            "Sabine Ocker;Sabine ;Ocker;elena.buehrle@gmx.net;;;0;;;;0;;1 (Der.Ka);1;01.04.20;4;01.04.;31.01.25;0;31.01.;Elena;Bührle;01777167018;;;Eisenhutstraße 66;72072 ;Tübingen;;;;;;;;0;Depot 1 (K)\n" +
            "Sebastian Ohlert;Sebastian ;Ohlert ;leosibeth@web.de;;;0;;;;0;;3 (Speicher);1;19.04.24;0;19.04.;;0;;Leonore;Sibeth;;017657791526;;;Tübingen;72072;;;;;;;;0;Depot 3 (S)\n" +
            "Andreas Ott;Andreas;Ott;andreas.ott1@web.de;;;0;;;;0;;2 (Der.Wa);1;01.04.23;1;01.04.;;0;;;;;0177 83 84 740;;Lammstraße ;Tübingen - Weilheim;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Francesca Panter;Francesca;Panter;franca.panter@gmail.com;;;0;;;;0;;3 (Speicher);1;24.01.25;0;24.01.;;0;;;;;015755746864;;Lange Gasse 22;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Pia Pfandzelter;Pia;Pfandzelter;pfandzelter@web.de;;;0;;;;0;;5 (Rott);1;01.04.19;5;01.04.;;0;;;;0176-81306258;;;Kornbergblick 2;Rottenburg ;72108;;;;;;;;0;Depot 5 (R)\n" +
            "Elisabeth Pothig;Elisabeth;Pothig;elisabethpothig@hotmail.com;;;0;;;;0;;2 (Der.Wa);1;01.04.23;1;01.04.;;0;;;;;01578 952 6827;;Hirschstrasse 12;Kusterdingen-Jettenburg;72127 ;;;;;;;;0;Depot 2 (WS)\n" +
            "Sigrun Preißing;Sigrun;Preißing;sigrun.preissing@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;Gottfried;Schubert;;07071/33119;;Provenceweg 3;Tübingen;72072;kramer.sabine@gmx.net;gottfried.schubert@posteo.de;;;;;;0;Depot 2 (WS)\n" +
            "Armin Rabsteyn;Armin;Rabsteyn;lea.prokop@googlemail.com;;;0;;;;0;;0 (WH);1;01.04.22;2;01.04.;;0;;Lea;Prokop;;;;;;;Armin.rabsteyn@googlemail.com;;;;;;;0;Depot 0 (WH)\n" +
            "Martha Raidt;Martha;Raidt;martha.raidt95@web.de;;;0;;;;0;;;;;0;;;0;;Johannes;Schroth;0157-74482063;;;;;;johannes.schroth@neckartalhof.de;;;;;;;0;Solawikreis (SK)\n" +
            "Michaela Rapp;Michaela;Rapp;michaela.kehrer@gmx.de;;;0;;;;0;;2 (Der.Wa);1;01.04.19;5;01.04.;;0;;;;;;;;;;;;;;;;;0;Depot 2 (WS)\n" +
            "Fabian Raßmann;Fabian ;Raßmann;fabian.rassmann@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.10.23;1;01.10.;;0;;;;;0178-2176058;;Roßbergstraße 31 ;Tübingen;72072 ;;;;;;;;0;Depot 2 (WS)\n" +
            "Jonathan Remppis;\"Jonathan\t\";Remppis;tobias.maurer@gmx.de;;;0;;;;0;;1 (Der.Ka);1;;0;;;0;;;;;;;;;;;;;;;;;0;Depot 1 (K)\n" +
            "Joely Resch;Joely;Resch;j.resch@zweileben.de;2;10.03.23;1;10.03.;auf neuem Konto überwiesen, Eieranteil Konteneingänge?;;0;;5 (Rott);1;01.12.19;5;01.12.;;0;;;;017664250218;;;Bodelschwinghweg 9;Rottenburg am Neckar;72108;;;;;;;;0;Depot 5 (R)\n" +
            "Merle Reuter;Merle;Reuter;merle.reuter@posteo.de;1;23.02.23;1;23.02.;immer noch altes Konto;;0;;5 (Rott);1;11.11.17;7;11.11.;;0;;;;;;;;;;;;;;;;;0;Depot 5 (R)\n" +
            "Natalie Richardt;Natalie;Richardt;Natalie.richardt@posteo.de;;;0;;;;0;;3 (Speicher);1;01.04.22;2;01.04.;;0;;Daniel;Richardt;;015228767552;;Bangertweg 23/1 ;Tübingen ;72070;Natalie.richardt@posteo.de;daniel.richardt@posteo.de;;;;;;0;Depot 3 (S)\n" +
            "Lukas Rieg;Lukas;Rieg;lukas.rieg@gmx.de;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;;;;015114449041;;Sieben-Höfe-Straße 64;Derendingen ;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Michael Ries;Michael;Ries;mic_ries@gmx.de;;;0;;;;0;;2 (Der.Wa);1;01.04.21;3;01.04.;;0;;Sebastian;Beblawy;01737887533;;;Provenceweg 8;Tübingen;72072;sbeblawy@gmx.de;;;;;;;0;Depot 2 (WS)\n" +
            "Pia Rimmele;Pia;Rimmele;piarimmele@yahoo.de;;;0;;;;0;;3 (Speicher);1;16.01.25;0;16.01.;;0;;;;;0176 50989527;;Sigwartstr. 15;Tübingen;72076;;;;;;;;0;Depot 3 (S)\n" +
            "Karl Ringger;Karl;Ringger;edringger@gmail.com;;;0;;;;0;;;1;;0;;;0;;;;;01635190213;;Friedrich-Zundel-Str ;Tübingen;72074;;;;;;;;0;Depot 6 (NEU)\n" +
            "Michael Roemer;\"Michael\t\";Roemer;mroemer@posteo.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;3 (Speicher);1;01.04.20;4;01.04.;;0;;Lara ;\"Ditrich\t\";;;;;;;l.ditrich@gmx.de;;;;;;;0;Depot 3 (S)\n" +
            "Wg Rothenburg;Wg;Rothenburg;post@tuebinger-rothenburg.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;Wg Rothenburg;Wg Rothenburg;;;;Schloßbergstraße 23;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Heinrich Ruoff;Heinrich;Ruoff ;rohrbacher-ruoff@gmx.de;;;0;;;;0;;2 (Der.Wa);1;01.04.23;1;01.04.;;0;;Elisabeth;Rohrbacher;;0176 61797717;;Magazinplatz 5 ;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Christina Rupsch;Christina ;Rupsch;crupsch@web.de;;;0;;;;0;;3 (Speicher);1;01.04.23;1;01.04.;;0;;;;;017670121872;;Dahlienweg 3;Tübingen;72072;;;;;;;;0;Depot 3 (S)\n" +
            "Barbara Saliger;Barbara;Saliger;bsaliger@yahoo.de;1;02.03.23;1;02.03.;;;0;;0 (WH);1;01.10.22;2;01.10.;;0;;Reinhild;Barkey;;;;;;;rbarkey@arcor.de;;;;;;;0;Depot 0 (WH)\n" +
            "Clara Sartingen;Clara;Sartingen;C.sartingen@gmx.de;;;0;;;;0;;4 (Dussl);1;01.04.22;2;01.04.;31.03.25;0;31.03.;;;;01789830742;;Ebertstraße 54;Tübingen;72072;;;;;;;;0;Depot 4 (DS)\n" +
            "Wolfgang u Roswitha Sautter;Wolfgang u Roswitha ;Sautter;Roswitha.sautter@outlook.de;;;0;;;;0;;4 (Dussl);1;01.12.20;4;01.12.;;0;;;;;;;;;;;;;;;;;0;Depot 4 (DS)\n" +
            "Thomas u. Antje Sautter-Strelczuk;Thomas u. Antje;Sautter-Strelczuk;Sautter-Strelczuk@t-online.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;070733591;;Badgasse 11;Ammerbuch-Entringen ;72119;;;;;;;;0;Depot 0 (WH)\n" +
            "Clara Sax;Clara;Sax;Clara@mtmedia.org;;;0;;;;0;;2 (Der.Wa);1;;0;;31.03.25;0;31.03.;;;;015780928927;;Amselweg 11;Tübingen;72076;;;;;;;;0;Depot 2 (WS)\n" +
            "Doris Schaller-Hauber;Doris;Schaller-Hauber;d.schaller-hauber@gmx.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;Doris ;Schaller-Hauber;;07071-40676;;Torstraße 21;;72070;;;;;;;;0;Depot 0 (WH), Solawikreis (SK)\n" +
            "Frank Schelling;Frank ;Schelling;frank.schelling@solar-energiekonzepte.de;;;0;;;;0;;4 (Dussl);1;01.12.19;5;01.12.;;0;;;;;0 74 73 – 27 31 00;www.solar-energiekonzepte.d;;;;info@solar-energiekonzepte.de;;;;;;;0;Depot 4 (DS)\n" +
            "Patrick Schenk;Patrick;Schenk;pm.schenk@web.de;;;0;;;;0;;3 (Speicher);1;21.03.24;0;21.03.;;0;;M. ;Venegas;;01728354082;;Pfrondorfer Straße 6/1;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Björn Scherer;Björn;Scherer ;bjoern.scherer@posteo.de;;;0;;;;0;;1 (Der.Ka);1;01.04.19;5;01.04.;;0;;Sol Maria; Sena Pritsch;;01793727922;;Hechinger str. 46;Tübingen;72072;;;;;;;;0;Depot 1 (K), Depotverantwortliche (DV)\n" +
            "Patrick Schirm;Patrick;Schirm;patrick.schirm@my-acc.org;;;0;;;;0;;0 (WH);1;01.01.24;1;01.01.;;0;;Dagmar ;Blind;0157-85960670;01590-3897168;;Im Rotbad;Tübingen;72076;dagmarlanius@web.de;;;;;;;0;Depot 0 (WH)\n" +
            "Brunhild Schmid;Brunhild ;Schmid;email@brunhild-schmid.de;1;23.02.23;1;23.02.;auf neuem Konto überwiesen;;0;;3 (Speicher);1;04.01.21;4;04.01.;;0;;;;0174-7871821;07071-400678;;Galgenbergstr. 22;Tübingen;72072;;;;;;;;0;Depot 3 (S), Solawikreis (SK)\n" +
            "Ann-Kathrin Schmidt;Ann-Kathrin ;Schmidt;akathrinbauer@gmail.com;;;0;;;;0;;2 (Der.Wa);1;;0;;;0;;;;0178 1325278;;;;;;;;;;;;;0;Depot 2 (WS)\n" +
            "Caroline Schmidt;Caroline;Schmidt;caroline.schmidt@uni-tuebingen.de;;;0;;;;0;;0 (WH);1;13.04.23;1;13.04.;31.03.25;0;31.03.;;;;017683108543;;Pfrondorfer Straße 9;Tübingen;72074;;;;;;;;0;Depot 0 (WH)\n" +
            "Florian Schmidt;Florian;Schmidt;schmidt@alpha-structure.com;1;16.02.23;1;16.02.;immer noch altes Konto;;0;;0 (WH);1;;0;;;0;;Eva-Maria;Kropp;;;;;;;evamaria.kropp@gmail.com;mr.schmidt@gmx.net;;;;;;0;Depot 0 (WH)\n" +
            "Thomas Schmidt;Thomas;Schmidt;tho.schmidt96@web.de;;;0;;;;0;;3 (Speicher);1;01.11.21;3;01.11.;;0;;Thomas ;Schmidt;0157 71558344;;;Kelternstraße 11;Tübingen;72070;niklas.best@gmx.de;bevermilan@gmail.com;;Sophi.rel@gmx.de;;;;0;Depot 3 (S)\n" +
            "Anna Schnapper;Anna ;Schnapper;michael.schnapper@gmail.com;;12.03.23;1;12.03.;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;Michael;Schnapper;017663873832;017663873832;;Schwabstr. 24;Tübingen;72074;schnapper.anna@gmail.com;jensenschnapper@gmail.com;;;;;;0;Depot 3 (S)\n" +
            "Beate Schnetzke;Beate;Schnetzke;beateschnetzke@gmx.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;Gudrun;Wamsler-Lutz;;015122205572;;Im Schönblick 48;Tübingen;72076;gwamsler-lutz@web.de;;;;;;;0;Depot 0 (WH)\n" +
            "Sarah und Kristof Scholl;Sarah und Kristof;Scholl;kristof.scholl@googlemail.com;;;0;;;;0;;4 (Dussl);1;01.11.22;2;01.11.;31.01.25;0;31.01.;;;;;;;;;;;;;;;;0;Depot 4 (DS)\n" +
            "Sebastian und Betti Scholl;Sebastian und Betti;Scholl;scholls@scholl-web.de;;;0;;;;0;;4 (Dussl);1;01.10.20;4;01.10.;;0;;;;;;;Niederhofenstrasse 5;Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Carolin Schreiber;Carolin;Schreiber;c.schreiber09@gmx.de;;;0;;;;0;;2 (Der.Wa);1;01.11.24;0;01.11.;;0;;Jana;Meierdierks;01525 3722326;;;Bonlanden 5 ;Tübingen ;72072;Jmeierdierks@web.de;;;;;;;0;Depot 2 (WS)\n" +
            "Sigrun Schreier;Sigrun;Schreier;sigrunschreier@posteo.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;;;0159-02351456;07071-7770222;;Mauerstraße 21;Tübingen ;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Beate Schröder;Beate;Schröder;beasch59@aol.com;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;5 (Rott);1;01.04.18;6;01.04.;;0;;;;;;;;;;johanna.93@web.de;;;;;;;0;Depot 5 (R)\n" +
            "Cornelius Schröder;Cornelius ;Schröder;Cornelius.schroeder@gmail.com;;;0;;;;0;;2 (Der.Wa);1;01.04.20;4;01.04.;;0;;Helena ;Tilmann;015751715105;;;Vischerstraße 6;Tübingen;72072;tilmannhelena@gmail.com;;;;;;;0;Depot 2 (WS)\n" +
            "Johannes Schroth;Johannes;Schroth;johannes.schroth@neckartalhof.de;;;0;;;;0;;;1;;0;;;0;;;;;;;;;;;;;;;;;0;Solawikreis (SK)\n" +
            "Fabian Schulz;Fabian ;Schulz ;fabian.schulz@googlemail.com;;;0;;;;0;;;1;01.04.24;0;01.04.;;0;;;;;+4917678034041;;Lange Gasse 27;Tübingen;72070;;;;;;;;0;Depot 6 (NEU)\n" +
            "Peter Schumacher;Peter;Schumacher;pitschumacher@gmx.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;;;0157 51505384;0176 81160394;;Neustadtgasse 1;Tübingen;72070;lisa.marie@posteo.de;pia.rox@web.de;;;;;;0;Depot 3 (S)\n" +
            "Tim Schumacher;Tim ;Schumacher ;jamira@mtmedia.org;;;0;;;;0;;2 (Der.Wa);1;01.04.19;5;01.04.;;0;;Mirjam;Seits;;;;Hechinger Strasse 23;Tübingen;72072;schumacher@mtmedia.org;;;;;;;0;Depot 2 (WS)\n" +
            "Carolin Schwarz;Carolin ;Schwarz;caro.lines@web.de;;;0;;;;0;;3 (Speicher);1;01.04.24;0;01.04.;31.03.25;0;31.03.;;;;015111588809;;Friedrich-Dannenmann-Str. 8;Tübingen;72070;Carolin.Schwarz@posteo.de;;;;;;;0;Depot 3 (S)\n" +
            "Helena Schwarz;Helena ;Schwarz;helena.w@gner-mail.de;;;0;;;;0;;3 (Speicher);1;01.04.19;5;01.04.;;0;;Helena  ;Wagner; ;0151 16961433;;Hasenbühlsteige 3;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Jil Schwender;Jil ;Schwender;;;;0;;;;0;;2 (Der.Wa);1;01.10.19;5;01.10.;31.03.25;0;31.03.;Ignasi;Canales;017676766344;;;Eberhardstrasse 9;Tübingen;72072;ignasi.canales.mundet@gmail.com;;;;;;;0;Depot 2 (WS)\n" +
            "Florian Schwinghammer;Florian;Schwinghammer ;flo.schwinghammer@gmail.com;;;0;;;;0;;5 (Rott);1;01.04.18;6;01.04.;;0;;Florian;Schwinghammer;0163-5369330;;;Weidenäckerweg 5;Nellingsheim;72149;flo.schwinghammer@gmail.com;ingathors@gmail.com;;;;;;0;Depot 5 (R), Depotverantwortliche (DV)\n" +
            "Michael und Katharina Sedding;Michael und Katharina;Sedding ;katharina@sedding.name;;;0;;;;0;;4 (Dussl);1;01.04.18;6;01.04.;;0;;Michael;Sedding;;07072/1260196;;Kastanienweg 10; Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Benjamin und Svenja Seeland;Benjamin und Svenja;Seeland;svenjaseeland@gmx.de;;;0;;;;0;;2 (Der.Wa);1;;0;;;0;;;;017662377873;07071 5495535;;Sieben-Höfe-Straße 112;Tübingen;72072;alexia81@gmx.de;;;;;;;0;Depot 2 (WS)\n" +
            "Julia Seemüller;Julia;Seemüller;seemuellerju@gmail.com;;;0;;;;0;;0 (WH);1;01.04.24;0;01.04.;;0;;;;;+4917645178204;;Otto-Erbe-Weg 52;Tübingen ;72070;;;;;;;;0;Depot 0 (WH)\n" +
            "Petra Seitz;\"Petra\t\";Seitz;petrallala@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.20;4;01.04.;;0;;Christian ;Hädrich;;07071 - 325 90 ;;\"Provenceweg 3\t\t\t\t\";Tübingen;72072;christianhaedrich@gmx.net;paula.r@posteo.de;kathakopp@gmx.de;;;;;0;Depot 2 (WS)\n" +
            "Björn Seufert;Björn;Seufert;bjoernseufert@gmx.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;;;015735184395;07071 7787166;;Stuttgarter Str. 7;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Anette Sidhu-Ingenhoff;Anette;Sidhu-Ingenhoff;sidhu-ingen@t-online.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;015770456371;;Karl-brennenstuhl-strase 9/1;Tübingen;;;;;;;;;0;Depot 0 (WH)\n" +
            "Elke Sindlinger;Elke;Sindlinger;e_sindy@yahoo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.19;5;01.04.;;0;;Lisa;Ginten;;;;;;;lisaginten@posteo.de;;;;;;;0;Depot 2 (WS)\n" +
            "Georg Smolka;Georg;Smolka;georg.smolka@posteo.de;;;0;;;;0;;3 (Speicher);1;23.01.25;0;23.01.;;0;;Lea;Hoppe;;017647756845;;Justinus-Kerner-Straße 29;Tübingen;72070;lea@hoppelorsch.de;;;;;;;0;Depot 3 (S)\n" +
            "Heidi Speidel;Heidi  ;Speidel;h-katharina@web.de;;;0;;;;0;;4 (Dussl);1;05.02.21;4;05.02.;;0;;;;0157-72710069;0157-72710069;;Kappelstrasse 10;Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Michael Speidel;Michael;Speidel;m.t.speidel@gmx.de;;;0;;;;0;;1 (Der.Ka);1;01.04.24;0;01.04.;;0;;;;;0176-32140666;;Im Feuerhägle 1;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Cora Spiegelhauer;Cora;Spiegelhauer;cora.spiegelhauer@gmail.com;;;0;;;;0;;3 (Speicher);1;26.04.24;0;26.04.;;0;;;;;0177 3452843;;Schloßbergstraße 33;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Peter Spraul;Peter;Spraul;peter.spraul@web.de;;;0;;;;0;;;1;;0;;;0;;;;;0163-1731715;;Burgholzweg 87/1;Tübingen;72070;;;;;;;;0;Fördermitglieder\n" +
            "Thilo Sprenger;Thilo ;Sprenger;thilosprenger@yahoo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.24;0;01.04.;;0;;;;;017644742914;;Königsbergerstraße 1;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Jan-Paul Spyra;Jan-Paul;Spyra;Janispyra@gmail.com;1;07.02.25;0;07.02.;;;0;;3 (Speicher);1;16.01.25;0;16.01.;;0;;Jan-Paul;Spyra;017684387271;017684387271;;Haaggasse 10;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Benjamin Steinert;Benjamin;Steinert;benjamin.steinert@bescl.de;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;0 (WH);1;12.02.21;4;12.02.;;0;;;;;015789237027;;Eduard-Spranger-Str. 26;Tübingen ;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Katharina Stelzel;Katharina;Stelzel;katharina.stelzel@gmx.de;;;0;;;;0;;3 (Speicher);1;01.04.22;2;01.04.;;0;;;;0176-98824027;;;Rappstr.;Tübingen;;;;;;;;;0;Depot 3 (S)\n" +
            "Florina Stengl;Florina ;Stengl ;florina-s@gmx.de;;;0;;;;0;;2 (Der.Wa);1;;0;;;0;;;;0157-52176466;;;Provenceweg 1;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Simon Strass;Simon;Strass;benedikte.sandbaek@gmail.com;;;0;;;;0;;3 (Speicher);1;01.04.21;3;01.04.;;0;;Benedikte;Sandbaek;0160 93924895;0160 93924895;;Beim Nonnenhaus 7;Tübingen;72070;simon@ironicbond.de;;;;;;;0;Depot 3 (S)\n" +
            "Yasmin Straub;Yasmin;Straub ;manueldieterich@posteo.de;1;03.05.24;0;03.05.;;;0;;3 (Speicher);1;01.04.18;6;01.04.;;0;;Manuel;Dieterich;0151 26379699;;;Hafengasse;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Christine Streich-Schneider;Christine;Streich-Schneider;christine@streich-schneider.com;;;0;;;;0;;4 (Dussl);1;01.04.23;1;01.04.;31.03.25;0;31.03.;Christine;Streich-Schneider;01637117137;070721264083;www.streich-schneider.com;Steinlachburg 6;Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Oliver Strobel;Oliver ;Strobel ;claudia.strobel@me.com;;;0;;;;0;;;1;;0;;;0;;;;;017683050060;;Ebertstraße 8;Tübingen ;72072;;;;;;;;0;Depot 6 (NEU)\n" +
            "Samantha Strohmenger;Samantha ;Strohmenger;Samanthastrohmenger@posteo.de;;;0;;;;0;;0 (WH);1;20.01.25;0;20.01.;;0;;;;;015128208821;;Wilonstraße 76;Tübingen ;72072 ;;;;;;;;0;Depot 0 (WH)\n" +
            "Axel Sumey;Axel;Sumey;axel@sumey.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;Lydia ;Maidl ;017624921166;070715685780;;Rappenberghalde 33;Tübingen;72074;lydia2023@t-online.de;;;;;;;0;Depot 3 (S), Solawikreis (SK)\n" +
            "Eva Tegeler;Eva;Tegeler ;eva.tegeler@web.de;;;0;;;;0;;3 (Speicher);1;01.04.23;1;01.04.;;0;;;;;015226808901;;Hausserstrasse;Tübingen;72076;;;;;;;;0;Depot 3 (S)\n" +
            "Harald Thelen;Harald ;Thelen;Margret.Walcher-Thelen@gmx.de;;;0;;;;0;;4 (Dussl);1;01.12.20;4;01.12.;31.03.25;0;31.03.;Margret;Walcher-Thelen;017680775017;07072-5961;;Lehrgasse 14;Dußlingen;72144;margret.walcher-thelen@gmx.de;;;;;;;0;Depot 4 (DS)\n" +
            "Justus Thoms;Justus;Thoms;justusthoms@web.de;;;0;;;;0;;3 (Speicher);1;24.02.21;3;24.02.;;0;;Katharina ;Becker;+4915161483938;015736207391;;Hölderlinstraße 21;Tübingen;72074;katarinabecker@googlemail.com;;;;;;;0;Depot 3 (S)\n" +
            "Elisabeth Tielsch-Staiger;Elisabeth ;Tielsch-Staiger;etielsch@web.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;;;;07071 64250;;Stauffenbergstr.24;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Jeannine Tischler;Jeannine;Tischler ;nine82@gmx.net;1;16.02.23;1;16.02.;auf neuem Konto überwiesen;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;Markus;Bender;;0176-20390734;;Galgenbergstraße 70;Tübingen;72072;markus.alexander.bender@gmail.com;jeannine.tischler@posteo.de;;;;;;0;Depot 2 (WS)\n" +
            "Anke Tolzin;Anke;Tolzin;anketolzin@gmx.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;erhard;glueck;;070716058959;;Kreuzberg 19;Tübingen;72070;erhard.glueck@gmx.de;;;;;;;0;Depot 0 (WH)\n" +
            "Franziska Trauner;Franziska;Trauner ;ftrauner@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;Sergej;Krohmer;015788172182;;;Waldstraße 21/1;72072;Tübingen;krohmers@googlemail.com;;;;;;;0;Depot 2 (WS)\n" +
            "Sarah Trenz;Sarah ;Trenz;sarah-maria_trenz@t-online.de;;;0;;;;0;;0 (WH);1;01.04.19;5;01.04.;;0;;;;0174-1725573;;;Torbogenweg 13;Tübingen;72070;;;;;;;;0;Depot 0 (WH)\n" +
            "Bernd Ulbrich;Bernd;Ulbrich;berulb@mailbox.org;;;0;;;;0;;0 (WH);1;01.05.23;1;01.05.;;0;;;;;01773495414;;Weiherhaldenstrasse 25;Tübingen ;72074;z.dobruca@gmx.de;;;;;;;0;Depot 0 (WH)\n" +
            "Jörg Umrath-Rebasoo;Jörg;Umrath-Rebasoo;bl.um@web.de;;;0;;;;0;;0 (WH);1;01.07.23;1;01.07.;;0;;;;;017099106114;;Berliner Ring 59;Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Günter und Nadine Vahlkampf f;Günter und Nadine ;Vahlkampf f;nskornicka@gmx.de;;;0;;;;0;;4 (Dussl);1;01.11.20;4;01.11.;;0;;;;01639148078;074739579160;;Lilienweg 4;Mössingen;72116;;;;;;;;0;Depot 4 (DS)\n" +
            "Tschano Vecsey;Tschano;Vecsey;t.mac.vecsey@gmx.net;;;0;;;;0;;1 (Der.Ka);1;01.07.23;1;01.07.;;0;;;;07071 7789173;01621 789558;;Derendingerstr. 105;Tübingen;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Felix Votteler;Felix;Votteler ;felix.votteler@posteo.de;;;0;;;;0;;2 (Der.Wa);1;01.04.18;6;01.04.;;0;;Ann-Margit;Lesser;017656879776;;;Huberstrasse 16;;;ann.lesser@posteo.de;;;;;;;0;Depot 2 (WS)\n" +
            "Anja und Thomas Wagner;Anja und Thomas ;Wagner ;tho.max@gmx.de;;;0;;;;0;;5 (Rott);1;;0;;;0;;;;;0170 1870730;;Neckarhalde 14;Rottenburg;72108;;;;;;;;0;Depot 5 (R)\n" +
            "Antonia Wagner;Antonia;Wagner ;aczitzmann@googlemail.com;;;0;;;19.04.24;0;19.04.;0 (WH);1;19.04.24;0;19.04.;31.03.25;0;31.03.;;;;017631258005;;Stäudach 87;Tübingen;72074;;;;;;;;0;Depot 0 (WH)\n" +
            "Philipp Wagner;Philipp;Wagner ;prw@duck.com;1;26.01.24;1;26.01.;;;0;;3 (Speicher);1;01.04.23;1;01.04.;;0;;Maria;Sutcliffe-Hetman;;015773654159;;Burgholzweg 134;Tübingen;72070;maria.hetman@gmail.com;philipp_wagner@gmx.de;;;;;;0;Depot 3 (S)\n" +
            "Pascal Yannick Waibel;Pascal Yannick ;Waibel ;anna.pha@web.de;;;0;;;;0;;0 (WH);1;01.10.20;4;01.10.;;0;;Anat-Hod;Phahal;0162-5779908 (Passi);0177-2176707 (Hod);;Albrecht-Dürer strasse 23;Tübingen;72076;pascal.waibel@posteo.de;;;;;;;0;Depot 0 (WH)\n" +
            "Gabi Waldmann;Gabi;Waldmann;gabifwaldmann@gmail.com;;;0;;;;0;;0 (WH);1;16.01.25;0;16.01.;;0;;;;0162 4676283;07071 7606077;;Falkenweg 30;Tübingen;72076;leonardo.waldmann@gmx.de;;;;;;;0;Depot 0 (WH)\n" +
            "Bernadett Watzke-Denzel;Bernadett;Watzke-Denzel;bernadett.watzke@gmx.de;;;0;;;;0;;1 (Der.Ka);1;01.04.18;6;01.04.;;0;;Wolfram;Denzel;;07071-7704443;;Heinlenstr. 49;Tübingen;72072;wolfram.denzel@gmx.de;;;;;;;0;Depot 1 (K), Solawikreis (SK)\n" +
            "Vanessa Weihgold;Vanessa ;Weihgold;v.weihgold@posteo.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;;;;0157-71410299;;Eugen-Bolz-Str. 4;Tübingen;72072;dhimpele@gmx.de;;;;;;;0;Depot 3 (S)\n" +
            "Anke und Gerhard Weischer-Kip;Anke und Gerhard ;Weischer-Kip;anke.weischer-kip@outlook.de;;;0;;;;0;;0 (WH);1;01.04.18;6;01.04.;;0;;;;;0157 - 89 24 25 63;;;;;gerhard.kip@t-online.de;;;;;;;0;Depot 0 (WH)\n" +
            "Michael Wenzler;Michael;Wenzler;michael.wenzler@posteo.de;;;0;;;;0;;3 (Speicher);1;01.04.24;0;01.04.;31.03.25;0;31.03.;;;;017660843529;;Lange Gasse 27;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Matthias Wernet;Matthias;Wernet;matthias-wernet@gmx.de;;;0;;;;0;;3 (Speicher);1;01.04.20;4;01.04.;;0;;;;01737399443;;;Melanchthonstraße 38 ;Tübingen ;72074;jojo.herden@googlemail.com;;elisabeth.wernet@gmx.de;;;;;0;Depot 3 (S)\n" +
            "Jasmin Westenberger;Jasmin;Westenberger;jasmin.westenberger@posteo.de;;16.02.23;1;16.02.;;;0;;3 (Speicher);1;01.04.22;2;01.04.;;0;;;;015784049878;015784049878;;Gösstr. 29;Tübingen;72070;;;;;;;;0;Depot 3 (S)\n" +
            "Nadja Wettering;Nadja;Wettering;nadja.wettering@posteo.de;;;0;;;;0;;3 (Speicher);1;01.04.24;0;01.04.;31.03.25;0;31.03.;;;;017677613637;;Schützenweg 4;Kusterdingen ;72127;;;;;;;;0;Depot 3 (S)\n" +
            "Lara Wicke;Lara ;Wicke;lara.wicke@posteo.de;;;0;;;;0;;3 (Speicher);1;;0;;;0;;;;0176-23644954;004917623644954;;Hallstattstraße 24;Tübingen ;72070;Tinarenno@posteo.de;;;;;;;0;Depot 3 (S)\n" +
            "Lara Sabrina Wicke;Lara Sabrina;Wicke;Stein.lu@web.de;;;0;;;;0;;3 (Speicher);1;01.02.24;1;01.02.;;0;;Lukas ;Stein;;0176 84058944;;Aixerstr. 27;Tübingen;72072;Tinarenno@posteo.de;;;;;;;0;Depot 3 (S)\n" +
            "Daniela Wiedmaier;Daniela;Wiedmaier;Marie-caroline@bachmair.info;;;0;;;;0;;2 (Der.Wa);1;23.04.21;3;23.04.;;0;;Marie-Caroline;Bachmair;;;;; Rtbg-Kiebingen;;Marie-caroline@bachmair.Info;;;;;;;0;Depot 2 (WS)\n" +
            "Peter und Sara Wilhelm;Peter und Sara;Wilhelm;familie.heinzelmannwilhelm@gmail.com;;;0;;;;0;;3 (Speicher);1;01.04.23;1;01.04.;31.03.25;0;31.03.;;Heinzelmann-;;01796802132;;Stauffenbergstrasse 72;Tübingen;72074;;;;;;;;0;Depot 3 (S)\n" +
            "Falko Winkelmann-Huber;Falko;Winkelmann-Huber ;familyhuber@gmx.de;;;0;;;;0;;0 (WH);1;01.04.20;4;01.04.;;0;;Huber;\"Barbara \t\";;;;\"Haydnweg 25 \t\t\t\";Tübingen;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Alexandra Winter;Alexandra ;Winter;winter_kevin@web.de;;;0;;;;0;;2 (Der.Wa);1;01.11.22;2;01.11.;;0;;Kevin ;;015756350057;015756350057;;;Tübingen;72072;;;;;;;;0;Depot 2 (WS)\n" +
            "Eckart Wizemann;Eckart;Wizemann;eckartwizemann@aol.com;;;0;;;;0;;;;;0;;;0;;;;0178-7568273; 07071/600242;;;;;mhw18@gmx.de;;;;;;;0;Erzeuger (TÜ), Solawikreis (SK)\n" +
            "Benjamin Wolf;Benjamin;Wolf;wolf_benjamin@gmx.de;;;0;;;;0;;4 (Dussl);1;01.04.19;5;01.04.;;0;;;;;07072-1261204;;Birkenstraße 11;Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Burglind Wolter;Burglind ;Wolter;burglind.wolter@web.de;;;0;;;;0;;0 (WH);1;01.04.20;4;01.04.;31.03.25;0;31.03.;;;;070719949760;;Hainbuchenweg 3;Tübingen;72076;wolternadine@web.de;gordon.wolter@web.de;;;;;;0;Depot 0 (WH)\n" +
            "Gordon und Nadine Wolter;Gordon und Nadine ;Wolter;gordon.wolter@web.de;;;0;;;;0;;0 (WH);1;01.07.19;5;01.07.;31.03.24;0;31.03.;;;0170-1108957;;;Berliner Ring 39;Tübingen;72076;wolternadine@web.de;;;;;;;0;Solawikreis (SK)\n" +
            "Sibylla Wolter;Sibylla;Wolter;sibyllaw@gmx.de;;;0;;;;0;;0 (WH);1;01.05.20;4;01.05.;;0;;Sibylla;Wolter;0176-23764144;;;Wennfelder Garten 3;;Tübingen;;;;;;;;0;Depot 0 (WH)\n" +
            "Desiree Yandemir;Desiree;Yandemir;d.yandemir@gmail.com;;;0;;;;0;;1 (Der.Ka);1;17.03.24;0;17.03.;31.03.25;0;31.03.;Desiree;Yandemir;;;;Lange Furche 31;Tübingen ;72072;;;;;;;;0;Depot 1 (K)\n" +
            "Dagmar Ziegler;Dagmar;Ziegler;jodeltanzglueck@gmx.de;;;0;;;;0;;3 (Speicher);1;08.10.21;3;08.10.;;0;;Milena;Baitinger;0176 344 123 55;07071 549 69 79;;;;;milena_baitinger@yahoo.de;;;;;;;0;Depot 3 (S), Solawikreis (SK)\n" +
            "Karsten und Theresia Ziegs;Karsten und Theresia;Ziegs;ziegs@posteo.de;;;0;;;;0;;0 (WH);1;;0;;31.03.25;0;31.03.;;;01632726835;;;Schwalbenweg 42;Tübingen ;72076;;;;;;;;0;Depot 0 (WH)\n" +
            "Eva-Maria Zimmermann;Eva-Maria;Zimmermann;ez@jc-opm.de;;;0;;;;0;;4 (Dussl);1;01.02.21;4;01.02.;;0;;;;;07072 - 85 09;;Robert-Wörner-Str.12;Dußlingen;72144;;;;;;;;0;Depot 4 (DS)\n" +
            "Heinz Dieter Zürn;Heinz Dieter ;Zürn;Inge.zuern@web.de;;;0;;;;0;;4 (Dussl);1;01.12.20;4;01.12.;;0;;Inge;Zürn;01637896468;070726448;;Uffhofenstr;Dusslingen;72144;inge@solawi-tuebingen.de;;;;;;;0;Depot 4 (DS)\n"
}