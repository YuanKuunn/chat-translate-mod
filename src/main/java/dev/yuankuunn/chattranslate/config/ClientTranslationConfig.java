package dev.yuankuunn.chattranslate.config;

import dev.yuankuunn.chattranslate.translation.TranslationBackend;
import java.util.Locale;
import net.minecraft.util.StringUtil;

public final class ClientTranslationConfig {
    public boolean enabled = true;
    public boolean incomingEnabled = true;
    public TranslationBackend outgoingBackend = TranslationBackend.OPENAI;
    public TranslationBackend incomingBackend = TranslationBackend.OPENAI;
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

    @Deprecated
    public TranslationBackend backend = null;

    public ClientTranslationConfig copy() {
        ClientTranslationConfig copy = new ClientTranslationConfig();
        copy.enabled = this.enabled;
        copy.incomingEnabled = this.incomingEnabled;
        copy.outgoingBackend = this.outgoingBackend;
        copy.incomingBackend = this.incomingBackend;
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
        copy.backend = this.backend;
        return copy;
    }

    public ClientTranslationConfig sanitize() {
        if (this.backend != null) {
            if (this.outgoingBackend == null) {
                this.outgoingBackend = this.backend;
            }
            if (this.incomingBackend == null) {
                this.incomingBackend = this.backend;
            }
        }
        this.outgoingBackend = this.outgoingBackend == null ? TranslationBackend.OPENAI : this.outgoingBackend;
        this.incomingBackend = this.incomingBackend == null ? this.outgoingBackend : this.incomingBackend;
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
        this.backend = null;
        return this;
    }

    private static String normalizeLanguage(String rawValue, String fallback) {
        if (StringUtil.isBlank(rawValue)) {
            return fallback;
        }
        return rawValue.trim().toLowerCase(Locale.ROOT);
    }
}
