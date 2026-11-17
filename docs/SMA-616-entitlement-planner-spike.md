# SMA-616 — Pure application entitlement contribution planning

**Status:** isolated prototype, **not yet CI-verified or integrated**. Implements only a narrow mathematical preparation step for [SMA-613](https://solawi-management.atlassian.net/browse/SMA-613) and [SMA-600](https://solawi-management.atlassian.net/browse/SMA-600).

## Motivation

The existing [SMA-615](https://solawi-management.atlassian.net/browse/SMA-615) database-backed application DAG answers **which applications an explicit application requires**. It does not answer **why a tenant may retain an effective entitlement after one of several independent purchases is cancelled**.

The pure planner converts an already authorized set of *explicit grant roots* into a read-only plan of contributions with provenance:

- `ApplicationGrantRoot(rootId, applicationIds)`: an explicit and independently revocable source. IDs must be unique per source; repeating an identical source is idempotent.
- `ApplicationEntitlementContribution(rootId, applicationId, reason)`: contribution reason is `EXPLICIT` for directly listed apps or `DEPENDENCY` for apps reached only through the database application DAG.
- `ApplicationEntitlementPlan`: effective application closure, dependency-first order, and explanation by root.

### Worked example

Given catalog edges:

```text
SHARE_MANAGEMENT -> BANKING
SHARE_MANAGEMENT -> DISTRIBUTION
```

and two explicit independent roots:

```text
Root S: SHARE_MANAGEMENT
Root B: BANKING
```

the plan explains:

```text
BANKING:        (Root S, DEPENDENCY), (Root B, EXPLICIT)
DISTRIBUTION:   (Root S, DEPENDENCY)
SHARE_MANAGEMENT:(Root S, EXPLICIT)
```

Remove Root S, re-plan with Root B, and **BANKING remains**. This is not equivalent to removing every application that was formerly reachable from Shares.

**Important distinction:** The planning result is not a tenant entitlement or user permission. Only a separate, authenticated, tenant-scoped reconciler can turn approved plan contributions into persisted entitlements. It must ensure that **all roots belong to the same authorized tenant**, and it must handle lifecycle status, transactions, versioning and billing separately.

## Architectural boundary

This prototype deliberately **does not**:

1. Introduce `tenant_* ` tables or resolve the pending SMA-597/598 tenant-ID and LegalEntity model.
2. Read or write user/org registration, grant `OWNER` or RRCT permissions, activate subscriptions, change trial state or apply payments.
3. Grant SEPA, DATEV or other modules merely because the user has Banking or `isMandatory = true`.
4. Add a global `AUCTIONS -> SHARE_MANAGEMENT` dependency. Auctions' integrated vs CSV-only modes remain under SMA-614.
5. Choose how direct roots are persisted, identified or revoked. Source provenance is an in-memory contract for further review.

The input is **not trusted** when it comes from HTTP or another tenant. Do not directly expose this planner as an activation endpoint.

## Unit tests

```bash
./gradlew :solawi-bid-backend:unitTest --tests 'org.solyton.solawi.bid.module.application.service.ApplicationEntitlementPlannerTest'
```

Test cases cover overlapping purchases and dependency roots, cancellation preserving independent Banking, the same dependency reached through multiple roots, idempotent retries, input-order independence, `EXPLICIT` precedence within a root, conflicting/empty/reserved roots, unknown apps, invalid catalogs and the CSV-only Auctions isolation boundary.

**Integration gate:** Ensure new branch CI passes before merge. Only after tenant-identity decisions and SMA-600's contribution persistence can we add an atomic, tenant-scoped `reconcileTenant` function with proper negative/permission tests.

## Relationship to the architecture contract

See the proposed contract at [AI Command PR #33](https://github.com/doctor-smith/ai-command/pull/33), especially its Sections 4–8. This prototype proves a *pure origin-sensitive calculation* but deliberately does **not** settle the persistent schema, billing model or tenant authorization policy.
