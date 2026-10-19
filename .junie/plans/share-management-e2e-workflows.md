---
sessionId: session-261016-222008-1hqr
---

# Overview & Goals

### Overview & Goals

This plan covers the first phase of a two-phase effort to gain e2e test coverage for the **Share Management** module of the solawi-bid application.

**Phase 1 (this plan):** Produce a comprehensive, human-readable document that describes share subscription workflows across **2–3 consecutive fiscal years**, together with the problems that may arise at each stage and their documented resolutions. This document will serve as the authoritative reference for Phase 2 e2e test authoring.

**Phase 2 (future plan):** Translate the workflow descriptions into executable Playwright (Kotlin) e2e tests inside the `e2e/` module, following the existing patterns (`LoginTest`, `NavigationTest`, `data-id` selectors).

### What the document will cover
- Share type and share offer lifecycle per fiscal year
- Full share subscription status machine with all meaningful actor-triggered transitions
- Multi-year rollover: the happy path and every documented failure mode
- SEPA mandate creation, attachment, and SEPA payment template synchronisation in context of the subscription lifecycle
- Ownership transfer and co-subscriber management
- Edge cases arising from year-to-year state carry-over

# Workflows

### Core Concepts

 Concept | Description |
---|---|
 **Fiscal Year** | Time-bounded period (e.g. 2024-01-01 – 2024-12-31). Tied to share offers. |
 **Share Type** | Named category of share provided by an organisation (`providerId`). |
 **Share Offer** | A specific share type available for a specific fiscal year. Unique per `(shareType, fiscalYear)`. |
 **Share Subscription** | A member's claim to one or more shares from an offer for a fiscal year. |
 **Status Machine** | The subscription transitions through a defined set of states (see below). |
 **Rollover** | The mechanism that carries an active subscription into the next fiscal year by creating a new `PendingActivation` subscription linked to the `RollingOver` predecessor. |
 **SEPA Mandate** | A direct-debit authorisation linked to the subscription via `SepaMandateDataMapping`. |

---

### Share Status State Machine

```
External ──(IMPORT)──► PendingActivation
                            │
              ┌─────────────┼──────────────────────────┐
              ▼             ▼                          ▼
     ActivationRejected  AwaitingAhcAuthorization  ClearedForAuction
              │             │                          │
              │        (auth ok)                  (approved)
              │             ▼                          ▼
              └────────► Subscribed ◄─────────────────┘
                            │
         ┌──────────────────┼──────────────────────────┐
         ▼                  ▼                          ▼
      Paused           PaymentFailed              RollingOver ──► RolledOver (terminal)
         │                  │                          │
      (resume)          (recover)              (user requests activation)
         ▼                  ▼                          ▼
      Subscribed         Subscribed              PendingActivation (Year N+1)

Terminal states: Cancelled, Expired, RolledOver
```

---

### Year-by-Year Workflow Narratives

#### Fiscal Year 1 — New Subscription

**Setup (Provider)**
1. Provider creates a `ShareType` (e.g. "Gemüseanteil").
2. Provider creates a `ShareOffer` for FY1, choosing `FIXED` or `FLEXIBLE` pricing.

**Happy path — no payment mandate required**
1. Member subscribes: `createShareSubscription` → status = `PendingActivation`.
2. Provider approves: `next(PendingActivation → Subscribed, NO_PAYMENT_MANDATE_REQUIRED, PROVIDER)` → member holds an active share for FY1.
3. Member receives produce at the assigned `DistributionPoint` for the full year.
4. Near year-end: Provider rolls over the subscription → status = `RollingOver`; a shadow subscription for FY2 is created with status = `PendingActivation`.

**Happy path — with SEPA mandate**
1. `PendingActivation` → `AwaitingAhcAuthorization` (PROVIDER, PAYMENT_MANDATE_REQUESTED).
2. Member signs SEPA mandate; `createSepaMandateWithRetry` creates the mandate entity.
3. AHC authorisation completed → `AwaitingAhcAuthorization → Subscribed` (PROVIDER/SYSTEM, AUTHORIZATION_COMPLETED).
4. `attachSepaMandate` links mandate to subscription and synchronises the SEPA payment template.

**Import path (External)**
1. Existing member data imported via `shares.subscriptions.import` → status transitions from `External` to any `internalStatus` (PROVIDER, IMPORT).

**Problems in FY1**

 Problem | Trigger | Resolution |
