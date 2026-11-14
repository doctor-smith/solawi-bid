# ADR: Community product CI and private Solyton operations boundary

- Status: **Proposed**
- Date: 2026-11-14
- Decision owners: repository owner and designated operations/security reviewers
- Scope: Phase 0 architecture only; no repository creation, license change, deployment, or secret migration is authorized by this ADR
- Related: [responsibility matrix](ci-cd-responsibility-matrix.md), [migration plan](solyton-ops-migration-plan.md)

## Context

The public `solawi-bid` workflow currently builds, tests, publishes, deploys, and smokes every push. Feature branches map to D, `main` maps to Q, and tags map to P. Deployment, server bootstrap, and backup implementation live beside product code and consume Solyton credentials.

The desired split has two independent responsibilities:

1. **Community product delivery** proves a commit and publishes immutable, attributable OCI artifacts.
2. **Solyton Ops** decides whether, when, and where an already-proven artifact may run, and owns environment secrets, D→Q→P promotion, smoke checks, rollback, backup, restore, retention, and recovery.

Community users must remain able to build, test, package, and operate their own installation without access to `solyton-ops`. Conversely, private Ops must never need to execute unreviewed source from a fork or feature branch with production credentials.

## Decision

Subject to human approval of this proposed ADR:

- `solawi-bid` remains the source of truth for application code, public build/test definitions, product Dockerfiles, local/E2E Compose, and publication of application images.
- A separate private `solyton-ops` repository becomes the source of truth for Solyton-specific host bootstrap, environment inventory, deployment, remote smoke checks, application rollback, database backup/verification/encryption/retention, restore, and disaster recovery.
- The repositories integrate only through a **versioned release contract containing immutable OCI digests and source identity**. Private Ops never calls a public workflow to deploy, and public CI never calls a private reusable workflow or receives private environment secrets.
- Deployment is independently initiated in private Ops. An authenticated notification may create a candidate, but cannot itself grant production authority.
- D, Q, and P consume the same image digests. Promotion changes approved environment state, not the built artifact.
- Application rollback and database restore are separate procedures. A failed deploy/smoke may automatically or manually return to a previously recorded application digest; it must never automatically restore a database.
- Existing public/contributed deployment code is not deemed proprietary by relocation. Provenance/license review gates any reuse; an independently authored private implementation is preferred where rights are uncertain.

## Release contract

The contract should be a small JSON document, signed/attested or otherwise bound to the successful Community workflow. A version-1 example:

```json
{
  "schemaVersion": 1,
  "repository": "doctor-smith/solawi-bid",
  "commit": "<40-character-git-sha>",
  "sourceRef": "refs/heads/main",
  "workflowRunId": "<github-actions-run-id>",
  "createdAt": "<RFC3339 UTC>",
  "images": {
    "frontend": "ghcr.io/doctor-smith/solawi-bid/frontend@sha256:<digest>",
    "backend": "ghcr.io/doctor-smith/solawi-bid/backend@sha256:<digest>"
  },
  "provenance": "<attestation-or-url>",
  "sbom": {
    "frontend": "<artifact-or-attestation-reference>",
    "backend": "<artifact-or-attestation-reference>"
  }
}
```

Required invariants:

- full commit SHA and full OCI digests; tags are display aliases only;
- frontend and backend originate from the same successful contract/run;
- a release tag may only alias artifacts already proven for the same current `main` commit;
- contract schema is validated in Community CI and again in Ops;
- Ops verifies registry identity, provenance/attestation, expected repository, commit policy, and digest existence before creating a deployment candidate;
- contract contains no credentials, host names, private inventory, customer data, or private repository URLs;
- schema changes are backward-compatible or use a new version with an explicit Ops compatibility gate.

## Target flow

