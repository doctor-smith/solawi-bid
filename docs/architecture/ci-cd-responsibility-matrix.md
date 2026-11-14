# CI/CD responsibility matrix

Status: Phase-0 audit, 2026-11-14

Audited baseline: `origin/main` at `f3ec9888f87af68e1cb049c633d5d7ebf6e2d0cf`

Related: [boundary ADR](solyton-ops-boundary-adr.md), [migration plan](solyton-ops-migration-plan.md)

## Purpose and classification

This document inventories the current delivery surface and proposes ownership after a Community/Ops split. It is an engineering audit, not a licensing opinion and not authorization to move or delete code.

The labels mean:

- **public-community**: product build, tests, developer tooling, and publication of verifiable product artifacts remain in `solawi-bid`.
- **private-ops**: Solyton-specific hosts, environments, credentials, deployment, backup, restore, and recovery belong in a future private `solyton-ops` repository.
- **shared-contract**: a deliberately small, versioned interface used by both sides; its schema and producer-side validation remain public.
- **needs-review**: ownership, provenance, security, or continued necessity must be decided before migration.

Facts below are observations at the audited commit. Target classifications and sequencing are proposals until the ADR is accepted.

## Executive findings

1. `.github/workflows/CI.yml` is both product CI and privileged CD. Every push is mapped to Develop, `main` to Staging, and a tag to Production; it then deploys and runs remote smoke tests.
2. The product build already labels images with the commit SHA and publishes `sha-<short-sha>` tags, but does not emit a durable, machine-readable release contract containing immutable registry digests for both images.
3. Deployment, server bootstrap, and database backup consume Solyton host and environment credentials from public-repository workflows. Fork pull requests do not currently trigger `CI.yml`, but the privileged workflow still checks out and executes repository code on pushes. Branch protection and write access therefore form part of the production trust boundary.
4. Backup exists, while restore verification, retention enforcement, encrypted off-site handling, and controlled recovery do not. The manual D backup path currently selects environment `Q` while using `MYSQL_PASSWORD_D`.
5. Current app rollback is not explicit. Deployments use mutable tags as inputs and recreate containers; database restore is not implemented and must remain a separate, higher-risk operation.
6. Git history shows material third-party contributions in CI/Ansible/deployment areas. Moving existing public code to a private repository does not make it proprietary; provenance and license obligations remain.

## Workflow and action inventory

| Current component | Trigger / runner | Current privileges and inputs | Outputs / coupling | Target | Findings and migration note |
|---|---|---|---|---|---|
| `.github/workflows/CI.yml`: version, change detection, build, tests, E2E | Every `push`; GitHub-hosted Ubuntu | Repository read; GHCR write; checks write; Actions read; OIDC write declared. Test credentials for E2E. | Test reports; one-day image tar artifacts; GHCR frontend/backend and CI-state tags. Couples source paths, Gradle, Dockerfiles, Compose, GHCR, E2E runner. | **public-community** | Keep product build/test/publish. Add explicit PR CI. Reduce permissions by job. Do not expose deployment environments or Solyton secrets. Verify whether OIDC is needed. |
| `.github/workflows/CI.yml`: deploy and remote smoke jobs | Same push run; environment selected as feature→Develop, `main`→Staging, tag→Production | D/Q/P DB and JWT secrets, owner password, SMTP password, server SSH key, GHCR PAT; repository code and composite deploy action execute with those values. | Direct mutation of Solyton server and a smoke result. Build success and production authority are in one run. | **private-ops** | Remove only after an independent Ops deployment is proven. Ops consumes an immutable artifact contract, not source checkout or mutable tags. Smoke failure invokes app rollback policy; never automatic DB restore. |
| `.github/workflows/backup-database.yml` | Manual D/Q/P or nightly schedule; GitHub-hosted Ubuntu | Server SSH key/address/user and per-environment DB password; host-key checking disabled. Schedule defaults to P. | SQL files on the deployment host. No digest, encryption, restore proof, retention, or off-site result. | **private-ops** | Move by replacement, not by claiming ownership of copied code. Fix the D→Q mapping in the private implementation. Implement Issues 19–21 before production cutover. |
| `.github/workflows/install-server.yml` | Manual; GitHub-hosted Ubuntu | Server SSH key/address/user; host-key checking disabled; root escalation through Ansible. | Installs Docker, swap, unattended upgrades, network, Traefik and ACME configuration on the Solyton host. | **private-ops** | Host bootstrap is Solyton-specific and highly privileged. Pin dependencies, establish host-key verification and protected approvals in Ops before retiring this workflow. |
| `.github/workflows/deploy_d_temp_test.yml` | Manual; GitHub-hosted Ubuntu | D credentials and owner account; calls deploy action with `latest`; omits required GHCR inputs at the audited commit. | Direct D deployment from mutable images. | **needs-review → retire** | Appears superseded by `CI.yml`; confirm no operational dependency, then disable/delete in a later reviewed PR. It must not be the migration fallback. |
| `.github/workflows/build-e2e-runner-image.yml` | `devops/*` path push, manual, reusable call, weekly schedule | Repository read and GHCR write; Docker socket mounted during image test. | E2E runner tags and digest. | **public-community** | Product test infrastructure stays public. Fix path case mismatch (`E2e` trigger vs `E2E` file), skip-step condition/name, pin external actions, and retain digest output. It must not receive Ops secrets; replace `secrets: inherit` with the minimum explicit interface. |
| `.github/workflows/build-gradle-base-image-frontend.yml` | Manual | Repository read and GHCR write. | Hard-coded `ghcr.io/doctor-smith/gradle-base-frontend:latest`, consumed by the frontend Dockerfile. | **public-community**, **needs-review** | Keep build tooling public, but publish a digest/versioned tag and remove the private-package assumption so Community builds are self-contained. |
| `.github/workflows/pr_ensure-pr-to-main.yml` | Manual | PR write, contents write, custom `PR_ACCESS_TOKEN`. | Draft PR to `main`. | **needs-review** | Repository governance is not Ops. Confirm necessity; if retained, use `GITHUB_TOKEN` with minimum permissions and no broad PAT. |
| `.github/actions/build-docker/action.yml` | Called by Community CI | Executes repository Dockerfiles, uploads tests and image tar; checks write through caller. | Image name/tag references and one-day tar; OCI revision/version labels. | **public-community** | Keep. Add digest/SBOM/provenance generation at publish time; action versions should be pinned to immutable SHAs. |
| `.github/actions/push-docker/action.yml` | Composite action; no current call found in `CI.yml` | Registry credentials; pushes all local tags via `docker push -a`. | Mutable tags; no digest output. | **needs-review → retire or narrow** | Current CI has an inline publisher. Confirm no external consumer; avoid `push -a` and return canonical digests if retained. |
| `.github/actions/deploy-application/action.yml` | Called by CI and temporary D workflow | SSH private key, server/DB/JWT/owner/SMTP/GHCR secrets. Host-key checking disabled; secrets passed in Ansible `-e` process arguments. | Runs Solyton deployment playbook. Local key is not explicitly removed. | **private-ops** plus **provenance review** | Reimplement/harden in Ops after rights review. Use protected environments, trusted inputs, verified host keys, secret files/vault/no-log handling, digest-only images, cleanup, concurrency, and audit records. Default frontend/backend image values are swapped and reference another namespace. |