---|---|---|
 Activation rejected | Requirements not met | Member updates → `ActivationRejected → PendingActivation` (USER, NEW_REQUIREMENTS) |
 AHC authorisation failed | Bank rejects mandate | `AwaitingAhcAuthorization → Suspended` (PROVIDER/SYSTEM, AUTHORIZATION_FAILED) → resolve issue → `Subscribed` |
 Payment failed | SEPA debit returned | `Subscribed → PaymentFailed` → member/provider resolves → `Subscribed` or `Suspended` |
 Member pauses | User decision | `Subscribed → Paused` (USER, USER_PAUSED) → `Paused → Subscribed` (USER, RESUME) |
 Member cancels before activation | User decision | `PendingActivation → Cancelled` (USER, USER_CANCELLED_BEFORE_ACTIVATION) |
 Provider cancels before activation | Admin decision | `PendingActivation → Cancelled` (PROVIDER, PROVIDER_CANCELLED_BEFORE_ACTIVATION) |
 Member cancels active share | User decision | `Subscribed → Cancelled` (USER/PROVIDER, USER_CANCEL) |
 Rejection not resolved | Deadline passed | `ActivationRejected → Expired` (SYSTEM, REJECTION_NOT_RESOLVED) |
 Unresolved payment / suspension | Deadline passed | `Suspended → Expired` (SYSTEM, UNRESOLVED_PERIOD_END) |

---

#### Fiscal Year 1 → FY2 Transition — Rollover

**Provider creates FY2 offer**
1. Provider creates a new `ShareOffer` for FY2 (same `ShareType`, new `FiscalYear`). This is a prerequisite for rollover.

**Happy path rollover**
1. Provider (or SYSTEM) calls `rollover(subscriptionId, toShareOfferId=FY2Offer, PROVIDER/SYSTEM, NEW_PERIOD)`.
2. Original subscription: `Subscribed → RollingOver`.
3. Shadow subscription created: `PendingActivation` (FY2, same `userProfile`, `distributionPoint`, `numberOfShares`, `pricePerShare`).
4. Member confirms continuation: `RollingOver → PendingActivation` (USER, USER_EVENTUAL_CHANGE). *(Note: the shadow is already in PendingActivation; this step refers to the original subscription being acknowledged by the user.)*
5. Provider approves shadow: `PendingActivation → Subscribed` (PROVIDER, SUBSCRIPTION_APPROVED) → Original automatically transitions `RollingOver → RolledOver`.

**Rollover failure modes**

 Problem | Cause | Resolution |
---|---|---|
 Cannot roll over from `Cancelled` | Subscription already terminated | Start fresh subscription in FY2 |
 Cannot roll over via USER | Only PROVIDER/SYSTEM may initiate rollover | Use PROVIDER or SYSTEM role |
 FY2 offer does not exist | Provider forgot to create it | Provider creates FY2 `ShareOffer` first |
 Member cancels after rollover | Member opts out of FY2 | Shadow `PendingActivation → Cancelled` (USER, USER_CANCELLED_BEFORE_ACTIVATION) |
 SEPA mandate not transferred | Mandate linked to FY1 offer | Provider attaches existing or new mandate to FY2 shadow subscription after rollover |
 Price change in FY2 | New pricing for next year | Provider updates shadow subscription before approval; SEPA payment template re-synced |
 Co-subscribers not copied | Rollover copies main subscriber only | Provider re-adds co-subscribers to shadow subscription |

---

#### Fiscal Year 2 — Ongoing Operations

**Ownership transfer (mid-year)**
1. `transfer(PROVIDER, subscriptionId, newOwnerId)`: status briefly becomes `Transfer` then returns to previous status.
2. `ahcAuthorized` is reset to `false`; existing `SepaMandateDataMapping` for the subscription is **deleted** and payment templates re-synced.
3. Provider must attach a new mandate for the new owner if SEPA payment is required.

**Problems in FY2**

 Problem | Cause | Resolution |
---|---|---|
 Mandate reference collision | Two mandates generated with same reference | `createSepaMandateWithRetry` retries up to 5 times with a new suffix |
 Old mandate still active after transfer | Mandate mapping deleted but mandate entity not revoked | Provider explicitly revokes old mandate via SEPA management |
 Duplicate co-subscriber username | Co-subscriber not yet a registered user | System auto-creates a `PENDING` user entity for the username |
 Payment template out of sync | Number of shares or price changed | `updateShareSubscription` automatically calls `updateSepaPaymentTemplate` when `totalAmount` changes |

---

#### Fiscal Year 2 → FY3 Transition

Same as FY1 → FY2, but **compounding concerns** may arise:

 Problem | Cause | Resolution |
---|---|---|
 Multiple active mandates | Mandate from FY1 never cleaned up | Provider reviews mandate list and deactivates stale mandates |
 Subscription in `Paused` state at rollover | User has paused before period end | `Paused → Expired` (SYSTEM, PERIOD_END); member must re-subscribe in FY3 |
 `Suspended` at year-end | Unresolved payment/issue | `Suspended → Expired` (SYSTEM, UNRESOLVED_PERIOD_END); start fresh in FY3 |
 `PaymentFailed` at rollover time | Late payment failure | Resolve payment first, then rollover; or cancel and start fresh |

