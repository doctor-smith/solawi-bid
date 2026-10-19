---
sessionId: session-261018-145117-svs1
---

# Requirements

### Overview & Goals
Implement executable Playwright/Kotlin e2e tests for the **Share Management** module, covering the multi-year subscription workflows already documented in `e22-suggestions.md` (fiscal-year setup, subscription lifecycle, rollover, SEPA mandates, ownership transfer). Tests live under `e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares` and every test method is tagged `@Workflow@Test` (the existing `@Workflow` tag annotation from `test-kinds.kt`, stacked with JUnit's `@Test` exactly like `@Smoke@Test` is used in `NavigationTest.kt`).

The attached document's Phase 1 output (`docs/share-management-workflows.md`) is produced first as the authoritative narrative reference, then used directly to derive the test code — replacing the originally-planned "scenario catalogue" markdown with real, runnable tests, per the user's explicit request.

### Scope
**In scope**
- A Markdown workflow-reference document (Step 1 of the attached plan).
- Adding the missing `data-id` selectors to the Share Management frontend pages/modals (none currently exist in `org.solyton.solawi.bid.module.shares` UI code) — required before any Playwright test can reliably target these elements.
- Shared e2e test fixtures/helpers for the shares module (provider context, unique share type/offer/fiscal-year creation via the UI).
- `@Workflow@Test`-tagged tests for: FY1 new subscription (happy paths + import), FY1 problems/resolutions, FY1→FY2 rollover (happy path + failure modes), FY2 ongoing operations (ownership transfer, SEPA re-sync), FY2→FY3 compounding problems.

**Out of scope**
- Backend/unit test changes (`ShareStatusRolloverTests.kt`, `ShareStatusRepositoryTest.kt` already exist and are not modified).
- SYSTEM/deadline-driven transitions that require real wall-clock expiry (e.g. `REJECTION_NOT_RESOLVED`, `UNRESOLVED_PERIOD_END`) are documented but exercised only where a backend action can be triggered directly within a test; otherwise they are noted as "not e2e-testable without time manipulation".
- Visual/CSS regression testing.

### User Stories
- As a QA engineer, I want a markdown document describing the full multi-year share workflow so that I have a single reference when writing or reviewing e2e tests.
- As a developer, I want `data-id` attributes on every share-management UI control I need to automate, following the same convention used in `application-management-page.kt`.
- As a CI pipeline, I want the new tests tagged `@Workflow` so they can be filtered/run as a dedicated suite alongside the existing `@Smoke`/`@Regression` tags.

### Functional Requirements
- Document: narrates FY1 setup → subscription → problems, FY1→FY2 rollover (happy + failure), FY2 operations, FY2→FY3 compounding issues, referencing `share-status-transitions.kt` and `share-status.change-reason.kt`.
- Tests: each test creates its own uniquely-named fixtures (share type/offer/subscription) the same way `UserManagementTest` creates uniquely-named users — no dependency on a full DB reset.
- Tests: every scenario from the document that is UI-drivable end-to-end gets at least one `@Workflow@Test` method; tests assert on `data-id`-targeted elements and/or URL state, never on translated copy (matching existing e2e conventions).

# Technical Design

### Current Implementation
- `e2e/src/test/kotlin/org/solyton/solawi/bid/test-kinds.kt` already defines `@Workflow` (`@Tag("workflow")`) alongside `@Regression`, `@UserJourney`, `@Smoke`. None of these are used yet anywhere in the suite.
- `NavigationTest.kt` shows the exact stacking convention requested: `@Smoke@Test fun test_useStoredState()`.
- `TestUtils.getCookie(browser)` logs in once as `TEST_USER`/`TEST_USER_PASSWORD` (the seeded application owner/organization admin) and persists `loggedInState.json`; `ApplicationManagementTest`/`UserManagementTest` both reuse this single admin session via `adminContext()`. The shares provider-role tests will reuse the same pattern (`TEST_USER` acts as the `ProviderId` owner on `ShareManagementForOrganizationsPage`).
- `util.kt` provides `getByDataId`, `put`, `clickOn`, `waitForLoadingToFinish` — all helpers assume `[data-id='...']` selectors exist in the frontend.
- **Gap found:** `org.solyton.solawi.bid.module.shares` frontend code (`page.share-management-for-organizations.kt`, `modal.share-offers.upsert.kt`, `modal.share-subscriptions.upsert.kt`, `sharestatus.dropdown.kt`, `modal.share-subscriptions.transfer-ownership.kt`, `modal.share-subscriptions.attach-or-upsert-sepa-mandate.kt`) contains **zero** `data-id` attributes today, even though the underlying button components (`SubmitButton`, `StdButton`, `CancelButton` in `button-base.kt`) already accept an optional `dataId: String?` parameter. This must be fixed before any shares e2e test can run reliably.
- `ShareManagementForOrganizationsPage(storage, providerId, up)` is the entry page (1768 lines) with `ShareTypeManagement`, `ShareOfferManagement`, `ShareSubscriptionManagement` composables — these are the UI surfaces the tests drive.

### Key Decisions
1. **Reuse the single-admin-session pattern** (`TestUtils`/`TEST_USER`) rather than introducing new roles — `TEST_USER`'s own organization is used as the `ProviderId` for all shares scenarios, consistent with how `ApplicationManagementTest`/`UserManagementTest` already treat `TEST_USER` as the acting admin.
2. **Instrument the frontend with `data-id`s as part of this effort**, following the `{page}.{section}.{entity}.{action}` naming convention already used in `application-management-page.kt` (e.g. `shares.page.share-type.create-button`, `shares.page.share-offer.{offerId}.status-dropdown`, `shares.page.share-subscription.{id}.rollover-button`).
3. **One test class per workflow phase** instead of one giant class, mirroring the one-class-per-feature convention (`UserManagementTest`, `ApplicationManagementTest`) and keeping each file focused and under ~250 lines.
4. **Fixtures are created per-test via the UI**, not via DB seeding/reset, per the documented convention in `DbReset.kt` ("most tests should just rely on migrations and create their own uniquely named fixtures").

### Proposed Changes
1. Add `docs/share-management-workflows.md` narrating the full lifecycle (content as already drafted in `e22-suggestions.md`'s "Workflows" section).
2. Add `dataId` parameters to the relevant composables/buttons in the shares frontend module (types, offers, subscriptions, status dropdown, rollover trigger, transfer-ownership modal, SEPA mandate modal).
3. Add `e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares/SharesTestSupport.kt` with shared helpers: `providerContext()`, `createShareTypeViaUi(page)`, `createShareOfferViaUi(page, shareType, fiscalYear)`, `createShareSubscriptionViaUi(page, offer)`.
4. Add four `@Workflow@Test`-tagged test classes covering the workflow phases (see File Structure).

### Data Models / Contracts
No backend/API contract changes. Tests interact purely through the existing UI and the documented status machine:
```
PendingActivation -> Subscribed (NO_PAYMENT_MANDATE_REQUIRED | AUTHORIZATION_COMPLETED)
Subscribed -> RollingOver -> RolledOver
Subscribed -> Paused -> Subscribed
PendingActivation -> Cancelled
Subscribed -> Transfer -> Subscribed (ownership transfer)
```

### Components
- **Frontend (modified):** `page.share-management-for-organizations.kt`, `sharestatus.dropdown.kt`, `modal.share-offers.upsert.kt`, `modal.share-subscriptions.upsert.kt`, `modal.share-subscriptions.transfer-ownership.kt`, `modal.share-subscriptions.attach-or-upsert-sepa-mandate.kt` — gain `data-id` attributes only, no behavioural change.
- **e2e (new):** `SharesTestSupport.kt` (shared fixtures), `ShareSubscriptionLifecycleWorkflowTest.kt`, `ShareSubscriptionProblemsWorkflowTest.kt`, `ShareRolloverWorkflowTest.kt`, `ShareOwnershipAndMultiYearWorkflowTest.kt`.

### File Structure
```
docs/
  share-management-workflows.md                      (new)
solawi-bid-frontend/.../module/shares/
  component/dropdown/sharestatus.dropdown.kt          (modified: + dataId)
  component/modal/modal.share-offers.upsert.kt        (modified: + dataId)
  component/modal/modal.share-subscriptions.upsert.kt (modified: + dataId)
  component/modal/modal.share-subscriptions.transfer-ownership.kt (modified: + dataId)
  component/modal/modal.share-subscriptions.attach-or-upsert-sepa-mandate.kt (modified: + dataId)
solawi-bid-frontend/.../application/ui/page/shares/
  page.share-management-for-organizations.kt          (modified: + dataId)
e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares/
  SharesTestSupport.kt                                (new)
  ShareSubscriptionLifecycleWorkflowTest.kt            (new)
  ShareSubscriptionProblemsWorkflowTest.kt             (new)
  ShareRolloverWorkflowTest.kt                         (new)
  ShareOwnershipAndMultiYearWorkflowTest.kt            (new)
```

### Risks
- Some transitions are SYSTEM/deadline-driven (`REJECTION_NOT_RESOLVED`, `UNRESOLVED_PERIOD_END`, `PERIOD_END`) and can't be reached purely by waiting in an e2e test; these will be noted as documented-but-not-automated in the test class KDoc, consistent with how `NavigationTest.kt` comments out `test_useInvalidStoredState`-style edge cases when infeasible.
- The 1768-line `page.share-management-for-organizations.kt` is dense; adding `data-id`s touches many call sites — changes will be additive-only (new optional `dataId` args) to avoid behavioural regressions.
- SEPA mandate flows depend on a configured legal entity/bank account for the provider; tests will check for and skip (with a clear message) scenarios where this precondition isn't already satisfied in the target environment, rather than trying to provision full SEPA infrastructure through the UI.

# Testing

### Validation Approach
- Run `./gradlew :e2e:test --tests "org.solyton.solawi.bid.module.shares.*"` against the local e2e environment (`script/e2e-local.sh`) to execute the new `@Workflow` suite.
- Confirm `@Workflow` tag filtering works via Gradle's JUnit tag mechanism, the same way `@Smoke`/`@Regression` are expected to be filterable.
- Manually spot-check that newly added `data-id` attributes render in the DOM (via Playwright's `page.getByDataId(...).first().isVisible`) before relying on them in assertions.

### Key Scenarios
- FY1 happy path, no SEPA mandate: create share type/offer, subscribe, provider approves -> `Subscribed`.
- FY1 happy path, with SEPA mandate: `AwaitingAhcAuthorization` -> `Subscribed` after mandate attachment.
- FY1 problem: activation rejected -> member updates -> `PendingActivation` again.
- FY1 problem: member pauses -> resumes -> `Subscribed`.
- FY1→FY2 rollover happy path: `Subscribed` -> `RollingOver`, shadow `PendingActivation` created, provider approves shadow -> original becomes `RolledOver`.
- FY1→FY2 rollover failure: attempting rollover with no FY2 offer present shows the expected error/validation state.
- FY2 ownership transfer: subscription briefly shows `Transfer`, SEPA mandate mapping removed, new owner must get a new mandate.
- FY2→FY3: a `Paused` subscription is correctly represented as not eligible for a plain rollover.

### Edge Cases
- Rollover attempted from a `Cancelled` subscription is rejected/disabled in the UI.
- Duplicate co-subscriber username triggers the documented auto-created `PENDING` user path, if reachable via UI.
- Re-running a test twice in the same environment must not collide on share type/offer names (timestamp-suffixed names, matching `UserManagementTest`'s `e2e-user-${System.currentTimeMillis()}` convention).

### Test Changes
- All new tests are additive; no existing e2e test is modified.
- SYSTEM-deadline-only transitions are explicitly excluded from automated coverage and documented as such in the workflow markdown and in test class KDoc comments.

# Delivery Steps

### * Step 1: Write the share management multi-year workflow reference document
A new `docs/share-management-workflows.md` exists describing the full FY1 -> FY2 -> FY3 share subscription lifecycle.
- Document share type/offer setup prerequisites.
- Narrate FY1 happy paths (no-mandate, SEPA-mandate, import) and every mid-year problem/resolution pair, referencing `share-status-transitions.kt` and `share-status.change-reason.kt`.
- Narrate the FY1->FY2 rollover happy path and every documented failure mode.
- Narrate FY2 ongoing operations (ownership transfer, mandate re-sync) and FY2->FY3 compounding problems.
- Include a summary table of all status transitions with actor and reason columns.

### ✓ Step 2: Add missing data-id selectors to the Share Management frontend
The shares UI exposes `data-id` attributes on every control needed by the planned e2e tests, following the `{page}.{section}.{entity}.{action}` convention from `application-management-page.kt`.
- Add `dataId` to share type create/edit controls in `ShareTypeManagement` (`page.share-management-for-organizations.kt`).
- Add `dataId` to share offer create/edit/list controls in `ShareOfferManagement`.
- Add `dataId` to share subscription create/edit/list controls in `ShareSubscriptionManagement`, including the status dropdown trigger and each status option in `sharestatus.dropdown.kt`.
- Add `dataId` to the rollover trigger, `modal.share-subscriptions.transfer-ownership.kt`, and `modal.share-subscriptions.attach-or-upsert-sepa-mandate.kt` submit/cancel buttons.
- Keep all changes additive (optional `dataId` params already supported by `button-base.kt`), with no behavioural changes.

###   Step 3: Build shared e2e test fixtures for the shares module
A `SharesTestSupport.kt` helper file exists under `e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares` providing reusable setup for all workflow tests.
- Add `providerContext()` reusing `TestUtils`'s stored `TEST_USER` session, mirroring `adminContext()` in `ApplicationManagementTest`/`UserManagementTest`.
- Add `createShareTypeViaUi(page)` and `createShareOfferViaUi(page, shareType, fiscalYear)` helpers that create uniquely-named fixtures (timestamp-suffixed, like `UserManagementTest`'s username generation).
- Add `createShareSubscriptionViaUi(page, offer)` and `dismissStrayErrorDialogIfPresent(page)` (same pattern as the other management tests).
- Add a shared `BeforeAll`/`AfterAll` companion pattern for Playwright/browser lifecycle, consistent with existing test classes.

###   Step 4: Implement FY1 new-subscription workflow tests
`ShareSubscriptionLifecycleWorkflowTest.kt` exists with `@Workflow@Test`-tagged methods covering FY1 setup through to an active subscription.
- `test_newSubscription_noMandate_becomesSubscribed`: create share type/offer, subscribe, provider approves via `NO_PAYMENT_MANDATE_REQUIRED` -> assert status `Subscribed`.
- `test_newSubscription_withSepaMandate_becomesSubscribed`: drive `PendingActivation` -> `AwaitingAhcAuthorization` -> mandate attachment -> `Subscribed`.
- `test_importedSubscription_appearsWithInternalStatus`: exercise the `External` -> internal status import path if reachable via the UI's import modal (`modal.share-subscriptions.import-share-data.kt`).
- All assertions rely on `data-id`-targeted elements and URL/status text added in the previous stage.

###   Step 5: Implement FY1 problem and resolution workflow tests
`ShareSubscriptionProblemsWorkflowTest.kt` exists with `@Workflow@Test`-tagged methods covering the documented mid-year problem/resolution pairs.
- `test_activationRejected_memberResubmits_returnsToPendingActivation`.
- `test_memberPauses_thenResumes_returnsToSubscribed`.
- `test_memberCancelsBeforeActivation_becomesCancelled` and `test_providerCancelsBeforeActivation_becomesCancelled`.
- `test_subscribedMember_cancelsActiveShare_becomesCancelled`.
- Document (in KDoc, not as a test) the SYSTEM-deadline-only transitions (`REJECTION_NOT_RESOLVED`, `UNRESOLVED_PERIOD_END`) that are out of scope for pure UI automation.

###   Step 6: Implement FY1-to-FY2 rollover workflow tests
`ShareRolloverWorkflowTest.kt` exists with `@Workflow@Test`-tagged methods covering the rollover mechanism end-to-end.
- `test_rollover_happyPath_createsShadowSubscriptionAndApprovesIt`: create FY2 offer, trigger rollover, assert original becomes `RollingOver`/`RolledOver` and shadow becomes `Subscribed`.
- `test_rollover_withoutFy2Offer_isRejectedOrDisababled`.
- `test_rollover_fromCancelledSubscription_isRejectedOrDisabled`.
- `test_rollover_memberCancelsShadowSubscription_becomesCancelled`.

###   Step 7: Implement FY2 operations and FY2-to-FY3 multi-year workflow tests
`ShareOwnershipAndMultiYearWorkflowTest.kt` exists with `@Workflow@Test`-tagged methods covering ongoing FY2 operations and compounding FY2->FY3 issues.
- `test_ownershipTransfer_resetsAhcAuthorizationAndRemovesMandateMapping`.
- `test_priceChangeOnRollover_updatesShadowSubscriptionBeforeApproval`.
- `test_pausedSubscription_atRollover_isNotEligibleForPlainRollover`, reflecting the documented FY2->FY3 `Paused -> Expired` compounding concern.
- Skip (with a clear message) any scenario whose SEPA/legal-entity precondition is not already satisfied in the target environment, per the Risks section of the Technical Design.