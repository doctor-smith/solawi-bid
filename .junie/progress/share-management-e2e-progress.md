---
Last updated: 2026-10-19
Phase: 1 (Audit) complete; Phase 2 (Report & Ask) in progress — building the fixture
prerequisite chain (organization -> banking -> fiscal year -> share management) for
the first workflow test (B1).
---

# Share Management E2E — Progress & Audit

## Current Phase / Workflow

**Phase 1 (Audit)** is complete (see matrix below). **Phase 2** is in progress: before
any shares workflow can be tested, a fully isolated test organization must be created
and wired up with Banking (for a fiscal year) and Share Management (to create share
types/offers). This setup chain is being built incrementally, with minimal additive
`data-id` instrumentation added to production UI code where missing (approved by the
user, see "Decisions Confirmed by User").

## Files Inspected

- `.junie/plans/share-management-e2e-workflows.md` (Phase-1 planning doc, narrative source)
- `.junie/plans/shares-module-e2e-workflow-tests.md` (Phase-2 planning doc, claims Step 2 done, Steps 3-7 not done)
- `docs/share-management-workflows.md` (authoritative narrative reference, 320 lines)
- `solawi-bid-api-data/.../module/shares/data/internal/share-status-transitions.kt` (state machine, 203 lines)
- `solawi-bid-api-data/.../module/shares/data/internal/share-status.change-reason.kt` (ChangeReason/ChangedBy enums)
- `e2e/src/test/kotlin/**` (full listing — no shares tests exist)
- `e2e/src/test/kotlin/org/solyton/solawi/bid/module/user/UserManagementTest.kt` (reference pattern: `adminContext()`, `dismissStrayErrorDialogIfPresent`, unique-fixture naming)
- `solawi-bid-frontend/.../application/ui/page/shares/page.share-management-for-organizations.kt` (1790 lines — provider-facing shares page)
- `solawi-bid-frontend/.../module/shares/component/modal/modal.share-subscriptions.bulk-edit.kt` (638 lines — the *only* way to change subscription status from the provider UI)
- `solawi-bid-frontend/.../module/shares/component/dropdown/sharestatus.dropdown.kt` (defined but **not used** anywhere in the page)
- `solawi-bid-frontend/.../module/shares/component/list/share-subscriptions.personal.list.kt` (member/"USER"-facing subscription list — no status actions, no data-ids)
- `e2e/build.gradle.kts`, `gradle.properties`, `script/e2e-local.sh` (build/JVM/test-command audit)
- `git status` (working tree check)

## Files Changed

- Created this progress file. No other files changed in Phase 1.

## Tests Implemented

None yet (Phase 1 is audit-only).

## Exact Test Commands Identified (not yet executed,)

Possible commands: prefer local docker e2e env!!! 

- Narrowest: `./gradlew :e2e:test --tests "org.solyton.solawi.bid.module.shares.*"` (requires `E2E_BASE_URL`, `TEST_USER`, `TEST_USER_PASSWORD`, optionally `E2E_RESET_DB`).
- Tag-filtered (whole `@Workflow` suite): `./gradlew :e2e:workflowTest` or `./script/e2e-local.sh workflowTest` (resets DB first).
- Local docker E2E environment lifecycle: `./script/e2e-local.sh build|up|down|test`.

## Environment / Build State

- `org.gradle.jvmargs=-Xmx2048m` in root `gradle.properties`; **no** per-module (`e2e`) JVM heap override in `e2e/build.gradle.kts`. No heap errors encountered yet (no tests were run).
- The local E2E docker-compose environment (`docker-compose.e2e.local.yml`) is **not currently running** (`docker compose ps` returned no containers). Starting it requires `script/e2e-local.sh build` (builds `solawi-bid/backend:local` and `solawi-bid/frontend:local` images) then `up` — this is a non-trivial, potentially slow operation that was **not** performed in Phase 1 per the "don't run the full suite / don't do expensive setup speculatively" rule.
- `git status --porcelain` shows only pre-existing untracked files unrelated to this effort (DB dumps under `infrastructure/database/`, generated PAIN XML files under `solawi-bid-backend/src/main/resources/`, `.bak` i18n files, `.continue/`). Nothing in `e2e/` or the shares frontend module is modified. Nothing needs to be preserved/stashed.

