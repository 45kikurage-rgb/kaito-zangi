# 更新と再配布

## 署名の維持

packageは `jp.kossacktouch.app`。初回署名証明書のSHA-256:

`20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302`

署名用の `kossacktouch.p12` とパスワードは所有者専用の別バックアップに保存する。公開ソース、APK、配布ページ、友人への説明には秘密鍵を含めない。鍵を紛失すると、このpackageの上書き更新ができなくなる。

更新時はAndroidManifest.xmlのversionCodeを必ず増やし、versionName、Store.VERSION、APKファイル名、配布ページの版情報、`latest.json`、更新履歴を同時に変更する。再ビルドしたAPKの署名証明書が同じこととSHA-256を検証する。

## 新規GitHubリポジトリ

リポジトリ名: `45kikurage-rgb/kaito-zangi`。独立した公開GitHubリポジトリとテスト版Releasesを作成済み。既存ARUNOMATICのリポジトリへは一切反映しない。

リポジトリ: https://github.com/45kikurage-rgb/kaito-zangi

テスト版Release: https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.4-test05

後続変更はこのリポジトリにpushする。署名鍵、signing.env、個人情報を含む元資料、toolchainはpushしない。`site/downloads` の署名済みAPKとSHA256SUMSを公開配布用に追加する。

## GitHub Releases

`.github/workflows/release.yml` は手動実行でmainの署名済み配布APKを検証し、初回テスト版Releaseへ添付する。ビルド用秘密鍵は利用しない。後続版はソースでバージョンを増やし、Releaseタグ/タイトル/ファイル名も更新する。

GitHub Actionsでビルドする場合、`SIGNING_STORE_BASE64`, `SIGNING_STORE_PASSWORD` を所有者がGitHub Secretsに設定し、`.github/workflows/build.yml` の手動実行を使う。秘密鍵を一般公開せずビルド成果物を生成できる。

## Cloudflare Pages

Cloudflareの新規Pagesプロジェクト `kaito-zangi` を作成し、上記GitHubリポジトリのmainを連携する。

| 設定 | 値 |
|---|---|
| Framework preset | None |
| Build command | 空欄（ビルド不要） |
| Build output directory | site |
| Root directory | リポジトリルート |
| Production branch | main |

初回は`site/downloads/kossack-touch-0.1.4-test05.apk`を直接配信できる。Release公開後は配布ページのボタンをReleaseのダウンロードURLに変更してもよい。APKにAPIサーバー等は不要。

別案として、Cloudflareダッシュボードで `site/` の内容をアップロードして静的ページを作成できる。公開前にAPKリンク、versionCode、SHA-256、スマートフォンの表示と初回説明を確認する。

## 配布範囲

友人へ渡すのは公開ページURLまたは署名済みAPK。ソースZIPを共有しても、所有者用の署名鍵バックアップは共有しない。本版はテスト版。未検証の5問連続操作を実運用確認済みと表記しない。

## 暫定GitHub Pages

Cloudflareの認証が拒否されたため、同じsiteをGitHub ActionsからGitHub Pagesにも配信する設定を追加。Cloudflareへの公開が完了したという意味ではない。ActionsのPublish download pageワークフローでsiteをアップロードし公開する。Cloudflareへ移してもAPK・署名・packageを変える必要はない。

暫定配布URL: https://45kikurage-rgb.github.io/kaito-zangi/
公開確認済み。siteの変更をmainに反映するとGitHub Pagesが自動更新される。APK自体はGitHub Releasesで管理している。
