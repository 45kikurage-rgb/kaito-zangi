#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_HOME:?Set ANDROID_HOME to the Android SDK root}"
mkdir -p build/out
python3 scripts/check-data.py
bash scripts/test.sh
GRADLE_BIN="${GRADLE_BIN:-./gradlew}"
"$GRADLE_BIN" --no-daemon :app:assembleRelease :app:lintRelease
cp app/build/outputs/apk/release/app-release-unsigned.apk build/out/aligned.apk
printf 'Android compilation, bundled Japanese OCR, manifest merging and lint verified.\n'
