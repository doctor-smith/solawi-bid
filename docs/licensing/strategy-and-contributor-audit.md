# Lizenzstrategie und Contributor-Abgrenzung – Solawi Bid

**Stand:** 2026-11-12 · **Status:** technische Vorprüfung / Entscheidungsvorlage, **keine Freigabe zur Umlizenzierung**  
**Referenz:** [`doctor-smith/ai-command#15`](https://github.com/doctor-smith/ai-command/issues/15)  
**Untersuchter Stand:** `doctor-smith/solawi-bid` `main` @ `8d8038ab6f9a8236b421441294d2801b0858a264`  
**Historischer Vergleich:** `Joystick01/solawi-bid` @ `4f34a3b164ac070aaf154c83e27d889a37626c15`; gemeinsamer Vorfahr `81c3f4d3777b9640e485f6540142561a0c7c39d7`.

> **Keine Rechtsberatung.** Die Einordnung von Bearbeitungen, Schutzfähigkeit, Lizenzkompatibilität und Vertragsumfang muss vor einer Veröffentlichung rechtlich bestätigt werden. `git blame`, Commit-Autoren und Textähnlichkeiten belegen die Entstehungsgeschichte, nicht abschließend die Rechtsinhaberschaft oder Schutzfähigkeit.

## 1. Ziel und Lizenzentscheidung

**Produktziel:** nicht-kommerzielle Quellcode-Nutzung zulassen und kommerzielle Nutzung auf gesonderte Vereinbarungen stützen.

**Befund:** Die gegenwärtige [`LICENSE`](../../LICENSE) enthält die vollständigen Apache-2.0-Bedingungen *und* verlangt eine zusätzliche kommerzielle Lizenz. Apache 2.0 gestattet grundsätzlich auch kommerzielle Nutzung. Diese Aussagen sind widersprüchlich; das Dokument soll nicht als verlässlich durchsetzbare Noncommercial-Schranke behandelt werden.

**Bevorzugtes Zielmodell (noch nicht freigegeben):**

1. Hauptprodukt unter **PolyForm Noncommercial 1.0.0** für neue Veröffentlichungen, sofern die erforderlichen Rechte am gesamten betroffenen Code geklärt sind.
2. **Separate kommerzielle Lizenz** mit eindeutigem Lizenzgeber, Vertragsgegenstand, Nutzungsrechten, Haftung/Support, Vergütung, Laufzeit, Beendigung und Distributionsrechten. Ein Vertriebsangebot bzw. ein `COMMERCIAL-LICENSE.md`-Hinweis ersetzt noch keinen Lizenzvertrag.
3. Einzelne Bestandteile wie E2E-Tests oder Deployment-Automation können **ausdrücklich** bei Apache 2.0 verbleiben. Lizenzgrenzen sind dann pro Verzeichnis/Datei und in der Distribution eindeutig zu dokumentieren.
4. Eingebundene Drittprodukte behalten immer ihre eigenen Lizenzen, unabhängig vom Hauptprodukt.

**Wichtige Grenze der PolyForm-Lizenz:** PolyForm Noncommercial 1.0.0 definiert auch bestimmte Organisationen (u. a. gemeinnützige Organisationen und Bildungseinrichtungen) als erlaubte Nutzer, unabhängig von ihrer Finanzierung. Wer „jede umsatzbezogene Nutzung“ ausschließen will, muss das Ziel mit dem Originalwortlaut abgleichen; PolyForm darf nicht stillschweigend umgedeutet werden. Noncommercial ist außerdem nicht OSI-Open-Source.

Eine neue Lizenz kann bereits wirksam erteilte Apache-2.0-Rechte an früheren Versionen/Beiträgen nicht rückwirkend widerrufen. Die Reichweite bestehender Apache-Rechte und die Rechteketten müssen vor jedem Lizenzwechsel geklärt werden.

## 2. Contributor-Provenienz und Abgrenzung

Historischer Kontext aus Issue #15: Apache-2.0-`LICENSE` ab 31.10.2024, studentische Beiträge im Sommersemester 2025, Zusatz „Commercial License“ erst 30.09.2025; laut Handoff gab es keine gesonderten Teilnahmebedingungen oder Contributor Agreements. Bei absichtlich eingereichten Beiträgen ist Apache 2.0 § 5 relevant. Eine bisherige Apache-2.0-Lizenz allein gibt **nicht** automatisch das Recht, fremde originäre Beiträge als alleinige PolyForm-Lizenz oder unter separat verhandelten proprietären Konditionen neu zu vergeben.

### Vergleichsmethode und Grenzen

GitHub-Fork, gemeinsamer Vorfahr, heutige Blob-SHAs und Dateihistorien wurden abgeglichen. Der genannte Fork unterscheidet sich vom gemeinsamen Vorfahren nur um vier Commits; der aktuelle `main` enthält über 1.000 spätere Commits. **Wichtig:** Bereits vor dem Fork-Abzweig in `main` übernommene Kai-Arbeit ist auch im gemeinsamen Vorfahren enthalten. „Identisch zum gemeinsamen Vorfahren“ ist deswegen *kein* Beleg für fehlende Kai-Urheberschaft.

Die folgenden Zeilenvergleiche verwenden die **Longest Common Subsequence** nichtleerer, getrimmter Zeilen zwischen Fork-Snapshot und `main`. Das ist ein technischer Anhaltspunkt für Textfortbestand, **kein** urheberrechtliches Maß. Kommentare, Standardschlüssel und allgemeine YAML-Syntax können den Wert erhöhen.

| Bereich / Datei | Nachweisbarer technischer Befund | Bewertung für Lizenzklärung |
| --- | --- | --- |
| `.github/actions/push-docker/action.yml` | Fork- und Main-Blob **identisch**; Dateihistorie mit Kay/`kknoepfle-exxeta`, 19.06.2025 | **Offen**: konkrete Inhalte prüfen; ggf. Apache-2.0-Bereich oder Zusatzrechte |
| `.github/workflows/install-server.yml` | Fork- und Main-Blob **identisch**; Dateihistorie mit Kay/`Joystick01`, 17.06.2025 | **Offen**: Deployment-Workflow gesondert abgrenzen |
| `.github/actions/deploy-application/action.yml` | 76 von 92 heutigen nichtleeren Zeilen im Fork in gleicher Reihenfolge vorhanden; historische Kay-Commits | **Offen / priorisiert**: substanzielle Übernahme möglich |
| `infrastructure/ansible/install-server.yml` | 56/60 heutige nichtleere Zeilen überlappen; spätere Änderungen durch Maintainer | **Offen / priorisiert** |
| `infrastructure/ansible/deploy-application.yml` | 119/249 heutige nichtleere Zeilen überlappen; umfangreiche spätere Änderungen durch Maintainer | **Offen**: abschnittsweise Herkunft prüfen |
| `docker-compose.yml` | 54/77 heutige nichtleere Zeilen überlappen; historischer Kay-Commit „localdev revolution“ | **Offen**, viel standardisierte Konfiguration |
| `.github/workflows/CI.yml` | 95/762 heutige nichtleere Zeilen überlappen | Größtenteils umgestaltet; verbleibende eigene Komposition separat prüfen |
| `infrastructure/docker/Dockerfile-Backend` | 6/60 heutige nichtleere Zeilen überlappen; heutiger Mehrstufen-Build umfassend anders | Weitgehend neu umgesetzt; Reststruktur untersuchen |
| `infrastructure/docker/Dockerfile-Frontend` | 8/68 heutige nichtleere Zeilen überlappen | Weitgehend neu umgesetzt |
| `infrastructure/nginx/default.conf` | Alle 11 nichtleeren Fork-Zeilen kommen im heutigen 18-Zeilen-Stand vor | Vor allem technische Konfiguration; ggf. individuell gestaltete Regel prüfen |
| `solawi-bid-frontend/src/jsMain/kotlin/org/solyton/solawi/bid/application/data/env/service.kt` | 36/80 heutige nichtleere Zeilen überlappen; Kay-Commit „enable frontend config injection“ vom 20.06.2025 | **Priorisiert**: konkrete Parsing-/Fallback-Implementierung vs. funktionale Idee trennen |
| `solawi-bid-frontend/src/main/resources/config.json` und `config.json.template` | Fork- und Main-Blobs jeweils **identisch** | Feldnamen, Datenformat und Fetch-Konzept sind nicht automatisch geschützt; die Herkunft trotzdem dokumentieren |
| `solawi-bid-backend/src/main/resources/application.env.conf` | 37/48 heutige nichtleere Zeilen überlappen | Vor allem Konfigurationswerte; konkrete Gestaltung prüfen |

Die **ursprüngliche Urheberschaft** einzelner Zeilen wurde damit nicht erschöpfend geprüft; insbesondere sind für jede möglicherweise relevante Stelle noch historische Commit-Patches, Autorenbeiträge und etwaige eigenständige Überarbeitungen zu bewerten.

### Contributor-spezifische Entscheidung

**Kai Knöpfle (`Joystick01`, teils `kknoepfle-exxeta`):** Die Annahme „Deployment inzwischen komplett ersetzt“ ist durch den Dateivergleich **nicht** gedeckt. Die neu geschriebenen Dockerfiles vermindern den Umfang, doch einzelne Actions, Workflows und Ansible-Dateien wurden technisch weitgehend erhalten. Die funktionale Idee „Frontend liest `/config.json`“ sowie Schlüssel und Schnittstellen können grundsätzlich unabhängig umgesetzt werden; die konkrete originelle Implementierung darf nicht allein auf Basis dieses Vergleichs frei relizenziert werden. **Empfehlung:** für den erwiesenen substantiellen Rest entweder konkret abgrenzbare Apache-Bereiche ausweisen, unabhängig neu implementieren oder zusätzliche Nutzungsrechte für PolyForm und kommerzielle Lizenzen einholen.

**Dominik Seitz (`ds243HdM`):** Commits am aktuellen `solawi-bid-frontend/src/jsMain/kotlin/org/solyton/solawi/bid/module/bid/component/bid-round-evaluation.kt` vom 31.07./01.08.2025 sind nachweisbar. Derzeit ist dort weiterhin Lets-Plot-Code vorhanden. Die Ablösung durch eine **fachlich und technisch neu entwickelte** Chart.js-Implementierung ist sinnvoll. Die bloße Umstellung der Chart-Bibliothek reicht nicht als Nachweis unabhängiger Neuschöpfung: Diagrammdaten, Komponentenstruktur und individuell gestaltete Darstellungslogik sind separat zu beurteilen. Das übrige, bereits vorher vorhandene Projektmaterial nicht pauschal Dominik zuordnen.

**`aiinhuu`:** Der Ursprung der `e2e/README.md` ist mit einem Commit vom 30.06.2025 belegt; das E2E-Buildskript wurde später vielfach umgebaut. Nach Prüfung konkreter Reste kann ein **separat unter Apache 2.0 verbleibendes E2E-Verzeichnis** der pragmatische Weg sein. Auch dort gilt: klare Lizenzgrenzen und korrekte Autorenhinweise.

### Entscheidungsbaum je Datei

1. **Nur Funktion, Protokoll, übliche Konfiguration oder triviale Standardanordnung erhalten?** Technisch dokumentieren; rechtliche Prüfung, ob überhaupt geschützte Gestaltung übernommen wurde.
2. **Individuell geprägter Code / eine Komposition erkennbar erhalten?** Zusatzrecht einholen, klar unter Apache 2.0 lassen **oder** technisch unabhängig ersetzen.
3. **Vollständig unabhängiger Ersatz?** Im PR nachvollziehbar machen, welche konkreten alten Abschnitte entfielen; die Umsetzung nicht lediglich kosmetisch umschreiben.
4. **Unklare Historie?** Nicht zur PolyForm-Umlizenzierung freigeben.

Eine Rechtevereinbarung sollte **zusätzliche, weitreichende Nutzungsrechte** (einschließlich Bearbeitung, Weitergabe, Unterlizenzierung und kommerzieller Parallelvergabe) regeln; sie bedeutet nicht, Urheberschaft zu „kaufen“. Zuständiger Rechtsbeistand muss die Formulierung prüfen.

## 3. Release-Gates

**Vor dem Ersetzen von `LICENSE`:**

- [ ] Lizenzziel/erlaubte Nutzungsfälle anhand Originalwortlaut von PolyForm verbindlich festlegen.
- [ ] Herkunft und verbleibende individuelle Anteile je Contributor dokumentieren (insbesondere Kai-Dateien und Dominiks Plot-POC).
- [ ] Für jede relevante Contributor-Datei: Zusatzrechte **oder** separat Apache 2.0 **oder** unabhängig ersetzter Code, mit Nachweis.
- [ ] Definieren, ob SaaS-only, Docker-Images, JARs und/oder Self-Hosting vertrieben werden; Lizenzhinweise pro Artefakt nachweisen.
- [ ] JDBC-Treiber-Risiko lösen (SMA-606), mit Tests und Build-Artefaktprüfung.
- [ ] Direkte **und** transitive Runtime-/Frontend-/Asset-Abhängigkeiten auditieren und notices vollständig machen.
- [ ] Kommerzielle Vertragsbedingungen und Contributor-Regeln (`CONTRIBUTING.md`) rechtlich prüfen.
- [ ] Die geplante `LICENSE`, alle Verzeichnis-/Datei-Ausnahmen und Notice-Dateien gemeinsam als **separaten Review-PR** freigeben.

**Bis dahin:** kein automatisches Überschreiben oder Umbenennen der aktuellen Lizenz und keine Aussage „kommerzielle Nutzung ist bei allen veröffentlichten Ständen sicher verboten“.

## 4. Quellen

- [Aktuelle `LICENSE`](../../LICENSE)
- [GitHub Issue #15 mit Handoff und historischen Angaben](https://github.com/doctor-smith/ai-command/issues/15)
- [Joysticks Fork-Snapshot](https://github.com/Joystick01/solawi-bid/commit/4f34a3b164ac070aaf154c83e27d889a37626c15)
- [Vergleich Fork-Snapshot gegen aktuelles `main`](https://github.com/doctor-smith/solawi-bid/compare/Joystick01:main...main)
- [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- [PolyForm Noncommercial 1.0.0 – verbindlicher Originaltext](https://polyformproject.org/licenses/noncommercial/1.0.0)
- [Technischer Dependency-Audit](dependency-audit.md)

