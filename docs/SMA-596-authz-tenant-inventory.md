# SMA-596: Authz-/Tenant-Grenzen und Regression-Baseline

Stand: 2026-11-10  
Code-Baseline: `feature/SMA-596-authz-tenant-inventory` / `4726e81e`

Diese Bestandsaufnahme ist ein Read-only-Ergebnis. Sie beschreibt den aktuellen
Code, aber legt noch keine neue Berechtigungssemantik fest. Die bereits unter
SMA-604 geschützten Permission-Schreibpfade sind in
[`permission-write-path-reachability.md`](permission-write-path-reachability.md)
detailliert beschrieben.

## Begriffe und Prüfregel

- **Authentifiziert**: Die Route liegt hinter `authenticate("auth-jwt")`.
- **Autorisierter Kontext**: Der Kontext, in dem das benötigte Recht geprüft
  wird. Er muss aus einem fachlich gebundenen Ziel abgeleitet werden, nicht nur
  aus einem frei wählbaren Header.
- **Zielressource**: Das Objekt, das gelesen oder verändert wird, zum Beispiel
  Organisation, Provider, Legal Entity, Zahlung oder Subscription.
- **EACH_TARGET**: Bei Batch-Operationen muss jedes Ziel aufgelöst und geprüft
  werden. Gleiche IDs im Payload sind nur eine Konsistenzprüfung, kein
  Berechtigungsnachweis.
- **`no`**: `accessCheckNeeded = false`; die deklarierte Prüfung ist deaktiviert.
  Diese Stellen sind keine dokumentierten Ausnahmen, sondern offene
  Autorisierungslücken.

## Äußere Grenze

`Application.setupRouting` schützt User, Organization, Permission, Auction,
Round, Bidders, Distribution, Shares, Application und Banking mit JWT.
Authentication, Health und `/api/version` sind öffentlich. `sendBid` liegt
ebenfalls außerhalb des JWT-Blocks und muss als eigener öffentlicher
Vertrauenspfad betrachtet werden.

JWT allein stellt nur die Identität fest. Eine fachliche Tenant-Grenze ist im
Schema nicht als `tenantId` modelliert. Isolation entsteht heute indirekt über
Context-, Organization-, Provider-, Party- und Legal-Entity-Relationen.

## Routenmatrix

| Bereich / Ziel | Kontextquelle | Aktueller Guard | Bewertung |
|---|---|---|---|
| Permission: User-Rollen in Context schreiben | Payload `contextId` | `MANAGE_USER_ROLE_CONTEXTS`, derived | geschützt; negative API-Regression vorhanden |
| Permission: Rollen-Rechte in Context schreiben | Payload `contextId` | `MANAGE_ROLE_RIGHT_CONTEXTS`, derived | geschützt; negative API-Regression vorhanden |
| Organization CRUD und Members | Context-Header; Action prüft teilweise Organization-Context erneut | generische Rechte | Header/Ziel-Bindung uneinheitlich; bestätigter Defekt in `UpdateMember` |
| Application mit Organization verbinden | Payload `organizationId`, `applicationId` | kein Route-Guard; UserApplication-Existenz in Repository | App-Besitz wird nur angenommen; keine nachgewiesene Berechtigung an Zielorganisation |
| Banking: Legal Entity personal | Payload/Query `partyId` | derived Banking-Context, `yes` | gute Zielableitung; Party-Typ-Konsistenz ungeklärt |
| Banking: Fiscal Year | Payload `legalEntityId` | create/update derived; read auskommentiert | Read kann fremde Legal Entities anfragen |
| Banking: Bank Accounts | Legal Entity oder User | read/create/update/import aktiv; delete und personal read deaktiviert | gemischte Semantik innerhalb derselben Ressource |
| Banking: Creditor Identifiers | `legalEntityId` | derived, aktiv | Zielbindung vorhanden |
| Banking: SEPA | Mandat/Collection/Payment/Message IDs bzw. Legal Entity | überwiegend `no` | höchste zusammenhängende offene Schreibfläche |
| Shares: Types, Offers, Subscriptions | Payload/Query oder tatsächliche Ressource → Provider → App-Context; Personal über JWT-Actor | Provider-Reads/Writes aktiv, Personal self-bound | geschützt; Quell- und Zielressourcen werden bei Änderungen serverseitig aufgelöst |
| Shares: Batch `update-statuses` | Payload-Provider plus DB-Provider jedes Ziels | EACH_TARGET/ALL_OF, aktiv | in SMA-596D geschlossen; leere Liste prüft den Payload-Provider |
| Shares: History | Subscription-IDs → Provider → App-Context | `READ_SHARE_SUBSCRIPTIONS`/Management, EACH_TARGET | geschützt; jede Subscription wird serverseitig aufgelöst |
| Distribution Points | Organization aus Provider/Payload/Actor | derived `DISTRIBUTION`-Context; Update prüft Quelle und Ziel | in SMA-596C geschlossen |
| Auction | Payload/Header/Action | create/update/read teilweise geschützt; configure/delete/accept/import ohne sichtbaren Guard | vollständige Zielbindung noch nicht nachgewiesen |
| Round | Payload-IDs | kein sichtbarer Route-Guard | create/state/export/evaluate/comment offen zu charakterisieren |
| Bidders | Payload/Query | kein sichtbarer Route-Guard | search/add offen zu charakterisieren |
| User/Profile | Actor, Payload oder Header | gemischt; Self-Service teilweise als Bypass im Guard | Header/Ziel-Manipulation und Self-vs-Admin getrennt testen |

