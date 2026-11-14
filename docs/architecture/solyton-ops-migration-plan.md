# Migration plan: Community CI to private Solyton Ops

Status: Phase-0 proposal, 2026-11-14

Baseline: `origin/main` at `f3ec9888f87af68e1cb049c633d5d7ebf6e2d0cf`

Depends on acceptance of the [boundary ADR](solyton-ops-boundary-adr.md) and uses the [responsibility matrix](ci-cd-responsibility-matrix.md).

## Goal and non-goals

The goal is a reversible transition in which public Community CI builds and proves immutable product artifacts, while private Solyton Ops independently promotes those exact artifacts through D→Q→P and owns operational security and recovery.

Phase 0 does **not** create a private repository, change licenses, move secrets, edit workflows/infrastructure, touch a server, deploy, back up, restore, or merge anything. Every implementation phase below requires separate human authorization.

## Safety principles

- Build once; promote exact frontend/backend OCI digests.
- Keep Community independently buildable and operable.
- No private-repository credential or environment secret in public CI.
- Never run fork/PR code with publish, host, decrypt, or deployment authority.
- Introduce the private path in shadow/parallel mode before removing the old path.
- Preserve an explicit fallback until each cutover gate is proven.
- App rollback by prior digest is normal deployment recovery; DB restore is exceptional, separately approved recovery.
- Treat licensing/provenance as a release gate, not an afterthought.

## Work packages and PR sequence

Each item is intended to be a small PR with one primary responsibility. PR numbers are future placeholders, not authorization.

### M0 — Human decisions and baseline capture

Owner: repository owner + operations/security/legal reviewers. No code change.

1. Review and accept/amend the proposed ADR.
2. Record current GitHub environment protection, branch/tag protection, package visibility, secrets and scopes, Actions usage/cost, and server-side schedules outside the repository.
3. Decide Ops repository ownership, access group, required reviewers, billing/plan constraints, runner model, and audit retention.
4. Complete the Issue 15 rights/provenance decision for operational files. Explicitly choose: retain under inherited terms, obtain rights, or independently reimplement.
5. Approve RPO/RTO, D/Q/P promotion gates, backup retention values, rollback authority, recovery roles, and age-key custodians.

Acceptance:

- signed-off ADR and named owners;
- no unresolved assumption about whether current public code becomes proprietary by moving it;
- current known-good deployed frontend/backend digests recorded per environment;
- current backup inventory and an emergency manual procedure recorded without putting secrets in Git.

Fallback: stop. No production state has changed.

### C1 — Public PR: split validation from publication

Repository: `solawi-bid`.

- Add explicit `pull_request` validation with read-only per-job permissions.
- Keep fork PRs free of package write and all environment secrets.
- Restrict image publication to trusted protected refs after all required tests/E2E succeed.
- Remove unused broad permissions such as OIDC unless the specific attestation job needs them.
- Pin external Actions to reviewed immutable SHAs (with update automation if desired).
- Preserve current deploy jobs unchanged for this PR so behavior is reversible.

Acceptance:

- fork simulation proves no secret/publish/deploy access;
- trusted `main` still produces tested artifacts;
- current deployment path remains available;
- permissions test/check documents the effective token grants.

Fallback: revert C1; no Ops dependency exists yet.

### C2 — Public PR: immutable release contract

Repository: `solawi-bid`.

- Resolve published frontend/backend OCI digests after push.
- Emit and retain `release-contract.json` version 1 with full source SHA, workflow run, image digests, SBOM and provenance references.
- Generate/attach SBOM and build provenance; verify repository/source identity.
- Continue tags for humans, but make the digest contract authoritative.
- Add schema/unit validation and a tamper/invalid-digest negative test.
- Make the frontend build self-contained: remove reliance on an inaccessible mutable private base image, or publish/version/pin the base appropriately.

Acceptance:

