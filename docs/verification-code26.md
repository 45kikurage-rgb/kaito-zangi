# code26 検証記録

- ソース: 9ff6c58395d3c9b52b5c247b127b5ace8e369ec0
- Java17 / Android SDK35 / build-tools35.0.0: scripts/build.sh成功。
- 回帰テスト14スイート・2,741チェックPASS。50問とCSV整合性PASS。
- APK: kaito-zangi-0.1.26.apk / jp.kossacktouch.app / versionCode26 / v0.1.26
- SHA-256: 943e4c29c78ed723c77782981867b2d465b2c6d1894a70f3bac394998860927e
- サイズ: 480278 bytes
- 標準apksigner v2/v3検証PASS。既存code25と証明書一致。
- 証明書: 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302
- 署名・package・versionCode上昇による更新互換性を確認。
- 外枠48dp固定、ボタン高さ48dp、状態は自動文字サイズ＋1行省略。
- 入力テストはメニュー終了後に画面を再取得し、世代・停止状態を確認して操作。
- 下→上探索順と上スワイプ実フロート高さ連動を維持。
- 旧APK・旧Release・設定保存先を維持。

実機上書き・設定保持・48dp描画・メニュー・LINEでのスワイプ/回答反映は未確認。