## Vollständige explizite `no`-Bestandsaufnahme

Im Routing existieren nach Aktivierung der Share-Import-Prüfung noch **23 aktive deaktivierte Prüfungen**:

- Banking (23): Bank Account delete/personal read; SEPA mandate create/read/update/attach;
  collection create/create-payments/create-successors/generate-message/update;
  payment create-ad-hoc/update-statuses/update/update-many/delete/delete-many;
  payment-link by-legal-entity/personal; message download/by-legal-entity/update-status/merge.
- Shares: keine; der persönliche Subscription-Read ist als authentifizierter
  Self-Service-Pfad an die JWT-Identität gebunden, und Import leitet den
  Share-Management-Context aus dem Provider im Payload ab.
- Distribution Points: keine; provider read, personal read, create und update
  leiten ihre Contexts serverseitig ab. Update verwendet ALL_OF für Quell- und
  Zielorganisation.

Zusätzlich ist der Guard für `GET banking/fiscal-years/all` auskommentiert.
Routes ohne sichtbaren Permission-Schritt sind in der Matrix separat markiert;
sie dürfen nicht automatisch mit den ursprünglich 42 expliziten Bypässen gleichgesetzt
werden, weil eine Action noch eine interne Prüfung enthalten kann.

## Bestätigte Defekte und Risiken

### P0: `UpdateMember` kann Rollen anderer Nutzer löschen

Die Delete-Bedingung lautet sinngemäß:

```kotlin
contextId == organization.contextId && userId !in roleIds
```

`roleIds` wird damit fälschlich mit `userId` verglichen; außerdem fehlt die
Einschränkung auf `memberId`. Beim Ändern eines Mitglieds können deshalb
Rollenzuordnungen anderer Nutzer im selben Organisationskontext gelöscht
werden. Die vorhandenen Tests prüfen Erfolg, fehlendes Recht und falschen
Header-Kontext, aber nicht den Erhalt eines zweiten Mitglieds.

### P0: Noch 23 deklarierte Prüfungen sind deaktiviert

Das ist besonders kritisch für SEPA-Schreiboperationen. Die Share-Routen sind
inzwischen providergebunden oder bei persönlichen Reads an den JWT-Actor
gebunden; in diesem Bereich wird kein `no` mehr übergeben.

### P1: Application→Organization prüft die Zielorganisation nicht

`connectApplicationToOrganization` akzeptiert jede `organizationId`, sobald
für den Actor eine `UserApplication` existiert. Der Kommentar setzt
Registrierung/Besitz gleich; eine Membership- oder Management-Berechtigung an
der Zielorganisation wird nicht geprüft. Das kann Contexts und OWNER-Rollen an
einer fremden Organisation erzeugen.