- clean Community checkout can build/test without private Ops;
- both images are addressable by immutable digest and carry source revision;
- a release tag aliases an artifact already proven for the same current `main` commit;
- contract contains no private data and validates against a documented schema;
- same contract can be fetched later within the agreed retention period.

Fallback: consumers continue using current tags and public deploy path; contract generation may be reverted independently.

### O1 — Private PR: Ops repository skeleton and policy tests

Repository: future private `solyton-ops`; requires M0 authorization.

- Create minimal access-controlled repository and CODEOWNERS/review policy.
- Add D/Q/P protected environments with distinct secrets and approvals; initially use non-production placeholders/test credentials only.
- Implement contract parser/policy validation and dry-run deployment plan. No server mutation.
- Add concurrency/dedup key `(environment, frontend digest, backend digest, schema version)` and immutable deployment record model.
- Add secret scanning, dependency/action pinning, logs/artifact retention, and a test proving arbitrary registry/repository refs are rejected.
- Do not copy contributor-affected files unless M0 explicitly cleared their use; prefer independent implementation from the ADR contract.

Acceptance:

- unauthorized users/forks cannot invoke privileged jobs or read secrets;
- malformed, replayed, untrusted-repository, mutable-tag-only, and unknown-schema contracts fail closed;
- dry run produces an auditable plan with no server access;
- duplicate candidate is a no-op and competing environment mutations serialize.

Fallback: archive/disable the unused private skeleton; public operation is unchanged.

### O2 — Private PR: D deployment and application rollback

Repository: `solyton-ops`.

- Implement digest-only D deployment using verified SSH host keys and minimum registry read credentials.
- Store prior/current digests and deployment evidence.
- Add post-deploy smoke tests and rollback to exactly recorded prior digests on failure.
- Ensure secrets are not passed in process arguments or logs; clean temporary key/material.
- Start with manual `workflow_dispatch` and protected D inputs. Notification automation is deferred.

Acceptance:

- two successful idempotent D deployments of the same candidate cause one mutation;
- deliberate smoke failure returns to prior digests and rollback smoke passes;
- deliberate rollback failure leaves an explicit degraded/maintenance result for human action;
- logs contain no secret values; deployed container inspection matches contract digests;
- no database restore occurs.

Fallback: disable O2 and use the existing public D deployment. The recorded pre-test digest is the app fallback.

### O3 — Private PRs: backup, restore verification, encryption, retention

Repository: `solyton-ops`. Implement in this dependency order:

1. **Issue 19 — restore verification:** select the exact application dump, hash it, import it into isolated MySQL 9, run structural/domain smoke checks, distinguish the `mysql` system dump, clean up, and publish verification metadata. Fix the current D→Q environment mapping error. Enforce host directory/file modes `0700`/`0600`.
2. **Issue 21 — age encryption:** establish age X25519 recipient, protected private-key injection and verified offline recovery copy; stream encryption/decryption with `pipefail` and no persistent plaintext. Pilot D, then Q. Do not enable P until offline recovery succeeds.
3. **Issue 20 — retention:** classify only encrypted, restore-verified backups; implement dry-run first, preserve the last recovery point, separate D/Q/P, record digest/metadata, add off-site copy, and require approval before deleting legacy/unclassified backups.
4. **Issue 24 — controlled recovery:** manual explicit environment/snapshot workflow, protected approval, maintenance mode, quarantine of current data, trusted-runner decrypt, isolated re-verification, restore, post-restore smoke, and immutable audit record.

Proposed retention values from Issue 20 (14 daily, 8 weekly, 12 monthly; releases at least 90 days or last 3; major releases 12 months) remain a human decision until M0 approves cost/RPO/RTO.

Acceptance:

