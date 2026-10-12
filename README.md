# 怪盗ザンギ

署名・公開作業を引き継ぐ場合は、最初に[既存署名鍵と公開手順](docs/signing-handoff.md)を確認してください。Workの一時ファイルがなくても、Libraryの所有者用バックアップから確認します。

カメラ追加前のcode10の動作に戻したAndroidアプリです。第1〜4問はLINEの画面要素から登録キーワードと50件の正答表を照合して専用A〜Dをタップ。第5問はローカル計算結果とキーボードを表示したところで停止し、入力・送信は本人操作です。標準／対象複製LINEの認識、初回参加／再挑戦、第4問後の上下スワイプを維持しています。

現在版: **0.1.14-calc15 / versionCode15**。第5問CSV計算マスターを同梱。上方向探索は画面高さの約10%、下方向は従来どおり。第5問は答え表示後に停止し、入力・送信は本人操作です。固定署名・package・設定保存先を維持。2,514自動チェック合格。実機上書き更新・設定保持・LINE受入は未確認です。

- 配布: https://45kikurage-rgb.github.io/kaito-zangi/
- 配布: https://kaito-zangi-test06.regal-elk-8007.chatgpt.site
- Release: https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.14-calc15
- 検証: [docs/verification-calc15.md](docs/verification-calc15.md)

利用者の2026-10-10指示「カメラ機能が良くないので、1つ前のに戻してください」に対応。code10 commit a09edf0bf8506338213c4f42ccd65c1dea302922 のアプリソース・資産・ビルド手順を復元し、版情報だけ変更。旧code01〜11 APK・Release・分割アーカイブは保持。復帰版の実機上書き更新・LINE受入は未確認。

## ビルド

code10と同じJDK17、Android SDK35のaapt／javac／D8／zipalignを使います。カメラ用Gradle依存は不要です。

```bash
export ANDROID_JAR=/path/to/android-sdk/platforms/android-35/android.jar
export ANDROID_BUILD_TOOLS=/path/to/android-sdk/build-tools/35.0.0
bash scripts/compile.sh
```

固定所有者鍵を安全な場所でSIGNING_STORE／SIGNING_STORE_PASSWORD／SIGNING_ALIASに指定した場合のみ `bash scripts/build.sh` で署名できます。鍵・パスワードはリポジトリや公開ページに含めません。CIの署名Secretsの現在の設定状態は未確認です（2026-10-12確認）。既存バックアップの復号・署名能力は確認済みです。通常pushのCIは回帰テストと未署名コンパイル、公開CIは署名済みAPKのチェックサム検証と配布を行います。

`bash scripts/test.sh` はSDK・署名鍵不要で、2,494チェックを実施します。
