package dev.yuankuunn.chattranslate.translation;

import java.util.concurrent.CompletableFuture;

public interface TranslationProvider {
    CompletableFuture<TranslationResult> requestCandidates(TranslationRequest request);
}