## Coverage Matrix

| # | Workflow | Expected transition | Existing test coverage | Verification status | Blockers | Next action |
|---|---|---|---|---|---|---|
| A1 | Share type creation | n/a (setup) | None | Not verified | None — `data-id`s exist (`shares.page.share-type.create-button`, etc.) | Implement as fixture helper, not a standalone test |
| A2 | FY-specific share offer creation | n/a (setup) | None | Not verified | None — `data-id`s exist (`shares.page.share-offer.create-button`, fiscal-year/share-type/pricing dropdowns, price input) | Implement as fixture helper |
| A3 | Member subscribes to offer | → `PendingActivation` | None | Not verified | None — `shares.page.share-subscription.create-button` + `modal.share-subscriptions.upsert.kt` have `data-id`s | Implement as fixture helper |
| B1 | FY1 approval, no mandate | `PendingActivation` →(PROVIDER, NO_PAYMENT_MANDATE_REQUIRED)→ `Subscribed` | None | Not verified | None — reachable via Bulk-Edit "Change status" (`shares.modal.bulk-edit.status-dropdown`); PROVIDER permission exists for this transition | **Highest-value, safely implementable first test** |
| B2 | Payment-mandate authorization | `PendingActivation`→`AwaitingAhcAuthorization`→`Subscribed` | None | Not verified | PROVIDER permission exists for both hops; SEPA mandate attach button has `data-id`; needs a configured legal entity/bank account in the env (per existing plan's "Risks" section) | Verify SEPA precondition in target env before implementing |
| B3 | Failed authorization | `AwaitingAhcAuthorization`→(PROVIDER/SYSTEM, AUTHORIZATION_FAILED)→`Suspended` | None | Not verified | PROVIDER permission exists | Implementable after B2 |
| B4 | Auction clearance + final approval | `PendingActivation`→`ClearedForAuction`→`Subscribed` | None | Not verified | PROVIDER permission exists for both hops | Implementable |
| B5 | Activation rejected → resubmission | `PendingActivation`→(PROVIDER, REQUIREMENTS_NOT_MET)→`ActivationRejected`; `ActivationRejected`→(**USER**, NEW_REQUIREMENTS)→`PendingActivation` | None | Not verified | **Second hop has no PROVIDER-permitted reason.** The only UI control for status changes (Bulk-Edit dropdown) hardcodes `ChangedBy.PROVIDER` and does `permissions[ChangedBy.PROVIDER]!!` — selecting `PendingActivation` as target here will throw `NullPointerException` client-side. See "Blockers" below. | **STOP — ask user** |
| B6 | Cancellation before activation (both actors) | `PendingActivation`→`Cancelled` (PROVIDER or USER reason) | None | Not verified | PROVIDER reason exists so provider-side cancel is UI-testable; USER-side cancel hits the same actor-hardcoding issue as B5 | Implement PROVIDER-cancel variant only, pending decision on USER variant |
| C1 | Pause / resume | `Subscribed`→(**USER**, USER_PAUSED)→`Paused`; `Paused`→(**USER**, RESUME)→`Subscribed` | None | Not verified | **Both hops are USER-only — no PROVIDER permission at all.** No reachable, `data-id`-instrumented UI control exists for either hop (Bulk-Edit hardcodes PROVIDER → crashes; the member-facing `share-subscriptions.personal.list.kt` has no pause/resume action and no `data-id`s at all). | **STOP — ask user** |
| C2 | Cancel after activation | `Subscribed`→(USER/PROVIDER, USER_CANCEL)→`Cancelled` | None | Not verified | PROVIDER reason exists, so provider-driven cancel is testable | Implementable |
| C3 | Payment failure / recovery | `Subscribed`→`PaymentFailed`→`Subscribed`/`Suspended`/`Cancelled` | None | Not verified | All reasons here have PROVIDER/SYSTEM permission | Implementable |
| C4 | Suspension & resolution | `Subscribed`→`Suspended`→`Subscribed`/`ClearedForAuction`/`Cancelled` | None | Not verified | All PROVIDER/SYSTEM-permitted | Implementable |
| C5 | Invalid actions from terminal/incompatible states | e.g. rollover from `Cancelled` | None | Not verified | `Cancelled`/`RolledOver` have `emptySet()` transitions — worth checking whether the UI disables/rejects these or whether the Bulk-Edit dropdown blindly offers them (same crash class as B5/C1) | Needs UI inspection during implementation |
| D1 | Rollover: Subscribed → RollingOver, shadow created | `Subscribed`→(PROVIDER/SYSTEM, NEW_PERIOD)→`RollingOver` + shadow `PendingActivation` on FY2 offer | None | Not verified | PROVIDER permission exists; reachable via Bulk-Edit "Rollover" option (`Checked.ROLLOVER`), not a per-row button | Implementable after B1 |
| D2 | Shadow activation → original `RolledOver` | shadow `PendingActivation`→`Subscribed`; original `RollingOver`→(PROVIDER/SYSTEM, ROLLED_OVER)→`RolledOver` | None | Not verified | PROVIDER permission exists for both | Implementable |
| D3 | Missing next-year offer | rollover attempted with no FY2 offer | None | Not verified | Need to confirm in UI whether Bulk-Edit's offer dropdown for rollover simply has no FY2 entries (safe) or allows an invalid selection | Needs UI inspection |
| D4 | Ineligible source status (e.g. `Cancelled`) | no `RollingOver` transition defined | None | Not verified | Same class of question as C5/D3 | Needs UI inspection |
| D5 | Shadow cancellation → orphaned rollover | shadow `PendingActivation`→`Cancelled` (USER reason) while original stays `RollingOver` | None | Not verified | Shadow cancel-before-activation also has a PROVIDER reason (`PROVIDER_CANCELLED_BEFORE_ACTIVATION`), so provider-driven variant is testable | Implementable (provider variant) |
| E1 | Ownership transfer | `Subscribed`/`PendingActivation`→`Transfer`→`PendingActivation`/`Subscribed` | None | Not verified | All PROVIDER-permitted; `modal.share-subscriptions.transfer-ownership.kt` has `data-id`s and a per-row trigger button | Implementable |
| E2 | New owner's activation | depends on E1 | None | Not verified | Same as B1/B2 depending on path chosen | Implementable after E1 |
| E3 | Mandate mapping / authorization reset | implicit (no dedicated status) | None | Not verified | Needs to confirm if/how mandate-mapping removal is observable in the UI (no dedicated `ShareStatus`) | Needs UI inspection |
| E4 | SEPA mandate re-attach/update | implicit (no dedicated status) | None | Not verified | `modal.share-subscriptions.attach-or-upsert-sepa-mandate.kt` has `data-id`s | Implementable, same SEPA precondition as B2 |
| F1 | FY2→FY3 rollover (repeat) | same as D1/D2 against a separate FY3 offer | None | Not verified | Same as D1/D2 | Implementable after D1/D2 pattern proven |
| F2 | Paused/suspended source not rolled over | `Paused`/`Suspended` have no `RollingOver` transition | None | Not verified | Depends on resolving C1 (Paused is USER-only to reach in the first place) | Blocked transitively by C1 |
| F3 | Updated offer price effect on shadow | data-level, no status transition | None | Not verified | Implementable via offer edit + shadow inspection | Implementable |
| F4 | Unresolved rollover recovery | manual intervention, no SYSTEM transition out of `RollingOver` other than `ROLLED_OVER` | None | Not verified | Documentation-only scenario per docs §6 | Likely a documented-not-tested case |
| G1 | External import | `External`→(PROVIDER, IMPORT)→any internal status | None | Not verified | `shares.page.share-subscription.import-button` + `modal.share-subscriptions.import-share-data.kt` have `data-id`s | Implementable |
| G2 | Expired → PendingActivation | `Expired`→(**USER**, NEW_PERIOD)→`PendingActivation` | None | Not verified | USER-only, same actor-hardcoding issue as B5/C1 | **STOP — ask user** |

**Not e2e-testable without time manipulation** (confirmed consistent between docs and state machine, explicitly out of scope per existing plan and user guidelines): `ActivationRejected→Expired` (REJECTION_NOT_RESOLVED), `Paused→Expired` (PERIOD_END), `Suspended→Expired` (UNRESOLVED_PERIOD_END), `Subscribed→Expired` when driven by SYSTEM (SUBSCRIPTION_END, though a PROVIDER-driven variant of this same transition *is* reachable and in scope, see C-group).

## Decisions Confirmed by User

1. USER-actor status transitions (pause/resume, activation-rejected resubmission, etc.):
   user will point out specific alternate Modifier Groups/actors in the Bulk-Edit modal
   "when explicitly needed" rather than answering exhaustively up front. Proceeding with
   clearly PROVIDER-driven workflows first; will come back to USER-only transitions
   individually as they're reached.
2. Local E2E docker environment: wait until the first test is fully implemented before
   starting `script/e2e-local.sh build`/`up` (confirmed by user).
3. `TEST_USER`'s organization does not have Share Management booked, and no existing
   organization accessible to `TEST_USER` has Banking + a fiscal year set up either
   (confirmed by user: "build from scratch").
4. User approved adding minimal, additive `data-id` test hooks to application-module
   production UI code (outside the shares module) where needed to automate the
   organization-setup admin flow, rather than having the user book applications
   manually each time.

## Frontend `data-id` instrumentation added so far (Phase 2)

- `solawi-bid-frontend/.../application/ui/page/application/private/private-application-management.kt`:
  added `dataId = "private-application-management.page.application.${application.name}.connect-button"`
  to the `ShareNodesButton` that opens the "connect application to organization" modal.
- `solawi-bid-frontend/.../module/application/component/modal/connect-application-to-organization-modal.kt`:
  added `dataId = "connect-application-to-organization.modal.organization-dropdown"` to the
  organization-picker `Dropdown` (options become `"...organization-dropdown.option.<key>"`).
- Confirmed (no change needed): `/app/management/organizations` already has
  `data-id="organization-management-page.button.create-organization"` on its "create
  organization" button; the resulting modal's name input has a plain HTML
  `id="organization-name"` (usable via `page.locator("#organization-name")`); and the
  generic `Modal` component's confirm button resolves to `"null.modal.submit-button"`
  when no custom `dataId` is passed to `Modal(...)` (confirmed by reading
  `solawi-bid-frontend/.../org/evoleq/compose/modal/modal-impl.kt`), which covers both
  this modal and the connect-application-to-organization modal.
- The `/app/management/organizations/with/shares` list page renders plain `<a>` links
  with the organization's name as link text (no `data-id` needed; a test can locate by
  link text) that navigate straight to `/app/management/organizations/{id}/share_management`.

