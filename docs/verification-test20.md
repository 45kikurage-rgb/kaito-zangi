# code20 公開・引き継ぎ記録

確認日: 2026-10-13 JST。0.1.20-test20 / versionCode20 / jp.kossacktouch.app。

## 完了

- code19のフロート文字1.5倍、▶再生・||停止・×閉じる、遮蔽回避スライド、クーポン表示リンク認識を継承。
- 別Workから共有されたUIログで、最新クーポンカード内の「クーポンを表示する」がIDなしのクリック可能TextViewで取得できることを確認。16201.jpgの下部共有アイコンを追加要件の根拠とした。
- 表示リンク→同じLINEのクーポンWebView→下部の共有ボタン1回まで実装。共有先は自動選択しない。0.5秒周期、2回照合、操作直前再取得、操作前記録、期限、重複防止、診断を維持。
- 2,711チェック、Android SDK35コンパイル、正式鍵でapksigner v2/v3署名検証成功。
- ソースcommit: 158f177c9f23ff38223bb78c92e5c33ce113a409
- 配布commit: 3cd49425d01993c0e8b7d1135ac18ae880d4fd5d
- PR #18: https://github.com/45kikurage-rgb/kaito-zangi/pull/18
- main統合: 00bdc287df972cb2b49f7b094b239d79b92b1570
- PR CI 38348745188、main CI 38348891988成功。Release 38348892011、Pages 38348892067成功。
- Sites version15公開成功。source 26e810d4bf2accaf56412c259d67a0ff273710c1。
- GitHub Releases、GitHub Pages、Sitesの3配布先から再取得したAPKは476,102 bytesで同一SHA-256・同一正式署名。Sitesは認証付き取得と公開URLの通常取得で一致。
- code15のlatest.jsonと全既存APKは変更なし。既存Sites内の18 APKも保全を比較確認。

APK SHA-256: `a79500f5c83833368559db337d0c680d4cadc4346104993e3a83e2491dabe170`

証明書 SHA-256: `20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302`

## 配布処理の追加修正

旧code18 workflowが最新版latest-test.jsonを読んでversionCode==18の検証に失敗。旧APKのハッシュ自体は成功し、APKの破損・置換はなし。PR #19で参照とトリガーをsite/releases/code18.jsonへ固定した。main 4659168011faac5182fc2a922b8f6fc4bdc0ee85、run 38349068427成功。code20 APKの再生成は行っていない。

## 実機未確認

code20の上書き・設定保持、クーポン表示リンク、共有ボタンの認識・押下、共有先一覧表示は未確認。写真は共有アイコンの内部ラベルやresource IDを確定する証拠ではない。ACTION_CLICKの戻り値を共有先一覧の表示確認とは扱わない。

ユーザー報告でcode18の第5問回答・送信・「クーポンをゲットする」は成功。これはcode20の実機完走確認とは区別する。

通常▶で実行する際は「第5問・入力テスト設定」をOFFにする。共有待機で止まる場合はSHARE_SELECTED / SHARE_TOOLBAR / AUTO_SHAREの診断と実際の画面を確認する。共有先の自動選択・ARUNOMATICへの自動転送は今回の範囲に含まれない。

## 公開先

- https://kaito-zangi-test06.regal-elk-8007.chatgpt.site/#test-apk
- https://45kikurage-rgb.github.io/kaito-zangi/#test-apk
- https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.20-test20

次回も最新main・配布manifest・ユーザー実機報告を確認し、署名鍵は既存の所有者バックアップを利用する。鍵やパスワードを公開しない。
