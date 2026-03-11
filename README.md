# Chat Translate Mod

Minecraft 1.21.11 Fabric client mod for translating chat with OpenAI or DeepL.

## Features

- Outgoing chat translation before sending
- Incoming chat translation shown as an extra line in chat
- Translation preview while typing in the chat screen
- Candidate selection with click or `1` `2` `3`
- Fixed tone labels for outgoing candidates: `Normal`, `Casual`, `Formal`
- Mod Menu + Cloth Config settings screen
- Selectable translation backend: `OpenAI` or `DeepL`

## Current Behavior

- Outgoing translation:
  - Translates from `Source language` to `Target language`
  - Shows up to 3 candidates in the chat UI
  - Sends the selected translation instead of the original message
- Incoming translation:
  - Translates incoming messages to `Incoming target language`
  - Shows the translated line under the original message
  - Applies to player chat, disguised chat, and normal system chat
  - Does not re-translate your own player messages

## Configuration

Open Mod Menu and configure:

- `Enable outgoing translation`
- `Enable incoming translation`
- `Translation API`
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

## Backend Notes

### OpenAI

- Best for outgoing multi-candidate suggestions
- Uses the configured `OpenAI model`
- Returns multiple tone-based translation candidates when possible

### DeepL

- Best for straightforward single-result translation
- Uses `api-free.deepl.com` when `Use DeepL Free API` is enabled
- Uses `api.deepl.com` when `Use DeepL Free API` is disabled
- Incoming translation works with DeepL as well
- Outgoing preview still works, but DeepL typically returns a single candidate

## Build

```powershell
.\gradlew.bat build
```

Output jar:

```text
build/libs/chat-translate-mod-0.1.0.jar
```