```text
fork/PR --------------------> Community checks (read-only, no publish/deploy secrets)
                                  |
trusted merge/push to main ------+--> build once + test + E2E
                                           |
                                           v
                                  OCI images by digest
                                  + SBOM/provenance
                                  + release-contract.json
                                           |
                         authenticated candidate notification
                         (notification grants no deploy rights)
                                           |
                                           v
                                  private solyton-ops
                             verify contract and policy
                                           |
                           human/automatic D gate + lock
                                           v
                              deploy digest --> smoke
                                           |
                             promote same digest to Q
                                           v
                              approval --> deploy/smoke
                                           |
                             promote same digest to P
                                           v
                       protected approval --> deploy/smoke
```

## Initiation alternatives

| Alternative | Security and coupling | Operational cost | Decision |
|---|---|---|---|
| Private `workflow_dispatch` with contract URI/digests | Strong separation and explicit operator identity; simplest to audit. Manual transcription can cause errors unless values are selected/validated. | Lowest Phase-1 implementation cost; some operator effort. | **Initial mechanism.** Use typed inputs or an Ops-side candidate list and validate all values. |
| GitHub App installed on both repositories | Can observe successful trusted runs and create an Ops candidate with short-lived, narrowly scoped tokens. Clear installation/audit boundary if permissions are minimal. More components and key management. | Medium implementation and maintenance cost. | **Preferred automation after the manual path is proven.** App creates candidate/dispatch only; environment approvals remain in Ops. |
| `repository_dispatch` / API call into private Ops | Simple event bridge, but requires a token/App capable of accessing the private repository. A long-lived private-repo token stored in public CI would invert the trust boundary. | Low code cost, potentially high credential risk. | **Only through a GitHub App or broker with short-lived minimal authority. Never a broad PAT in public CI.** |
| Generic webhook/broker | Platform-neutral, can verify signed payload and deduplicate. Adds a public endpoint, availability, replay defense, secret rotation, logs, and incident response. | Highest operational cost. | **Defer** unless cross-platform needs justify it. |
| Public workflow calling a private reusable workflow | Exposes private coupling and requires public-side authorization to private code/secrets; Community forks cannot use it independently. | Superficially low, strategically costly. | **Rejected.** |

## Trust boundaries and least privilege

### Community repository

- `pull_request`: read-only source checkout and test reporting; no package write, environment secrets, or deployment. Treat all fork content as untrusted.
- trusted `push` to protected `main`/release ref: package write only for the publication job after tests pass.
- permissions are declared per job. Remove unused OIDC or grant it only to an attestation job.
- external Actions and build bases are pinned/verified; generated provenance identifies source and builder.
- a release notification cannot name arbitrary images outside the allowed repository/digest policy.

### Private Ops repository

- private inventory, host names, SSH trust, environment secrets, age keys, approvals, runbooks, and recovery evidence remain private.
- workflows accept a validated release contract, never a branch to check out from `solawi-bid` for privileged execution.
- D/Q/P use distinct GitHub environments and secrets. P requires protected reviewers; Q/P promotions require the previous stage's successful immutable deployment record.
- deployment tokens can read only the required image packages. Registry publication credentials are not available to Ops runners.
- trusted runner labels and environment protection are required for restore/decryption. Untrusted inputs are never interpolated into shell/Ansible commands without strict validation.

## Promotion, concurrency, and idempotency

The deployment key is `(environment, backend digest, frontend digest, contract version)`. Ops persists a deployment record with candidate, approver, previous digests, workflow run, start/end time, and smoke outcome.

- one deployment/restore operation per environment at a time;
- same key already successful: no-op with auditable success;
- same key currently running: deduplicate, do not queue duplicate mutation;
- different candidate for the same environment: serialize; do not cancel a running host mutation mid-flight;
- D may be automatic after contract verification; Q requires successful D; P requires successful Q and a human protected-environment approval;
- promotion always uses the exact same digests. Rebuilds create a new candidate and restart promotion at D;
- emergency rollback has its own audited operation and lock, and cannot bypass artifact allow-list/provenance checks.

## Failure and rollback policy

### Application deployment/smoke failure

1. Stop promotion immediately and keep the environment lock.
2. Capture deployment and smoke evidence without logging secrets.
3. If containers did not change, leave current version active and mark candidate failed.
4. If a new version became active, redeploy the previously recorded frontend/backend digests and run rollback smoke checks.
5. If rollback smoke fails, keep the environment in a declared degraded/maintenance state and require human intervention.
6. Never select `latest`, rebuild, or infer the previous version from a mutable tag.

