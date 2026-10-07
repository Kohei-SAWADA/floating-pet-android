#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT=$(mktemp -d "${TMPDIR:-/tmp}/floating-pet-smoke.XXXXXX")
trap 'rm -rf "$OUT"' EXIT HUP INT TERM
if command -v javac >/dev/null 2>&1; then
  javac --release 17 -d "$OUT" "$ROOT"/app/src/main/java/com/dot/floatingpet/core/*.java "$ROOT/scripts/CoreSmoke.java"
else
  # Some runtimes include the compiler module but omit the javac launcher/ct.sym.
  java -Xmx256m -m jdk.compiler/com.sun.tools.javac.Main -source 17 -target 17 -d "$OUT" "$ROOT"/app/src/main/java/com/dot/floatingpet/core/*.java "$ROOT/scripts/CoreSmoke.java"
fi
java -Xmx128m -cp "$OUT" CoreSmoke
