package dev.yuankuunn.chattranslate.translation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record TranslationResult(String primaryText, List<TranslationCandidate> candidates, String resolvedSourceLanguage) {
    public TranslationResult {
        Map<String, TranslationCandidate> deduplicated = new LinkedHashMap<>();
        if (candidates != null) {
            for (TranslationCandidate candidate : candidates) {
                if (candidate != null && !candidate.text().isBlank()) {
                    deduplicated.putIfAbsent(candidate.text().trim(), candidate.normalize());
                }
            }
        }

        if (primaryText != null && !primaryText.isBlank()) {
            deduplicated.putIfAbsent(primaryText.trim(), new TranslationCandidate("", primaryText.trim()).normalize());
        }

        List<TranslationCandidate> normalizedCandidates = new ArrayList<>(deduplicated.values());
        if (normalizedCandidates.isEmpty()) {
            throw new IllegalArgumentException("Translation candidates cannot be empty");
        }

        candidates = List.copyOf(normalizedCandidates);
        primaryText = normalizedCandidates.getFirst().text();
        resolvedSourceLanguage = resolvedSourceLanguage == null ? "" : resolvedSourceLanguage.trim();
    }
}