---

### SEPA Mandate Reference Generation

The reference format is `{prefix}-{year}-{counter}` (e.g. `MANDAT-2024-001`).
- Counter is derived from the highest counter already stored for that creditor in the current year.
- If a unique constraint collision occurs, `createSepaMandateWithRetry` retries up to 5 times.
- `reUseActiveMandate=true` (default) means if an active mandate with the same prefix already exists for that bank account, it is reused rather than creating a duplicate.

# Technical Context

### Relevant Source Files

 Area | File |
---|---|
 Status machine definition | `solawi-bid-api-data/.../share-status-transitions.kt` |
 Status values & `from()` | `solawi-bid-api-data/.../share-status.kt` |
 Change reasons & actors | `solawi-bid-api-data/.../share-status.change-reason.kt` |
 Rollover & status transitions | `solawi-bid-backend/.../share-status.repository.kt` |
 Subscription CRUD | `solawi-bid-backend/.../share-repository.kt` |
 SEPA mandate creation/attachment | `solawi-bid-backend/.../sepa.mandates.repository.kt` |
 Subscription schema | `solawi-bid-backend/.../bid.share-subscription.kt` |
 Rollover API action | `solawi-bid-backend/.../shares.subscriptions.rollover.kt` |
 Existing unit tests | `solawi-bid-backend/src/test/.../ShareStatusRolloverTests.kt` |
 Existing unit tests | `solawi-bid-backend/src/test/.../ShareStatusRepositoryTest.kt` |
 E2e test harness | `e2e/src/test/kotlin/` (Playwright, Kotlin, `data-id` selectors) |

### Key Backend Facts for e2e Test Planning
- Subscriptions and share offers are **linked to a specific `FiscalYear` entity** — tests must create fiscal year records as preconditions.
- Share offer uniqueness constraint: one offer per `(shareType, fiscalYear)` pair.
- Rollover creates a **new** subscription entity; the old one stays in `RollingOver`/`RolledOver`.
- History is tracked in `ShareSubscriptionStatusHistory` with optional `rollingOverFromShareSubscriptionId` for tracing rollover chains.
- Ownership transfer **deletes** `SepaMandateDataMapping` for the transferred subscription.
- SEPA payment templates are auto-synced on: rollover, attachment, transfer, and share amount/price changes.

# Delivery Steps

###   Step 1: Write multi-year workflow description document
A Markdown document (`docs/share-management-workflows.md`) is created that narrates the full share management lifecycle across FY1, FY1→FY2, FY2, and FY2→FY3 transitions.

- Document **Share Type and Share Offer setup** steps that must precede any subscription.
- Describe the **happy-path subscription flow** (no-mandate and with-SEPA-mandate variants) for FY1.
- Describe the **import path** from `External` status.
- Document every **mid-year problem** (activation rejection, AHC failure, payment failure, pause, cancellation, suspension, expiry) with its trigger, affected status transition, and resolution steps — referenced to `share-status-transitions.kt`.
- Describe the **rollover mechanism** (FY1→FY2): prerequisites, step-by-step state changes for both the original and the shadow subscription, and how the `rollingOverFromShareSubscriptionId` history link is created.
- Document every **rollover failure mode** (invalid source status, missing FY2 offer, SEPA mandate gap, co-subscriber loss, price change handling).
- Describe **ongoing FY2 operations**: ownership transfer (mandate deletion + re-sync), mid-year co-subscriber updates, payment recovery.
- Describe **FY2→FY3 compounding problems**: stale mandates, paused/suspended subscriptions at year-end, late payment failures.
- Include a summary table of all status transitions used in the workflows with actor and reason columns.

###   Step 2: Define e2e test scenarios catalogue
A companion Markdown file (`docs/share-management-e2e-scenarios.md`) that maps the workflow narrative to concrete, numbered test scenarios ready for Phase 2 implementation.

- One scenario per significant workflow path (happy path + each problem/resolution pair).
- Each scenario entry specifies: **Preconditions** (fiscal years, share type, offer, user accounts, mandates), **Steps** (UI actions via `data-id` selectors or API calls), **Expected outcomes** (status displayed, history entries, mandate links).
- Scenarios are grouped by fiscal year phase for easy sequencing during implementation.
- Mark scenarios that require **seeded test data** vs. those that are fully UI-driven.
- Identify which scenarios can reuse the shared `loggedInState.json` session pattern from `TestUtils` and which need a provider-role session.
- Note frontend `data-id` selectors that need to be **added** to the shares UI before the test can be written (gap analysis between existing selectors and required ones).