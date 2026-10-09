#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_JAR:?Set ANDROID_JAR to platform android.jar}"
: "${ANDROID_BUILD_TOOLS:?Set ANDROID_BUILD_TOOLS to SDK build-tools directory}"
: "${SIGNING_STORE:?Set SIGNING_STORE to private fixed keystore}"
: "${SIGNING_STORE_PASSWORD:?Set SIGNING_STORE_PASSWORD}"
SIGNING_ALIAS="${SIGNING_ALIAS:-kossacktouch}"
bash scripts/compile.sh
java -jar "$ANDROID_BUILD_TOOLS/lib/apksigner.jar" sign --ks "$SIGNING_STORE" --ks-key-alias "$SIGNING_ALIAS" --ks-pass env:SIGNING_STORE_PASSWORD --key-pass env:SIGNING_STORE_PASSWORD --out build/out/signed-candidate.apk build/out/aligned.apk
java -jar "$ANDROID_BUILD_TOOLS/lib/apksigner.jar" verify --verbose --print-certs build/out/signed-candidate.apk > build/signature-verification.txt
EXPECTED_CERT='20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302'
if ! grep -q "^Signer #1 certificate SHA-256 digest: $EXPECTED_CERT$" build/signature-verification.txt; then
  rm -f build/out/signed-candidate.apk
  printf 'Existing application signing certificate mismatch.\n' >&2
  exit 1
fi
mv build/out/signed-candidate.apk build/out/kossack-touch-0.1.6-test07.apk
sha256sum build/out/kossack-touch-0.1.6-test07.apk > build/SHA256SUMS
printf 'APK built and signature verified.\n'
