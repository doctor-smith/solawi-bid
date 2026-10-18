# Share Management: Multi-Year Workflow Reference

This document narrates the full lifecycle of a share subscription across fiscal years (FY1 → FY2 → FY3), as
implemented by the status machine in
`solawi-bid-api-data/src/commonMain/kotlin/org/solyton/solawi/bid/module/shares/data/internal/share-status-transitions.kt`
and the reasons enumerated in
`solawi-bid-api-data/src/commonMain/kotlin/org/solyton/solawi/bid/module/shares/data/internal/share-status.change-reason.kt`.

It is the authoritative narrative reference used to derive the `@Workflow@Test`-tagged Playwright/Kotlin e2e tests
under `e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares`.

## 1. Prerequisites: Share Type & Offer Setup

Before any subscription can exist, a provider (an organization acting as `ProviderId`) must set up:

1. **Share Type** — a reusable definition of what is being offered (e.g. "Vegetable Share", "Vegetable + Eggs").
   Created once via `ShareTypeManagement` on `ShareManagementForOrganizationsPage`.
2. **Share Offer** — a concrete, fiscal-year-scoped offer of a share type (price, quantity, period). Created via
   `ShareOfferManagement`, always tied to exactly one fiscal year. A new fiscal year requires a new offer even if the
   share type is reused.
3. **Share Subscription** — a member's (user's) subscription to a specific offer, with its own status lifecycle
   managed via `ShareSubscriptionManagement`.

Each fiscal year (FY1, FY2, FY3, ...) therefore has its own set of offers; subscriptions are rolled over from one
fiscal year's offer to the next year's offer rather than being edited in place (see Section 3, Rollover).

## 2. FY1: New Subscription Workflows

A new subscription always starts in `PendingActivation` (or, for imported legacy data, in `External`).

### 2.1 Happy path — no payment mandate required

```
PendingActivation --(PROVIDER | SYSTEM, NO_PAYMENT_MANDATE_REQUIRED)--> Subscribed
```

The member subscribes to an offer; the provider determines no SEPA mandate is required (e.g. cash/bank-transfer
payment) and directly approves the subscription. The subscription now shows as `Subscribed`.

### 2.2 Happy path — with SEPA mandate

```
PendingActivation --(PROVIDER | SYSTEM, PAYMENT_MANDATE_REQUESTED)--> AwaitingAhcAuthorization
AwaitingAhcAuthorization --(PROVIDER | SYSTEM, AUTHORIZATION_COMPLETED)--> Subscribed
```

The provider requests a payment mandate; the subscription moves to `AwaitingAhcAuthorization` while the member
attaches/authorizes a SEPA mandate (via `modal.share-subscriptions.attach-or-upsert-sepa-mandate.kt`). Once the
authorization completes, the subscription becomes `Subscribed`.

If the authorization fails instead:

```
AwaitingAhcAuthorization --(PROVIDER | SYSTEM, AUTHORIZATION_FAILED)--> Suspended
```

### 2.3 Happy path — auction-cleared subscription

```
PendingActivation --(PROVIDER, SUBSCRIPTION_APPROVED)--> ClearedForAuction
ClearedForAuction --(PROVIDER, SUBSCRIPTION_APPROVED)--> Subscribed
```

Used when the provider wants subscriptions vetted through an auction/allocation step before final approval.
`ClearedForAuction` can also fall back to `PendingActivation` (`REQUIREMENTS_NOT_MET`) or escalate to `Suspended`
(`AUTO_SUSPENSION` / `ADMIN_ACTION`).

### 2.4 Imported subscriptions

```
External --(PROVIDER, IMPORT)--> <any internal status>
```

Legacy/external subscription data can be imported directly into any internal status (not just `PendingActivation`),
via `modal.share-subscriptions.import-share-data.kt`. This is the only transition where a subscription can jump
straight to e.g. `Subscribed` or `Paused` without passing through the normal flow states.

## 3. FY1: Mid-Year Problems & Resolutions

### 3.1 Activation rejected → member resubmits

```
PendingActivation --(PROVIDER, REQUIREMENTS_NOT_MET)--> ActivationRejected
ActivationRejected --(USER, NEW_REQUIREMENTS)--> PendingActivation
```

The provider rejects the pending activation (e.g. missing information). The member updates their data/requirements
and is placed back into `PendingActivation`, restarting the FY1 happy-path flow.

