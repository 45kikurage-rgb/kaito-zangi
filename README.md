# 回答ザンギ

LINEの「超良問スポーツ6」を端末内で認識・正答照合・計算する独立Androidアプリ。ARUNOMATICとは別のpackage・署名・リポジトリです。

## ダウンロード

- code 6 テスト版配布ページ: https://kaito-zangi-test06.regal-elk-8007.chatgpt.site
- 既存ページ（code 5を保持）: https://45kikurage-rgb.github.io/kaito-zangi/
- テスト版APK / ソース: https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.5-test06

新しいテスト版専用ページをSitesで一般公開。既存GitHub Pagesの改修ブランチからの更新は失敗し、既存URLは前版を保持。mainソース統合はPR #1で未マージ。テスト版の一般公開と、実機での動作検証を区別しています。

## 現在の版

- 0.1.5-test06 / versionCode 6
- package: `jp.kossacktouch.app`（今後固定）
- Android 8.0/API 26以上、target API 35
- Nothing Phone 2a / CMF Phone 1でのインストール・5問連続操作は**未検証**
- 第1〜4問: 50件の正答表を構造化。50件すべてを問題文の固有キーワードで識別し、正答表に登録したA〜Dへ直接照合。画像や選択肢本文は判定に使用しません。
- 第5問: 三角形、等差数列（区間和・閾値）、両端の並び、倍数5条件、ボクシング（差と約数・互いに素）の演算を実装。毎回の条件を抽出し、固定の数字を回答しません。
- 実例「ライトフライ48kg・フェザー57kg・互いに素」の期待値63をテスト。
- 第5問: 専用フォームの入力欄と回答ボタンがある場合はフォームを優先。動画で確認した「回答方法」「答えは数字」「小数点/スペース」のガイドが第5問に先行する場合だけ、LINE入力欄を開き、数値のみを1回送信。入力済みの文字は書き換えない。入力・送信欄のresource IDが対応しない場合は停止。実機未検証。
- 第4問後の回答方法画面では、提供動画どおりトーク領域を上へ移動して少し下へ戻す操作を交互に行い、隠れている「答え方確認しました！」を最大6回まで探索します。見つかった専用ボタンだけを押し、第5問を表示します。
- OCRは未搭載。UI要素取得不可時は停止。
- 初回参加登録: 3本の追加動画を確認し、導入の専用選択肢と既存情報の「登録する」に対応。「変更する」は押さない。都道府県・高校は現在見えている候補からランダムに選び、スクロールしない。生年・性別・規約同意は本人がLINEで操作した後に再生して続行。登録UI全走査は未提供、クリック成功は実機確認待ち。
- 標準/複製LINE: jp.naver.line系列のpackage、超良問ドリルのタイトル、LINE固有の画面構造を併せて認識。package名入力画面を削除。実行中のLINE切替で停止し、新しい対象トークで再生。未確認回答・登録記録はpackage別に保存。独自packageの仮想化アプリや要素取得不可の複製は対応外。
- リッチメニュー: 要素で「開始」を取得できる場合のみ自動タップ。提供UI-treeの画像だけのメニューは本人が開始を1回押す。推測座標で押さない。
- アイコン: 既存の指定デザインを使用し、Adaptive Iconの安全領域に収めた。アプリ内ロゴ、サイトの名称・説明・キャッシュ更新にも対応。

## 使い方

APKをインストール → アプリ内の権限設定をON → スタート → LINEの超良問ドリルを開く → 現在画面でフロートの▶再生（初回登録・再挑戦・問題表示・次問にも対応）。停止は■。完了時は待機に戻ります。フロート上部はドラッグで移動できます。

## データとプライバシー

`app/src/main/assets/answers.json` に問題識別キーワードと登録済みの正答文字A〜Dを保存。第1〜4問は問題文だけを照合し、画像・意味解析・選択肢本文の判定は行いません。

INTERNET権限なし。中央サーバー、端末同期、AI API、広告、解析SDKなし。画面取得はLINE package系列とネイティブの画面構造に限定し、超良問ドリルのトークタイトルを追加照合します。保存する履歴は認識した問題・回答・操作結果・エラーだけで、通常会話・アカウント情報・認証情報を保存/外部送信しません。Androidバックアップは無効。

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

## 最新の検証資料

- `docs/verification-test06.md`: 動画確認・実装・未検証を分離した改修報告。

## 成果物・追加資料

- `app/`: ソース・正答データ・専用アイコン
- `site/`: Cloudflare Pages用のスマートフォン向け配布ページ/PWA
- `docs/verification.md`: 検証結果と実機確認手順
- `docs/update.md`: 署名維持・更新・GitHub/Cloudflare公開手順
- `tests/fixtures/`: 会話/個人情報を除去したクイズ部分だけのUIテスト資料

**署名鍵とパスワードはGitHub・配布ページ・友人向けソースZIPに含めません。** 更新には同じ鍵を使用し、versionCodeを増やします。
