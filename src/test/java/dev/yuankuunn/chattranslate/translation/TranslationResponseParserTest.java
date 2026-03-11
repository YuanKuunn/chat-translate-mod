package dev.yuankuunn.chattranslate.translation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationResponseParserTest {
    @Test
    void parsesPrimaryAndCandidates() {
        String payload = """
            {
              \"primaryText\": \"Hello there\",
              \"candidates\": [
                {\"toneLabel\": \"普通\", \"text\": \"Hello there\"},
                {\"toneLabel\": \"カジュアル\", \"text\": \"Hey there\"},
                {\"toneLabel\": \"フォーマル\", \"text\": \"Hi there\"}
              ],
              \"resolvedSourceLanguage\": \"ja\"
            }
            """;

        TranslationResult result = TranslationResponseParser.parseModelPayload(payload);

        assertEquals("Hello there", result.primaryText());
        assertEquals(3, result.candidates().size());
        assertEquals("カジュアル", result.candidates().get(1).toneLabel());
        assertEquals("Hey there", result.candidates().get(1).text());
        assertEquals("ja", result.resolvedSourceLanguage());
    }

    @Test
    void fallsBackToStringCandidates() {
        String payload = """
            {
              \"candidates\": [\"Hello there\", \"Hey there\", \"Hi there\"]
            }
            """;

        TranslationResult result = TranslationResponseParser.parseModelPayload(payload);

        assertEquals("Hello there", result.primaryText());
        assertEquals("", result.candidates().get(0).toneLabel());
    }
}