**Not e2e-testable without time manipulation:** if the member never resolves the rejection, a deadline-driven
SYSTEM/PROVIDER transition fires:

```
ActivationRejected --(PROVIDER | SYSTEM, REJECTION_NOT_RESOLVED)--> Expired
```

### 3.2 Member pauses → resumes

```
Subscribed --(USER, USER_PAUSED)--> Paused
Paused --(USER, RESUME)--> Subscribed
```

A subscribed member can pause (e.g. seasonal break) and later resume, returning to `Subscribed`.

**Not e2e-testable without time manipulation:** if a paused subscription is never resumed before the fiscal period
ends, the system expires it automatically:

```
Paused --(SYSTEM, PERIOD_END)--> Expired
```

### 3.3 Cancellation before activation

```
PendingActivation --(PROVIDER, PROVIDER_CANCELLED_BEFORE_ACTIVATION)--> Cancelled
PendingActivation --(USER, USER_CANCELLED_BEFORE_ACTIVATION)--> Cancelled
```

Either the provider or the member can cancel a subscription that has not yet been activated. Also reachable from
`ActivationRejected`:

```
ActivationRejected --(USER | PROVIDER | SYSTEM, USER_OR_PROVIDER_CANCEL)--> Cancelled
```

### 3.4 Cancellation of an active subscription

```
Subscribed --(USER | PROVIDER, USER_CANCEL)--> Cancelled
```

