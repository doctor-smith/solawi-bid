# E2E-Module
In order to run the E2E tests, you need to have the following prerequisites installed:
- Docker & Docker Compose

## Dedicated local E2E environment
Running the E2E tests against your regular local development environment
(`docker-compose.yml`) is not safe, since the tests write to the database.
Instead, use the dedicated, constant E2E environment defined in
[`docker-compose.e2e.local.yml`](../docker-compose.e2e.local.yml). It uses
ports distinct from the development environment, so both environments can
run side by side without conflicts:

| Service  | Development port | E2E port |
|----------|-------------------|----------|
| Frontend | 8080               | 18080    |
| Backend  | 8081               | 18081    |
| Database | 3306               | 13306    |

By default, the E2E tests navigate against `http://localhost:8080`, i.e. the
regular development environment / CI (so that existing setups, and CI in
particular, keep working unchanged). When using the dedicated local E2E
environment, `script/e2e-local.sh test`/`run` automatically point the tests to
`http://localhost:18080` instead. This can also be set manually via the
`E2E_BASE_URL` env var (or property in `local.properties`) if needed.

A helper script, [`script/e2e-local.sh`](../script/e2e-local.sh), is provided
in the root folder of the project to manage this environment:

```shell
# Build the local images used by the E2E environment (only needed once, or after code changes)
./script/e2e-local.sh build

# Start the E2E environment
./script/e2e-local.sh up

# Run the E2E tests. This automatically resets the database to a clean, empty
# state first (recreating the database and backend containers, so the backend
# re-creates the schema and the owner/test user itself on startup, exactly
# like in CI) - see "Full database resets" below.
./script/e2e-local.sh test

# Stop the E2E environment
./script/e2e-local.sh down

# Or run the full cycle (build, up, test, down) in one go
./script/e2e-local.sh run

# Reset the database on its own, without running any tests (e.g. to poke
# around in a clean environment manually)
./script/e2e-local.sh reset-db
```

### Full database resets
Most tests should rely on migrations and simply create their own,
uniquely-named fixtures (e.g. `UserManagementTest` generates a timestamped
email per created user), so they don't depend on a clean database and don't
collide with leftover data from previous runs.

Sometimes, though, a truly clean slate is useful - e.g. to avoid the database
growing unboundedly across local test runs. For that, `script/e2e-local.sh
test`/`run` set the `E2E_RESET_DB=true` environment variable, which the test
suite itself picks up via a JUnit 5 extension
([`DbResetExtension`](src/test/kotlin/DbResetExtension.kt), auto-registered
for every test class) that runs
[`DbReset.resetOnce()`](src/test/kotlin/DbReset.kt) before the first test.
That, in turn, shells out to `./script/e2e-local.sh reset-db` exactly once per
test run - the same container recreation described above, just triggered
programmatically instead of as a separate manual step.

This is opt-in and disabled by default (via `E2E_RESET_DB`), so it never runs
in CI (which already starts from a fresh `docker-compose.e2e.yml` on every
run) or during a plain `./gradlew :e2e:test`. To opt in manually - e.g. from
your IDE's test runner, without going through the script - set
`E2E_RESET_DB=true` yourself, either as an environment variable or in
`local.properties`.

### Note on the owner/test user credentials
On startup, the backend reads `APPLICATION_OWNER_USERNAME`/`APPLICATION_OWNER_PASSWORD`
from [`solawi-bid-backend/.env.e2e`](../solawi-bid-backend/.env.e2e) (a local,
git-ignored file, see `.env.e2e.dist` for a template) and creates that user with a
bcrypt hash of the password. The `TEST_USER`/`TEST_USER_PASSWORD` used by the E2E
tests (set in `local.properties`) must log in as that same user, so
`TEST_USER_PASSWORD` must be the **plaintext** whose bcrypt hash is stored in
`APPLICATION_OWNER_PASSWORD` — otherwise every test that needs to log in will fail
with correct-looking setup but wrong credentials.

Note that `APPLICATION_OWNER_PASSWORD` is consumed via Docker Compose's `env_file`,
which applies the same `${VAR}`/`$VAR` interpolation as the compose YAML itself. A
literal `$` in the bcrypt hash (e.g. `$2a$10$...`) must therefore be escaped as `$$`,
**not** `\$` — backslash-escaping does not prevent interpolation and silently
corrupts the hash, which breaks login for every test without any obvious error.

### Note on the frontend base image
`Dockerfile-Frontend` builds `FROM ghcr.io/doctor-smith/gradle-base-frontend:latest`,
an image built/pushed by the "Build Gradle Base Image For Frontend" GitHub
workflow. That image is private on GHCR, so pulling it locally would require
registry credentials most developers don't have, failing with an error like
`ERROR [internal] load metadata for ghcr.io/doctor-smith/gradle-base-frontend:latest`.
`./script/e2e-local.sh build` avoids this entirely: it never tries to pull
that image. If it's not already present locally, it builds it locally from
[`infrastructure/docker/Dockerfile-Frontend-Base`](../infrastructure/docker/Dockerfile-Frontend-Base)
instead (one-time, may take a while since it installs Chrome; cached
afterwards, so subsequent `build` runs are fast).

## Running the tests
After the environment is up and the database has been reset, run the tests
via the helper script so they are pointed at the dedicated environment
(port `18080`):
```shell
./script/e2e-local.sh test
```
Running `./gradlew :e2e:test` directly instead will use the default
`http://localhost:8080` and therefore test against the regular development
environment, unless `E2E_BASE_URL` is set manually.