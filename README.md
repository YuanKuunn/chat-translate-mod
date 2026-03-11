# Chat Translate Mod

[日本語版 README](README.ja.md)

Minecraft 1.21.11 Fabric client mod for translating outgoing and incoming chat with OpenAI or DeepL.

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
