# SMA-596 – Autorisierungsgrenzen und Regressionen

Stand 27.11.2026. Die Integration in PR #379 steht auf `c65c904f`. Die
Prüfmatrix erfasst zusätzlich den vorgeschlagenen Code bis `2212774a`.
Die Ausgangsinventur vom 14.11. ist über die Git-Historie verfügbar.
Der Stand der Abnahme und die privaten Detailbefunde stehen in Jira.
[SMA-596](https://solawi-management.atlassian.net/browse/SMA-596)
· [Integration #379](https://github.com/doctor-smith/solawi-bid/pull/379)

JWT identifiziert den Actor in den registrierten Verwaltungsrouten.
Authentication, Health und Version sind öffentlich. Die öffentliche
Gebotsabgabe hat einen eigenen fachlichen Vertrag. Die Autorisierungsgrenzen
beruhen auf Context-, Organization-, Provider-, Party- und Legal-Entity-Relationen.
Das Schema hat noch kein eigenständiges Tenant-Modell.

Plattformbezogene Kontoaktionen lösen den Context über das gespeicherte
Modul `ORGANIZATION_MANAGEMENT` von `APPLICATION_MANAGEMENT` auf und
prüfen die Zugehörigkeit zum Plattform-Root. Das gilt auch nach einer
Änderung der aus Baumpositionen berechneten Pfadanzeige.

## User und Profile

Die Routen stehen in
[`module/user/routing/routing.kt`](../solawi-bid-backend/src/main/kotlin/org/solyton/solawi/bid/module/user/routing/routing.kt).

| Registrierte Route | Grenze und Datenpfad | Nachweis oder Restarbeit |
| --- | --- | --- |
| GET `users/all`, POST `users/query` | Bestehende Aufrufstellen verwenden das Benutzerverzeichnis persönlich und administrativ. Die Query-Route prüft `MANAGE_USERS`. | Sichtbarkeit je Aufrufstelle und autorisierte Zielmenge fachlich festlegen. |
| POST `users/create` | `CREATE_USER` im serverseitig aufgelösten Plattform-Context `APPLICATION/MANAGEMENT/ORGANIZATION_MANAGEMENT` → `CreateNewUser` → `createUserEntity` → Benutzerzeile und Standardrollen. | Plattform-Positivfall und Organisations-Negativfall. Abgelehnte Anlage lässt Benutzer und Rollenzuordnungen unverändert. D-008 greift bei aktiven Konten. |
| PATCH `users/update` | `UpdateUser` → JWT-Actor muss Zielkonto sein → bisherige Credentials prüfen → Benutzerzeile ändern. | SMA-612. Eigener Zugriff, fremdes Ziel, ungültiges JWT und falsches bisheriges Passwort sind geprüft. |
| PATCH `users/change-password`, `users/update-username` | `UPDATE_USER` im serverseitig aufgelösten Plattform-Context → jeweilige Action → Benutzerzeile. | SMA-611 und administrativer Teil von SMA-612. Festes Plattformrecht, D-008, anonym abgewiesener Reset, und gespeicherte Credentials nach Ablehnung geprüft. Prüfung einer veröffentlichten Umgebung bleibt gesondert. |
| PATCH `users/profiles/read-by-ids` | Eigene ID-Menge oder `MANAGE_USERS` → `ReadUserProfiles` → Profil und Adressen anhand der User-IDs. | Eigener Read und fremde bzw. gemischte Ziele getestet. Administrativen Zielumfang zusammen mit dem Verzeichnis klären. |
| POST `users/profiles/create`, PATCH `users/profiles/update` | Eigener Actor oder `MANAGE_USERS` → Profil-Repository. Update bindet Profil-ID und Benutzer-ID sowie vorhandene Adressen an dasselbe Profil. | Eigener Create/Update. Abgelehnte Änderungen lassen Profile und Adressen unverändert. |
| POST `users/profiles/import` | `MANAGE_USERS` → `ImportProfiles` → Benutzer- und Profil-Repositories. | Profilimport hält neue Konten ohne zugewiesenes Passwort in `PENDING`. Administrativen Zielumfang zusammen mit der Organisationsmitgliedschaft festlegen. |
| DELETE `users/profiles/delete` | Registrierter Platzhalter ohne fachliche Mutation. | Künftige Lösch- und Aufbewahrungsregel separat festlegen. |

## Auction, Round und Bidders

Die implementierten Actions verwenden Exposed-Transaktionen. Einige
Routen delegieren direkt an Transaction-Funktionen und DAOs. Die
Route-, Action- und Service-Dateien liegen im
[`module/bid`](../solawi-bid-backend/src/main/kotlin/org/solyton/solawi/bid/module/bid).

| Registrierte Route | Serverauflösung und Verarbeitung | Nachweis oder Restarbeit |
| --- | --- | --- |
| POST `auction/create` | `CreateAuction` klont die Auction-Policy des Actors, weist Rollen zu und setzt den gespeicherten Auction-Context. Die Organisation wird ggf. über `OrganizationApplicationContexts` verknüpft. | Bestehender Create-Positivfall. Vertragsentscheidung für persönliches und organisationsbezogenes Provisioning offen. |
| GET `auction/all` | `ReadAllAuctions` filtert `Auctions` auf die Contexts, in denen der Actor `READ_AUCTION` hat. | Bestehende Listen- und Auction-Workflows. |
| PATCH `auction/update` | Der Mutationsteil `updateAuctions` hat noch keine fachliche Implementierung. | Bei Implementierung jedes Update-Ziel auflösen und prüfen. |
| PATCH `auction/configure` | Gespeicherter Quell- und Ziel-Context benötigen `UPDATE_AUCTION` → `configureAuction` → `Auctions`, Details und ggf. Context-Zuordnung. | Gemischte Rechte führen zur Ablehnung. Persistenz unverändert. |
| DELETE `auction/delete` | Jedes gespeicherte Ziel benötigt `DELETE_AUCTION` → `deleteAuctions` → `Auctions`; akzeptierte Auctions bleiben gesperrt. | Gemischte Ziele abgewiesen. `AcceptedRounds` wird mit dem Auktion-Fremdschlüssel abgeglichen. |
| PATCH `auction/accept-round` | Zusammengehöriges Auction-Round-Paar und `UPDATE_AUCTION` im Auction-Context → `AcceptRound` → `AcceptedRounds`. | Positiver vollständiger Auction-Ablauf. Ablehnung und unveränderte Daten bei fremdem Ziel oder inkonsistentem Paar. |
| POST `auction/bidder/import` | Gespeicherte Ziel-Auction, `IMPORT_BIDDERS` → Importfunktion → Bidder und Auction-Bidder-Zuordnungen. | Vorhandener Import-Positivfall und neue Ablehnung mit Persistenzprüfung. |
| POST `auction/bidder/import-bidders-from-organization` | Ziel-Auction mit `IMPORT_BIDDERS` und gespeicherte Quellorganisation mit `READ_ORGANIZATION` → Import aus Mitgliedschaft und Profilen → Auction-Bidder-Zuordnungen. | Fehlendes Recht an der Quelle lässt das Ziel unverändert. |
| DELETE `auction/bidder/delete` | Registrierter Platzhalter ohne fachliche Mutation. | Bei Implementierung Auction und Teilnahme gemeinsam prüfen. |
| POST `round/create` | Gespeicherte Parent-Auction, `UPDATE_AUCTION` → `CreateRound` → `Rounds`. | Bestehender Positivfall, neue Ablehnung mit Persistenzprüfung. |
| PATCH `round/change-state`, POST `round/add-comment` | Gespeicherte Round → Auction-Context, `UPDATE_AUCTION` oder `MODERATE_AUCTION` → `Rounds` bzw. Round-Kommentare. | Positiver Status- und Kommentarfall sowie unveränderte Daten nach Ablehnung. |
| PATCH `round/evaluate`, `round/pre-evaluate`, `round/export-results` | Gespeichertes Auction-Round-Paar. Auswertung braucht `UPDATE_AUCTION` oder `MODERATE_AUCTION`, Export `Export Results`. `getResults` liest `BidRounds` der zugehörigen Auction. | Auswertung und Export im bestehenden API-Ablauf. Fremde oder inkonsistente Ziele werden abgewiesen. |
| PATCH `bidders/search`, POST `bidders/add` | `SearchBidderMails` und `AddBidders` arbeiten mit dem bestehenden Suchverzeichnis `SearchBidders`. | Geltungsbereich der Verzeichnisdaten und Rechte für Lesen bzw. Pflegen bestätigen. |
| POST `bid/send` | Öffentliche `StoreBid`-Action. Gespeicherte Runde, Auction, Teilnahme und offener Status bestimmen, ob der BidRound geschrieben werden darf. | Test akzeptiert ein Gebot für eine geöffnete Runde und überprüft unveränderte Daten bei gesperrter Runde. Vertrag zur persönlichen Bestätigung separat entscheiden. |

Die Hilfsroute `bid/all` wird im Anwendungsrouting nicht registriert.
Für die registrierten Platzhalter gibt es keine fachliche Erfolgsbaseline.

## RRCT und die übrigen Ressourcenpfade

- Ein Einzelrecht verlangt dieselbe Rolle und denselben Context in
  `UserRoleContext` und `RoleRightContexts`. Der Eltern-/Kind-Baum
  bestimmt Beziehungen und Pfade. Ein Recht wird durch den Baum allein
  weder vererbt noch in einen anderen Context übertragen. Die DB-Regressionen
  prüfen Einzelrecht, Alternativen und wiederholtes Kopieren der Policy.
- `ANY_OF` sucht je geprüftem Context ein passendes Recht. Die leere
  Alternativmenge ergibt false. `ALL_OF`/EACH_TARGET prüft alle ermittelten
  Ziele, bei alternativen Rechten je Ziel separat. Für eine leere Menge
  bleibt die Allquantifizierung mathematisch wahr. Das Fach-API legt
  fest, ob ein leerer Auftrag zulässig ist.
- SEPA-Batches, die mindestens ein Ziel verlangen, melden HTTP 400.
  Shares-Statusbatches prüfen auch ohne Einträge den angegebenen Provider.
  Shares-History ohne IDs liefert keine historischen Datensätze. Persönliche
  Distribution ohne Mitgliedschaften liefert leere Resultate. Ein leerer
  Auction-Löschauftrag ändert keine Daten. Die Antwortliste bleibt unabhängig
  davon auf die Lese-Rechte des Actors beschränkt.
- Die Permission-Write-Routen prüfen Rechte am explizit adressierten
  Ziel-Context. Siehe
  [Permission-Schreibpfade](permission-write-path-reachability.md).
  Fachliche Ressourcen-IDs werden bei den folgenden Routen serverseitig
  auf Contexts bezogen. Ein Header kann die Auflösung nicht ersetzen.

| Bereich | Ressourcenbindung und Regression |
| --- | --- |
| Organization CRUD und Members | Organisationsobjekt und dessen Context werden in den Actions verwendet. Die Organisationsliste ermittelt effektive Rechte je Context anhand von RRCT. Bei Einstieg über `APPLICATION` wird ggf. der zugehörige Standard-Organisationskontext geprüft. Member-Updates bleiben auf bestehende Mitgliedschaften beschränkt. Das Entfernen eines Nichtmitglieds lässt unabhängige Context-Rollen intakt. Die Sichtbarkeit von Vorfahren, Geschwistern und Unterorganisationen bleibt fachlich zu präzisieren. |
| Persönliche Applications und Modules | Listen und Context-Relationen lesen ausschließlich die Buchungen des JWT-Actors. Für den Einstieg `APPLICATION` können explizite Rechte in den gespeicherten Einschreibungs- und Verwaltungs-Contexts verwendet werden. Dieselbe Auflösung gilt für eigene Buchungsaktionen mit dem dafür benannten Recht. |
| Application↔Organization | Zielorganisation und UserApplication. Die Repository-Prüfung verlangt Mitgliedschaft. Der Produktvertrag für zusätzliche Manager-/Owner-Rechte bleibt offen. |
| Banking | Legal Entity → Party → Banking-Context. Fiscal-Year-Updates prüfen Quelle und Ziel. Kontolöschung folgt dem gespeicherten Eigentümer, persönliche Konto-Reads dem JWT-Actor. Creditor Identifier sind an die Legal Entity gebunden. |
| SEPA | Collections, Payments, Messages und Mandate aus Ressourcen-ID bzw. Legal Entity zum Banking-Context. Persönliche Reads und Links folgen dem Actor. #402/#403 decken leere und fremde Ziele, beide Contexts und Persistenz nach Ablehnung ab. |
| Shares | Types, Offers, Subscriptions, Ownership Transfer, History und Import über gespeicherte Provider-, Subscription- und Zielbeziehungen. Persönliche Reads folgen dem Actor. #401–#403 decken Quellen und Ziele sowie Persistenz und Historie ab. |
| Distribution Points | Zielorganisation aus Provider/Actor und gespeicherten Ressourcen. Update prüft Quell- und Zielorganisation. Die API-Regression prüft Cross-Context-Zugriff. |

## Offene Owner- und Manager-Regeln

| Entscheidung | Aktuelles Verhalten und Risiko | Optionen, Auswirkungen und Empfehlung |
| --- | --- | --- |
| Last Owner bei Rollenwechsel oder Austritt | Rollenneuzuweisung und Entfernen einer Mitgliedschaft sind getrennte fachliche Vorgänge. Eine gemeinsame Invariante für den letzten effektiven Owner ist noch zu spezifizieren. Ohne sie kann ein verwalteter Context seine Zuständigkeit verlieren. | Owner-Zahl bei jeder betroffenen Schreibtransaktion absichern oder das Verwaisen mit einer Plattform-Recovery zulassen. Die erste Variante verlangt einen Übergabeprozess, die zweite zusätzliche Recovery-Rechte und Auditierung. Empfehlung ist eine atomare Übergabe für Contexts mit verbindlichem Owner. |
| Self-Lockout und Delegation | Inhaber administrativer Rechte können Rollen ändern. Der gewünschte Erhalt der eigenen administrativen Handlungsfähigkeit und die Wiederherstellung sind noch zu bestimmen. | Selbstentzug generell sperren oder gestatten, sobald ein weiterer berechtigter Admin übernimmt. Eine Sperre erschwert legitime Austritte. Empfehlung ist eine bestätigte Übergabe mit geprüften Restrechten und gesonderter Recovery. |
| Mitgliedschaft und App-Verknüpfung | Die Verknüpfung verlangt derzeit Mitgliedschaft. Mitgliedschaft und die Übernahme administrativer Verantwortung für eine Anwendung sind unterschiedliche Vorgänge. | Verknüpfung für jedes Mitglied erhalten, ein ausdrückliches Organisationsrecht verlangen oder einen Freigabeprozess einführen. Die letzten beiden Optionen ändern Delegation bzw. UI-Abläufe. Empfehlung ist ein explizites Organisationsrecht nach Bestätigung der Rollenverantwortung. |

Die E2E-Einrichtung eigener Organisationen hängt von der fachlichen
Delegation in gebuchten Anwendungen ab. Der gezielte
Payment-Mandate-Workflow erreicht nach der Korrektur des Einstiegskontexts
diese Grenze und erhält HTTP 403. Die weitere Abnahme erfordert eine klare
Regel für anwendungsspezifische Rollen und eine darauf abgestimmte
Test-Fixture. Sicherheitsrelevante Details und Optionen stehen in Jira.

Noch zu klären sind außerdem der bereichsübergreifende Geltungsbereich der
Benutzer- und Bidder-Verzeichnisse, der Auction-Provisioning-Vertrag,
die persönliche Bestätigung öffentlicher Gebote und die Konsistenz von
`LegalEntityType` und Party-Typ. Rechtemodell und Entitlements
bleiben getrennt. Eine über alle Schreibtransaktionen durchgesetzte
Tenant-Invariante gehört in das Tenant-Kernmodell.

## Testnachweis

Auf `2212774a` bestanden 135 Backend-API-, 119 Backend-Unit- und 716
Datenbanktests sowie Detekt für Produktions- und Testquellen. Evoleq
bestand 377 JVM- und 215 Browser-Tests, das API-Daten-Modul 2 JVM-Tests.
Der [Integrationslauf für c65c904f](https://github.com/doctor-smith/solawi-bid/actions/runs/45948799565)
ist vollständig erfolgreich. Den E2E-Nachweis des geprüften Stands
führt [Review-PR #404](https://github.com/doctor-smith/solawi-bid/pull/404). `regressionTest` und `securityTest` erfassen
jetzt denselben gezielt klassifizierten HTTP-Regressionstest für die
persönlichen SEPA-Mandate. Die Testfälle zur Context- und Datenbankisolation
liegen in den Backend-API- und DB-Suiten.
