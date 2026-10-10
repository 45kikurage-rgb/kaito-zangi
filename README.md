# 回答ザンギ

カメラ追加前のcode10の動作に戻したAndroidアプリです。第1〜4問はLINEの画面要素から登録キーワードと50件の正答表を照合して専用A〜Dをタップ。第5問はローカル計算結果とキーボードを表示したところで停止し、入力・送信は本人操作です。標準／対象複製LINEの認識、初回参加／再挑戦、第4問後の上下スワイプを維持しています。

現在版: **0.1.12-icon13 / versionCode13**。選択画像でアイコン・配布ページを統一。code12のカメラなし動作を維持し、上書き更新用のcode13です。package `jp.kossacktouch.app`、署名、設定・履歴の保存先を維持。カメラボタン・CameraActivity・OCR・カメラ権限はありません。外部通信・端末間連携もありません。

- 配布: https://45kikurage-rgb.github.io/kaito-zangi/
- 配布: https://kaito-zangi-test06.regal-elk-8007.chatgpt.site
- Release: https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.12-icon13
- 検証: [docs/verification-icon13.md](docs/verification-icon13.md)

利用者の2026-10-10指示「カメラ機能が良くないので、1つ前のに戻してください」に対応。code10 commit a09edf0bf8506338213c4f42ccd65c1dea302922 のアプリソース・資産・ビルド手順を復元し、版情報だけ変更。旧code01〜11 APK・Release・分割アーカイブは保持。復帰版の実機上書き更新・LINE受入は未確認。

## ビルド

code10と同じJDK17、Android SDK35のaapt／javac／D8／zipalignを使います。カメラ用Gradle依存は不要です。

```bash
export ANDROID_JAR=/path/to/android-sdk/platforms/android-35/android.jar
export ANDROID_BUILD_TOOLS=/path/to/android-sdk/build-tools/35.0.0
bash scripts/compile.sh
```

固定所有者鍵を安全な場所でSIGNING_STORE／SIGNING_STORE_PASSWORD／SIGNING_ALIASに指定した場合のみ `bash scripts/build.sh` で署名できます。鍵・パスワードはリポジトリや公開ページに含めません。CIの署名Secretsは未設定。通常pushのCIは回帰テストと未署名コンパイル、公開CIは署名済みAPKのチェックサム検証と配布を行います。

`bash scripts/test.sh` はSDK・署名鍵不要で、2,494チェックを実施します。
