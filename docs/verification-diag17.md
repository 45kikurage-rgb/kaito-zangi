# code17 検証・公開記録

更新日: 2026-10-12。package: jp.kossacktouch.app。versionName: 0.1.16-diag17。versionCode: 17。

## ソース・自動検証

- ベースmain: 1936f4039fe932b77fb4ef189c6f184ae75d62bf。
- APKのアプリソース: 9fd19b4e6277eab287f271cb2c067e3bf9b76ee5 (PR #11)。
- ローカル2,549チェック、Android35コンパイル、D8、zipalignに成功。
- PR CI: https://github.com/45kikurage-rgb/kaito-zangi/actions/runs/38249995593 成功。
- 動画の説明文による誤クリア、高校選択前の「わかった！」、右側の導入選択、登録確認「変更する」の除外を回帰検証。
- 入力テストに送信操作はない。通常の自動送信と分離し、候補拡張は入力だけのテストに限定。

## 署名・更新互換

- 以前に復元済みの正式所有者鍵を使用。新鍵は作成していない。保管先・復元手順は docs/signing-handoff.md。
- apksigner標準検証成功、v2/v3署名。
- code15・16・17の証明書SHA-256一致: `20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302`。
- APK SHA-256: `7518db97b30068b1c99f632b28632dd2a91ecd3b8a7e681ec4f94a718ef55eea`。467,910 bytes。
- package維持、versionCode増加、SharedPreferencesのlocal/line_<package>保存先と既存履歴形式を維持。アセット全てがcode16とバイト一致。
- 以上は静的な上書き互換条件。実機・エミュレーターでのインストールと設定保持試験は未実施。

## 保全

- code15のlatest.json・既存APKの全バイトを維持。code16 APKも維持。
- code16メタデータをsite/releases/code16.jsonへ保存し、過去版workflowも固定メタデータを検証するように変更。
- 鍵・パスワード・会話・動画はGit/公開物へ追加していない。

## 実機で確認する項目（未実施）

1. code15または16から上書きし、権限・事前承認・フロート位置・履歴を確認。
2. 未登録状態の導入二択→「わかった！」→学校選択→登録→第1〜4問。
3. 第5問の答えが入力前と停止後に表示され、手動操作を案内すること。
4. 入力テストON、回答案内と第5問を表示して▶。空欄で再生1/2/3。成功時の数字を手動で消して次へ。テスト中の送信がないこと。
5. 診断コピーを共有。再生3はクリップボード退避不可なら理由を表示して無操作となる。復元・コピーも端末差を確認。
6. テストOFFの通常再生で第5問の自動入力/送信・クーポン表示。未確認時は再送しないこと。

## 公開

- PR #11 merge: 2fb7ec36cde0345e16478657f3a921fbb839fb36。main CI run 38250269491成功。
- 配布PR #12 merge: cd9ab16905fd69b43000590b2b174949f038cbdb。
- GitHub Release: https://github.com/45kikurage-rgb/kaito-zangi/releases/tag/v0.1.16-diag17 （別プリリリース、latest=false）。公開run 38250473244成功。
- GitHub Pages: https://45kikurage-rgb.github.io/kaito-zangi/#test-apk 。公開run 38250473267成功。
- Sites: https://kaito-zangi-test06.regal-elk-8007.chatgpt.site/#test-apk 。既存publicを維持。version13 / source 9b71fb9ece96f912dcdddbb8fd7795eb8cfb66d7 / deployment appgdep_6accd59793688191b985fc2b0af92fa9、succeeded。
- 2026-10-12 21:43 JST以降、上記3配布先からAPKを独立再取得し、467,910 bytesと上記SHA-256の完全一致、標準apksignerで正式証明書の一致を確認。
- Pages/SitesのHTMLでcode17テスト枠とcode15リンクを確認。latest-test.json=17 / latest.json=15。
- 両ページのcode15 APKも再取得し、旧SHA-256 `327ceb997d425e048e5452483f4fa443ec967ffedc78ca631a63372093f3ba31` に一致。
- 過去code16 Releaseは変更せず、固定メタデータへ切り替えた公開run 38250473239も成功。

実機未検証のためcode17は別枠テスト版。公開成功・静的更新互換・実機受入は別扱い。Webのリンク/メタデータ/公開APKを検証し、ブラウザ画面の目視QAは未実施。
