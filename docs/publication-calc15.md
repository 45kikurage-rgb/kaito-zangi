# code15 公開検証

- Version: 0.1.14-calc15 / versionCode 15
- 基準main: 4712476dd65892b088333ca17248d47966deec9b
- 配布commit: 0b6a4e8a3246aa23dde5a7b4e666f23c816110dd (release/code15)
- code15 CI: https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38141860670 — success
- Release CI: https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38141860732 — success
- GitHub Release: https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.14-calc15 — APK登録済み、実機未検証のためprerelease
- Sites: https://kaito-zangi-test06.regal-elk-8007.chatgpt.site — 公開version11、deployment succeeded
- Sites source commit: 60c07c15a5175634e9f2994f45554fc389f8041b
- SitesとGitHub Releaseの公開URLから認証ヘッダーなしでAPKを再取得。両方455622 bytes、生成APKとSHA-256一致。
- SHA-256: 327ceb997d425e048e5452483f4fa443ec967ffedc78ca631a63372093f3ba31
- Sites latest.json: code15、同一SHA、physicalDeviceVerified=falseを再取得確認。
- 旧code14と新code15の署名一致。公開Sites APKも署名証明書一致。
- 証明書SHA-256: 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302
- APK内のanswers.jsonとCSVはソースとバイト一致。旧正答データ・画像・過去配布資産保持。
- 上書き更新の互換条件（同一package/署名、増加versionCode）、設定保存処理の不変を静的確認。実機/エミュレーターへのインストール、設定保持、LINE動作は未確認。

mainへの直接pushは当初自動承認審査で拒否されたが、2026-10-11 22:14 JSTのユーザー指示「本番までお願いします」を受け、PR #7をmainへマージ済み。

## main・GitHub Pages 本番反映完了（2026-10-11）

- Merge commit: 1bb900d294161e65db698f0690cb182533a3cd0a
- main CI: https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38142441149 — success
- Pages deploy: https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38142441146 — success
- Release workflow: https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38142441154 — success、既存code15配布資産を保持
- https://45kikurage-rgb.github.io/kaito-zangi/ のHTMLとlatest.jsonを直接取得し、code15表示・リンク一致を確認。
- Pages公開APKをHTTP 200で再取得。455622 bytes、SHA-256 327ceb997d425e048e5452483f4fa443ec967ffedc78ca631a63372093f3ba31。生成APK・Sites・GitHub Releaseと一致。
- 公開完了と実機受入は別。実機上書き更新・設定保持・LINE動作は引き続き未確認。
https://github.com/45kikurage-rgb/kaito-zangi/pull/7