Database schema compatibility must be assessed before release. Forward-only schema changes that make the prior app incompatible require an explicit migration/rollback plan and may prevent automated app rollback.

### Database recovery

Restore is a distinct manually initiated workflow with an explicit environment and snapshot, protected approval, maintenance mode, quarantine/snapshot of current data, age decryption on a trusted runner, isolated verification, and a post-restore smoke/audit record. Failure remains in maintenance mode. A deploy or application smoke failure never triggers restore.

## Backup and recovery boundary

The target incorporates the contracts developed in Issues 19, 20, 21, and 24:

- exact application dump is identified, hashed, and restored into isolated MySQL 9 before it is marked usable;
- system-schema dumps are distinguished and are not normal application restore inputs;
- backup host directory/file permissions target `0700`/`0600` rather than the current `0755` directory;
- age X25519 encryption is performed without persistent plaintext; private keys remain in protected Ops/offline custody;
- retention applies only to restore-verified artifacts, preserves the last recovery point, supports dry-run and approval for legacy cleanup, and separates D/Q/P;
- off-site copies, metadata, checksums, encryption recipient, verification result, and retention class are auditable;
- application rollback by digest and DB restore by snapshot are separate runbooks and approvals.

## Provenance and licensing boundary

This ADR makes no proprietary-rights claim. Existing Apache-licensed/public history and contributor grants remain available under their applicable terms. Git attribution is an audit input, not a legal conclusion.

Before reusing existing CI, Ansible, Docker, or config in a private implementation:

1. map the relevant lines/files to commits and authors, including the deployment work identified for Kay/`joystick01` and E2E work identified for `aiinhuu`;
2. compare the fork/commit evidence captured in Issue 15;
3. determine whether applicable license obligations are acceptable, additional rights are available, or an independently authored replacement is required;
4. preserve copyright/license notices and third-party notices;
5. record the human/legal decision. No repository move itself changes the result.

The Community release contract should remain public so independent operators can consume the same artifacts. Private value comes from independently developed operational policy, private inventory/integration, support, and controlled execution—not from obscuring previously public code.

## Consequences

Positive:

- untrusted/community source no longer shares an execution path with production authority;
- Community remains independently buildable and deployable;
- build-once/promote-by-digest improves reproducibility, rollback, and auditability;
- Ops changes can be reviewed under a narrower, private trust boundary;
- backup/recovery controls become explicit instead of incidental to deployment.

Costs and trade-offs:

- two repositories and a versioned contract add coordination and maintenance;
- a GitHub App or broker adds operating cost if automatic candidate creation is desired;
- digest/provenance/SBOM storage and private Actions usage may increase registry/CI cost;
- independently rewriting contributor-affected Ops code costs more initially but reduces rights uncertainty;
- environment protection may require an appropriate GitHub plan or an equivalent external approval mechanism.

## Rejected shortcuts

- moving current public deployment files unchanged and calling them proprietary;
- keeping P secrets in the public repository while merely moving workflow YAML;
- public CI invoking private reusable workflows;
- deploying mutable `latest`, branch, or release tags without resolving and recording digests;
- rebuilding separately for D/Q/P;
- exposing secrets to fork/PR workflows or executing fork code on a privileged/self-hosted runner;
- treating a SQL dump's existence as a verified backup;
- automatic DB restore after deployment failure.

## Open human decisions

1. Accept/amend/reject this ADR and the release-contract fields.
2. Select private Ops repository owner, reviewers, GitHub plan/environment approval mechanism, and cost ceiling.
3. Decide initial manual dispatch versus immediate GitHub App investment; this ADR recommends manual first.
4. Complete the contributor/provenance and license decision before copying any existing operational implementation.
5. Approve D/Q/P gates, rollback authority, recovery roles, RPO/RTO, retention values, and offline age-key custodians.
6. Confirm GHCR visibility and cross-repository read access without a broad PAT.
