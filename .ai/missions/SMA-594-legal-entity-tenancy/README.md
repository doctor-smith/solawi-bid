# SMA-594 — Solyton LegalEntity / Tenancy / RRCT

> **Status:** Planungs- und Arbeitsbranch; noch keine produktiven Codeänderungen oder DB-Migrationen.  
> **Branch:** `feature/SMA-594-legal-entity-tenancy` (von `main`)

## Einstieg

- **Jira Epic:** [SMA-594](https://solawi-management.atlassian.net/browse/SMA-594)
- **Erster Task (P0):** [SMA-595](https://solawi-management.atlassian.net/browse/SMA-595) — Cross-Context-Grant reproduzieren und `isGrantedOneOf` absichern
- **Koordination mit ChatGPT und Mensch:** [AI Command #7](https://github.com/doctor-smith/ai-command/issues/7)
- **Bestandsaufnahme:** [AI Command LegalEntity-Assessment](https://github.com/doctor-smith/ai-command/blob/main/research/solyton-multitenancy-legal-entity-assessment.md)
- **Identity / Tenant-Mission:** [AI Command #6](https://github.com/doctor-smith/ai-command/issues/6)
- **Vererbung & RRCT:** [AI Command #8](https://github.com/doctor-smith/ai-command/issues/8)

## Zugeordnete Tasks

| Reihenfolge | Jira | Ziel |
| --- | --- | --- |
| 1 | [SMA-595](https://solawi-management.atlassian.net/browse/SMA-595) | Kontext-übergreifenden Grant testen und mit minimalem Fix verhindern |
| 2 | [SMA-596](https://solawi-management.atlassian.net/browse/SMA-596) | Authz-Flächen inventarisieren; Negativtests und Baseline |
| 3 | [SMA-597](https://solawi-management.atlassian.net/browse/SMA-597) | LegalEntity / User / Organization ADR und Integrität |
| 4 | [SMA-598](https://solawi-management.atlassian.net/browse/SMA-598) | Tenant, Membership, Context-Bindung und Platform Tenant |
| 5 | [SMA-599](https://solawi-management.atlassian.net/browse/SMA-599) | Additiver Backfill-Plan und Dry Run |
| 6 | [SMA-600](https://solawi-management.atlassian.net/browse/SMA-600) | Tenant-Entitlements für Apps, Module und Bundles |
| 7 | [SMA-601](https://solawi-management.atlassian.net/browse/SMA-601) | Application Management in eigenem Plattform-Tenant |
| 8 | [SMA-602](https://solawi-management.atlassian.net/browse/SMA-602) | Context-Vererbung und `Role × Right × Context × Time` |
| 9 | [SMA-603](https://solawi-management.atlassian.net/browse/SMA-603) | Optionales Premium-Modul bzw. App für Advanced Permissions |

## Arbeitsauftrag an Codex — als Erstes

1. Arbeitsbranch überprüfen und `SMA-595` lesen.
2. Bestehende Tests und `module/permission/action/db/is-granted.kt` auf beiden `isGrantedOneOf`-Overloads prüfen.
3. Kleinen Datenbank-Regressionstest für zwei Contexts (A und B) mit gleicher Role-ID schreiben. User besitzt die Role nur in A; das Right existiert in B. **Erwartung:** kein Zugriff in B.
4. Fehler reproduzieren und aktuellen Stand im [Coordination Issue #7](https://github.com/doctor-smith/ai-command/issues/7) mit `[Codex]` melden.
5. Einen minimalen Fix vorbereiten, der UserRoleContext und RoleRightContexts am `contextId` korrekt verknüpft; Tests für List<ContextId>-Overload ergänzen. **Vor Merge und vor allem vor destruktiven Änderungen menschliche Freigabe einholen.**
6. Findings, Testausgaben und PR/Commit-Links nach Jira SMA-595 und in den Coordination Channel schreiben. Keine Endlosschleifen zwischen Agenten.

## Architektur-Grenzen

- Identity: Human-LegalEntity und Auth-Principal fachlich verknüpfen, aber nicht unüberlegt User-Passwort-/Login-Daten in die Bank-Domain verschieben.
- Tenant: zahlende LegalEntity (HUMAN oder ORGANIZATION) als wirtschaftlicher Owner; Tenant als Zugriff-/Isolationsebene; Platform Tenant für Application Management.
- Context: ein permission-scoped Subtree innerhalb eines Tenants, niemals implizites Tenant-übergreifendes Recht.
- Vererbung der Role-Right-Policy ist **nicht** die automatische Vererbung einer UserRoleContext-Zuweisung.
- Zeit: sowohl Policy als auch User-Assignment können zeitlich eingegrenzt werden; die wirksame Zeit ist die Schnittmenge.
- Defaults: Core Authz-Enforcement ist für alle verfügbar; individuell editierbare Policies können ein kostenpflichtiges Application-/Module-Feature sein.
- Tests und Migrationen: additiv, überprüfbar, reversibel. Keine produktiven Backfills ohne gesonderte Zustimmung.

## Git-Workflow

Empfehlung: pro Task eigene kleine, reviewbare Commits/PRs; Jira-Key im Commit/Branch-Namen beibehalten, z. B. `SMA-595 test: reproduce cross-context authorization leak`. Der Epic-Branch ist als gemeinsame Ausgangsbasis gedacht; vor parallelen Writes durch mehrere Agenten Branch-/PR-Grenzen absprechen.
