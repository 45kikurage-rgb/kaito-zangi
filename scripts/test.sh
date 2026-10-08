#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/tests
java com.sun.tools.javac.Main --release 8 -encoding UTF-8 -d build/tests app/src/main/java/jp/kossacktouch/core/*.java tests/CoreTests.java
java -cp build/tests CoreTests | tee build/test-results.txt
python3 scripts/generate-ui-tests.py
java com.sun.tools.javac.Main --release 8 -encoding UTF-8 -classpath build/tests -d build/tests build/UiTests.java
java -cp build/tests UiTests | tee -a build/test-results.txt

python3 scripts/generate-answer-tests.py
java com.sun.tools.javac.Main --release 8 -encoding UTF-8 -classpath build/tests -d build/tests build/AnswerFixture.java tests/VideoFixture.java tests/AnswerTests.java tests/FlowTests.java
java -cp build/tests AnswerTests | tee -a build/test-results.txt
java -cp build/tests FlowTests | tee -a build/test-results.txt
