package dev.yuankuunn.chattranslate.translation;

public record TranslationRequest(
    String sourceText,
    String sourceLanguage,
    String targetLanguage,
    int candidateCount,
    String model,
    String apiKey,
    boolean useFreeApi
) {
}
