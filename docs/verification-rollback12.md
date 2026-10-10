# 回答ザンギ code12 / code10復帰版

2026-10-10利用者指示「カメラ機能が良くないので、1つ前のに戻してください」。基準main 2f8595a50db96669d1882357469dcfee0f29d888。統括CURRENT Version2026-10-09.1／更新2026-10-08T20:50:11.617998Zは前回確認から変更なし。

code10 commit a09edf0bf8506338213c4f42ccd65c1dea302922 のapp/src/main、scripts/compile.sh・build.sh・test.shを復元。manifest・Store・MainActivityの版情報だけ0.1.11-rollback12／12に変更。カメラActivity・文字処理・OCRモデル・権限・フロート📷を削除。ビルドもcode10のSDK35 aapt／javac／D8へ復帰。

2,494自動チェックとAndroid35コンパイルが成功。既存のMathEngine、AnswerBank、LINE操作・ガード、SharedPreferences local／line_packageはcode10のまま。実機での上書き更新とデータ保持は未確認。

所有者専用バックアップから固定鍵を安全に使用し、v2/v3署名APKを生成。旧code10とcode11の証明書SHA-256 20f8f43972be30b9b3ac94f83e3bcb8d5bf98d3c81b0b47bb7a24b19aa57d302に一致。更新番号12は旧10／11より大きい。旧APKは削除しない。秘密鍵・パスワードはリポジトリや配布物に含めない。

APKサイズ: 463724 bytes
SHA-256: b0d379f90a8c12f47997fbb34536be32cd3d3e1555eb27ed4f879e3f3ba9721a

カメラ権限・通信権限がないこと、CameraActivity／OCRモデルがAPKにないことをソフトウェア上で検証。LINE以外の誤操作防止、標準・複製LINE、初参加・再挑戦・第4問上下スワイプ、第5問の本人操作引渡しは既存回帰テスト成功。実機上書き・LINE動作は受入待ち。
