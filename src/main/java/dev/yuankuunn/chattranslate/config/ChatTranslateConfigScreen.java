package dev.yuankuunn.chattranslate.config;

import dev.yuankuunn.chattranslate.ChatTranslateClient;
import dev.yuankuunn.chattranslate.translation.TranslationBackend;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ChatTranslateConfigScreen {
    private ChatTranslateConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ClientTranslationConfig workingCopy = ChatTranslateClient.get().configManager().getSnapshot();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("chattranslate.title"));
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("chattranslate.config.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("chattranslate.config.enabled"), workingCopy.enabled)
            .setDefaultValue(true)
            .setSaveConsumer(value -> workingCopy.enabled = value)
            .build());
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("chattranslate.config.incoming_enabled"), workingCopy.incomingEnabled)
            .setDefaultValue(true)
            .setSaveConsumer(value -> workingCopy.incomingEnabled = value)
            .build());
        category.addEntry(entryBuilder.startEnumSelector(Component.translatable("chattranslate.config.outgoing_backend"), TranslationBackend.class, workingCopy.outgoingBackend)
            .setDefaultValue(TranslationBackend.OPENAI)
            .setEnumNameProvider(value -> Component.translatable("chattranslate.config.backend." + value.name().toLowerCase()))
            .setSaveConsumer(value -> workingCopy.outgoingBackend = value)
            .build());
        category.addEntry(entryBuilder.startEnumSelector(Component.translatable("chattranslate.config.incoming_backend"), TranslationBackend.class, workingCopy.incomingBackend)
            .setDefaultValue(TranslationBackend.OPENAI)
            .setEnumNameProvider(value -> Component.translatable("chattranslate.config.backend." + value.name().toLowerCase()))
            .setSaveConsumer(value -> workingCopy.incomingBackend = value)
            .build());
        category.addEntry(entryBuilder.startStrField(Component.translatable("chattranslate.config.openai_api_key"), workingCopy.openAiApiKey)
            .setDefaultValue("")
            .setSaveConsumer(value -> workingCopy.openAiApiKey = value)
            .build());
        category.addEntry(entryBuilder.startStrField(Component.translatable("chattranslate.config.openai_model"), workingCopy.openAiModel)
            .setDefaultValue("gpt-4o-mini")
            .setSaveConsumer(value -> workingCopy.openAiModel = value)
            .build());
        category.addEntry(entryBuilder.startStrField(Component.translatable("chattranslate.config.deepl_api_key"), workingCopy.deepLApiKey)
            .setDefaultValue("")
            .setSaveConsumer(value -> workingCopy.deepLApiKey = value)
            .build());
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("chattranslate.config.deepl_use_free_api"), workingCopy.deepLUseFreeApi)
            .setDefaultValue(true)
            .setSaveConsumer(value -> workingCopy.deepLUseFreeApi = value)
            .build());
        category.addEntry(entryBuilder.startStrField(Component.translatable("chattranslate.config.source_language"), workingCopy.sourceLanguage)
            .setDefaultValue("ja")
            .setSaveConsumer(value -> workingCopy.sourceLanguage = value)
            .build());
        category.addEntry(entryBuilder.startStrField(Component.translatable("chattranslate.config.target_language"), workingCopy.targetLanguage)
            .setDefaultValue("en")
            .setSaveConsumer(value -> workingCopy.targetLanguage = value)
            .build());
        category.addEntry(entryBuilder.startStrField(Component.translatable("chattranslate.config.incoming_target_language"), workingCopy.incomingTargetLanguage)
            .setDefaultValue("ja")
            .setSaveConsumer(value -> workingCopy.incomingTargetLanguage = value)
            .build());
        category.addEntry(entryBuilder.startIntField(Component.translatable("chattranslate.config.debounce_millis"), workingCopy.debounceMillis)
            .setDefaultValue(700)
            .setMin(150)
            .setMax(3_000)
            .setSaveConsumer(value -> workingCopy.debounceMillis = value)
            .build());

        builder.setSavingRunnable(() -> ChatTranslateClient.get().configManager().save(workingCopy));
        return builder.build();
    }
}
