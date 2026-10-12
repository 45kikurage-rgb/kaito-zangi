# 怪盗ザンギ code16 改修・署名公開ゲート（2026-10-12）
 
## 現状
- 開発: PR #8 / feature/registration-preapproval-20261012
- 改修版: versionCode 16, versionName 0.1.15-auto16。公開済みは code15 のまま。
- 構文・Android SDK35コンパイル/既存データと回帰テスト: GitHub PR Verify成功。例 https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38213651159 の verify ジョブ成功。
- 正式署名: **未完了**。同じrunの build ジョブで SIGNING_STORE_BASE64 / SIGNING_STORE_PASSWORD が空欄、Fixed owner signing key is unavailable により失敗。公開版差替禁止。
- GitHub main、GitHub Releases、Pages最新APKは未変更（code15維持）。

## 実装
1. 初回登録事前承認（個別端末でON）: 表示候補からランダム選択、本人情報等・既知の参加規約選択を含む。未知画面は停止。
2. 登録選択肢不在時は確認済みLINEの登録文脈に限り短い上スワイプ約10%＋従来の下スワイプを上限3往復で探索。
3. 第5問の数値回答: 問題文→計算→LINE入力欄1件→ACTION_SET_TEXT→同一数値の再確認→送信ボタン1件→二重送信防止の記録→1回送信。
4. クーポン: 「クーポンをゲットする」→「クーポンを表示する」。重複クリック禁止。画面表示を検証できなければ要確認として停止。
5. フロート: コンパクト56dp、「怪盗ザンギ」「▶」「×」の横並び。実行中168dpかつ全幅、二つの操作ボタンは各56dp、位置保存とディスプレイ上端／カットアウト配置への試行。

## 必須残作業
1. **code15で使用した既存owner固定署名鍵** を秘密のまま安全に取得。別鍵の発行・再インストール依頼は行わない。
2. GitHub Actions の `SIGNING_STORE_BASE64`, `SIGNING_STORE_PASSWORD` を適切に登録（キー内容をログやrepoに掲載しない）。署名用ワークフロー手動起動。
3. `scripts/build.sh` でcode16を署名し、`apksigner verify --print-certs` により証明書 SHA-256 **20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302** を旧APKと照合。署名が一致しない場合公開中止。
4. 実機上書き（Nothing Phone 2a, CMF Phone 1）と設定保持、LINE表示・登録選択・入力・送信・クーポン表示・フロート座標・ステータスバー操作を検証。
5. 署名済みAPKをGitHub Releaseへ登録、Pagesのダウンロード先・latest.json・PWA cache・SHA256を整合更新。旧版を維持しつつ公開APKを再取得してハッシュ・署名を比較する。
6. mainへのPR統合、本番更新・実機受入を別々に記録。前後でコード15を維持したロールバック経路を保全。

## 既知の未検証点
- ネイティブLINEの青い送信ボタンの実際のresource idと入力欄ACTION_SET_TEXT可否は未確認。認識不能なら送信しない。
- クーポンWebViewが画面テキストを公開しない場合、リンクはクリックされても自動完了の確証がない。確認停止となる。
- ステータスバーへの重なりはAndroid/ランチャー実機によって異なるため、端末2機種で要確認。
- ご本人以外へ配布した場合、事前承認設定は自動引継ぎせず端末ごとの選択が必要。
