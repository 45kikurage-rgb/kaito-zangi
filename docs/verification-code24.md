# code24 正式署名・配布検証

- 対象: 怪盗ザンギ 0.1.24 / versionCode 24 / jp.kossacktouch.app
- アプリソース: c729a979738f10af682ed1dd5c012d5af349b724（fad863eにアプリ内の旧版番号・高さ説明のみ訂正）
- Java 17 / Android platform 35 / build-tools 35.0.0でscripts/build.sh成功。
- 既存14テストスイート合計2,720チェックPASS。50問の照合データ、CSV整合性、第5問計算・入力・クーポン回帰PASS。
- 所有者バックアップの既存鍵を使用。新しい署名鍵は生成していない。
- Android標準apksigner: v2/v3検証PASS。
- 署名証明書SHA-256: 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302
- code23とcode24の証明書・packageが一致し、versionCodeは23から24へ増加。上書き互換条件PASS。
- code24 APK SHA-256: 9f7862d569a41d8565c980b50f0355dff5a8aea665b14afc8b23399f23c4b407
- APKサイズ: 476,182 bytes
- code23と過去APKは保持。全配布APKの既存SHA256SUMS検証PASS。
- GitHubで署名済みAPKを再検証してから新規Releaseを作成するworkflowを追加。秘密鍵はGitHubに登録・コミットしない。

## ソフトウェア確認と実機確認の区別

通常フロート144dp固定、縮小72dp、入力テスト360dp、行動表示、文字自動縮小・省略をコードで確認。回答問題優先、最新画面再照合、pendingによる二重タップ防止を確認。

停止・回答・完了など履歴へ記録された内容は全文を保持する。すべての一時statusメッセージを履歴保存する仕様ではなく、最新状態全文は設定に保持しアプリ画面へ表示する。

実機の上書きインストール・設定保持、再生前後の高さと文字収まり、報告された第2問BタップとLINEでの正解反映、第5問からクーポン共有までの実動作は未確認。ソフトウェアテストや本番公開を実機確認済みとは扱わない。
