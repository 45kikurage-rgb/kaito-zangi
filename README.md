# コサックタッチ

LINEの「超良問スポーツ6」を端末内で認識・正答照合・計算する独立Androidアプリ。ARUNOMATICとは別のpackage・署名・リポジトリです。

## ダウンロード

- 配布ページ（暫定GitHub Pages）: https://45kikurage-rgb.github.io/kossack-touch/
- テスト版APK / ソース: https://github.com/45kikurage-rgb/kossack-touch/releases/tag/v0.1.0-test01

Cloudflare Pagesへの公開は未完了です。テスト版の一般公開と、実機での動作検証を区別しています。

## 現在の版

- 0.1.0-test01 / versionCode 1
- package: `jp.kossacktouch.app`（今後固定）
- Android 8.0/API 26以上、target API 35
- Nothing Phone 2a / CMF Phone 1でのインストール・5問連続操作は**未検証**
- 第1〜4問: 50件の正答表を構造化。46件は問題文の照合対象、画像依存4件は自動回答無効。
- 第5問: 三角形、等差数列（区間和・閾値）、両端の並び、倍数5条件、ボクシング（差と約数・互いに素）の演算を実装。毎回の条件を抽出し、固定の数字を回答しません。
- 実例「ライトフライ48kg・フェザー57kg・互いに素」の期待値63をテスト。
- 数字入力は**クイズ専用WebViewフォームの入力欄と回答ボタンを両方確認できる場合のみ**実行。資料に専用フォームのUI全走査がないため、既存のLINEトークの第5問画面では計算後に要確認で止まる可能性があります。LINEの通常入力欄・送信ボタンは操作しません。
- OCRは未搭載。UI要素取得不可時は停止。
- 初回参加登録: 動画で確認した専用選択肢を認識し、既存情報の「変更する」を避けて「登録する」を選択。新規の生年・性別・都道府県・高校は表示された専用選択肢からランダムに選択し、参加規約に同意して進みます。問題画面なら登録操作をスキップ。画面変化の確認、30操作/5分の上限、再操作防止を実装。登録画面のUI全走査が未提供のため、実機操作は未検証。

## 使い方

APKをインストール → アプリ内の権限設定をON → スタート → LINEの超良問ドリルを開く → フロートの▶再生（初回登録画面にも対応）。停止は■。完了時は待機に戻ります。フロート上部はドラッグで移動できます。

## データとプライバシー

`app/src/main/assets/answers.json` に問題識別情報・問題文の全文または元資料の先頭部分・正答・照合トークン・有効/無効を保存。短縮された問題文は全文と偽らず `source_prefix` と区別します。選択肢の位置だけで回答せず、正答本文を照合します。

INTERNET権限なし。中央サーバー、端末同期、AI API、広告、解析SDKなし。画面取得は許可されたLINE packageに限定し、超良問ドリルのトークタイトルを追加照合します。保存する履歴は認識した問題・回答・操作結果・エラーだけで、通常会話・アカウント情報・認証情報を保存/外部送信しません。Androidバックアップは無効。

## ビルド

Android SDKのplatform 35、build-tools 35.0.0、JDK 17、Python 3、zipを用意します。第三者ライブラリ/Gradle依存を使わずSDKのaapt・javac・D8・zipalign・apksignerでビルドします。

```bash
export ANDROID_JAR=/path/to/android-sdk/platforms/android-35/android.jar
export ANDROID_BUILD_TOOLS=/path/to/android-sdk/build-tools/35.0.0
export SIGNING_STORE=/private/kossacktouch.p12
export SIGNING_STORE_PASSWORD='private-password'
export SIGNING_ALIAS=kossacktouch
bash scripts/build.sh
```

`bash scripts/test.sh` だけならAndroid SDKと署名鍵は不要です（JDK 17、Python 3が必要）。

## 成果物・追加資料

- `app/`: ソース・正答データ・専用ベクターアイコン
- `site/`: Cloudflare Pages用のスマートフォン向け配布ページ/PWA
- `docs/verification.md`: 検証結果と実機確認手順
- `docs/update.md`: 署名維持・更新・GitHub/Cloudflare公開手順
- `tests/fixtures/`: 会話/個人情報を除去したクイズ部分だけのUIテスト資料

**署名鍵とパスワードはGitHub・配布ページ・友人向けソースZIPに含めません。** 更新には同じ鍵を使用し、versionCodeを増やします。