A member (or the provider on the member's behalf) can cancel an already-`Subscribed` share at any time during the
fiscal year.

### 3.5 Payment failures

```
Subscribed --(PROVIDER | SYSTEM, PAYMENT_FAILED)--> PaymentFailed
PaymentFailed --(PROVIDER, PAYMENT_RECOVERED)--> Subscribed
PaymentFailed --(PROVIDER, PAYMENT_NOT_RESOLVED | SYSTEM)--> Suspended
PaymentFailed --(PROVIDER, USER_CANCEL_AFTER_FAILURE)--> Cancelled
```

A SEPA mandate failure (e.g. insufficient funds, mandate revoked) moves a `Subscribed` share to `PaymentFailed`. The
provider can mark it recovered (back to `Subscribed`), escalate to `Suspended` if unresolved, or cancel it.

### 3.6 Suspension & resolution

```
Subscribed --(PROVIDER | SYSTEM, AUTO_SUSPENSION)--> Suspended
Suspended --(PROVIDER | SYSTEM, ISSUE_RESOLVED)--> Subscribed | ClearedForAuction
Suspended --(USER | PROVIDER | SYSTEM, TERMINATION)--> Cancelled
```

**Not e2e-testable without time manipulation:**

```
Suspended --(SYSTEM, UNRESOLVED_PERIOD_END)--> Expired
```

## 4. FY1 → FY2 Rollover

Rollover moves an active subscription from one fiscal year's offer into the next fiscal year's equivalent offer,
without losing history. It always starts from `Subscribed`:

```
Subscribed --(PROVIDER | SYSTEM, NEW_PERIOD)--> RollingOver
```

### 4.1 Happy path

When a provider (or the system, at period boundaries) triggers a rollover:

1. The original FY1 subscription transitions `Subscribed -> RollingOver`.
2. A **shadow subscription** is created in FY2's offer, starting in `PendingActivation`.
3. The provider reviews/approves the FY2 shadow subscription (same FY1 happy-path rules apply: `NO_PAYMENT_MANDATE_REQUIRED` /
   `AUTHORIZATION_COMPLETED`), which drives it to `Subscribed`.
4. Approving the shadow subscription also resolves the original `RollingOver` subscription to `RolledOver`:

```
RollingOver --(PROVIDER | SYSTEM, ROLLED_OVER)--> RolledOver
```

`RolledOver` is a terminal status — the FY1 record now exists only as history; the FY2 shadow carries the live
subscription forward.

### 4.2 Member requests a change during rollover

```
RollingOver --(USER, USER_EVENTUAL_CHANGE)--> PendingActivation
```

If the member wants to change something about the rolled-over subscription (e.g. switch share type/quantity) before
it is finalized, the FY1 record itself moves back to `PendingActivation` rather than silently carrying forward.

### 4.3 Failure modes

- **No FY2 offer exists yet:** triggering rollover without a corresponding next-fiscal-year offer must be
  rejected/disabled in the UI — there is no valid target for the shadow subscription.
- **Rollover from a non-`Subscribed` status (e.g. `Cancelled`):** the state machine has no `RollingOver` transition
  defined for `Cancelled` (`Cancelled` has `emptySet()` as its transition set), so the UI must reject or disable the
  rollover action for cancelled subscriptions.
- **Member cancels the FY2 shadow subscription** before it is approved: the shadow (still `PendingActivation`)
  follows the normal FY1 cancellation path (`USER_CANCELLED_BEFORE_ACTIVATION` → `Cancelled`), while the original
  FY1 subscription remains `RollingOver` (orphaned) — this is a scenario worth surfacing to the provider.

## 5. FY2: Ongoing Operations

### 5.1 Ownership transfer

```
Subscribed --(PROVIDER, TRANSFER_OWNERSHIP)--> Transfer
Transfer --(PROVIDER, TRANSFER_OWNERSHIP)--> PendingActivation | Subscribed
```

When a provider transfers a subscription to a new owner (e.g. co-subscriber takes over), the subscription briefly
enters `Transfer`. As part of the transfer:

- Any existing SEPA mandate mapping for the previous owner is removed.
- The AHC authorization state is reset, since the new owner has not yet authorized anything.
- The subscription then moves either back to `PendingActivation` (new owner must complete the full activation flow,
  including a new SEPA mandate if required) or directly to `Subscribed` if no new authorization is needed.

Transfer is also reachable directly from `PendingActivation`:

```
PendingActivation --(PROVIDER, TRANSFER_OWNERSHIP)--> Transfer
```

### 5.2 SEPA mandate re-sync

A subscription's SEPA mandate can be (re-)attached or updated at any point via
`modal.share-subscriptions.attach-or-upsert-sepa-mandate.kt`, most commonly while in `AwaitingAhcAuthorization` (new
subscription) or after an ownership transfer resets the mandate mapping (Section 5.1). This does not have its own
dedicated `ShareStatus` — it is a precondition for the `AUTHORIZATION_COMPLETED` transition.

## 6. FY2 → FY3: Compounding Problems

The same rollover mechanism (Section 4) applies again from FY2 to FY3, but issues from FY2 can compound:

- **A `Paused` subscription at rollover time:** `Paused` has no direct `RollingOver` transition in the state
  machine — only `Subscribed`, `Expired` (via `RESUME`, `PERIOD_END`), and `Cancelled` are reachable from `Paused`.
  This means a subscription left `Paused` through the FY2→FY3 boundary is **not eligible for a plain rollover**; the
  member must first `RESUME` (back to `Subscribed`) before a rollover can be triggered, or the subscription will
  eventually expire (`PERIOD_END`, SYSTEM-deadline-only, not e2e-testable without time manipulation).
- **A `Suspended` subscription at rollover time:** similarly has no `RollingOver` transition; it must first be
  resolved (`ISSUE_RESOLVED` → `Subscribed`/`ClearedForAuction`) or it will eventually expire
  (`UNRESOLVED_PERIOD_END`, SYSTEM-deadline-only).
- **Price changes on rollover:** the FY3 shadow subscription is created against the FY3 offer's current price; if
  the provider changes the FY3 offer's price after creating the shadow but before the member/provider approves it,
  the shadow subscription reflects the updated price at approval time (no separate `ShareStatus` transition — this
  is a data-level concern on the shadow subscription, not a status-machine concern).
- **A `RollingOver` subscription whose shadow was cancelled (Section 4.3) is never resolved:** it remains in
  `RollingOver` until a human (provider) notices and manually intervenes (e.g. creates a new shadow or reverts via
  `USER_EVENTUAL_CHANGE`); there is no automatic SYSTEM transition out of `RollingOver` other than `ROLLED_OVER`.

## 7. Summary Table: All Status Transitions

| From | To | Actor(s) | Reason(s) |
|---|---|---|---|
| External | *(any internal status)* | PROVIDER | IMPORT |
| PendingActivation | ActivationRejected | PROVIDER | REQUIREMENTS_NOT_MET |
| PendingActivation | AwaitingAhcAuthorization | PROVIDER, SYSTEM | PAYMENT_MANDATE_REQUESTED |
| PendingActivation | ClearedForAuction | PROVIDER | SUBSCRIPTION_APPROVED |
| PendingActivation | Subscribed | PROVIDER | NO_PAYMENT_MANDATE_REQUIRED, SUBSCRIPTION_APPROVED |
| PendingActivation | Subscribed | SYSTEM | NO_PAYMENT_MANDATE_REQUIRED, PAYMENT_MANDATE_APPROVED |
| PendingActivation | Transfer | PROVIDER | TRANSFER_OWNERSHIP |
| PendingActivation | Cancelled | PROVIDER | PROVIDER_CANCELLED_BEFORE_ACTIVATION |
| PendingActivation | Cancelled | USER | USER_CANCELLED_BEFORE_ACTIVATION |
| ActivationRejected | PendingActivation | USER | NEW_REQUIREMENTS |
| ActivationRejected | Expired | PROVIDER, SYSTEM | REJECTION_NOT_RESOLVED *(not e2e-testable)* |
| ActivationRejected | Cancelled | USER, PROVIDER, SYSTEM | USER_OR_PROVIDER_CANCEL |
| AwaitingAhcAuthorization | Subscribed | PROVIDER, SYSTEM | AUTHORIZATION_COMPLETED |
| AwaitingAhcAuthorization | Suspended | PROVIDER, SYSTEM | AUTHORIZATION_FAILED |
| AwaitingAhcAuthorization | Cancelled | USER | USER_CANCEL |
| ClearedForAuction | Subscribed | PROVIDER | SUBSCRIPTION_APPROVED |
| ClearedForAuction | PendingActivation | PROVIDER | REQUIREMENTS_NOT_MET |
| ClearedForAuction | Suspended | SYSTEM | AUTO_SUSPENSION |
| ClearedForAuction | Suspended | PROVIDER | ADMIN_ACTION |
| Subscribed | RollingOver | PROVIDER, SYSTEM | NEW_PERIOD |
| Subscribed | Paused | USER | USER_PAUSED |
| Subscribed | PaymentFailed | PROVIDER, SYSTEM | PAYMENT_FAILED |
| Subscribed | Transfer | PROVIDER | TRANSFER_OWNERSHIP |
| Subscribed | Suspended | PROVIDER, SYSTEM | AUTO_SUSPENSION |
| Subscribed | Expired | PROVIDER, SYSTEM | SUBSCRIPTION_END |
| Subscribed | Cancelled | USER, PROVIDER | USER_CANCEL |
| PaymentFailed | Subscribed | PROVIDER | PAYMENT_RECOVERED |
| PaymentFailed | Suspended | PROVIDER, SYSTEM | PAYMENT_NOT_RESOLVED |
| PaymentFailed | Cancelled | PROVIDER | USER_CANCEL_AFTER_FAILURE |
| Paused | Subscribed | USER | RESUME |
| Paused | Expired | SYSTEM | PERIOD_END *(not e2e-testable)* |
| Paused | Cancelled | USER | USER_CANCEL |
| Suspended | Subscribed | PROVIDER, SYSTEM | ISSUE_RESOLVED |
| Suspended | ClearedForAuction | PROVIDER, SYSTEM | ISSUE_RESOLVED |
| Suspended | Expired | SYSTEM | UNRESOLVED_PERIOD_END *(not e2e-testable)* |
| Suspended | Cancelled | USER, PROVIDER, SYSTEM | TERMINATION |
| Expired | PendingActivation | USER | NEW_PERIOD |
| RollingOver | PendingActivation | USER | USER_EVENTUAL_CHANGE |
| RollingOver | RolledOver | PROVIDER, SYSTEM | ROLLED_OVER |
| Transfer | PendingActivation | PROVIDER | TRANSFER_OWNERSHIP |
| Transfer | Subscribed | PROVIDER | TRANSFER_OWNERSHIP |
| Cancelled | *(terminal)* | — | — |
| RolledOver | *(terminal)* | — | — |

## 8. Out-of-Scope for Pure UI Automation

The following transitions are driven purely by wall-clock deadlines (`SYSTEM` actor, time-based `ChangeReason`s) and
cannot be triggered by waiting inside an e2e test without manipulating system time or invoking a backend job
directly:

- `ActivationRejected -> Expired` (`REJECTION_NOT_RESOLVED`)
- `Paused -> Expired` (`PERIOD_END`)
- `Suspended -> Expired` (`UNRESOLVED_PERIOD_END`)
- `Subscribed -> Expired` (`SUBSCRIPTION_END`) when driven by `SYSTEM` rather than `PROVIDER`

These are documented here for completeness but are only exercised in e2e tests where a backend action/endpoint can
be triggered directly; otherwise they are explicitly out of scope, consistent with the Risks section of the
technical design.
