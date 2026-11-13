# AI Command — Solyton Application-Wide UI/UX Facelift Exploration

**Jira:** [SMA-607](https://solawi-management.atlassian.net/browse/SMA-607)  
**Status:** Exploration / Konzept / Entscheidungsgrundlage — keine produktive Implementierung  
**Projekt:** Solyton / solawi-bid, Kotlin Multiplatform / Compose Web  
**Sprache der Ergebnisse:** Deutsch

## 0. Mission und Abgrenzung

**Wichtig: Das Facelift betrifft die GESAMTE Anwendung, nicht nur das Dashboard.**

Das bestehende Solyton ist fachlich weit entwickelt, erscheint durch sein uneinheitliches, funktionales und teilweise steriles Frontend aber manchen Anwendern wie ein Prototyp. Gesucht ist ein konsistentes, produktreifes und wiederverwendbares Designsystem samt Navigations- und Migrationskonzept für **sämtliche bestehenden und künftigen Module und Oberflächen**. Das Dashboard ist lediglich der erste visuelle Referenzpunkt und Pilot.

Arbeite **explorativ**: Repository prüfen, Ist-Situation dokumentieren, Interaktions-/Designvarianten abwägen, Architektur entwerfen, Risiken benennen und einen inkrementellen Rollout planen. **Keinen breiten UI-Rewrite durchführen und keine produktiven Änderungen außerhalb des Explorationsberichts.**

## 1. Gewählte Designsprache

**Modern Enterprise × Modern Organic**, mit Erhalt des Solyton-Logos und der Sonnenblume:
- Klare, hochwertige Enterprise-Informationsarchitektur; einheitliche Typografie, Navigation, Iconographie und Interaktionszustände.
- Dunkelgrüne Sidebar, helle cremefarbene Hauptfläche, ruhige weiße Karten, sparsame Sonnenblumengelb-Akzente und warme Sekundärfarben.
- Motivik/Illustrationen sparsam einsetzen: produktives Verwaltungs-/SaaS-System, **keine dekorative Demo**.
- Spacing-Skala, Typografie, Eckenradien, Elevation, Statusfarben und semantische CSS-/Compose-Tokens standardisieren.
- Unverbindliche Farb-Kandidaten: Waldgrün #254C3B; Gelb #E8AE28; Braun #705133; Hintergrund #F8F9F5; Oberfläche #FFFFFF; Text #24312C. Erst CI-Abgleich und Kontrasttests.
- Responsives Layout, Dark-/High-Contrast-Optionen prüfen (nicht voraussetzen), gute Keyboard-Navigation, sichtbarer Fokus, Loading-/Fehler-/Leerzustände.

## 2. Umfang: Gesamte Anwendung auditieren

Inventar der **real existierenden** UI-Flächen und wiederkehrenden Muster erstellen:
- Application Shell, Header, Sidebar, Breadcrumbs, Kontextwechsel, Menüs.
- Dashboard / Startseite und globale persönliche Funktionen.
- Organisationen, Mitglieder/Benutzer, Rollen und Berechtigungen.
- Anteilsmanagement und workflowspezifische Zustandsoberflächen.
- Depotverwaltung, Banking, Auktionen sowie weitere im Repository vorhandene Module.
- Tabellen, Filter, Sortierung, Pagination, Formularfelder, Validierung, Wizards, Dialoge/Modals, Notifications, Tabs, Charts, Uploads und Detailansichten.
- Responsivität und Mobile, Internationalisierung und Status-/Fehlermeldungen.
- Gemeinsame Komponenten in bestehenden Paketen/Source-Sets und funktionale Überschneidungen identifizieren.

Für jede Teilfläche: relevanten Codepfad, heutigen Stilansatz, Wiederverwendbarkeit, UX-Schwäche, Änderungsrisiko und ungefähre Priorität dokumentieren. **Keine Module aus Vermutungen erfinden.**

## 3. Navigationsmodell: Modul × Organisation

Die **Sidebar repräsentiert Module/Funktionen**, der **Organisationsselektor in der Application Shell repräsentiert den Arbeitskontext**.

Zu klären:
- Globaler Kontext vs. 'Alle Organisationen' vs. einzelne Organisation; welcher Kontext für welche Route überhaupt sinnvoll ist.
- Nur autorisierte Module und Organisationen anzeigen. Bestehende Tenant-, Entitlement-, Rollen- und Permission-Regeln nicht umgehen.
- Persönliche und plattformweite Admin-Ansichten getrennt von organisationsgebundenen Ansichten behandeln.
- Verhalten bei Kontextwechsel, Deep Links, Refresh, abgeschnittenen Berechtigungen, leeren Organisationslisten, zuletzt ausgewählter Organisation und mehrstufigen Workflows.
- Kontext darf nicht nur auf Client-Ebene erzwungen werden; Backend bleibt maßgeblich für Autorisierung.
- Matrix-Sicht für Rechte/Module ggf. in der Administration anbieten, nicht als große Standard-Dashboard-Tabelle.

Liefere ein Interaktionsmodell und ein Routing-/State-Konzept mit überprüfbaren Invarianten.

## 4. Dashboard als Pilot, nicht als Gesamtziel

Das bisherige Link-Kachel-Verzeichnis in eine nützliche, berechtigungsabhängige Übersicht verwandeln:
- Eigene Organisationen und Anteile (nur wenn echte Daten/Permissions verfügbar).
- Persönlich relevante Aktivitäten, offene Aufgaben und laufende Prozesse.
- Schnelleinstiege in bestehende Module.
- Optional in späterem Schritt: Solyton-/organisationsbezogene Nachrichten und Neuigkeiten.
- Status-, Empty-, Loading- und Fehlerzustände; keine erfundenen Live-Daten.
- Desktop- und Mobile-Wireframes; Text-/Icon-/Card-Spezifikation.

Die Gestaltung des Dashboards ist **Referenz für ein anwendungsweites Component Design System** und kein isoliertes Spezialdesign.

## 5. Theme- und Layout-Architektur: Community Edition und Enterprise Edition

Untersuche und skizziere eine **editionsfähige, plugin-/erweiterungsfreundliche Architektur**:

### Gemeinsamer offener Kern / Community
- Offene, dokumentierte **Theme API / Theme Engine** mit semantischen Design-Tokens (Farben, Typografie, Größen, Dichten, Oberflächen, Zustände) und begrenzten Layout-Erweiterungspunkten.
- CE wird mit **einem offiziellen Default-Theme** ausgeliefert. Entwickelnde können zusätzliche **Community-Themes und optionale Layout-Presets** nach offener Spezifikation erzeugen und beim Build einbinden; falls zur Laufzeit unterstützt, begründen, wie das sicher funktioniert.
- Offenes Manifest-/Descriptor-Format für Themes (ID, Version, kompatible API-Version, Name, Autor, Lizenz, Tokens, Assets, optional Layout-Preset) mit Validierung.
- Community darf neue Themes/Layout-Presets bauen, ohne proprietäre EE-Implementierung zu benötigen.
- Theming soll visuelle Darstellung variieren, **nicht** Geschäftslogik, Rechteprüfung oder freie Codeausführung im Browser erweitern.
- Unterscheide klar zwischen rein deklarativen Tokens/Layouts und ausführbarem Drittanbieter-Code. Unvertrauenswürdige JS-/HTML-/CSS-Uploads nicht blind zulassen.

### Enterprise (proprietäre, getrennt paketierte Komponenten)
- **Theme Management**: Katalog, Aktivieren/Wechseln, organisations-/tenantbezogene Zuweisung, Persistenz, Rollen/Rechte, Vorschau, ggf. Branding.
- **Premium Theme Packs** und optionale Premium-Layouts in separaten proprietären Modulen/Artefakten.
- EE darf offene Community-Themes konsumieren/kuratieren (je nach deren Lizenz); CE darf keine Laufzeitabhängigkeit auf EE haben.
- Eigene Edition-/Entitlement-Grenzen plus serverseitige Prüfung von verwalteten EE-Funktionen. **Frontend-Assets sind nicht zuverlässig geheimzuhalten**: Premium-Assets nur ausliefern, wenn ihre Distribution vorgesehen und rechtlich passend geregelt ist.
- Rechtliche Zulässigkeit von übernommenen Community-Themes (Lizenzen, Attribution, Contributor-Rechte) explizit prüfen; 'Community Theme in EE übernehmen' bedeutet nicht automatisch proprietär umlizenzieren.

### Zu vergleichende technische Varianten
A. Offene Engine + Theme-Dateien/Design-Tokens; EE verwaltet Themes.  
B. Plug-in-/Manifest-Modell (declarative Themes und vordefinierte Layout-Slots).  
C. Compile-Time-Registry für CE, Runtime-Katalog/Selection für EE.  
D. Source-Set-, Gradle-Modul- oder separate Repository-Trennung für CE/EE und Premium-Themes.

Bewerte Kotlin/Compose-Web-Eignung (CSS Custom Properties vs. Compose-style DSL, komponentenweites Theming, Hot-Swap ohne Reload, i18n, CSP/Sicherheit, SSR soweit relevant, Build-/Bundle-Größe, Versionierung, Zugänglichkeitsanforderungen). **Keine technische Entscheidung ohne Audit und Trade-off-Matrix.**

Skizziere mögliche Modulgrenzen (nur Vorschlag, an reale Struktur anpassen):
- solyton-ui-foundation (Tokens, Komponenten, offene Engine)
- solyton-ui-theme-default (CE Standarddesign)
- solyton-ui-theme-community (frei entwickelte Pakete / externe Repos)
- solyton-ui-theme-enterprise-* (proprietäre Theme-Packs)
- solyton-ui-theme-management-ee (Theme-Switcher, Katalog, Zuweisung)
- solyton-ui-layout-* (sichere, dokumentierte Layout-Presets)

## 6. E2E-Tests, UI-Screenshots und I18N als Pflichtbestandteil

**Jede Migrationswelle muss bestehende fachliche E2E-Workflows und Übersetzungen erhalten.** E2E und I18N dürfen nicht als spätes Aufräumen geplant werden.

### 6.1 E2E und visuelle Baseline
- Bestehende Kotlin/Playwright-E2E-Workflows, Fixtures, Tags, Selektoren, isolierte Testumgebung und kritische Pfade anhand des Codes inventarisieren.
- Die vorhandenen Klick-/Workflow-Anleitungen möglichst als **wiederholbare Browser-Runs** nutzen: Codex/Playwright führt sie mit stabilen Testdaten aus und erzeugt **Screenshots vor dem Facelift** für modul-, seiten- und zustandsbezogene UI-Inventare. Nach jeder Migrationswelle identische Szenarien und **Nachher-Screenshots** erstellen.
- Screenshots versionieren/ablegen, mit definierten Viewports, Locale, Testdaten und stabilen UI-Zuständen; sensible/personenbezogene Daten und Zugangsdaten aus Aufnahmen ausschließen. Tool-/Browserzugriff und nötige Berechtigungen dokumentieren; die Automatisierung muss nicht auf jeder Codex-Umgebung sofort verfügbar sein.
- Alte und neue DOM-Strukturen, Routen, Organisation-/Modulwahl, Modals, Formulare, Responsive-Verhalten, Rechte und Entitlements erfassen. Selektoren bevorzugt semantisch (Role/Accessible Name) bzw. gezielte `data-testid` statt fragilen CSS-Klassen/Texten.
- **Test-Migrationsmatrix**: Fläche/Workflow → existierende E2E-Tests → erforderliche Anpassungen → neue/ergänzte Tests → Baseline-Screenshot → Nachher-Screenshot → Ergebnis.
- Kritische Geschäftsprozesse, Organisationwechsel, direkte Links, Refresh, CE/EE-Zustände, Theme-Auswahl/Fallbacks und Barrierefreiheit berücksichtigen.
- Testausführung ausschließlich mit dafür vorgesehener **isolierter E2E-Umgebung**, nicht gegen Produktivdaten oder die normale Entwicklungsdatenbank.
- Visuelle Regression dort einsetzen, wo sie stabil und wartbar ist; dynamische Inhalte maskieren/normalisieren. Funktionale Assertions sind durch Screenshots nicht ersetzbar.

### 6.2 Internationalisierung (I18N)
- Den tatsächlichen I18N-Stack, unterstützte Sprachen, Ressourcen-/Key-Struktur, Fallbacks, Sprachwechsel und Persistenz im Repository erfassen.
- Hardcodierte Texte, doppelte/verwaiste Keys, unübersetzte Fehler-/Validierungs-/Statusmeldungen und uneinheitliche Benennungen finden; Konsistenz für Module, Sidebar, Organisationen und Dashboard planen.
- Neue Komponenten, leere Zustände, Statusbadges, Dialoge, Theme-Management, Formulare und zugängliche Beschriftungen vollständig lokalisierbar entwerfen.
- Pluralformen, Platzhalter, Datums-/Zahlen-/Währungsformate, Textlängen und responsive Übersetzungsoverflows prüfen; Fallback-Verhalten definieren.
- **Themes bleiben sprachunabhängig** (Tokens/Assets/Layout). Texte und Accessibility-Labels gehören in die Anwendung bzw. in definierte I18N-Ressourcen, nicht fest in Community- oder Premium-Themes.
- Locale-bezogene E2E-Tests für die tatsächlich unterstützten Sprachen sowie Sprachwechsel, Layout und Fehlermeldungen einplanen. Eine größere I18N-Migration nur empfehlen, wenn der Audit sie begründet.

### Definition of Done pro späterer Implementierungswelle
- Betroffene E2E-Tests angepasst und in der isolierten Testumgebung erfolgreich; kritische Workflows unverändert funktionsfähig.
- Relevante Screens mit reproduzierbaren Vorher-/Nachher-Aufnahmen dokumentiert; visuelle Abweichungen bewertet.
- Neue bzw. geänderte Texte für alle unterstützten Sprachen vorhanden und auf Fallback, Accessibility und Überlauf geprüft.
- Testabdeckung, offene Risiken und Rollback in der Welle ausgewiesen.

## 7. Konkrete Ergebnisse

Lege die Exploration als nachvollziehbare Markdown-Dokumentation im Repository ab (z. B. unter .ai/ oder docs/, **bestehende Konvention zuerst prüfen**). Erwartet:

1. **UI-Inventar:** Module, Seiten, Screens und Component-Stile mit Dateireferenzen.
2. **UX-Zielbild:** AppShell, Modul × Organisation, Rechte, Desktop/Mobile und zentrale Workflows.
3. **Designsystem-Spezifikation:** semantische Tokens, Komponentenliste, Zustände, a11y.
4. **Theme-/Edition-Architekturentscheidungsvorlage:** CE/EE-Grenzen, Community-Erweiterungen, Premium-Module, Layout-Plug-ins, Sicherheits-/Lizenz-/Betriebsaspekte.
5. **E2E- und I18N-Migrationsmatrix einschließlich Vorher-/Nachher-Screenshot-Baselines:** Testinventar, Selektoren, Workflows, Sprachressourcen, Locales und QA je Welle.
6. **Migration Roadmap für die gesamte Anwendung:** priorisierte Wellen, zuerst Shell + Dashboard als Pilot, dann gemeinsame Components, danach alle Module schrittweise. Je Welle Abhängigkeiten, Aufwand, Risiken, Tests, Rollback.
7. **Folge-Issues/Tasks als Vorschlag** mit testbaren Akzeptanzkriterien (noch keine neuen Tickets ohne separaten Auftrag).
8. **Offene Fragen** und Entscheidungen, die der Product Owner treffen muss.

## 8. Akzeptanzkriterien für die Exploration

- Nicht nur Dashboard: **vollständiger belegbarer UI-Scope** inklusive Admin-, Fachmodul- und Workflow-Screens.
- Navigation funktioniert im Konzept über Modul × Organisationskontext ohne Sicherheitsregressionen.
- Ein konsistentes Theme- und Component-Modell mit überprüfbaren Kontrast-, Fokus- und Zustandsregeln ist beschrieben.
- Community kann Themen/Preset-Layouts unabhängig von EE entwickeln, EE kann diese nach Lizenzprüfung verwenden.
- Premium-Themes und Theme-Management sind technisch und lizenzseitig **separierbar**, ohne CE zu kompromittieren.
- Klare Unterscheidung von verfügbarer Funktion, Designvorschlag, Zukunftsidee.
- Kleine, realistische Rollout-Schritte statt Big-Bang-Rewrite.
- Jede Welle besitzt einen realistischen E2E- und I18N-Update-/Validierungsplan; Screenshots sind reproduzierbar und datenschutzkonform.
- Ergebnisse mit Code-/Architekturbelegen, Trade-offs und konkret überprüfbaren nächsten Schritten.

## Nicht im Umfang
- Sofortige Umgestaltung des ganzen Frontends oder produktiver Refactor.
- Neue Messaging-/News-/Analytics-Backend-APIs ohne vorherige Machbarkeitsprüfung.
- Änderung der fachlichen Berechtigungen, Entitlements oder Geschäftsprozesse.
- Abschließende juristische Bewertung von Community-/Premium-Lizenzen.
