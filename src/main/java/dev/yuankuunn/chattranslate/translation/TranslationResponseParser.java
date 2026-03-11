package dev.yuankuunn.chattranslate.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.StringUtil;

public final class TranslationResponseParser {
    private TranslationResponseParser() {
    }

    public static TranslationResult parseModelPayload(String rawContent) {
        try {
            JsonObject root = JsonParser.parseString(extractJson(rawContent)).getAsJsonObject();
            List<TranslationCandidate> candidates = new ArrayList<>();

            String primaryText = readOptionalString(root, "primaryText");
            JsonArray candidateArray = root.getAsJsonArray("candidates");
            if (candidateArray != null) {
                for (JsonElement element : candidateArray) {
                    TranslationCandidate candidate = parseCandidate(element);
                    if (candidate != null && !candidate.text().isBlank()) {
                        candidates.add(candidate);
                    }
                }
            }

            if (candidates.isEmpty()) {
                if (!StringUtil.isBlank(primaryText)) {
                    candidates.add(new TranslationCandidate("", primaryText));
                }
            }

            if (candidates.isEmpty()) {
                throw new TranslationException("No usable translation candidates were returned.");
            }

            return new TranslationResult(firstNonBlank(primaryText, candidates.getFirst().text()), candidates, firstNonBlank(
                readOptionalString(root, "resolvedSourceLanguage"),
                readOptionalString(root, "detectedSourceLanguage")
            ));
        } catch (RuntimeException exception) {
            if (exception instanceof TranslationException translationException) {
                throw translationException;
            }
            throw new TranslationException("OpenAI returned malformed translation data.", exception);
        }
    }

    private static String extractJson(String rawContent) {
        if (StringUtil.isBlank(rawContent)) {
            throw new TranslationException("OpenAI returned an empty translation payload.");
        }

        int start = rawContent.indexOf('{');
        int end = rawContent.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new TranslationException("OpenAI did not return a JSON object.");
        }
        return rawContent.substring(start, end + 1);
    }

    private static String readOptionalString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return "";
        }
        return element.getAsString();
    }

    private static TranslationCandidate parseCandidate(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return new TranslationCandidate("", element.getAsString()).normalize();
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            return new TranslationCandidate(
                readOptionalString(object, "toneLabel"),
                firstNonBlank(readOptionalString(object, "text"), readOptionalString(object, "translation"))
            ).normalize();
        }
        return null;
    }

    private static String firstNonBlank(String first, String second) {
        if (!StringUtil.isBlank(first)) {
            return first;
        }
        return StringUtil.isBlank(second) ? "" : second;
    }
}