## Infrastructure and supporting inventory

| Current component | Current role and dependency | Target | Findings and migration note |
|---|---|---|---|
| `infrastructure/docker/Dockerfile-Backend` | Builds/tests the backend and produces the runtime image; includes the MariaDB driver and its LGPL notice. | **public-community** | Product packaging stays public. Current base images are tag-based; pin or record resolved base digests and emit provenance/SBOM. MariaDB replaced Connector/J at the audited baseline, so the stale Connector/J release blocker from Issue 15 is resolved in code, subject to artifact verification. |
| `infrastructure/docker/Dockerfile-Frontend` and `Dockerfile-Frontend-Base` | Builds/tests frontend; depends on hard-coded mutable private GHCR base image; runtime uses Nginx. | **public-community**, base dependency **needs-review** | Make the Community build independently reproducible. Publish the base openly or replace it with a standard/pinned build path. Preserve license notices for all base/runtime components. |
| `infrastructure/docker/Dockerfile-E2E-Runner` | Public test runner with Playwright, Docker, Java and Gradle. | **public-community** | Keep, but validate the declared `MIT` OCI label against the actual repository/dependency licensing; do not use the label as a legal conclusion. Pin downloaded inputs and minimize root/Docker-socket exposure. |
| `docker-compose.yml`, `docker-compose.e2e.yml`, `docker-compose.e2e.local.yml` | Local development and isolated E2E topology, MySQL 9, frontend/backend integration. | **public-community** | These are necessary for a self-contained Community project. Test-only credentials are acceptable only if clearly non-production. The local compose references an untracked `setup-local.sql` path at this baseline and should be checked separately. |
| `infrastructure/database/setup.sql` | E2E database initialization. | **public-community** | Product test fixture/schema support, not an operational backup or production migration mechanism. |
| `infrastructure/nginx/default.conf`, `infrastructure/apache/httpd.conf` | Runtime frontend configuration; Apache file may be legacy. | Nginx **public-community**; Apache **needs-review** | Keep product-runtime configuration required to run the published image. Confirm whether Apache is unused before retirement. |
| `infrastructure/ansible/deploy-application.yml` | Creates host directories/network, performs pre-deploy dumps, runs MySQL 9, frontend and backend containers, Traefik labels, health waits, resource limits. | **private-ops** plus **provenance review** | Environment/host policy belongs in Ops. Backups are mode `0755`; dumps have no explicit restrictive mode, encryption, verification, retention, or off-site copy. Deployment pulls tag references and has no explicit previous-digest rollback transaction. |
| `infrastructure/ansible/backup-database.yml` and requirements | Produces application and `mysql` system dumps on host. | **private-ops** | Separate application dump from system-schema emergency handling. Adopt the restore-verification and encryption contracts from Issues 19–21. |
| `infrastructure/ansible/install-server.yml` and requirements | Host bootstrap and Traefik/ACME setup using unpinned Galaxy roles/collections. | **private-ops** | Pin dependencies; replace embedded operator email/configuration with private inventory; protect Docker socket and admin endpoint. |
| `script/e2e-local.sh`, `script/determine-version.sh` | Developer build/test orchestration and version derivation. | **public-community** | Keep. `determine-version.sh` fetches the network and assumes tags; document/offline-harden separately. |
| `script/clean-dbeaver-dump.sh`, `script/migrate.sh` | Manual SQL cleanup and an incomplete migration launcher. | **needs-review** | Do not treat either as a recovery tool. Confirm consumers and test before retaining or retiring. |
| Root/module Gradle files, `buildSrc/`, wrapper and `gradle/libs.versions.toml` | Product compilation, tests, dependency graph and image build inputs. | **public-community** | Keep as Community build contract. Dependency locking/SBOM and immutable action/base references are future supply-chain hardening. |

