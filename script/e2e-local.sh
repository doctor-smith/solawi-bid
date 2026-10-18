#!/bin/bash
# Manage a dedicated, constant local E2E test environment (see docker-compose.e2e.local.yml).
#
# This environment is separate from the regular local development environment
# (docker-compose.yml) so that running E2E tests never writes into the database
# you use for day-to-day development, and so the database can be reset to a
# known state before every test run.
#
# Usage: ./script/e2e-local.sh <command>

set -euo pipefail
cd "$(dirname "$0")/.."

COMPOSE_FILE="docker-compose.e2e.local.yml"
DB_SERVICE="solawi-bid_database"
BACKEND_SERVICE="solawi-bid_backend"
# Must match the frontend port published in $COMPOSE_FILE. Kept distinct from the
# regular development environment (and from CI, which still uses 8080) so both can
# run side by side without conflicts, without having to touch CI at all.
E2E_BASE_URL="http://localhost:18080"
# Base image Dockerfile-Frontend builds FROM. It's built/pushed by the
# "Build Gradle Base Image For Frontend" GitHub workflow, but is private on
# GHCR, so pulling it locally needs credentials most developers don't have,
# failing with "ERROR [internal] load metadata for ...". We therefore never
# pull it here; ensure_frontend_base_image() below builds it locally instead.
GRADLE_BASE_FRONTEND_IMAGE="ghcr.io/doctor-smith/gradle-base-frontend:latest"

usage() {
  cat <<EOF
Usage: $0 <command>

Commands:
  build       Build the local Docker images (solawi-bid/frontend:local, solawi-bid/backend:local)
  up          Start the E2E environment (frontend, backend, database)
  down        Stop the E2E environment (database content is kept until the containers are removed)
  reset-db    Reset the E2E database to a clean state (same as CI: fresh DB + migrations). Mostly
              useful on its own, e.g. to inspect a clean database; "test"/"run" below already do
              this automatically before the first test.
  test        Reset the database, then run the E2E test suite (./gradlew :e2e:test)
  run         Full local cycle: build, up, test (which resets the database), down
EOF
}

ensure_frontend_base_image() {
  if docker image inspect "$GRADLE_BASE_FRONTEND_IMAGE" >/dev/null 2>&1; then
    echo "==> Using locally cached $GRADLE_BASE_FRONTEND_IMAGE"
    return
  fi

  # Deliberately not trying "docker pull" here: the image is private on GHCR,
  # so pulling it would require registry credentials most developers don't
  # have. Building it locally avoids that dependency entirely.
  echo "==> $GRADLE_BASE_FRONTEND_IMAGE not found locally."
  echo "==> Building it locally instead from infrastructure/docker/Dockerfile-Frontend-Base (one-time, may take a while)."
  docker build -f infrastructure/docker/Dockerfile-Frontend-Base -t "$GRADLE_BASE_FRONTEND_IMAGE" .
}

build_images() {
  echo "==> Building solawi-bid/backend:local"
  docker build -f infrastructure/docker/Dockerfile-Backend -t solawi-bid/backend:local .

  ensure_frontend_base_image

  echo "==> Building solawi-bid/frontend:local"
  docker build --pull=false -f infrastructure/docker/Dockerfile-Frontend -t solawi-bid/frontend:local .
}

up() {
  echo "==> Starting E2E environment ($COMPOSE_FILE)"
  docker compose -f "$COMPOSE_FILE" up -d --wait
}

down() {
  echo "==> Stopping E2E environment ($COMPOSE_FILE)"
  docker compose -f "$COMPOSE_FILE" down
}

reset_db() {
  # Recreate the database service from scratch instead of replaying an old dump
  # (infrastructure/database/setup-local.sql). That dump is a snapshot of a real
  # dev database, so its schema can drift from what the current backend code/
  # migrations expect (and its seeded users won't match APPLICATION_OWNER_USERNAME/
  # APPLICATION_OWNER_PASSWORD from solawi-bid-backend/.env.e2e, e.g. TEST_USER).
  # "rm -v" removes the database container's anonymous /var/lib/mysql volume along
  # with it, so the next "up" starts MySQL empty; the backend then creates the
  # schema and the owner/test user itself on startup, exactly like in CI.
  # The backend only creates the schema/owner user once, on startup, so it must be
  # recreated together with the database to pick up the fresh, empty state.
  echo "==> Resetting E2E database (recreating $DB_SERVICE and $BACKEND_SERVICE from a clean state)"
  docker compose -f "$COMPOSE_FILE" rm -fsv "$BACKEND_SERVICE" "$DB_SERVICE"
  docker compose -f "$COMPOSE_FILE" up -d --wait "$DB_SERVICE" "$BACKEND_SERVICE"
  echo "==> Database reset complete"
}

run_tests() {
  echo "==> Running E2E tests against $E2E_BASE_URL"
  # E2E_RESET_DB makes the test suite itself reset the database before the first
  # test (see e2e/src/test/kotlin/DbReset.kt and DbResetExtension.kt), so callers
  # no longer need to invoke reset_db() separately beforehand.
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=true ./gradlew :e2e:test
}

case "${1:-}" in
  build) build_images ;;
  up) up ;;
  down) down ;;
  reset-db) reset_db ;;
  test) run_tests ;;
  run)
    build_images
    up
    run_tests
    down
    ;;
  *)
    usage
    exit 1
    ;;
esac
