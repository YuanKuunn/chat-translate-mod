# Chat Translate Mod

Minecraft 1.21.11 Fabric client MOD that translates outgoing chat through OpenAI before it is sent.

## Features

- Debounced translation preview while typing in chat
- Multiple candidate translations rendered above the chat box
- Outgoing chat interception that sends the translated text instead of the original
- Config screen via Mod Menu + Cloth Config
- Translation provider abstraction so incoming chat translation and more backends can be added later

## Configuration

Open Mod Menu and configure:

- `Enabled`
- `OpenAI API Key`
- `Source Language`
- `Target Language`
- `Model`
- `Debounce (ms)`

The config file is saved to `config/chattranslate.json`.

## Build

A local Gradle distribution was unpacked in this workspace so the wrapper could be generated.

```powershell
.\gradlew.bat build
```