### P1: Verbleibende ID-basierte Reads brauchen Ressourcenauflösung

SEPA-Downloads erhalten lediglich IDs. Eine robuste Prüfung muss erst
`ID → Ressource → Legal Entity → Context` auflösen und anschließend alle Ziele
prüfen. Der Client darf den Kontext nicht durch Auswahl einer ID oder eines
Headers bestimmen. Shares-History und Share-Batch-Updates lösen inzwischen jede
Subscription serverseitig zum Provider-Context auf.

### P2: Legal-Entity-Typ ist nicht gegen Party-Typ abgesichert

`LegalEntityType` (`HUMAN`/`ORGANIZATION`) wird aus dem Request gespeichert und
transformiert. In der untersuchten Route ist keine Konsistenzprüfung zur
referenzierten Party sichtbar. Ob dies nur Datenqualität oder eine spätere
Autorisierungsgrundlage betrifft, ist noch offen.

## Regression-Baseline: nächste kleine Schnitte

1. **SMA-596A – Organization Member Integrity**: Zwei Mitglieder mit Rollen
   anlegen, nur eines aktualisieren und beweisen, dass Actor und zweites Mitglied
   unverändert bleiben. Danach den Delete-Filter minimal korrigieren.
2. **SMA-596B – Application/Organization Cross-Boundary**: Actor besitzt die
   Anwendung, aber weder Mitgliedschaft noch Managementrecht an Organisation B;
   `connect-organization` muss ohne Seiteneffekt scheitern. Positivfall für eine
   berechtigte Organisation ergänzen.
3. **SMA-596C – Provider-bound Writes**: Zuerst Distribution oder Shares
   `update-statuses`: gleicher Tenant positiv, fremder Provider negativ,
   manipulierter Header negativ, Mixed-Provider-Batch und leere Liste negativ.
   Erst danach den jeweiligen `no`-Schalter entfernen.

SEPA sollte anschließend in einem eigenen Slice pro Ressourcenwurzel erfolgen;
23 Pfade auf einmal umzuschalten wäre nicht klein und schwer überprüfbar.

## Fakten, Annahmen, Unsicherheiten

**Fakten**

- Die äußere JWT-Topologie und ursprünglich 42 `no`-Stellen wurden aus dem
  Routing erhoben; nach den bisherigen Slices verbleiben 23 in Banking/SEPA.
- Die beiden direkten Permission-Schreibpfade leiten ihren Kontext inzwischen
  aus dem Payload ab.
- Der `UpdateMember`-Delete-Filter und die fehlende Organization-Prüfung beim
  App-Connect sind bis in Repository/SQL verfolgt.
- Es gibt im Backend keine gefundene explizite `tenantId`-Spalte oder
  `Tenant`-Entität.

**Annahmen**

- Organization/Provider/Legal Entity bilden fachlich die beabsichtigten
  Tenant-Grenzen. Diese Zuordnung muss als Produktentscheidung bestätigt
  werden.
- `no` ist gemäß SMA-604 unfertige Prüfung und keine bewusst öffentliche
  Fachoperation.

**Unsicherheiten**

- Für Auction, Round und Bidders ist noch nicht für jede Action bewiesen, ob
  sie intern dieselbe Zielbindung erzwingt.
- Die gewünschte Semantik für persönliche Reads gegenüber Management-Rechten
  ist nicht überall dokumentiert.
- Die verbindliche Regel für `LegalEntityType` gegenüber User/Organization ist
  noch nicht entschieden.

## Abschlusskriterium für SMA-596

SMA-596 ist abgeschlossen, wenn die drei Regression-Slices als ausführbare
Tests oder klar abgegrenzte Folgetickets vorliegen, jede offene Route eine
benannte Kontextquelle und Zielsemantik (`ALL_OF`, `ANY_OF`, `EACH_TARGET` oder
`RELATED_BY`) besitzt und die P0/P1-Funde priorisiert in die Umsetzung gehen.
