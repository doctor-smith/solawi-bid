# Share Management E2E Agent — Operating Rules

## Mission

Implement and verify comprehensive end-to-end tests for Share Management,
covering subscription lifecycles, state transitions, and multi-year rollovers.

Use the existing plans and documentation as the starting point.

Existing resources:

- .junie/plans/share-management-e2e-workflows.md
- .junie/plans/shares-module-e2e-workflow-tests.md
- docs/share-management-workflows.md

Primary implementation references:

- solawi-bid-api-data/src/commonMain/kotlin/org/solyton/solawi/bid/module/shares/data/internal/share-status-transitions.kt
- solawi-bid-api-data/src/commonMain/kotlin/org/solyton/solawi/bid/module/shares/data/internal/share-status.change-reason.kt

Target E2E test directory:

- e2e/src/test/kotlin/org/solyton/solawi/bid/module/shares

## 1. Human supervision is mandatory

The user understands the application and can navigate it manually.

Ask the user for assistance whenever application-specific knowledge is
needed. Do not spend excessive time guessing how the UI behaves.

STOP and ask before proceeding when:

- The correct UI workflow or navigation is unclear.
- An expected action is missing, disabled, or behaves unexpectedly.
- Authentication, permissions, or organization membership blocks progress.
- Required providers, members, fiscal years, offers, or other test data
  cannot be created or identified safely.
- The expected result conflicts with the implementation or documentation.
- A workflow could modify or delete valuable data.
- A test depends on an unavailable external service, payment authorization,
  or other environment-specific behavior.
- The environment appears unstable or memory pressure is increasing.
- A backend action, database modification, or system-time manipulation
  would be necessary.
- A meaningful implementation decision requires assumptions about business
  rules that cannot be established from the code.

When asking, provide:

1. What you were trying to do.
2. What you observed.
3. What you expected.
4. What you have already checked.
5. The exact question or manual action needed from the user.

Do not ask questions that can be answered directly by inspecting the
repository or existing tests.

After the user provides guidance, record the answer in the appropriate
project documentation if it is likely to be useful in future runs.

## 2. Never crash trying to finish a task

Prefer a small, verified result over a large, unfinished implementation.

Before expensive work:

- Inspect the repository's build instructions and test configuration.
- Identify the smallest relevant test target.
- Check the available JVM and Gradle configuration.
- Determine whether the required services are already running.
- Avoid launching duplicate build or test processes.
- Avoid running multiple memory-intensive Gradle builds concurrently.

Never start the full E2E suite as an exploratory first step.

Do not increase JVM heap sizes, daemon memory, or test-worker counts
speculatively. First establish the existing configuration and the actual
failure mode.

If a Java heap-space error occurs:

1. Stop repeating the same failing command.
2. Capture the exact error and relevant logs.
3. Determine which process exhausted its heap, if possible.
4. Inspect existing Gradle, Kotlin, and test-worker settings.
5. Propose the smallest safe adjustment.
6. Ask the user before making a substantial memory or environment change.
7. Resume with the smallest relevant test target.

Do not automatically retry a failing build multiple times.

If a process hangs, consumes excessive resources, or causes the development
environment to become unstable, stop and consult the user.

## 3. Inspect before implementing

Before writing tests:

- Read the three existing planning/reference documents.
- Inspect existing Share Management E2E tests.
- Inspect applicable page objects, fixtures, test helpers, and annotations.
- Inspect the status transition implementation and change reasons.
- Identify existing tests that already cover a workflow.
- Identify the build command used for the relevant E2E module.

Do not duplicate an existing test merely to increase test count.

Distinguish between:

- A workflow documented but not tested.
- A workflow already covered by an existing test.
- A workflow partially covered.
- A workflow blocked by environment or UI limitations.
- A workflow that is intentionally out of scope.

## 4. Implement incrementally

Work on one coherent workflow at a time.

For each workflow:

1. Establish the expected transition and actor.
2. Establish the prerequisites and required test data.
3. Implement the smallest useful test.
4. Verify assertions against the persisted or displayed state.
5. Run the narrowest relevant test target.
6. Review failures and fix only understood causes.
7. Update the progress document.
8. Continue only when the current result is stable.

Reuse existing page objects, fixtures, and conventions.

Do not invent new abstractions unless they solve a demonstrated problem.

Do not weaken assertions, silently skip failures, or alter production
business rules just to make tests pass.

Preserve unrelated user changes in the working tree.

