package com.yuakn.chattranslate.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class DeepLTranslationProvider implements TranslationProvider {
    private static final URI FREE_API_URI = URI.create("https://api-free.deepl.com/v2/translate");
    private static final URI PRO_API_URI = URI.create("https://api.deepl.com/v2/translate");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Override
    public CompletableFuture<TranslationResult> requestCandidates(TranslationRequest request) {
        HttpRequest httpRequest = HttpRequest.newBuilder(request.useFreeApi() ? FREE_API_URI : PRO_API_URI)
            .header("Authorization", "DeepL-Auth-Key " + request.apiKey())
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(buildPayload(request).toString(), StandardCharsets.UTF_8))
            .build();

        return this.httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            .thenApply(this::validateHttpResponse)
            .thenApply(DeepLTranslationProvider::parseTranslation)
            .exceptionallyCompose(exception -> CompletableFuture.failedFuture(unwrap(exception)));
    }

    private static JsonObject buildPayload(TranslationRequest request) {
        JsonObject payload = new JsonObject();
        JsonArray texts = new JsonArray();
        texts.add(request.sourceText());
        payload.add("text", texts);
        if (request.sourceLanguage() != null && !request.sourceLanguage().isBlank()) {
            payload.addProperty("source_lang", request.sourceLanguage().toUpperCase());
        }
        payload.addProperty("target_lang", request.targetLanguage().toUpperCase());
        return payload;
    }

    private String validateHttpResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new TranslationException("DeepL request failed with status " + response.statusCode() + ".");
    }

    private static TranslationResult parseTranslation(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray translations = root.getAsJsonArray("translations");
            if (translations == null || translations.isEmpty()) {
                throw new TranslationException("DeepL returned no translations.");
            }

            JsonObject first = translations.get(0).getAsJsonObject();
            String translatedText = first.get("text").getAsString();
            String detectedSourceLanguage = first.has("detected_source_language")
                ? first.get("detected_source_language").getAsString()
                : "";

            return new TranslationResult(
                translatedText,
                List.of(new TranslationCandidate("normal", translatedText)),
                detectedSourceLanguage
            );
        } catch (RuntimeException exception) {
            if (exception instanceof TranslationException translationException) {
                throw translationException;
            }
            throw new TranslationException("Failed to parse DeepL translation response.", exception);
        }
    }

    private static RuntimeException unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        if (current instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        return new TranslationException("Unexpected translation error.", current);
    }
}