## Fixture prerequisite chain for the first workflow test (B1) — fully mapped

Since no existing organization accessible to `TEST_USER` has Banking/Share
Management/Distribution booked, the fixture for every shares test needs, in order:
1. Create a brand-new organization with a unique name
   (`/app/management/organizations`, `organization-management-page.button.create-organization`
   + `#organization-name` + `null.modal.submit-button`). **Confirmed via backend code**
   (`createRootOrganization` in `solawi-bid-backend/.../module/user/schema/repository/organization.kt`):
   the creator (`TEST_USER`) is automatically inserted into `UserOrganization`, which is
   the exact table backing the `members` field (`val members by UserEntity via
   UserOrganization` in `user-management.organisation.kt`) — so `TEST_USER` is
   automatically eligible to be the "member" subscribing to a share offer in this new
   org, no separate member-management step needed.
2. Book the Banking application (`BANKING_APPLICATION_NAME = "BANKING"`) to it via the
   same `/app/management/private` connect-button + organization-dropdown + submit flow
   already used for Share Management.
3. Expand the "Fiscal Years" section on `/app/management/organizations/{id}/banking`
   (now `dataId="banking.page.fiscal-years.chevron"`, added by us) and create one via
   `dataId="banking.page.fiscal-years.create-button"` (added by us) + existing
   `upsert-fiscal-year.form.input.date.start`/`.end` (`type="date"` inputs, already had
   `data-id`s) + `null.modal.submit-button`.
