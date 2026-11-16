# Prototype: State-based E2E workflows and a business-readable report

This is an **isolated proof of concept**, not a blanket conversion of the E2E suite. It changes just one scenario:

- [ShareSubscriptionApprovalWorkflowTest.kt](../e2e/src/test/kotlin/org/solyton/solawi/bid/test/module/shares/ShareSubscriptionApprovalWorkflowTest.kt)
- [E2eState.kt](../e2e/src/test/kotlin/org/solyton/solawi/bid/state/E2eState.kt)
- [E2eReportTest.kt](../e2e/src/test/kotlin/org/solyton/solawi/bid/state/E2eReportTest.kt)

## Layers and responsibilities

| Layer | Responsibility |
| --- | --- |
| OOP Page Objects (unchanged) | Playwright locators, forms, dialogs, clicks, browser UI details |
| Existing TestWorkflow classes (unchanged) | Reusable business operations, with typed inputs and outputs |
| E2eEnvironment | Owns Playwright user contexts and closes them once per scenario |
| E2eState | Carries the active TestUserContext, environment, and reporting sink |
| evoleq State + Kleisli | Sequences operations and passes typed workflow results to later steps |
| E2eReport | Records business steps and independent assertion results as Markdown |
| JUnit 5 | Executes the scenario, determines pass/fail, and emits standard XML/HTML results |

Existing session management and browser setup in the test class remain unchanged except that the new E2eEnvironment owns user sessions. The existing TestUtils cookie setup remains.

## How the scenario reads

The scenario uses the same State/Kleisli composition as the backend routes:

~~~kotlin
State<E2eState, ApprovalFixture>(fixture) *
CreateMember() *
SwitchUser<MemberScenario> { it.user } *
PrepareShares() *
Subscribe() *
Approve() *
VerifyApproval() runOn initial
~~~

The named arrows are small adapters over existing workflows, for example:

~~~kotlin
private fun CreateMember(): E2eStep<ApprovalFixture, MemberScenario> =
    (workflow(::CreateTestUserWorkflow) reportedAs "Admin creates a test user")
        .retaining(ApprovalFixture::createUser, ::MemberScenario)
~~~

`workflow(constructor)` is curried: it produces `I -> State<E2eState, O>`.
The input arrives through composition; the active browser context is supplied at execution.
`reportedAs` decorates that arrow without changing its input/output types.
`retaining(select, combine)` focuses on a workflow input and combines its result with
its original surrounding data, propagating the workflow's returned state.

The scenario uses typed phases: fixture → member → prepared environment → pending
subscription → approved subscriptions. Each phase retains its predecessor and adds
the workflow result. There are no nullable result slots or captured subscription values.
`SwitchUser` changes the browser context while passing its entire input through unchanged.
`verifyAll` gives each independent expectation the same typed input and also passes it
through on success. Neither operation replaces useful data with `Unit`.

The operator is **evoleq's existing State/Kleisli composition**, not a new workflow engine.
The final checks verify both the domain status and the UI status.

## Reporting

After execution, including on an exception, the test attempts to write:

**e2e/build/reports/tests/scenarios/share-subscription-approval.md**

The table captures business-readable step titles, pass/fail status and durations. The final independent expectations are **both evaluated** before a combined assertion is raised. This is *applicative-style accumulation*, not a full Applicative abstraction yet.

The report intentionally omits user names, tokens, passwords, request bodies and raw exception messages. Technical stack traces remain in JUnit output.

## Testing and running

Unit verification (no browser required):

`E2eCompositionTest` covers deferred workflow execution, retained payloads, returned
state propagation, user switching and cleanup, independent checks, prerequisite
failures, and cancellation. Playwright interfaces use strict in-memory stubs.

~~~shell
./gradlew :e2e:test --tests 'org.solyton.solawi.bid.state.*'
~~~

Full isolated E2E scenario (needs the dedicated E2E environment):

~~~shell
./script/e2e-local.sh up
E2E_BASE_URL=http://localhost:18080 ./gradlew :e2e:test --tests 'org.solyton.solawi.bid.test.module.shares.ShareSubscriptionApprovalWorkflowTest'
~~~

When using the standard E2E helper, follow [e2e/README.md](../e2e/README.md) for database isolation, seeded users and environment setup. The E2E scenario has not been validated against a live browser as part of this spike.

## Known limitations and follow-up work

1. **Failure diagnostics:** The existing withFailureDiagnostics implementation writes sensitive data (including passwords) to its legacy log files. The *new* report does not, but the legacy diagnostics need separate redaction and a clear retention policy.
2. **Error semantics:** evoleq State propagates exceptions, so a failed prerequisite aborts the dependent workflow chain. A mutable report sink preserves earlier entries across the failure. More structured error handling and cancellation policies can follow.
3. **Validation algebra:** The current check combiner is an accumulator of independent, sequential assertions, not a generic Applicative instance. Prove the utility before introducing a more general algebra.
4. **Browser concurrency:** Never evaluate independent checks concurrently on the same Playwright Page. Independence in validation refers to absence of data dependence, not guaranteed thread-safety.
5. **Test ownership:** Tests still control their browser lifecycle. A future JUnit 5 extension could create E2eEnvironment, publish attachments, and clean up even when setup fails.
6. **Run status:** Local Gradle verification was attempted but the wrapper distribution download failed because `services.gradle.org` is unreachable in this environment. Compilation, unit tests, and the browser scenario still require CI or a configured local environment.

## Why this belongs in the book

This example demonstrates how **OOP for integration boundaries** and **FP for business-level composition** can complement one another. A manager can read the steps, a developer can inspect each workflow and Page Object, and an AI coding agent can receive narrowly scoped implementation tasks.

The name **evoleq** derives from *evolution equation*: a fitting connection to the view of applications and tests as discrete, input-driven dynamical systems.
