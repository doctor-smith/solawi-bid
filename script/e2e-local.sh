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
  build            Build the local Docker images (solawi-bid/frontend:local, solawi-bid/backend:local)
  build-frontend   Build only solawi-bid/frontend:local and recreate its container if the
                   environment is already up. Handy after frontend-only changes, so you don't have
                   to wait for a backend rebuild as well.
  build-backend    Build only solawi-bid/backend:local and recreate its container if the
                   environment is already up. Handy after backend-only changes, so you don't have
                   to wait for a frontend rebuild as well.
  up               Start the E2E environment (frontend, backend, database)
  down             Stop the E2E environment (database content is kept until the containers are removed)
  reset-db         Reset the E2E database to a clean state (same as CI: fresh DB + migrations). Mostly
                   useful on its own, e.g. to inspect a clean database; "test"/"run" below already do
                   this automatically before the first test.
  test [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E test suite (./gradlew :e2e:test)
  smokeTest [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E smoke tests (./gradlew :e2e:smokeTest)
  regressionTest [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E regression tests (./gradlew :e2e:regressionTest)
  userJourneyTest [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E user-journey tests (./gradlew :e2e:userJourneyTest)
  workflowTest [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E workflow tests (./gradlew :e2e:workflowTest)
  securityTest [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E security tests (./gradlew :e2e:securityTest)
  performanceTest [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E performance tests (./gradlew :e2e:performanceTest)
  devTest [--reset-db[=true|false]]
                   Reset the database (default: true), then run the E2E tests tagged @Develop (./gradlew :e2e:developmentTest)
  filterTest <pattern> [--reset-db[=true|false]]
                   Reset the database (default: true), then run only tests matching <pattern> (e.g. "org.solyton.solawi.bid.module.shares.*" or a single
                   "...ClassName.methodName")
  status-browsers  Show running Playwright browser instances
  kill-browsers    Stop all running Playwright browser instances
  run              Full local cycle: build, up, test (which resets the database), down
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

build_frontend() {
  ensure_frontend_base_image

  echo "==> Building solawi-bid/frontend:local"
  docker build --pull=false -f infrastructure/docker/Dockerfile-Frontend -t solawi-bid/frontend:local .
}

build_images() {
  build_backend
  build_frontend
}

# Rebuilds a single image and, if the environment is already up, recreates just that
# service's container so the freshly built image actually gets picked up (plain
# "docker compose up -d" would otherwise keep the old container running since the
# image tag itself didn't change).
recreate_service_if_running() {
  local service="$1"
  if docker compose -f "$COMPOSE_FILE" ps --status running --services | grep -qx "$service"; then
    echo "==> Recreating $service with the freshly built image"
    docker compose -f "$COMPOSE_FILE" up -d --force-recreate --no-deps --wait "$service"
  fi
}

up() {
  echo "==> Starting E2E environment ($COMPOSE_FILE)"
  docker compose -f "$COMPOSE_FILE" up -d --wait
}

down() {
  echo "==> Stopping E2E environment ($COMPOSE_FILE)"
  docker compose -f "$COMPOSE_FILE" down
}


parse_reset_db_flag() {
  local arg="${1:-}"
  case "$arg" in
    --reset-db|--reset-db=true|true|"") echo "true" ;;
    --reset-db=false|false) echo "false" ;;
    *)
      echo "ERROR: Unknown reset-db argument: $arg. Expected --reset-db, --reset-db=true, or --reset-db=false." >&2
      exit 1
      ;;
  esac
}

maybe_reset_db() {
  local should_reset="$1"
  if [[ "$should_reset" == "true" ]]; then
    reset_db
  fi
}

reset_db() {
  echo "==> Resetting E2E database (recreating $DB_SERVICE and $BACKEND_SERVICE from a clean state)"
  docker compose -f "$COMPOSE_FILE" rm -fsv "$BACKEND_SERVICE" "$DB_SERVICE"
  docker compose -f "$COMPOSE_FILE" up -d --wait "$DB_SERVICE" "$BACKEND_SERVICE"
  echo "==> Database reset complete"
}

run_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E tests against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:test
}

run_smoke_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E smoke tests against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:smokeTest
}

run_regression_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E regression tests against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:regressionTest
}

run_user_journey_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E user-journey tests against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:userJourneyTest
}

run_workflow_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E workflow tests against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:workflowTest
}

run_security_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E security tests against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:securityTest
}

run_performance_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E performance tests against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:performanceTest
}

run_dev_tests() {
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${1:-}")
  echo "==> Running E2E tests under development against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:developmentTest
}

# Runs only the tests matching a Gradle test-filter pattern (e.g. a single class or
# method), instead of a whole tag-based suite, for fast iteration during development.
run_filtered_tests() {
  local pattern="$1"
  local reset_db_flag
  reset_db_flag=$(parse_reset_db_flag "${2:-}")
  echo "==> Running E2E tests matching \"$pattern\" against $E2E_BASE_URL (reset_db=$reset_db_flag)"
  maybe_reset_db "$reset_db_flag"
  E2E_BASE_URL="$E2E_BASE_URL" E2E_RESET_DB=false ./gradlew :e2e:test --tests "$pattern"
}

case "${1:-}" in
  build) build_images ;;
  build-frontend)
    build_frontend
    recreate_service_if_running solawi-bid_frontend
    ;;
  build-backend)
    build_backend
    recreate_service_if_running solawi-bid_backend
    ;;
  up) up ;;
  down) down ;;
  reset-db) reset_db ;;
  test) run_tests "${2:-}" ;;
  smokeTest) run_smoke_tests "${2:-}" ;;
  regressionTest) run_regression_tests "${2:-}" ;;
  userJourneyTest) run_user_journey_tests "${2:-}" ;;
  workflowTest) run_workflow_tests "${2:-}" ;;
  securityTest) run_security_tests "${2:-}" ;;
  performanceTest) run_performance_tests "${2:-}" ;;
  devTest) run_dev_tests "${2:-}" ;;
  status-browsers) status_browsers ;;
  kill-browsers) kill_browsers ;;
  filterTest)
    if [[ -z "${2:-}" ]]; then
      echo "ERROR: filterTest requires a Gradle test-filter pattern, e.g.:" >&2
      echo "  $0 filterTest \"org.solyton.solawi.bid.module.shares.*\"" >&2
      exit 1
    fi
    run_filtered_tests "$2" "${3:-}"
    ;;
  run)
    build_images
    up
    run_tests --reset-db=true
    down
    ;;
  *)
    usage
    exit 1
    ;;
esac