4. Book the Share Management application the same way (connect-button
   `private-application-management.page.application.SHARE_MANAGEMENT.connect-button`).
5. Navigate to `/app/management/organizations/with/shares`, click the link matching the
   fixture's unique organization name (plain `<a>` text, no `data-id` needed) to land on
   `ShareManagementForOrganizationsPage`.
6. Create a share type: `shares.page.share-type.create-button` +
   `shares.modal.upsert-share-type.name-input`/`.key-input`/`.description-input` +
   `shares.modal.upsert-share-type.modal.submit-button`.
7. Create a fiscal-year-scoped share offer: `shares.page.share-offer.create-button` +
   `shares.modal.upsert-share-offer.fiscal-year-dropdown`/`.share-type-dropdown`/
   `.pricing-type-dropdown`/`.price-input` (FIXED pricing only)/
   `.ahc-authorization-required-dropdown` + `shares.modal.upsert-share-offer.modal.submit-button`.
8. **New blocker found**: creating a member subscription
   (`shares.page.share-subscription.create-button` →
   `modal.share-subscriptions.upsert.kt`, `dataId="shares.modal.upsert-share-subscription"`)
   also requires selecting a `DistributionPoint`
   (`shares.modal.upsert-share-subscription.distribution-point-dropdown`) — this field is
   mandatory in `validate()`/`update()` (`change.distributionPoint.new!!.distributionPointId`,
   would NPE on submit if never set) and distribution points only exist once the
   **Distribution** application (`DISTRIBUTION_APPLICATION_NAME`, a *fifth* module
   alongside Organizations/Banking/Users/Share-Management) is booked for the
   organization and at least one is created via
   `solawi-bid-frontend/.../application/ui/page/distribution/page.distribution-management-for-organizations.kt`
   (`createDistributionPoint` dispatch found there; exact button/input `data-id`s not
   yet verified). Once a distribution point exists, the subscription's "member" is
   picked via `shares.modal.upsert-share-subscription.user.<username>.select-button`
   (here: `TEST_USER`'s own username, since it's automatically a member — see point 1),
   fiscal year and share offer dropdowns, then
   `shares.modal.upsert-share-subscription.modal.submit-button`.
