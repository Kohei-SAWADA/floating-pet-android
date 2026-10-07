#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT=$(mktemp -d "${TMPDIR:-/tmp}/floating-pet-poll-benchmark.XXXXXX")
trap 'rm -rf "$OUT"' EXIT HUP INT TERM
JAVAC=javac
JAVA=java
if [ -n "${JAVA_HOME:-}" ]; then
  JAVAC="$JAVA_HOME/bin/javac"
  JAVA="$JAVA_HOME/bin/java"
fi
"$JAVAC" -d "$OUT" \
  "$ROOT/app/src/main/java/com/dot/floatingpet/core/PollPolicy.java" \
  "$ROOT/benchmarks/PollPolicyBenchmark.java"
"$JAVA" -cp "$OUT" PollPolicyBenchmark
