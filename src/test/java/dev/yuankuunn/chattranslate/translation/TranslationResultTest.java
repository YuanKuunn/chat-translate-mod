package dev.yuankuunn.chattranslate.translation;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationResultTest {
    @Test
    void keepsToneLabelWhenPrimaryMatchesCandidateText() {
        TranslationResult result = new TranslationResult(
            "Hello there",
            List.of(new TranslationCandidate("casual", "Hello there")),
            "JA"
        );

        assertEquals("Hello there", result.primaryText());
        assertEquals("casual", result.candidates().getFirst().toneLabel());
        assertEquals("JA", result.resolvedSourceLanguage());
    }
}
