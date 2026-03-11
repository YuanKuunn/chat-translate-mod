package dev.yuankuunn.chattranslate.config;

import dev.yuankuunn.chattranslate.translation.TranslationBackend;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTranslationConfigTest {
    @Test
    void sanitizeClampsValuesAndNormalizesLanguages() {
        ClientTranslationConfig config = new ClientTranslationConfig();
        config.sourceLanguage = " JA ";
        config.targetLanguage = " EN ";
        config.incomingTargetLanguage = " KO ";
        config.debounceMillis = 10;

        config.sanitize();

        assertEquals("ja", config.sourceLanguage);
        assertEquals("en", config.targetLanguage);
        assertEquals("ko", config.incomingTargetLanguage);
        assertEquals(150, config.debounceMillis);
    }

    @Test
    void sanitizeMigratesLegacyOpenAiFields() {
        ClientTranslationConfig config = new ClientTranslationConfig();
        config.backend = TranslationBackend.OPENAI;
        config.openAiApiKey = "";
        config.openAiModel = "";
        config.apiKey = "legacy-key";
        config.model = "legacy-model";

        config.sanitize();

        assertEquals("legacy-key", config.openAiApiKey);
        assertEquals("legacy-model", config.openAiModel);
        assertEquals("", config.apiKey);
        assertEquals("", config.model);
    }
}