9. For B1 specifically: open Bulk-Edit on the new subscription, select target status
   `Subscribed` in the status dropdown (exact `data-id` not yet re-verified this
   session, approximately `shares.modal.bulk-edit.status-dropdown` per Phase-1 audit) —
   this picks `NO_PAYMENT_MANDATE_REQUIRED` automatically since it hardcodes
   `ChangedBy.PROVIDER` and takes the *first* `PROVIDER`-permitted reason for
   `PendingActivation`→`Subscribed`, which is `NO_PAYMENT_MANDATE_REQUIRED` (confirmed
   order in `share-status-transitions.kt` during Phase 1).

A dedicated `general_purpose` subagent (`banking-fiscal-year-flow-explorer`, handle
`agent-1`) confirmed the Banking application name/URL segment, the organization
dropdown's option-key format, the share-type/share-offer `data-id`s, and the
fiscal-year creation flow/`data-id`s (folded into the list above). The additive
`data-id` changes for fiscal-year creation (`CardChevrons` gaining an optional
`dataId` parameter threaded through `chevrons.kt`, plus the two banking-page call
sites) were implemented and the frontend was recompiled successfully
(`./gradlew :solawi-bid-frontend:compileKotlinJs` → `BUILD SUCCESSFUL`, no heap
issues, ~26s).

