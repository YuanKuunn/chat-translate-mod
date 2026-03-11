package dev.yuankuunn.chattranslate.config;

import dev.yuankuunn.chattranslate.translation.TranslationBackend;
import java.util.Locale;
import net.minecraft.util.StringUtil;

public final class ClientTranslationConfig {
    public boolean enabled = true;
    public boolean incomingEnabled = true;
    public TranslationBackend backend = TranslationBackend.OPENAI;
    public String openAiApiKey = "";
    public String deepLApiKey = "";
    public String sourceLanguage = "ja";
    public String targetLanguage = "en";
    public String incomingTargetLanguage = "ja";
    public String openAiModel = "gpt-4o-mini";
    public boolean deepLUseFreeApi = true;
    public int debounceMillis = 700;

    @Deprecated
    public String apiKey = "";

    @Deprecated
    public String model = "";

    public ClientTranslationConfig copy() {
        ClientTranslationConfig copy = new ClientTranslationConfig();
        copy.enabled = this.enabled;
        copy.incomingEnabled = this.incomingEnabled;
        copy.backend = this.backend;
        copy.openAiApiKey = this.openAiApiKey;
        copy.deepLApiKey = this.deepLApiKey;
        copy.sourceLanguage = this.sourceLanguage;
        copy.targetLanguage = this.targetLanguage;
        copy.incomingTargetLanguage = this.incomingTargetLanguage;
        copy.openAiModel = this.openAiModel;
        copy.deepLUseFreeApi = this.deepLUseFreeApi;
        copy.debounceMillis = this.debounceMillis;
        copy.apiKey = this.apiKey;
        copy.model = this.model;
        return copy;
    }

    public ClientTranslationConfig sanitize() {
        this.backend = this.backend == null ? TranslationBackend.OPENAI : this.backend;
        if (StringUtil.isBlank(this.openAiApiKey) && !StringUtil.isBlank(this.apiKey)) {
            this.openAiApiKey = this.apiKey;
        }
        if (StringUtil.isBlank(this.openAiModel) && !StringUtil.isBlank(this.model)) {
            this.openAiModel = this.model;
        }
        this.openAiApiKey = this.openAiApiKey == null ? "" : this.openAiApiKey.trim();
        this.deepLApiKey = this.deepLApiKey == null ? "" : this.deepLApiKey.trim();
        this.sourceLanguage = normalizeLanguage(this.sourceLanguage, "ja");
        this.targetLanguage = normalizeLanguage(this.targetLanguage, "en");
        this.incomingTargetLanguage = normalizeLanguage(this.incomingTargetLanguage, this.sourceLanguage);
        this.openAiModel = StringUtil.isBlank(this.openAiModel) ? "gpt-4o-mini" : this.openAiModel.trim();
        this.debounceMillis = Math.clamp(this.debounceMillis, 150, 3_000);
        this.apiKey = "";
        this.model = "";
        return this;
    }

    private static String normalizeLanguage(String rawValue, String fallback) {
        if (StringUtil.isBlank(rawValue)) {
            return fallback;
        }
        return rawValue.trim().toLowerCase(Locale.ROOT);
    }
}