## Current trust and artifact flow

```text
push to any branch / main / tag
        |
        v
public repository workflow executes checked-out code
        |
        +--> Gradle/Docker tests --> temporary image tar --> E2E
        |
        +--> GHCR mutable branch/SHA/tag references
        |
        +--> same run receives D/Q/P + SSH + SMTP + registry secrets
        |         |
        |         +--> Ansible mutates Solyton host and database containers
        |
        +--> smoke tests against deployed D/Q/P
```

This is a coupling problem even if fork PRs do not currently run `CI.yml`: any actor or automation able to land/push code onto a triggering ref can influence code executed in a privileged run. The target design makes a private, reviewed Ops workflow the only holder of environment authority.

## Secret and permission map

| Secret / authority | Current consumer | Target owner | Required control |
|---|---|---|---|
| `GITHUB_TOKEN` with GHCR/checks/actions access | Public CI jobs | Community CI | Per-job least privilege; no deployment authority. Fork/PR jobs read-only and never receive publish credentials. |
| D/Q/P MySQL, JWT, owner and smoke-user passwords | Public-repo CI deploy/smoke; backup workflow | Private Ops environments | Separate environment-scoped secrets; P approval; secret masking/no process arguments; rotation during cutover. |
| `SERVER_KEY`, server address/user | Public-repo install/deploy/backup | Private Ops environments | Environment protection, verified SSH host keys, short-lived credentials where feasible, audit and rotation. |
| `SMTP_PASSWORD` | Public-repo deploy | Private Ops | Environment-scoped; never available to product CI. |
| `GHCR_PAT` | Public-repo deploy and target host login | Private Ops | Prefer narrowly scoped read token or registry-native mechanism; no write scope on deployment host. |
| `PR_ACCESS_TOKEN` | Manual PR helper | Repository governance if retained | Replace with minimum `GITHUB_TOKEN` or retire. It is not an Ops secret. |
| Future age private key | Not implemented | Private Ops protected environment plus offline recovery custody | Never place in Community repo or ordinary product CI; automated use only on trusted restore-verification runner. |

## Provenance and licensing constraints

Facts from repository history and the Issue 15 audit:

- The repository and its history are public under their existing notices. Relocating a file does not revoke prior grants or make existing public/contributed implementation proprietary.
- Git history attributes material deployment/CI/Ansible changes to contributors including Kay/`joystick01`; E2E-related work includes `aiinhuu`. Commit counts and blame are evidence for review, not proof of copyright ownership or legal status.
- Current `main` has since replaced MySQL Connector/J with MariaDB Connector/J and includes the LGPL-2.1 notice in the backend image. The actual released artifact should still be verified as part of release provenance.
- A future Ops implementation may remain under its inherited license, be created from independently authored new work, or be used only after contributor rights are cleared. This is a human/legal decision, not a Git operation.

Before moving or substantially reusing any deployment implementation, record file/commit provenance, compare the contributor fork/commits identified in Issue 15, preserve notices, and obtain the required rights decision. New private Ops code should be independently specified by the public artifact contract and operational requirements rather than copied merely to change visibility.

## Parallel work and uncertainty

- Open PR #380 contains the detailed license/contributor audit and remains a draft. Its legal gates must be resolved before any proprietary claim.
- Open PR #383 adds a dump pseudonymization tool. It is not backup encryption, restore verification, retention, or disaster recovery and is not a dependency of this migration.
- Open PR #379 concerns tenancy and does not change the audited CI/Ops boundary.
- Issues 19–21 and 24 specify backup verification, retention, age encryption, and recovery behavior; no implementation was assumed present.
- Repository/environment protection rules, GHCR package visibility, actual secret scopes, server state, and external scheduled jobs are not visible from this checkout. They require a human settings/operations review before cutover.

## Phase-0 acceptance evidence

- The inventory covers every file under `.github/workflows`, `.github/actions`, `infrastructure/ansible`, and `infrastructure/docker`, plus database/web configs, Compose, scripts, and build configuration.
- Each component has a proposed ownership classification and coupling note.
- No workflow, action, infrastructure, secret, repository setting, or server was changed by this audit.
