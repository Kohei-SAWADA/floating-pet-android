<p align="right"><a href="README.md">English</a> | <strong>日本語</strong></p>

# Floating Pet for Android

<p align="center"><img src="docs/assets/floating-pet-hero.png" alt="Floating Pet。Androidのホーム画面に小さなもふもふペットを表示し、ドラッグ・サイズ調整・タップでChatGPTを開けるアプリのイメージ図" width="960"></p>

<p align="center"><strong>いつものホーム画面に、小さな相棒。</strong><br>ドラッグで移動。好みのサイズに。タップでChatGPTへ。</p>

Floating Petは、端末内だけで動く小さなAndroid向けペットアプリです。標準のホームアプリを検出すると、落ち着いたペットを重ねて表示します。同梱のサンプル **Mofu（もふ）** を使うか、対応する透明PNGスプライトシートを読み込めます。

**開発プレビュー：1.2.0-dev。** ソースと**debug署名の開発版APK**を公開しています。安定版・正式署名版・Google Play配布ではありません。APKの配布先は[GitHubの開発版リリース](https://github.com/Kohei-SAWADA/floating-pet-android/releases/tag/v1.2.0-dev)です。[導入手順](docs/INSTALL.md)、[検証状況](VERIFICATION.md)、[リリースチェックリスト](docs/RELEASE_CHECKLIST.md)をご確認ください。Android **8.0 / API 26以降**が必要です。

サムネイルは**機能を説明するイメージ図**で、実機スクリーンショットではありません。下には実機の設定画面とMofuの表示成功例を掲載しています。追加撮影には[撮影ガイド](docs/SCREENSHOTS.md)を使えます。

本アプリはOpenAIの公式・提携・公認製品ではありません。ChatGPTの名称は、アプリを開くショートカットの説明にだけ使用しています。サンプルペットは新たに生成した独自デザインで、OpenAIの公式キャラクターではありません。[素材の由来](docs/ASSET_PROVENANCE.md)をご確認ください。

## できること

- ホーム画面を検出すると小さなペットを表示し、判定できない場合は非表示に
- ドラッグで移動、**48〜240 dp**の幅でサイズ調整、画面サイズに合わせた位置保存
- 待機中は小さく呼吸し、触れると短く反応、ドラッグ中は方向に応じたポーズに
- タップでインストール済みの公式ChatGPT Androidアプリを起動。Floating Pet自体のログインやAPIキーは不要
- 日本語端末でも初回は英語で起動し、English / 日本語 / 端末の言語をアプリ内で選択。Android 13以降はアプリ別言語設定とも同期
- Mofuのサンプルと、v1/v2の規定サイズの透明PNGに対応
- 通知から一時非表示・再表示・停止。端末再起動後の自動起動なし
- ネットワーク権限・アクセス解析・広告・アカウント機能・実行時の外部ライブラリなし

## はじめに

### ダウンロードまたはビルドして導入

[GitHubの開発版リリース](https://github.com/Kohei-SAWADA/floating-pet-android/releases/tag/v1.2.0-dev)の [`Floating-Pet-1.2.0-dev-English-default-debug.apk`](https://github.com/Kohei-SAWADA/floating-pet-android/releases/download/v1.2.0-dev/Floating-Pet-1.2.0-dev-English-default-debug.apk) を使うか、下の手順でdebug APKをビルドしてください。[導入ガイド](docs/INSTALL.md)では、旧版と署名が違う場合の注意も説明しています。信頼できる配布元のAPKだけを導入します。このdebugビルドは検証用で、安定版・正式な配布版ではありません。

### 言語を選ぶ

この版は、日本語設定の端末でも初回は英語で起動します。設定画面の上部で **English**、**日本語**、**Use device language（端末の言語）** を選べます。言語を変更すると表示中のペットが停止するので、変更後に **Show pet / ペットを表示** を押してください。Android 13以降は「設定 → アプリ → Floating Pet → 言語」にも反映されます。古い対応Androidではアプリ内のボタンを使います。Android設定で先に選んだ言語がある場合は、その選択を尊重します。

### 初回の操作

1. **Floating Pet**を開き、**サンプルペットを使う**を選びます。手持ちの画像なら**spritesheet.png を選択**を使います。
2. **Androidの表示許可を開く**から、このアプリの「他のアプリの上に表示」を許可します。
3. **使用状況へのアクセスを設定**からFloating Petを有効にします。標準ホームアプリの検出に使用し、利用イベントは端末のメモリ内だけで処理します。
4. 必要に応じて通知を許可します。通知のボタンで一時非表示・再表示・停止できます。
5. サイズを調整し、**ペットを表示**を押してホームへ戻ります。表示されないときは、別のアプリを開いてからもう一度ホームへ戻ってください。
6. ドラッグで移動し、タップでChatGPTを開けます。終了するときは設定画面の**ペットを停止**を使います。

[プライバシー](PRIVACY.md)に記載した権限の範囲を確認し、納得できる場合にだけ許可してください。アプリが自動で権限を付与することはありません。

### 「App was denied access」で表示許可を有効にできない

実機で起きた拒否画面、間違えやすい階層、表示できた結果を順に示します。写真はユーザー提供の実画面をトリミングしたもので、残した範囲の文字や画素は変更していません。

**この画面が出たとき**

<p align="center">
  <img src="docs/screenshots/overlay-permission-disabled.png" alt="Floating Pet 1.2.0-devの「他のアプリの上に表示」がDisabledになっているAndroid設定画面" width="320">
  <img src="docs/screenshots/overlay-access-denied.png" alt="Androidの「App was denied access」警告。制限付き権限のリスクが説明されている" width="320">
</p>

APKと開発元を信頼し、権限のリスクを理解できる場合だけ進めます。Play Protectはオンのままにしてください。[Google公式の制限付き設定ガイド](https://support.google.com/android/answer/12623953?hl=ja)に沿った、アプリ単位の操作です。

1. **アプリ情報へ移動します。** 警告をCloseで閉じ、端末の **設定 → アプリ → すべてのアプリ → Floating Pet** を開きます。親画面の **App info（アプリ情報）** に留まります。
2. **画面のタイトルを確認します。** **App permissions** なら1回、**All permissions**なら2回戻ります。下の写真の⋮は階層が違うため、All permissionsしか出ません。**Manage app if unused** は変更不要です。

<p align="center">
  <img src="docs/screenshots/permissions-subpage-menu.png" alt="間違えやすい例。App permissions画面の⋮にはAll permissionsしか表示されない" width="500">
  <br><em>この写真はApp permissionsです。親のApp infoへ戻ってください。</em>
</p>

3. **信頼できるこのアプリだけを許可します。** **App info**の **⋮ / その他 → Allow restricted settings（制限付き設定を許可）** を選びます。本人確認が出たら端末上で操作し、PINやパスワードは他の人へ送らないでください。
4. **表示許可へ戻り、オンにします。** **他のアプリの上に表示 → Floating Pet** のスイッチをオンにします。アプリへ戻り、必要なら **Set up usage access** を設定して、**Show pet** を押しホームへ戻ります。制限を解除しただけでは、表示スイッチはオンになりません。

**実際に表示できた例**

<p align="center">
  <img src="docs/screenshots/home-screen-mofu.png" alt="ユーザーが成功を確認した実機例。ホーム画面にFloating Petのアイコンと公開用サンプルMofuが表示されている" width="600">
  <br><em>2026-10-07にユーザーから成功の連絡と、このホーム画面が提供されました。</em>
</p>

この写真は、ユーザーの端末でMofuを表示できた例です。全機種・すべての操作を検証したものではありません。App infoのメニュー自体は撮影されていないため、番号手順はGoogle公式案内に基づいています。機種名とAndroid版は未確認です。

**まだメニューが出ない場合：** 警告を閉じてからApp infoを開き直し、警告内の **Learn how to allow access** も確認してください。[Androidの確認フローの説明](https://android.googlesource.com/platform/prebuilts/fullsdk/sources/+/refs/heads/androidx-constraintlayout-release/android-35/android/app/ecm/EnhancedConfirmationManager.java)では、先に警告を表示する理由が説明されています。それでも拒否される場合は機種名とAndroid版を記録し、管理端末なら管理者へ確認してください。ADBでの強制許可、端末全体の保護の無効化、繰り返しの再インストールは避けます。制限付き設定の許可により、そのアプリで他の保護対象設定も選べる場合があるため、必要な権限だけを許可してください。

### いまのアプリで同じサンプルを試す

同梱画像は[Mofuのspritesheet.png](app/src/main/assets/pets/mofu/spritesheet.png)です。**PNG原本**をサイズ変更せずダウンロードし、既存アプリの画像選択から読み込んでください。1.1.3がすでに対応するv1形式なので、絵を試すだけなら新しいAPKは不要です。縮小サムネイルやシートのスクリーンショットは使用しないでください。

新しい**サンプルペットを使う**ボタンは、このソースからビルドした版に追加されます。画像を変更すると表示中のペットが停止するので、**ペットを表示**を押して再開します。読み込みを取り消したり失敗したりしても、以前の画像は残ります。

## 権限とプライバシー

| 権限・機能 | 目的 | 操作する場所 |
| --- | --- | --- |
| 他のアプリの上に表示 | ドラッグ可能なペットの描画 | Androidの表示許可設定 |
| 使用状況へのアクセス | 標準ホームアプリの検出 | Androidの使用状況アクセス設定 |
| フォアグラウンドサービス | 自分で開始したペットを維持 | 常駐通知と停止ボタン |
| 通知 | 非表示・再表示・停止ボタン | Androidの通知設定 |

インターネット・ストレージ読み取り・アクセシビリティ・マイク・カメラ・位置情報・連絡先の権限は要求しません。選んだPNGはアプリ専用領域へコピーします。設定にはサイズ・位置・動作の選択だけを保存し、アプリ利用履歴は保存・送信しません。ChatGPTを開いた後はChatGPT側のプライバシー条件が適用されます。

[プライバシーの詳細](PRIVACY.md) · [セキュリティ](SECURITY.md)

## ホーム判定と電池について

Androidの利用イベントは遅れて届く履歴情報で、現在の最前面画面を瞬時に取得するAPIではありません。**厳密にホームだけに表示する保証はありません。** アプリ切り替え直後に短く残る場合があります。ホームアプリ内の「最近使ったアプリ」やアプリ一覧は、ホームと区別できない場合があります。分割画面・ピクチャーインピクチャー・イベントの遅延・メーカー独自のバックグラウンド制限は実機での確認が必要です。

**反応優先**は画面オン・ロック解除中に約200 ms間隔、オフにすると省電力モードの約500 ms間隔で判定します。反応優先の照会頻度は最大2.5倍ですが、電池消費が2.5倍という意味ではありません。照会が遅い・失敗する場合は自動で間隔を空け、画面オフ時は定期照会を停止します。手動の非表示・停止をホーム判定が上書きすることはありません。

アニメーションはアプリ内設定・Androidのアニメーション設定・省電力モードに従います。待機中の描画は表示時だけ最大25 fpsです。Androidの保護された画面やオーバーレイ制限を回避しません。[構成の説明](docs/ARCHITECTURE.md)も参照してください。

## 自分のペットを使う

| 形式 | PNGの正確なサイズ | 配置 | 使用コマ数 |
| --- | --- | --- | --- |
| v1 | 1536 × 1872 px | 8列 × 9行 | 57 |
| v2 | 1536 × 2288 px | 8列 × 11行 | 73 |

- 1コマは**192 × 208 px**、背景透明、PNG全体で**12 MiB以下**
- 共通の行はidle、running-right、running-left、waving、jumping、failed、waiting、running、review
- v1の各行のコマ数は**6、8、8、4、5、8、6、6、6**
- v2はさらに2行・各8コマの視線方向ポーズを追加
- 現在の穏やかな動作制御では最初の5行を使用。残りは対応形式の素材であり、追加のアプリ機能を意味しません
- 任意サイズのシート・JPEG・単体アバター画像には非対応
- PNGヘッダー・寸法・デコード可否・アルファ対応・容量を検証後、元画像を一括で置換

[スプライトの仕様](docs/SPRITES.md) · [Mofuの由来](docs/ASSET_PROVENANCE.md)

## ソースからビルド

必要なもの：**JDK 17または21**、Android SDK **platform 35**と**Build Tools 35.0.0**、同梱のGradle wrapper。SDKの導入時は[GoogleのAndroid SDK利用規約](https://developer.android.com/studio/terms)をご確認ください。

`ANDROID_HOME`を設定するか、`local.properties.example`をGit管理対象外の`local.properties`にコピーしてSDKのパスを指定します。

```sh
chmod +x gradlew
./gradlew --no-daemon --max-workers=1 assembleDebug testDebugUnitTest lintDebug
```

Windowsでは`./gradlew`の代わりに`gradlew.bat`を使用します。

- debug APK：`app/build/outputs/apk/debug/app-debug.apk`
- 単体テスト：`app/build/reports/tests/testDebugUnitTest/index.html`
- lint：`app/build/reports/lint-results-debug.html`
- ソース・梱包検査：`python3 scripts/check_repository.py`

Gradle **8.13**、Android Gradle Plugin **8.9.2**、JUnit **4.13.2**を固定しています。Gradle配布物のチェックサムを固定し、wrapper JARも[公式チェックサム](https://gradle.org/release-checksums/)と照合しています。ビルド依存はGoogle Maven・Maven Central・Gradle Plugin Portalから取得します。

既存ソースとの互換性のため、application IDは元の`com.dot.floatingpet`を維持しています。これはブランドの権利を示すものではありません。手元でビルドしたdebug APKは、以前のAPKと開発用証明書が異なる場合があります。証明書が異なると上書きインストールできず、先にアンインストールすると設定と取り込み画像が消えます。秘密の署名鍵は同梱していません。安定版・正式署名版を配布する前にアプリIDと署名方式を決めてください。

## リポジトリ構成

```text
app/src/main/             Androidソース・英語/日本語リソース・サンプル
app/src/test/             Android非依存の制御ロジックの単体テスト
docs/assets/              README用サムネイル（イメージ図）
docs/screenshots/         権限画面の切り抜きと撮影メモ
docs/                     導入・素材仕様・プライバシー・公開手順
scripts/                  オフラインのリポジトリ/画像検査
benchmarks/               照会間隔のモデル測定（電池実測ではありません）
```

## 検証と公開

この変更版で実際に確認した範囲は[VERIFICATION.md](VERIFICATION.md)に記録しています。元の1.1.3の検証記録は別ファイルで残しており、今回の変更版のビルド・実機試験が通ったことを意味しません。

所有者の承認により、コード・素材の一般利用ライセンスを未選択のまま開発プレビューを公開します。公開されたこと自体は、再利用・再配布の一般的な許諾ではありません。安定版・正式署名版に向けて、ライセンス選択、素材の由来と権利の確認、広範な実機試験、追加スクリーンショット、正式署名とアプリIDの判断が残っています。[ライセンス状況](LICENSE-STATUS.md)、[外部依存の表記](THIRD_PARTY_NOTICES.md)、[開発への参加](CONTRIBUTING.md)を参照してください。
