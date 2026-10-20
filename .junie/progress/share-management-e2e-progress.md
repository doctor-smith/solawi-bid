---
Last updated: 2026-10-22
Phase: 1 (Audit) complete; Phase 2 (fixture-chain build-out) complete and VERIFIED —
`ShareSubscriptionApprovalWorkflowTest` (workflow B1) now passes end-to-end in a real
`./script/e2e-local.sh devTest` run (1 test, 0 failures, 0 errors, ~95s). All P0 blockers
from the previous session (`FY-1`, `FY-2`, the legal-entity/creditor-id context-id
mismatch, and the uncaught client-side crash) are resolved. Phase 3 (remaining
workflows B2-G2) has not started yet.
---

# Share Management E2E — Progress & Audit

## Current Phase / Workflow

**Phase 1 (Audit)** is complete (see matrix below). **Phase 2 is now complete and
verified**: `ShareSubscriptionApprovalWorkflowTest.kt` (the `@Develop @Workflow`-tagged
test, workflow B1) builds a fully isolated fixture organization from scratch —
organization creation, booking Banking/Distribution/Share Management, granting every
role on each application context, legal-entity + creditor-ID creation (as a two-step
create-then-edit flow, confirmed by the user to be the only supported production flow),
fiscal-year creation, share type/offer creation, distribution-point creation, member
subscription creation, and the final provider approval via Bulk-Edit — and **the whole
chain now passes in a real local run** (confirmed independently, see "Environment /
Build State"). This was a joint effort: the user fixed several issues directly in the
IDE (role-assignment, missing form fields, navigation stability, the `FY-1` year-0
default bug, a modal-identity/storage fix) while this session fixed the
legal-entity/creditor-identifier context-id mismatch (`CTX-1`/`CTX-2` below) and
updated the test to match the confirmed two-step legal-entity flow. See the Known
Issues Register for the full list.

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

- Phase 1: created this progress file. No other files changed.
- Phase 2 (this and prior sessions): additive `data-id` instrumentation across several
  application/banking/organization-module UI files (see "Frontend `data-id`
  instrumentation added so far" below for the full list with exact strings).
- `e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares/ShareSubscriptionApprovalWorkflowTest.kt`
  (new) — the fixture-building workflow test itself (see "Tests Implemented").
- `e2e/src/test/kotlin/util.kt` (modified by the user) — `clickOn`/`waitForLoadingToFinish`
  switched from one-shot Playwright `waitFor` to auto-retrying `assertThat(locator)...`
  assertions, to stabilize flaky test runs.
- `.junie/progress/share-managemet-e2e-progress-user.md` (new, added by the user) — the
  user's own working notes; kept as-is, untouched, and consolidated (not duplicated)
  into the "Known Issues Register" below.

## Tests Implemented

- `ShareSubscriptionApprovalWorkflowTest` (`@Develop`, `@Workflow`) — covers workflow B1
  (`PendingActivation` →(PROVIDER, NO_PAYMENT_MANDATE_REQUIRED)→ `Subscribed`), building
  its own fixture organization from scratch (steps 1-7 listed under "Fixture prerequisite
  chain" below). **Implementation complete AND verification complete** — the full test
  passes end-to-end in a real local run (see "Environment / Build State" for the exact
  run). This is the first shares e2e test to go green.
- An older, URL-navigation-based version of the same test function is kept commented out
  in the same file for reference (superseded by the click-driven `@Develop` version).

## Exact Test Commands Identified

Prefer the local docker e2e env (`./script/e2e-local.sh`) over calling `:e2e:test` directly
(the latter would affect the development database, per project guidelines).

- Narrowest, whole-module: `./gradlew :e2e:test --tests "org.solyton.solawi.bid.module.shares.*"`
  (requires `E2E_BASE_URL`, `TEST_USER`, `TEST_USER_PASSWORD`, optionally `E2E_RESET_DB`) —
  not recommended standalone; use the script instead.
- Ad-hoc single-test iteration: `./script/e2e-local.sh filterTest "<gradle-test-pattern>"`.
- Tag-filtered: `./script/e2e-local.sh workflowTest` (whole `@Workflow` suite) or
  `./script/e2e-local.sh devTest` (just `@Develop`-tagged tests, the narrowest option
  while this single test is being iterated on) — both reset the e2e DB first.
  **Actually run** (see "Environment / Build State" below): `./script/e2e-local.sh devTest`,
  2026-10-22 08:07 UTC, 95.6s, **0 failures — fully passing**.
- Local docker E2E environment lifecycle: `./script/e2e-local.sh build|up|down|test`.

## Environment / Build State

- `org.gradle.jvmargs=-Xmx2048m` in root `gradle.properties`; **no** per-module (`e2e`) JVM heap override in `e2e/build.gradle.kts`. No heap errors encountered in any run so far.
- **Update 2026-10-21**: the local E2E docker-compose environment *has* since been built and started (`./script/e2e-local.sh build` + `up`), and the `developmentTest` Gradle task (filters by the new `@Develop` tag, see `e2e/build.gradle.kts:167-178` and `script/e2e-local.sh:182-188,210`) was actually run against it:
  - Command: `./script/e2e-local.sh devTest` (resets the e2e DB first, then runs only `@Develop`-tagged tests).
  - Result: 2026-10-20 20:18, runtime ~80s, **1 test, 1 failure** —
    `org.solyton.solawi.bid.module.shares.ShareSubscriptionApprovalWorkflowTest` >
    "provider approves pending subscription without payment mandate (develop)".
  - Failure point: `ShareSubscriptionApprovalWorkflowTest.kt:323`, inside
    `createFiscalYearViaAdminUi` — `Locator expected not to be visible, waiting for
    locator("[data-id='banking.upsert-fiscal-years.modal']")` ... `unexpected value
    "visible"`, i.e. the fiscal-year modal's submit button does not close the modal.
    Root cause: `FY-1` in the Known Issues Register below.
  - Everything before that point — organization creation, booking
    Banking/Distribution/Share Management, granting every role on each application
    context, and legal-entity + creditor-ID creation — **passed**.
  - Raw results: `e2e/build/test-results/developmentTest/TEST-org.solyton.solawi.bid.module.shares.ShareSubscriptionApprovalWorkflowTest.xml`;
    diagnostics: `e2e/build/reports/tests/screenshots/{failure.png,failure-console.log,failure-network.log,failure.html}`.
- `git status --porcelain` additionally shows (beyond the pre-existing untracked files noted previously): `e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares/ShareSubscriptionApprovalWorkflowTest.kt` (new), `e2e/src/test/kotlin/util.kt` (modified — `clickOn`/`waitForLoadingToFinish` now use `assertThat(locator)...` instead of one-shot `waitFor`), and `.junie/progress/share-managemet-e2e-progress-user.md` (new, the user's own working notes — kept as-is, untouched). These are all tracked as part of this effort, not stray files.
- **Update 2026-10-21, later in the same session**: after fixing `FY-1`, rebuilt the frontend
  image (`./script/e2e-local.sh build-frontend`, `BUILD SUCCESSFUL`) and re-ran
  `./script/e2e-local.sh devTest` once more:
  - Result: 2026-10-21 08:20 UTC, runtime 80.6s, **1 test, 1 failure** — same test, same
    assertion line (`ShareSubscriptionApprovalWorkflowTest.kt:323`), but a **different
    underlying symptom**: the `AssertionFailedError` itself still reports the
    `banking.upsert-fiscal-years.modal` locator resolving to **9** elements (matching the
    very first failure seen before any fix), yet the HTML page snapshot saved moments
    later by the test's own failure-diagnostics code shows only **1** matching element,
    fully rendered and visible, with no stray/overlapping error dialog. See `FY-2` in the
    Known Issues Register.
  - Confirms `FY-1` (the year-0 default) is a real, fixed bug, but it was **not** the
    (or not the only) cause of the modal failing to close — a second, deeper issue
    remains open and needs live diagnosis before it can be fixed.
- **Update 2026-10-22 (fully passing run)**: between sessions, the user fixed `FY-2`
  directly (a modal-identity fix — giving each rendered modal a stable Compose `key`
  based on its own id, plus separating the `modals` list out of the main `Application`
  state in `storage.kt` so a modal-list change doesn't require a full-state write), and
  this session fixed a related context-id mismatch in legal-entity/creditor-identifier
  handling (see `CTX-1`/`CTX-2` below) plus updated the test's legal-entity fixture step
  to match the only supported production flow (create legal entity first, then edit it
  to add the creditor id). Re-ran `./script/e2e-local.sh devTest`:
  - Result: 2026-10-22 08:07 UTC, runtime **95.6s**, **1 test, 0 failures, 0 errors —
    fully passing**.
  - The full fixture chain now completes successfully end-to-end: organization
    creation → booking Banking/Distribution/Share Management → granting roles →
    legal-entity + creditor-ID creation (two-step) → fiscal-year creation → share
    type/offer creation → distribution-point creation → member subscription creation
    (starts `PENDING_ACTIVATION`) → Bulk-Edit approval → subscription becomes
    `SUBSCRIBED`.
  - Three pre-existing, already-deferred background errors (`FE-1`/`FE-2`/`FE-3`,
    403/404/404 on `bank-accounts/all`, `legal-entities/personal`,
    `creditors/identifiers/by-legal-entity`) still appear in `failure-network.log`-style
    diagnostics but do **not** block the test — confirmed benign/cosmetic for a
    freshly created organization with no legal entity yet at the time those specific
    calls fire.
  - Raw results: `e2e/build/test-results/developmentTest/TEST-org.solyton.solawi.bid.module.shares.ShareSubscriptionApprovalWorkflowTest.xml`
    (`tests="1" failures="0" errors="0" time="95.645"`).

## Coverage Matrix

| # | Workflow | Expected transition | Existing test coverage | Verification status | Blockers | Next action |
|---|---|---|---|---|---|---|
| A1 | Share type creation | n/a (setup) | `ShareSubscriptionApprovalWorkflowTest.kt` (`createShareTypeViaAdminUi`) | **Verified** — passes as part of the full B1 run | None | Covered as a fixture helper; a dedicated standalone test is not planned |
| A2 | FY-specific share offer creation | n/a (setup) | `ShareSubscriptionApprovalWorkflowTest.kt` (`createShareOfferViaAdminUi`) | **Verified** — passes as part of the full B1 run | None | Covered as a fixture helper |
| A3 | Member subscribes to offer | → `PendingActivation` | `ShareSubscriptionApprovalWorkflowTest.kt` (`createShareSubscriptionViaAdminUi`) | **Verified** — passes as part of the full B1 run, subscription confirmed `PENDING_ACTIVATION` | None | Covered as a fixture helper |
| B1 | FY1 approval, no mandate | `PendingActivation` →(PROVIDER, NO_PAYMENT_MANDATE_REQUIRED)→ `Subscribed` | `ShareSubscriptionApprovalWorkflowTest.kt` | **Implementation complete AND verification complete** — full test passes (0 failures, 95.6s, 2026-10-22) | None | **Done.** Next: pick the next-highest-value workflow (e.g. B2 payment-mandate authorization, or D1 rollover) reusing this fixture chain |
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

## Known Issues Register

Consolidates `share-managemet-e2e-progress-user.md` (the user's own working notes, kept
as-is, untouched) with this session's own findings. Priority follows the triage done
this session: **P0** = actively blocking current progress, high-confidence root cause;
**P1** = real friction, safe to address now; **P2** = deferred by the user ("next
sprint"); **P3** = needs more live-environment diagnosis before any fix.

| ID | Area | Description | Priority | Status | Next action |
|---|---|---|---|---|---|
| FY-1 | Frontend (source code) | `UpsertFiscalYearsModal` silently defaulted the untouched start/end date field to `FiscalYear.default` (year 0/1, `fiscal-year.kt:16`) | P0 | **Fixed** — both `onInput` handlers now fall back to `guessedNextFiscalYear` instead of `FiscalYear.default`; also fixed a related display bug where the End field's placeholder incorrectly showed `guessedNextFiscalYear.start` (`banking.upsert-fiscal-years-modal.kt:109-146`) | Done; see `FY-2` for the real remaining blocker uncovered by re-running after this fix |
| FY-2 | Frontend (source code) | Even after `FY-1`, the fiscal-year modal (and other modals) could stay open after a successful submit, or fail to open at all, due to two compounding issues: (1) rendered modals in `ModalLayer`'s `roots().forEach { ... }` had no stable Compose identity across recompositions, and (2) the `modals` list lived inside the single monolithic `Application` state in `storage.kt`, so changing it required a full-state `write()`, racing with other in-flight recompositions | P0 | **Fixed by user** — (1) `evoleq/.../org/evoleq/compose/modal/modal.kt`: wrapped each root modal's `component()` call in `key(root.id) { ... }`; (2) `storage.kt`: pulled `modals` out into its own `remember { mutableStateOf(...) }`, read/written independently of the rest of `Application` (reference-identity check: `newApplication.modals !== modals`) | Done — confirmed fixed by the fully-passing 2026-10-22 run |
| CTX-1 | Frontend (source code) | `createCreditorIdentifier`/`updateCreditorIdentifier` dispatches in `page.banking-application-for-organizations.kt`'s `LegalEntity` composable passed `legalEntityState.legalEntityId` (the legal-entity row's own database id) as the permission-context key, but Banking's permission contexts are keyed by the **organization/provider id** everywhere else in the same file (e.g. `createFiscalYear(providerId.value, ...)`) — causing the backend to reject these calls with "No such context" for any freshly created organization | P0 | **Fixed this session** — both dispatches now also pass `providerId` alongside the real `legalEntityId` (backend/frontend action signatures extended to carry both ids: one for context resolution, one for the actual DB lookup), implemented jointly with the user | Done — confirmed fixed by the fully-passing 2026-10-22 run |
| CTX-2 | Frontend (source code) | The `GET /banking/creditors/identifiers/by-legal-entity` read route derived its permission context directly from the `legal_entity` query parameter, which (after `CTX-1`'s partial fix) could be the real legal-entity id rather than the organization id, still causing "No such context" 500s on this one read endpoint | P0 | **Fixed by user** — the route/action now also accepts the `provider` id and uses it for context derivation, matching the create/update routes | Done — confirmed fixed by the fully-passing 2026-10-22 run |
| LE-1 | E2E test code / confirmed production flow | The "create legal entity" dialog lets the user type a creditor id into the same form, but its `onOk` handler never dispatched creating it (only the separate "edit" dialog does) — attempting to fix this naively would require a legal-entity id that isn't known client-side until the create call completes | P1 | **Resolved by decision, not a code fix** — the user confirmed creditor-id entry is only ever supported via a later "edit" step, never combined with initial creation; `createLegalEntityWithCreditorIdViaAdminUi` in the test was updated to a two-step create-then-edit flow to match | Done |
| E2E-1 | E2E test code | Role assignment via the "manage permissions" modal did not work at all | P0 | **Fixed by user** — `grantOwnerPermissionViaAdminUi` now only checks *unchecked* boxes via `input[type='checkbox'][data-id^='manage-user-permissions.modal.role.']`, with a sanity check that at least one exists | None — monitor in future runs |
| E2E-2 | E2E test code | Some form fields were not filled in at all in certain flows (notably the legal-entity address form) | P0 | **Fixed by user** — `createLegalEntityWithCreditorIdViaAdminUi` now fills `#recipientName`/`#organizationName`/`#addressLine1`/`#addressLine2`/`#postalCode`/city | None — monitor in future runs |
| E2E-3 | E2E test code | No ad-hoc test-name/class filtering in `./script/e2e-local.sh` (only whole-tag commands like `devTest`/`workflowTest`) | P1 | **Fixed** — new `filterTest <pattern>` command added, forwarding to `./gradlew :e2e:test --tests "<pattern>"` with the same env vars as the other commands | Done |
| E2E-4 | E2E test code | `failure-network.log` entries capture the failing request's own URL but not the page URL the test was on when it failed | P1 | **Fixed** — `withFailureDiagnostics` now appends a `source-page-url: ${page.url()}` line to each logged entry | Done |
| E2E-5 | E2E test code | User selection in `UpsertShareSubscriptionModal` is unreliable in Playwright — the click sometimes doesn't register | P3 | Partially mitigated — `assertThat(selectedUser).hasValue(username)` moved right after the click | Keep monitoring; no further change until observed failing live |
| FE-1 | Frontend (deferred) | `GET banking/bank-accounts/all` → 403 Access denied | P2 | Deferred to next sprint (per user) | Permission check fix, out of scope here |
| FE-2 | Frontend (deferred) | `GET banking/legal-entities/personal` → 404 No such legal entity party | P2 | Deferred to next sprint (per user) | Permission check fix, out of scope here |
| FE-3 | Frontend (deferred) | `GET banking/creditors/identifiers/by-legal-entity` → 500 "No such context" | P2 | Deferred to next sprint (per user) | Render-guard so the page only renders once a context entity is stored, out of scope here |
| UNK-1 | Unclear blame | General e2e flakiness: tests fail intermittently on fine production code; removing debug screenshots made failures *more* frequent (timing/race sensitivity, not a deterministic bug) | P3 | Open, needs live reruns to correlate | Continue the `assertThat(...)` auto-retry migration already underway; do not blindly re-add screenshots or arbitrary waits |
| UNK-2 | Unclear blame | Compose-for-Web form rerendering causes Playwright race conditions not seen in production | P3 | Open | No blanket fix proposed; same direction as UNK-1 |
| CLARIFY-1 | Needs clarification | "Page Reload does not really work on all pages" — data missing after full refresh vs. present when navigating by clicking | P3 | Needs clarification | Needs a concrete reproducing page/scenario from the user before any fix is attempted |
| CLARIFY-2 | Needs clarification | Using the dedicated Application card (Banking/Shares/Distribution) to reach the org-application page leads to an incomplete store "in some cases" | P3 | Needs clarification (user says "clarify which") | Needs the user to specify a concrete repro before any fix is attempted |

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

## Highest-Value Next Action

**Update 2026-10-22**: B1 is done (implemented and verified). Blocker #1 above (the
Bulk-Edit actor-hardcoding bug blocking C1/B5/G2/USER-only transitions) is still open
and unchanged — it will need a decision before any of those specific workflows can be
implemented. The next highest-value, **not** blocked by it: **B2 (payment-mandate
authorization)** or **D1 (FY1→FY2 rollover)**, both PROVIDER-driven and reusable on top
of the now-working fixture chain. Recommend picking up B2 next, since D1's rollover
needs a second fiscal year/share offer (FY2) as an extra prerequisite on top of B1's
fixture.

## Session Log

One dated entry per work session, newest first, so future sessions don't have to
re-derive history from git diffs alone.

### 2026-10-22

- User confirmed the first shares e2e test (`ShareSubscriptionApprovalWorkflowTest`,
  workflow B1) is passing completely after further fixes made directly in the IDE,
  including a fix for `FY-2` (giving each modal a stable Compose `key` based on its own
  id in `evoleq/.../modal.kt`, and separating the `modals` list out of the monolithic
  `Application` state in `storage.kt` so modal-list changes don't require a full-state
  write — see `facts.storage.md`).
- This session additionally diagnosed and fixed a context-id mismatch
  (`CTX-1`/`CTX-2`): Banking's `createCreditorIdentifier`/`updateCreditorIdentifier`
  dispatches and the `by-legal-entity` creditor-identifier read route were using the
  legal entity's own database id for permission-context resolution instead of the
  organization/provider id used everywhere else in the Banking module, causing
  "No such context" errors for freshly created organizations — fixed jointly with the
  user by passing both ids explicitly (one for context, one for the DB lookup).
- Confirmed with the user that entering a creditor id together with a brand-new legal
  entity in one combined step is **not** a supported production flow; updated
  `createLegalEntityWithCreditorIdViaAdminUi` in the test to the correct two-step
  create-then-edit flow (logged as `LE-1`, resolved by decision).
- Re-ran `./script/e2e-local.sh devTest` independently to verify: **1 test, 0
  failures, 0 errors, 95.6s** — `ShareSubscriptionApprovalWorkflowTest` now passes
  end-to-end for the first time.
- Updated this file throughout: header/phase summary, "Tests Implemented", "Exact Test
  Commands", "Environment / Build State", Coverage Matrix rows A1-A3/B1, the Known
  Issues Register (`FY-2`/`CTX-1`/`CTX-2`/`LE-1` all marked fixed/resolved), and this
  "Highest-Value Next Action" section.
- **Next action**: pick up workflow **B2** (payment-mandate authorization) as the next
  test, reusing the now-verified fixture chain; confirm the SEPA/bank-account
  precondition in the test environment first (per the existing plan's "Risks" section)
  before implementing.

### 2026-10-21

- Reviewed the working tree and both progress files against the actual latest
  `developmentTest` run artifacts (JUnit XML + `failure-network.log`/`failure.png`,
  dated 2026-10-20 20:18) to confirm what the user had changed/fixed directly in the
  IDE since the last agent session.
- Confirmed: role-assignment fix and missing-form-fields fix (both now **Fixed by
  user**, see `E2E-1`/`E2E-2` in the Known Issues Register), click-driven navigation
  (new `@Develop`-tagged test function), `assertThat(...)`-based stabilization in
  `util.kt`, and the context-header addition to `failure-network.log` are all present
  in the working tree as described by the user.
- New finding this session: identified the root cause of the current blocker (`FY-1`)
  via read-only inspection of `banking.upsert-fiscal-years-modal.kt` and `fiscal-year.kt`
  — the untouched date field silently defaults to a year-0/year-1 value, likely
  triggering a Compose-Web remount race that leaves the fiscal-year modal's submit
  non-functional.
- Triaged all open items (this session's and the user's) into a prioritized table and
  consolidated everything into this file's new "Known Issues Register".
- Updated the Coverage Matrix (rows A2, B1), "Environment / Build State" (recorded the
  actual `developmentTest` run instead of the stale "not yet executed" note), "Files
  Changed" and "Tests Implemented" (both were stale "none yet" placeholders).
- Fixed `FY-1` in `banking.upsert-fiscal-years-modal.kt` (both date fields now fall back
  to `guessedNextFiscalYear` instead of the year-0 `FiscalYear.default`, plus a related
  End-field display bug), confirmed via `:solawi-bid-frontend:compileKotlinJs`.
- Added the `filterTest <pattern>` command to `script/e2e-local.sh` (`E2E-3`, fixed) and
  a `source-page-url` field to every `failure-network.log` entry in
  `ShareSubscriptionApprovalWorkflowTest.kt`'s `withFailureDiagnostics` (`E2E-4`, fixed);
  both verified via `bash -n`/`:e2e:compileTestKotlin`.
- Rebuilt the frontend image and re-ran `./script/e2e-local.sh devTest`: the test still
  fails at the same line (`ShareSubscriptionApprovalWorkflowTest.kt:323`), but the
  symptom changed — `FY-1` is confirmed fixed, yet the fiscal-year modal still doesn't
  close after a correctly filled, successful submit. The failure message itself reports
  9 matching DOM elements at the moment of the timeout, while the HTML snapshot saved
  moments later shows only 1 (visible, no overlapping dialog). Logged as new item `FY-2`.
- **Next action**: `FY-2` needs live, click-by-click diagnosis of the `Modals<Int>`
  storage/removal and recomposition path in `modal-impl.kt`/
  `banking.upsert-fiscal-years-modal.kt` before any further fix is attempted — this is
  beyond safe guessing and should be the first thing picked up next session, ideally
  with the user's input on whether this `Modals<Id>` pattern has caused similar
  "stuck open" symptoms elsewhere in the app.
