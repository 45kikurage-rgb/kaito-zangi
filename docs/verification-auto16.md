# code16 署名・配布検証

ソース: PR #8 head 08f88640b701575941b2c9f00baab52291215efc。
PR CI run 38224799340成功。main統合 df1b639d93e1534c14cc20a66e91c81138135e26。
Libraryの既存owner backupを使用し、scripts/build.sh、Android SDK標準apksignerで署名。新鍵作成なし。
Android35コンパイル、2,531自動チェック成功。v2/v3署名有効。
旧code15 APK実体との証明書SHA-256一致: 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302
APK: 459718 bytes / SHA-256 2479267eb26d8ef7f2c9cd70c725fa438d5567241e461efc4e9b2b5f179e8a4b
package jp.kossacktouch.app、versionCode16、versionName0.1.15-auto16、minSdk26、targetSdk35。
正答表50件と第5問CSVはcode15ソースから変更なし、APK同梱内容もバイト一致。
Storeの設定・履歴保存先は不変。これは静的確認であり実機の設定保持や上書きインストールは未実施。
実機受入: Nothing Phone 2a/CMF Phone 1で更新・設定保持、第5問入力と送信、登録事前承認、クーポン表示、フロート移動と上端配置を確認する。
code15 APK/latest.jsonはバイト単位で維持。code16はlatest-test.jsonに分離。

## 本番公開後の検証（2026-10-12 UTC）

- PR #9をmainへ統合: 1da180be67a66a837c6611fec85934e39873bd10。
- main Android CI [38227767900](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38227767900)、[Pages配信](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38228283649)、[テスト版Release作成](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38228283676)すべてsuccess。
- [GitHub Releases](https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.15-auto16)はprereleaseとして公開。既存リリースの資産は変更なし。
- [GitHub Pagesの別枠](https://45kikurage-rgb.github.io/kaito-zangi/#test-apk)と[既存サイトの別枠](https://kaito-zangi-test06.regal-elk-8007.chatgpt.site/#test-apk)でcode16リンクを確認。
- 既存サイトはversion12、本番デプロイsucceeded。ソースcommit: 21807fcb94d81cbd65b68ba58b195bbd6b272ec4。
- GitHub Releases、GitHub Pages、既存サイトの3公開URLからcode16 APKを再取得。すべて459718 bytes、SHA-256 **2479267eb26d8ef7f2c9cd70c725fa438d5567241e461efc4e9b2b5f179e8a4b**でローカル正式署名APKと完全一致。
- 再取得APKを標準apksignerで検証しv2/v3署名有効、既存証明書SHA-256一致を再確認。
- 両ダウンロードサイトのcode15 APKを再取得。455622 bytes、SHA-256 **327ceb997d425e048e5452483f4fa443ec967ffedc78ca631a63372093f3ba31**で従来配布物と一致。
- 両サイトのlatest.jsonはcode15、latest-test.jsonはcode16。現行版とテスト版は分離済み。

実機による上書き更新・設定保持・LINE操作は未実施。上書き更新の署名/package/versionCode条件は満たすが、実機動作を確認済みとは扱わない。同一packageなので併存インストールはできず、code16からcode15へ通常の上書きダウングレードはできない。
