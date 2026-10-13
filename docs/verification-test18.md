# code18 検証・公開記録

2026-10-13。現行code15を維持し、別枠テスト版code18を公開。

| 項目 | 結果 |
|---|---|
| ソース | b97f5fe19c0a4357362ac4f3609614fb3acc4501 / PR #14 |
| ソースmain統合 | ee10056e2fba233cb107935144d4bb6c87307bfd |
| 署名APK公開main | 382d4f16fa61a88a16e829a0f201bd966ccc34a0 / PR #15 |
| versionCode / versionName | 18 / 0.1.18-test18 |
| package | jp.kossacktouch.app |
| APKサイズ | 472,006 bytes |
| APK SHA-256 | d27b35fa78fcdd98b3f1ee533829ef8f5929b280f393b668f934eb269fe7039a |
| 証明書SHA-256 | 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302 |
| 署名 | 既存所有者バックアップ鍵。標準apksigner v2/v3検証成功、旧code15/17と証明書一致 |
| ビルド | Java17 / Android SDK35 / D8 / zipalign 成功 |
| 自動チェック | 2,671成功。実機試験ではない |
| 正答データ | 旧code17 APK内のassets全2ファイルとバイト一致 |
| 設定 | package・local/line_保存先維持。Storeの変更はVERSIONのみ |
| 実機上書き・設定保持 | 未確認。更新互換条件の確認と区別 |
| code18の自動送信・クーポン押下・他機種完走 | 未確認 |

## CI

- [PR #14 検証 38345198678](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38345198678): success
- [main 検証 38345298028](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38345298028): success
- [code18 Release 38345421069](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38345421069): success
- [Pages 38345420959](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38345420959): success
- [旧code17保全 38345421082](https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38345421082): success

## 公開先からの独立再取得

2026-10-13、次の3経路からAPKを再取得。すべて472,006 bytes、上記SHA-256と一致し、v2/v3署名と既存証明書を再検証。

- [GitHub Release](https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.18-test18)（prerelease、latest=false）
- [GitHub Pages](https://45kikurage-rgb.github.io/kaito-zangi/#test-apk)
- [既存ダウンロードページ](https://kaito-zangi-test06.regal-elk-8007.chatgpt.site/#test-apk)

両ページのlatest.jsonはcode15、latest-test.jsonはcode18。両ページからcode15 APKも再取得し、以前のSHA-256 `327ceb997d425e048e5452483f4fa443ec967ffedc78ca631a63372093f3ba31` と一致。既存APK全件のチェックサム検査成功。
Sites version14 / source `01a5b8bf9671ce9242d3d99450c4dd06a934ae49` / deployment `appgdep_6acdb05aa1b08191b40c9dbcf02752aa` succeeded。

## 実機で確認すること

アンインストールせずcode18へ上書きし、初回登録事前承認・フロート位置・LINE別履歴が保持されることを確認する。
「第5問・入力テスト設定」をOFFにして通常再生。第5問の答え表示→数字入力→読み戻し→送信→「クーポンをゲットする」を確認する。ONの再生1〜3は従来どおり入力だけ。
要素未検出時に0.5秒で再確認し、最大6往復・26秒まで探索すること、他機種でも進むことを確認する。送信結果不明の際は再送しない。停止した場合は答えが残ることと診断のAUTO_S4/AUTO_S6/AUTO_S7/AUTO_COUPONを確認する。

ユーザー提供code17ログで確認済みなのは、Nothing/A015・Android16・複製LINEの入力候補と直接入力／読み戻し一致。初回アンケートから最終問題までの到達はユーザー報告。5問正解の写真だけで自動送信やcode18完走を確認済みにはしない。
