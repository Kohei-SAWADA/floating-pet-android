#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
OUT=$(mktemp -d "${TMPDIR:-/tmp}/floating-pet-sample.XXXXXX")
trap 'rm -rf "$OUT"' EXIT HUP INT TERM
if command -v javac >/dev/null 2>&1; then
  javac --release 17 -d "$OUT" "$ROOT/app/src/main/java/com/dot/floatingpet/core/SpriteAtlas.java" "$ROOT/scripts/ValidateSample.java"
else
  java -Xmx256m -m jdk.compiler/com.sun.tools.javac.Main -source 17 -target 17 -d "$OUT" "$ROOT/app/src/main/java/com/dot/floatingpet/core/SpriteAtlas.java" "$ROOT/scripts/ValidateSample.java"
fi
java -Xmx128m -Djava.awt.headless=true -cp "$OUT" ValidateSample "$ROOT/app/src/main/assets/pets/mofu/spritesheet.png"