## Blockers & Unresolved Questions

1. **Actor-hardcoding bug in the Bulk-Edit status dropdown** (`modal.share-subscriptions.bulk-edit.kt`, the "Change status" section, ~lines 172-213): the dropdown lists **every** `ShareStatus` as a potential target (it iterates all keys of `shareStatusTransitionsWithPermissions`, not just the valid transitions from the current source status), and on selection does:
   ```
   targetStatusPermissions?.first { it.shareStatus == value }?.permissions[ChangedBy.PROVIDER]?.first()!!
   ```
   This hardcodes the actor to `PROVIDER` and non-null-asserts the result. For any transition where the state machine only grants the reason to `USER` (e.g. `Subscribed→Paused`, `Paused→Subscribed`, `ActivationRejected→PendingActivation`, `RollingOver→PendingActivation`, `Expired→PendingActivation`, `AwaitingAhcAuthorization→Cancelled` by USER, `Paused→Cancelled` by USER), selecting that target status in the UI will throw a client-side `NullPointerException` instead of performing the transition. Separately, selecting a target status that isn't a valid transition from the current source status at all (`.first {...}` finds nothing) would throw `NoSuchElementException`.
   - This blocks the **pause/resume workflow (C1)**, the **activation-rejected resubmission workflow (B5's second hop)**, the **rollover "member requests a change" workflow (D-group's USER_EVENTUAL_CHANGE hop)**, and **Expired→PendingActivation (G2)** from being e2e-testable through the provider UI as currently implemented.
   - The member-facing page (`share-subscriptions.personal.list.kt`) does not offer an alternative path either: it has no `data-id`s and its only action is a generic "Edit" button (enabled only in `PendingActivation`) that performs a plain field update, not a status-machine transition.
   - **Question for the user:** Is there another UI entry point for these USER-actor transitions that I haven't found (e.g. a different page, a notification/approval widget, or a workflow reachable only after certain preconditions)? If not, should these transitions be (a) documented as out-of-scope/blocked due to a frontend gap, (b) treated as a bug report to be filed separately without fixing production code in this E2E effort, or (c) fixed as a minimal, additive frontend change (make the actor selectable, or default to the first available actor for the chosen target) so they become testable — the last option would mean touching production logic, which these rules ask me to confirm with you first.

2. **Starting the local E2E environment**: `docker-compose.e2e.local.yml` is not currently running, and no shares tests exist yet to run. Before I can execute even the narrowest test command, `script/e2e-local.sh build` (docker image builds) and `up` are needed, which is a non-trivial, potentially slow operation. I have not started this yet and will only do so once the first test (see below) is implemented, to avoid idle/expensive environment time — flagging here in case you'd rather I start it now in parallel.

## Highest-Value Next Action (pending the above answers)

Implement `test_newSubscription_noMandate_becomesSubscribed` (FY1 happy path, no SEPA
mandate; row B1 above) first: it requires no USER-actor transition, has full `data-id`
coverage on every control it needs (share type, share offer, subscription creation,
bulk-edit status change to `Subscribed`), and matches the existing plan's Step 4 first
test case. This does not depend on the answer to Blocker #1, so it can proceed once
Blocker #2 (environment startup) is confirmed.
