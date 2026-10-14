# code25 検証記録

- ソース: b6af307060fec129019f373f3bd504d2790870e0
- Java17 / Android SDK35 / build-tools35.0.0: scripts/build.sh成功。
- 回帰テスト14スイート・2,740チェックPASS。50問とCSV整合性PASS。
- APK: kaito-zangi-0.1.25.apk / jp.kossacktouch.app / versionCode25 / v0.1.25
- SHA-256: b7f58d254197cb9a7b8e9460d192f8a27690aed4d3ecc88e3c72a98002b635d5
- サイズ: 476182 bytes
- 標準apksigner v2/v3検証PASS。既存code24と証明書一致。
- 証明書: 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302
- 旧APK、Release、設定保存先を維持。署名鍵・パスワードはGitへ含めない。
- density別のフロート高さ移動、狭い領域のclamp、下→上順、6往復26秒、下距離・速度維持をテスト。
- フロート外枠72dp固定、通常は状態＋操作、入力テストは36dp+36dp。ボタン重なり時の既存位置退避は維持。
- 旧code23/24の公開workflowは、最新版メタデータ更新だけでは起動しないよう対象パスを限定。

実機上書き・設定保持・フロート描画・LINEでのスワイプ/回答反映は未確認。公開検証と実機確認を混同しない。
