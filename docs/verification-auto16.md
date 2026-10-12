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

公開APKの再取得結果は公開後に記録する。
