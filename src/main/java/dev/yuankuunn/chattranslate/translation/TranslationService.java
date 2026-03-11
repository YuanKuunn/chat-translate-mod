package dev.yuankuunn.chattranslate.translation;

import dev.yuankuunn.chattranslate.config.ClientTranslationConfig;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

public final class TranslationService {
    private static final int OUTGOING_CANDIDATE_COUNT = 3;
    private static final int INCOMING_CANDIDATE_COUNT = 1;

    private final Supplier<ClientTranslationConfig> configSupplier;
    private final Map<TranslationBackend, TranslationProvider> providers;

    public TranslationService(Supplier<ClientTranslationConfig> configSupplier, Map<TranslationBackend, TranslationProvider> providers) {
        this.configSupplier = configSupplier;
        this.providers = Map.copyOf(providers);
    }

    public boolean isEnabled() {
        return this.snapshot().enabled;
    }

    public boolean isIncomingEnabled() {
        return this.snapshot().incomingEnabled;
    }

    public int debounceMillis() {
        return this.snapshot().debounceMillis;
    }

    public Optional<Component> getConfigurationError() {
        ClientTranslationConfig config = this.snapshot();
        if (!this.providers.containsKey(config.backend)) {
            return Optional.of(Component.translatable("chattranslate.preview.missing_backend"));
        }

        return switch (config.backend) {
            case OPENAI -> {
                if (StringUtil.isBlank(config.openAiApiKey)) {
                    yield Optional.of(Component.translatable("chattranslate.preview.missing_openai_api_key"));
                }
                if (StringUtil.isBlank(config.openAiModel)) {
                    yield Optional.of(Component.translatable("chattranslate.preview.missing_openai_model"));
                }
                yield Optional.empty();
            }
            case DEEPL -> StringUtil.isBlank(config.deepLApiKey)
                ? Optional.of(Component.translatable("chattranslate.preview.missing_deepl_api_key"))
                : Optional.empty();
        };
    }

    public CompletableFuture<TranslationResult> requestOutgoingCandidates(String sourceText) {
        ClientTranslationConfig config = this.snapshot();
        return this.request(
            config,
            sourceText,
            config.sourceLanguage,
            config.targetLanguage,
            OUTGOING_CANDIDATE_COUNT
        );
    }

    public CompletableFuture<TranslationResult> requestIncomingTranslation(String sourceText) {
        ClientTranslationConfig config = this.snapshot();
        if (!config.incomingEnabled) {
            return CompletableFuture.failedFuture(new TranslationException("Incoming translation is disabled."));
        }
        return this.request(
            config,
            sourceText,
            "",
            config.incomingTargetLanguage,
            INCOMING_CANDIDATE_COUNT
        );
    }

    private CompletableFuture<TranslationResult> request(
        ClientTranslationConfig config,
        String sourceText,
        String sourceLanguage,
        String targetLanguage,
        int candidateCount
    ) {
        if (!config.enabled) {
            return CompletableFuture.failedFuture(new TranslationException("Translation is disabled."));
        }

        Optional<Component> configurationError = this.getConfigurationError();
        if (configurationError.isPresent()) {
            return CompletableFuture.failedFuture(new TranslationException(configurationError.get().getString()));
        }

        TranslationProvider provider = this.providers.get(config.backend);
        if (provider == null) {
            return CompletableFuture.failedFuture(new TranslationException("Selected translation backend is unavailable."));
        }

        TranslationRequest request = new TranslationRequest(
            sourceText.trim(),
            sourceLanguage,
            targetLanguage,
            candidateCount,
            config.openAiModel,
            config.backend == TranslationBackend.OPENAI ? config.openAiApiKey : config.deepLApiKey,
            config.deepLUseFreeApi
        );
        return provider.requestCandidates(request);
    }

    private ClientTranslationConfig snapshot() {
        return this.configSupplier.get().copy().sanitize();
    }
}
