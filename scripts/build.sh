#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_JAR:?Set ANDROID_JAR to platform android.jar}"
: "${ANDROID_BUILD_TOOLS:?Set ANDROID_BUILD_TOOLS to SDK build-tools directory}"
: "${SIGNING_STORE:?Set SIGNING_STORE to private fixed keystore}"
: "${SIGNING_STORE_PASSWORD:?Set SIGNING_STORE_PASSWORD}"
SIGNING_ALIAS="${SIGNING_ALIAS:-kossacktouch}"
mkdir -p build/classes build/generated build/dex build/out
python3 scripts/check-data.py
bash scripts/test.sh
"$ANDROID_BUILD_TOOLS/aapt" package -f -m -J build/generated -M app/src/main/AndroidManifest.xml -S app/src/main/res -A app/src/main/assets -I "$ANDROID_JAR"
find app/src/main/java build/generated -name '*.java' -print > build/sources.txt
java com.sun.tools.javac.Main --release 8 -encoding UTF-8 -classpath "$ANDROID_JAR" -d build/classes @build/sources.txt
find build/classes -name '*.class' -print > build/classes.txt
java -cp "$ANDROID_BUILD_TOOLS/lib/d8.jar" com.android.tools.r8.D8 --release --min-api 26 --lib "$ANDROID_JAR" --output build/dex @build/classes.txt
"$ANDROID_BUILD_TOOLS/aapt" package -f -M app/src/main/AndroidManifest.xml -S app/src/main/res -A app/src/main/assets -I "$ANDROID_JAR" -F build/out/unsigned.apk
cp build/out/unsigned.apk build/out/uncompressed.apk
(cd build/dex && zip -q -j ../out/uncompressed.apk classes*.dex)
"$ANDROID_BUILD_TOOLS/zipalign" -f -p 4 build/out/uncompressed.apk build/out/aligned.apk
java -jar "$ANDROID_BUILD_TOOLS/lib/apksigner.jar" sign --ks "$SIGNING_STORE" --ks-key-alias "$SIGNING_ALIAS" --ks-pass env:SIGNING_STORE_PASSWORD --key-pass env:SIGNING_STORE_PASSWORD --out build/out/kossack-touch-0.1.4-test05.apk build/out/aligned.apk
java -jar "$ANDROID_BUILD_TOOLS/lib/apksigner.jar" verify --verbose --print-certs build/out/kossack-touch-0.1.4-test05.apk > build/signature-verification.txt
sha256sum build/out/kossack-touch-0.1.4-test05.apk > build/SHA256SUMS
printf 'APK built and signature verified.\n'
