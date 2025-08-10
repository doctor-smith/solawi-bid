#!/bin/bash
# Usage: ./clean-dbeaver-dump.sh input.sql output.sql

input="$1"
output="$2"

if [[ -z "$input" || -z "$output" ]]; then
  echo "Usage: $0 input.sql output.sql"
  exit 1
fi

# 1. Remove any lines starting with DBeaver/mysql client meta commands (\...) or SET statements
# 2. Remove leading BOM if present
# 3. Remove comment headers that MySQL init hates
# 4. Convert CRLF to LF

grep -vE '^\\' "$input" | \
grep -vE '^--' | \
grep -vE '^SET ' | \
sed '1s/^\xEF\xBB\xBF//' | \
sed '/^$/N;/^\n$/D' | \
dos2unix > "$output"

echo "✅ Cleaned dump written to $output"
