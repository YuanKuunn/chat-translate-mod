# Chat Translate Mod

Minecraft 1.21.11 Fabric client mod for translating outgoing and incoming chat with OpenAI or DeepL.

<details>
<summary>English</summary>

## Requirements

- Minecraft `1.21.11`
- Java `21`
- Fabric Loader
- Fabric API
- Cloth Config
- Mod Menu recommended for opening the config GUI

## Features

- Outgoing chat translation before sending
- Incoming chat translation shown as an extra line in chat
- Live translation preview while typing in the chat screen
- Up to 3 outgoing candidates with fixed tone labels: `Normal`, `Casual`, `Formal`
- Candidate selection with mouse hover, arrow keys, Enter, or direct click
- Separate translation API selection for outgoing and incoming translation
- Mod Menu + Cloth Config settings screen

## Current Behavior

- Outgoing translation
  - Translates from `Source language` to `Target language`
  - Shows up to 3 candidates in the chat UI
  - Use `Up` / `Down` to move selection and `Enter` to insert the selected translation into the input box
  - Clicking a candidate inserts it immediately
  - Sends the selected translation instead of the original message
- Incoming translation
  - Translates incoming messages to `Incoming target language`
  - Shows the translated line under the original message
  - Applies to player chat, disguised chat, and normal system chat
  - Does not re-translate your own player messages

## Configuration

Open Mod Menu and configure:

- `Enable outgoing translation`
- `Enable incoming translation`
- `Outgoing translation API`
- `Incoming translation API`
- `OpenAI API key`
- `OpenAI model`
- `DeepL API key`
- `Use DeepL Free API`
- `Source language`
- `Target language`
- `Incoming target language`
- `Debounce (ms)`

Config is stored at:

```text
<Minecraft directory>/config/chattranslate.json
```

## Privacy And API Key Notes

- Your chat text is sent to the selected translation provider API.
- API keys are stored locally in `config/chattranslate.json` as plain text.
- Do not share your config file or include it in screenshots, bug reports, or uploads.
- This mod expects each user to configure their own OpenAI or DeepL API key.

## Credits

- Project owner: `YuanKuunn`
- All coding for this mod was done with Codex

## Backend Notes

### OpenAI

- Best for outgoing multi-candidate suggestions
- Uses the configured `OpenAI model`
- Returns multiple tone-based translation candidates when possible

### DeepL

- Best for straightforward single-result translation
- Uses `api-free.deepl.com` when `Use DeepL Free API` is enabled
- Uses `api.deepl.com` when `Use DeepL Free API` is disabled
- Can be selected independently for outgoing or incoming translation
- Outgoing preview still works, but DeepL typically returns a single candidate

## Build

```powershell
.\gradlew.bat build
```

Output jar:

```text
build/libs/chat-translate-mod-0.1.0.jar
```

</details>

<details>
<summary>日本語</summary>

## 前提環境

- Minecraft `1.21.11`
- Java `21`
- Fabric Loader
- Fabric API
- Cloth Config
- Mod Menu 推奨
  - 設定 GUI を開くために使います

## 主な機能

- 送信前のチャット翻訳
- 受信チャットの翻訳を別行で表示
- チャット入力中のリアルタイム翻訳プレビュー
- 送信候補を最大 3 件表示
  - `普通` `カジュアル` `フォーマル`
- 候補をマウスホバー、矢印キー、Enter、クリックで選択可能
- 送信翻訳と受信翻訳で別の API を選択可能
- Mod Menu + Cloth Config の設定画面

## 現在の挙動

### 送信翻訳

- `翻訳元言語` から `翻訳先言語` に翻訳します
- チャット UI に最大 3 件の候補を表示します
- `↑` / `↓` で候補を移動し、`Enter` で入力欄に反映します
- 候補をクリックするとその場で入力欄に反映します
- 送信時は原文ではなく、選択された翻訳文が送られます

### 受信翻訳

- 受信メッセージを `受信翻訳先言語` に翻訳します
- 原文の下に翻訳文を追加表示します
- プレイヤーチャット、偽装チャット、通常のシステムチャットに適用します
- 自分自身が送ったプレイヤーチャットは再翻訳しません

## 設定項目

Mod Menu から以下を設定できます。

- `送信チャット翻訳を有効化`
- `受信チャット翻訳を有効化`
- `送信翻訳API`
- `受信翻訳API`
- `OpenAI APIキー`
- `OpenAIモデル`
- `DeepL APIキー`
- `DeepL Free API を使う`
- `翻訳元言語`
- `翻訳先言語`
- `受信翻訳先言語`
- `デバウンス(ms)`

設定ファイルの保存先:

```text
<Minecraft フォルダ>/config/chattranslate.json
```

## プライバシーと API キーについて

- チャット内容は、選択した翻訳 API に送信されます
- API キーは `config/chattranslate.json` に平文で保存されます
- 設定ファイルを共有したり、スクリーンショットや不具合報告に含めたりしないでください
- 利用者ごとに OpenAI または DeepL の API キー設定が必要です

## クレジット

- プロジェクトオーナー: `YuanKuunn`
- この MOD のコーディングはすべて Codex を使って行っています

## バックエンド補足

### OpenAI

- 複数候補の提案に向いています
- 設定した `OpenAIモデル` を使用します
- 可能な限り文体差のある候補を返します

### DeepL

- 単純で安定した翻訳に向いています
- `DeepL Free API を使う` が有効な場合は `api-free.deepl.com` を使用します
- 無効な場合は `api.deepl.com` を使用します
- 送信と受信で個別に選択できます
- 送信プレビューは動きますが、候補は 1 件寄りになります

## ビルド

```powershell
.\gradlew.bat build
```

出力 jar:

```text
build/libs/chat-translate-mod-0.1.0.jar
```

</details>
