# Current Tasks & Verification

## 2026-10-31: VersionInfo Component & E2E Smoke Test
- **Component**: `solawi-bid-frontend/.../application/ui/component/version-info.kt`
- **Issue**: Overlay opened, but version and commit data were blank due to missing reactive state updates from `Storage` lenses during async fetch.
- **E2E Test Added**: `e2e/src/test/kotlin/org/solyton/solawi/bid/test/application/VersionInfoTest.kt`
    - Tagged with `@Smoke` and `@Test`.
    - Verifies presence of `app.version-info` container on `/app/dashboard`.
    - Verifies label text and toggles overlay via click.
    - Asserts visibility and data of `FRONTEND` and `BACKEND` sections (version, commit, environment).
- **Execution**: Can be run via `./script/e2e-local.sh filterTest "org.solyton.solawi.bid.test.application.VersionInfoTest"`.