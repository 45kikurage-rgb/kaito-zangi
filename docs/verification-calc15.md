# 怪盗ザンギ code15 検証記録

基準main: 4712476dd65892b088333ca17248d47966deec9b。基準CI run 38140957066 verify成功、署名buildはskip。
版: 0.1.14-calc15 / versionCode15。Android35コンパイル、2,514回帰チェック成功。
署名: 保存済みowner backupの既存鍵。新鍵作成なし。旧code14と新code15のAPK実体をapksignerで検査し、v2/v3有効、証明書SHA-256一致。
証明書: 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302
APK: 455622 bytes / SHA-256 327ceb997d425e048e5452483f4fa443ec967ffedc78ca631a63372093f3ba31

package jp.kossacktouch.app、minSdk26、targetSdk35を維持し、versionCode14→15。Store.javaの変更は表示VERSIONのみで設定・履歴保存処理は不変。answers.jsonと画像は基準mainから変更なし。CSV同梱を確認。
以上は上書き更新互換条件と設定保存処理の静的確認。実機またはエミュレーターへの更新インストール、更新前後の設定保持は未実施・未確認。

実機で確認する項目: 旧版をアンインストールせず上書き、設定・履歴・権限保持、標準／複製LINEの第1〜4問、上約10%・下従来の探索、第5問計算値表示と本人操作への引き渡し、別画面時停止。
公開と公開APK再取得の結果は公開検証記録に別途記載する。