- synthetic and D backups complete the exact restore-verification path;
- corruption, wrong age key, missing metadata, wrong environment, and unavailable off-site copy fail closed;
- offline-key recovery drill is evidenced before P;
- retention dry-run is reviewed before first deletion and never deletes the last recovery point;
- recovery rehearsal demonstrates maintenance/quarantine/failure behavior without touching P;
- application rollback and DB restore have different workflows, permissions, locks, and approvals.

Fallback: do not delete the current backup path or any legacy file. Disable new schedules; preserve both encrypted and legacy recovery points until a human confirms recoverability.

### O4 — Private PR: Q and P promotion gates

Repository: `solyton-ops`.

- Promote the same D-proven digests to Q, then the same Q-proven digests to P.
- Require protected approvals for P and the approved gate for Q.
- Add environment locks, deployment records, smoke/rollback behavior, maintenance notification, and least-privilege environment secrets.
- Validate schema/app rollback compatibility for each candidate.

Acceptance:

- attempting Q without successful D fails; attempting P without successful Q/approval fails;
- digest drift or rebuild between stages fails;
- staged smoke failure blocks later stages and applies the approved app rollback policy;
- simultaneous deploy/restore attempts serialize safely;
- known-good P prior digests remain available and tested for rollback.

Fallback: keep P on the prior digest and disable private promotion. Existing public deploy remains available until cutover C4.

### O5 — Private PR: optional candidate automation

Repository: `solyton-ops` plus minimum public integration if approved.

- Begin only after manual dispatch is stable.
- Prefer a narrowly scoped GitHub App that observes a successful trusted Community run and creates an Ops candidate/dispatch.
- Use short-lived installation tokens, signed/verified event identity, replay protection and dedup.
- The event does not approve Q/P and carries no Ops secret back to Community.

Acceptance:

- spoofed/replayed/feature-branch/fork events fail;
- loss of the App delays candidate creation but does not prevent manual dispatch or affect running environments;
- token permissions and rotation are documented and reviewed.

Fallback: disable/uninstall the App and use manual dispatch. Avoid a long-lived private-repository PAT in public CI.

### C3 — Public PR: shadow-mode separation

Repository: `solawi-bid`.

- Keep Community build/test/publish and release contract.
- Stop automatic public D deployment first; retain a time-boxed, explicitly manual legacy fallback with approvals while Ops D/Q/P proves stable.
- After D evidence, stop Q automatic deployment; after Q/P rehearsals and approvals, stop P automatic deployment.
- Remove environment secrets from jobs as each stage exits; rotate credentials that were available to the public repository.
- Do not yet delete historical implementation needed for fallback/provenance review.

Acceptance per environment:

- at least the human-approved number of successful private deployments and one rollback rehearsal;
- backup/recovery gates appropriate to that environment are green;
- no digest drift between Community contract and running containers;
- on-call/operator runbook and ownership are accepted;
- legacy fallback invocation is protected and audited.

Fallback: re-enable only the still-present manual legacy path for that environment under incident approval. Never run both controllers concurrently.

### C4 — Public PR: remove Solyton-specific privileged operation

Repository: `solawi-bid`; only after all cutover gates.

- Delete/retire public-repository deploy, server-install and backup workflows/actions and Solyton-specific Ansible/inventory/config that are no longer part of Community operation.
- Keep generic product Dockerfiles, local/E2E Compose, Community operational documentation, and public release contract.
- Retire obsolete temporary and PR-helper workflows after confirming no consumer.
- Replace removed operations with links to generic self-hosting interfaces, not private implementation details.
- Preserve Git history and all required notices; deletion from HEAD is not a rights or confidentiality mechanism.

Acceptance:

- public CI has no D/Q/P, host, SMTP, backup-decrypt, or production-registry-read secret;
- Community clean checkout still builds, tests, publishes (when authorized), and documents independent operation;
- private Ops is sole controller for Solyton D/Q/P and recovery;
- credentials formerly exposed to public-repository workflows are rotated;
- repository and environment settings audit confirms no orphaned secrets or triggers.

