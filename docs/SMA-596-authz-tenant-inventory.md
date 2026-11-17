# SMA-596: Authz-/Tenant-Grenzen und Regression-Baseline

Stand: 2026-11-14 (Dokumentationsabgleich; keine neue Testausführung)  
Code-Baseline: [`feature/SMA-596-authz-tenant-inventory` @ `40b50a39`](https://github.com/doctor-smith/solawi-bid/tree/40b50a39fe7335e93ced32fcfba5482c7e6a89dc)  
Jira: [SMA-596](https://solawi-management.atlassian.net/browse/SMA-596) – **In Progress**; Architektur: [AI Command #9](https://github.com/doctor-smith/ai-command/issues/9)

Diese Bestandsaufnahme wird mit den SMA-596-Slices fortgeschrieben. Sie
beschreibt den aktuellen Code und die inzwischen aktivierte
Berechtigungssemantik. Die bereits unter
SMA-604 geschützten Permission-Schreibpfade sind in
[`permission-write-path-reachability.md`](permission-write-path-reachability.md)
detailliert beschrieben.

> **Korrekturstand 14.11.2026:** Die früheren P0/P1-Befunde und die 42
> deaktivierten Guards beschreiben die **historische Ausgangslage**. Auf dem
> referenzierten SMA-596-Branch ist der `UpdateMember`-Löschfilter korrigiert,
> App→Organization prüft inzwischen Organisationsmitgliedschaft, und die
> Inventur meldet **keine aktiven `no`-Guards** mehr im betrachteten Routing.
> Dies belegt **nicht** die vollständige Tenant-Sicherheit oder Integration
> in `main`. Code und Test-Assertions wurden über GitHub gelesen; **keine
> neuen Tests ausgeführt**.

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
| Organization CRUD und Members | Context-Header; Action prüft teilweise Organization-Context erneut | `MANAGE_USERS` im Ziel-Org-Context und weitere Rechte | `UpdateMember`-Löschfilter korrigiert; übrige Header-/Zielbindungen noch nicht vollständig inventarisiert |
| Application mit Organization verbinden | Payload `organizationId`, `applicationId` | Repository: `requireOrganizationMembership` + UserApplication | Mitgliedschaft wird geprüft; 403 für fremde Organisation im Testcode; ob Membership statt Manager-/Owner-Recht genügt, ist offen |
| Banking: Legal Entity personal | Payload/Query `partyId` | derived Banking-Context, `yes` | gute Zielableitung; Party-Typ-Konsistenz ungeklärt |
| Banking: Fiscal Year | Payload/Query `legalEntityId` → Legal Entity → Party | create derived; update/read ALL_OF | geschützt; Update prüft Quell- und Ziel-Party |
| Banking: Bank Accounts | Legal Entity, tatsächlicher Account-Owner oder JWT-Actor | Provider-Operationen aktiv; personal read self-bound | geschützt; Delete löst den Owner serverseitig auf |
| Banking: Creditor Identifiers | `legalEntityId` | derived, aktiv | Zielbindung vorhanden |
| Banking: SEPA | Mandat/Collection/Payment/Message IDs bzw. Legal Entity | resource-derived, ALL_OF/EACH_TARGET; Personal Reads self-bound | geschützt; IDs werden serverseitig zu Legal Entity/Party/Banking-Context aufgelöst |
| Shares: Types, Offers, Subscriptions | Payload/Query oder tatsächliche Ressource → Provider → App-Context; Personal über JWT-Actor | Provider-Reads/Writes aktiv, Personal self-bound | geschützt; Quell- und Zielressourcen werden bei Änderungen serverseitig aufgelöst |
| Shares: Batch `update-statuses` | Payload-Provider plus DB-Provider jedes Ziels | EACH_TARGET/ALL_OF, aktiv | in SMA-596D geschlossen; leere Liste prüft den Payload-Provider |
| Shares: History | Subscription-IDs → Provider → App-Context | `READ_SHARE_SUBSCRIPTIONS`/Management, EACH_TARGET | geschützt; jede Subscription wird serverseitig aufgelöst |
| Distribution Points | Organization aus Provider/Payload/Actor | derived `DISTRIBUTION`-Context; Update prüft Quelle und Ziel | in SMA-596C geschlossen |
| Auction | Payload/Header/Action | create/update/read teilweise geschützt; configure/delete/accept/import ohne sichtbaren Guard | vollständige Zielbindung noch nicht nachgewiesen |
| Round | Payload-IDs | kein sichtbarer Route-Guard | create/state/export/evaluate/comment offen zu charakterisieren |
| Bidders | Payload/Query | kein sichtbarer Route-Guard | search/add offen zu charakterisieren |
| User/Profile | Actor, Payload oder Header | gemischt; Self-Service teilweise als Bypass im Guard | Header/Ziel-Manipulation und Self-vs-Admin getrennt testen |

## Vollständige explizite `no`-Bestandsaufnahme

Laut aktueller Branch-Inventur existieren im betrachteten Routing **keine aktiven expliziten `no`-Prüfungen** mehr (ursprünglich 42; kein vollständiger dynamischer Sicherheitsnachweis):

- Banking: Collections, Payments und Messages leiten ihre Contexts aus den
  gespeicherten Zielressourcen ab. Batch-Operationen prüfen alle betroffenen
  Ressourcen und lehnen leere Zielmengen ab. Persönliche Mandate und
  Payment-Links sind an die JWT-Identität gebundene Self-Service-Pfade.
- Shares: keine; der persönliche Subscription-Read ist als authentifizierter
  Self-Service-Pfad an die JWT-Identität gebunden, und Import leitet den
  Share-Management-Context aus dem Provider im Payload ab.
- Distribution Points: keine; provider read, personal read, create und update
  leiten ihre Contexts serverseitig ab. Update verwendet ALL_OF für Quell- und
  Zielorganisation.

Routes ohne sichtbaren Permission-Schritt sind in der Matrix separat markiert;
sie dürfen nicht automatisch mit den ursprünglich 42 expliziten Bypässen gleichgesetzt
werden, weil eine Action noch eine interne Prüfung enthalten kann.

## Bestätigte Defekte und Risiken

### P0 – behoben auf SMA-596-Branch: `UpdateMember`-Löschfilter

Historischer Befund: `userId notInList roleIds` verglich User- mit
Rollen-IDs und löschte ohne `memberId`-Filter potenziell fremde
Rollenzuweisungen. **Bereits korrigiert** mit
[Commit `2ca16465`](https://github.com/doctor-smith/solawi-bid/commit/2ca16465e741779ffce1723376a7f27e5a04827e):

```kotlin
UserRoleContext.deleteWhere {
    (UserRoleContext.contextId eq organization.context.id) and
        (UserRoleContext.userId eq memberId) and
        (UserRoleContext.roleId notInList roleIds)
}
```

[`OrganizationRoutingTests`](https://github.com/doctor-smith/solawi-bid/blob/40b50a39fe7335e93ced32fcfba5482c7e6a89dc/solawi-bid-backend/src/test/kotlin/org/solyton/solawi/bid/module/usermanagement/routing/organization-routing-tests.kt#L583-L675)
enthält eine Regression-Assertion, dass beim Bearbeiten eines Mitglieds
die Rollen des Owners erhalten bleiben. **Implementiert und im Code
nachgewiesen; keine aktuelle Testausführung und keine Integration in
`main` nachgewiesen.** Weitere Mitglieder, andere Contexts und
Parallelitätsfälle bleiben für vertiefte Tests relevant.

### Erledigt: Deklarierte Prüfungen waren deaktiviert

Die SEPA-Schreiboperationen sind inzwischen resource-derived geschützt. Im
Routing wird kein `no` mehr an einen Permission-Guard übergeben.

### P1 – teilweise geschlossen: Application→Organization

[`connectApplicationToOrganization`](https://github.com/doctor-smith/solawi-bid/blob/40b50a39fe7335e93ced32fcfba5482c7e6a89dc/solawi-bid-backend/src/main/kotlin/org/solyton/solawi/bid/module/application/repository/connect-applications-and-organizations.kt)
ruft jetzt **`requireOrganizationMembership(organizationId, userId)`**
vor der Mutation auf. In
[`ApplicationOrganizationTests`](https://github.com/doctor-smith/solawi-bid/blob/40b50a39fe7335e93ced32fcfba5482c7e6a89dc/solawi-bid-backend/src/test/kotlin/org/solyton/solawi/bid/module/application/routing/appliction-organization-tests.kt#L109-L147)
sind der negative API-Fall für eine fremde Organisation (erwartet 403)
und der legitime Verbindungsweg im Testcode vorhanden.

**Rest:** Mitgliedschaft ist kein nachgewiesenes Organisations-Manage-
oder Owner-Recht. Fachliche Policy entscheiden, ob ein normales Mitglied
Anwendungen verbinden und dadurch OWNER-Rollen im App-Context erzeugen
darf. 403 allein beweist keinen fehlenden Seiteneffekt; gezielt prüfen.

### Erledigt: ID-basierte SEPA-Reads lösen Ressourcen serverseitig auf

SEPA-Downloads und Legal-Entity-Reads lösen
`ID → Ressource → Legal Entity/Party → Banking-Context` auf. Der Client kann
den Kontext nicht durch einen Header bestimmen. Shares-History und
Share-Batch-Updates lösen ebenfalls jede Subscription serverseitig zum
Provider-Context auf.

### P2: Legal-Entity-Typ ist nicht gegen Party-Typ abgesichert

`LegalEntityType` (`HUMAN`/`ORGANIZATION`) wird aus dem Request gespeichert und
transformiert. In der untersuchten Route ist keine Konsistenzprüfung zur
referenzierten Party sichtbar. Ob dies nur Datenqualität oder eine spätere
Autorisierungsgrundlage betrifft, ist noch offen.

## Regression-Baseline – tatsächlicher Fortschritt / nächste Schritte

1. **SMA-596A – Organization Member Integrity:** Codefix `2ca16465`
   und Regression-Assertion (Owner-Rollen bleiben bei Member-Update gleich)
   vorhanden. Testlauf auf aktuellem HEAD und Last-Owner-/Cross-Context-
   Varianten separat nachweisen.
2. **SMA-596B – Application/Organization Cross-Boundary:** Membership-
   Prüfung implementiert und 403-Negativfall in `ApplicationOrganizationTests`
   vorhanden. Offen: Manager-/Owner-Policy, fehlende Seiteneffekte, Tests.
3. **SMA-596C/D – Distribution & Shares:** Guards und Quell-/Ziel-
   bzw. EACH_TARGET-Checks laut aktuellem Branch-Inventar aktiviert; konkrete
   negative Testresultate für aktuellen SHA erfassen.
4. **SEPA-Slices:** Mandates/Collections/Payments/Messages und ID-basierte
   Reads laut Routing resource-derived geschützt; negative Tests einschließlich
   Batch/leer/fremde Legal Entity und Seiteneffekten nachweisen.
5. **Nächste Sicherheitsinventur:** `putUserRoleContext` (letzter Owner,
   Self-Lockout), Auction, Round, Bidders, öffentliches `bid/send`, User/
   Profile; pro realer Route Guard, serverseitige Ressource→Context-Bindung,
   DB-Filter, Policy, positive/negative Tests und Rest-Risiko auflisten.

**Teststand nicht verwechseln:** Existierender Kotlin-Testcode ist keine
aktuell erfolgreiche Testausführung. Im [AI Command Coordination
Channel #7](https://github.com/doctor-smith/ai-command/issues/7) berichtete
Codex früher 8/8 Permission-API-, 7/7 Permission-DB- und 22/22 isolierte
E2E-Tests; diese Berichte stammen **vor** den neuesten SMA-596-Slices.
Für Branch-HEAD `40b50a39` wurde hier kein neuer lokaler Testlauf belegt.

## Fakten, Annahmen, Unsicherheiten

**Fakten**

- Die äußere JWT-Topologie und ursprünglich 42 `no`-Stellen wurden aus dem
  Routing erhoben; nach den bisherigen Slices verbleibt keine davon.
- Die beiden direkten Permission-Schreibpfade leiten ihren Kontext inzwischen
  aus dem Payload ab.
- Der `UpdateMember`-Delete-Filter ist auf diesem Branch mit `2ca16465` korrigiert; ein Regression-Assert ist vorhanden.
- Die App→Org-Verknüpfung verlangt jetzt Membership; ein stärkeres Organisations-Manage-/Owner-Recht ist noch nicht entschieden.
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
- Last-Owner-/Self-Lockout-Schutz in `putUserRoleContext` sowie vollständige
  Ressource↔Context-/Tenant-Bindings und aktuelle Tests bleiben offen.
- Die gewünschte Semantik für persönliche Reads gegenüber Management-Rechten
  ist nicht überall dokumentiert.
- Die verbindliche Regel für `LegalEntityType` gegenüber User/Organization ist
  noch nicht entschieden.

## Abschlusskriterium für SMA-596

SMA-596 ist abnahmefähig, wenn (1) die umgesetzten Slices inklusive
`UpdateMember`, App→Org, Shares, Distribution und SEPA mit aktuellen
Testläufen/Reviews nachgewiesen sind, (2) die übrigen tatsächlichen Routes
(Auction/Round/Bidders/User/Profile und weitere) inklusive interner
Repository-/SQL-Filter in einer prüfbaren Ziel-/Context-/Policy-Matrix
(`ALL_OF`, `ANY_OF`, `EACH_TARGET`, `RELATED_BY`) erfasst sind, (3) Owner-/
Self-Lockout-Invarianten und Cross-Context-/Tenant-Bindungen geprüft und
(4) offene P0/P1-Punkte als bewusste Entscheidungen/Folgeaufträge abgegrenzt
sind. Ein Merge nach `main` ist ein gesondert freizugebender Schritt.