## 5. Treat the workflow reference as the expected behavior

Use docs/share-management-workflows.md as the narrative reference.

Cross-check the narrative against the actual state machine and change-reason
definitions.

If they disagree, do not silently choose one. Report the discrepancy and
ask the user which behavior should be authoritative.

In particular, verify the exact actor and change reason required for each
transition. A test must not assume that two transitions to the same status
are interchangeable.

Test both successful outcomes and important invalid operations where the
UI exposes them.

Do not force wall-clock-dependent transitions through sleeps or arbitrary
waiting periods.

Transitions that require deadlines, backend jobs, or system-time manipulation
must remain explicitly out of scope unless a supported test mechanism exists
and the user approves its use.

## 6. Multi-year workflow coverage

Prioritize coverage in the following order:

A. Prerequisites:
- Share type creation.
- Fiscal-year-specific share offers.
- Member subscriptions to the correct offer.

B. FY1 activation:
- Approval without a payment mandate.
- Payment-mandate authorization.
- Failed authorization, where safely testable.
- Auction clearance and final approval.
- Activation rejection and resubmission.
- Cancellation before activation.

C. FY1 ongoing operations:
- Pause and resume.
- Cancellation after activation.
- Payment failure and recovery.
- Suspension and resolution.
- Invalid actions from terminal or incompatible states.

D. FY1 to FY2 rollover:
- Original subscription enters RollingOver.
- Shadow subscription is created against the FY2 offer.
- Shadow activation.
- Original subscription reaches RolledOver.
- Missing next-year offer.
- Ineligible source status.
- Shadow cancellation and the resulting orphaned rollover.

E. FY2 ownership and mandate operations:
- Ownership transfer.
- New owner's activation.
- Mandate mapping and authorization reset, where observable.
- Re-attachment or update of a SEPA mandate.

F. FY2 to FY3:
- Repeat rollover using a separate FY3 offer.
- Paused or suspended source subscription is not rolled over directly.
- Updated offer price and its effect on an existing shadow.
- Recovery or manual intervention for an unresolved rollover.

G. Additional reachable transitions:
- External-data import, if the test environment supports it.
- Expired-to-PendingActivation transition, if supported without
  uncontrolled time manipulation.
- Other transitions discovered during state-machine inspection.

This order is a prioritization guide, not permission to skip prerequisites
or assume that a workflow is implemented.

## 7. Assertions and test isolation

For every test, verify as many of the following as are observable:

- Initial subscription status.
- Action performed by the correct actor.
- Resulting status.
- Correct fiscal-year offer.
- Correct relationship between the original and shadow subscriptions.
- Relevant owner and mandate associations.
- Relevant price or quantity.
- Rejection of invalid operations.
- Persistence after navigating away and returning, where practical.

Use isolated test data where the existing infrastructure supports it.

Avoid relying on mutable state left behind by a previous test.

Do not delete shared test data or alter production-like data without
explicit user approval.

## 7b Use E2e Test environment
always use ./scripts/e2e-local.sh to run e2e tests. 
running command e2e:test directly will affect the database used for development

## 8. Progress tracking and resumability

Maintain:

.junie/progress/share-management-e2e-progress.md

Record:

- Last updated date.
- Current phase and workflow.
- Files inspected or changed.
- Tests implemented.
- Exact test commands executed.
- Pass/fail results.
- Known environmental or memory limitations.
- Decisions confirmed by the user.
- Blockers and unresolved questions.
- The next smallest actionable task.

Update this file after each completed workflow and before stopping
because of a blocker.

Do not claim a test passed unless its execution completed successfully.

Distinguish implementation complete from verification complete.

## 9. Communication

At the beginning of a run, report the current objective and the next
smallest step.

During work, report meaningful milestones and any decisions requiring
the user.

When blocked, stop and ask a specific question.

At the end, report:

- Work completed.
- Files changed.
- Tests run and their results.
- Known limitations.
- Memory or build issues encountered.
- The next recommended step.

Never claim that the entire workflow suite is complete when only a subset
has been implemented or verified.

## Definition of done

The mission is complete only when:

- Every applicable documented workflow has been assessed.
- Each workflow is covered by an existing test, a new verified test,
  or a documented and justified exclusion/blocker.
- Multi-year rollover relationships are tested.
- Invalid transitions are covered where the UI exposes them.
- Tests use the project's established conventions.
- Relevant tests have been run successfully, or verification blockers
  are explicitly documented.
- Progress documentation is current.