Fallback: revert C4 only if the old credentials/path remain deliberately recoverable and approved. Prefer fixing private Ops; do not recreate broad public authority casually.

## Dependency map

```text
M0 decisions/provenance
   |
   +--> C1 validation/publish split --> C2 digest contract ----+
   |                                                           |
   +--> O1 private policy skeleton <----------------------------+
             |
             +--> O2 D deploy + app rollback
             |
             +--> O3.1 restore verify --> O3.2 age --> O3.3 retention
             |                                  |
             |                                  +--> O3.4 recovery
             |
             +--> O4 Q/P promotion (requires D + recovery gates)
                       |
                       +--> optional O5 automation
                       |
                       +--> C3 staged cutover --> C4 public cleanup
```

## Cutover checklist

For each environment, a human release owner records:

- [ ] candidate contract and exact frontend/backend digests;
- [ ] provenance/SBOM policy result;
- [ ] prior known-good digests and rollback compatibility;
- [ ] backup identifier, checksum, encryption and latest restore-verification result;
- [ ] required D/Q predecessor result;
- [ ] environment lock/concurrency state;
- [ ] approver identity where required;
- [ ] deployment and smoke result;
- [ ] running-container digest verification;
- [ ] rollback or maintenance result on failure;
- [ ] no database restore unless a separate recovery approval exists.

## Cost, security, and operability trade-offs

| Choice | Lower-cost option | Stronger-control option | Proposed progression |
|---|---|---|---|
| Candidate initiation | Manual dispatch | GitHub App with short-lived token | Manual first, App after stable evidence. |
| Runner | GitHub-hosted | Hardened trusted/self-hosted for network/private-key operations | Hosted for contract validation; explicitly hardened trusted runner where restore/network requires it. Never expose it to fork code. |
| Approvals | Manual checklist outside GitHub | Protected environments/CODEOWNERS/audited gates | Use protected environments if available; document equivalent external control otherwise. |
| Provenance | Digest + source labels | Signed attestations, SBOM policy | Contract/digest first; add verifiable attestations before P cutover. |
| Backup storage | Single host | Encrypted off-site, versioned/immutable storage | Off-site is required before declaring recovery complete; size/provider is a cost decision. |
| Ops implementation | Reuse existing files | Independent implementation | Reuse only after rights clearance; independently implement contributor-uncertain areas. |

## Security tests required before public-path removal

- fork PR cannot read secrets, publish packages, reach private runners, or create an Ops candidate accepted for deployment;
- feature-branch contract, arbitrary registry reference, mutable-only tag, digest mismatch, replay, and unknown schema are rejected;
- secrets do not appear in command lines, summaries, Ansible output, artifacts, cache, or temporary files;
- SSH host-key mismatch fails closed;
- package-read credential cannot publish/delete packages; Community publisher cannot access Ops;
- concurrent duplicate deploy is idempotent; competing deploy/restore is serialized;
- smoke failure rolls app back to recorded digests; DB restore is not invoked;
- corrupted/wrong-key/unverified backup cannot pass retention or recovery;
- failure during restore leaves maintenance mode and evidence for human intervention.

## Human gates and stop conditions

Stop the migration and keep the last working path when any of the following is unresolved:

- no accepted rights/licensing route for implementation proposed for private reuse;
- no immutable digest contract or provenance validation;
- any private credential is required in public/fork CI;
- Community can no longer build/test independently;
- no recorded prior digest or rollback rehearsal for the target environment;
- no restore-verified encrypted recovery point for the environment's approved gate;
- production approval, on-call owner, RPO/RTO, or offline key custody is unnamed;
- simultaneous old/new controllers could mutate the same environment;
- observed running digest differs from the approved contract.

## Phase-0 result

This plan is documentation only. It deliberately leaves all current workflows, infrastructure, secrets, settings, servers, backups, and deployments untouched. The next authorized step is M0 review—not repository creation or production change.
