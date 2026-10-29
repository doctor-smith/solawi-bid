#!/usr/bin/env bash
set -euo pipefail

git fetch origin main --tags >&2

branch=$(git symbolic-ref --quiet --short HEAD || true)

if [[ "$branch" == "main" ]]; then
  version=$(git describe --tags --abbrev=0 HEAD)
else
  base_commit=$(git merge-base HEAD origin/main)
  version=$(git describe --tags --abbrev=0 "$base_commit")
fi

echo "$version"