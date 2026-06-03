package com.yuakn.chattranslate.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yuakn.chattranslate.ChatTranslateClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class OpenAiTranslationProvider implements TranslationProvider {
    private static final URI CHAT_COMPLETIONS_URI = URI.create("https://api.openai.com/v1/chat/completions");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    @Override
    public CompletableFuture<TranslationResult> requestCandidates(TranslationRequest request) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", request.model());
        payload.addProperty("temperature", 0.3D);

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        payload.add("response_format", responseFormat);

        JsonArray messages = new JsonArray();
        messages.add(buildMessage("system", buildSystemPrompt(request)));
        messages.add(buildMessage("user", buildUserPrompt(request)));
        payload.add("messages", messages);

        HttpRequest httpRequest = HttpRequest.newBuilder(CHAT_COMPLETIONS_URI)
            .header("Authorization", "Bearer " + request.apiKey())
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
            .build();

        return this.httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            .thenApply(this::validateHttpResponse)
            .thenApply(OpenAiTranslationProvider::extractModelContent)
            .thenApply(TranslationResponseParser::parseModelPayload)
            .exceptionallyCompose(exception -> CompletableFuture.failedFuture(unwrap(exception)));
    }

    private static JsonObject buildMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private static String buildSystemPrompt(TranslationRequest request) {
        String sourceLanguage = request.sourceLanguage() == null || request.sourceLanguage().isBlank()
            ? "the automatically detected language"
            : request.sourceLanguage();
        return "You are a translation engine for Minecraft chat. Return only JSON with keys primaryText, candidates, and resolvedSourceLanguage. "
            + "Translate concise player chat from " + sourceLanguage + " to " + request.targetLanguage() + ". "
            + "Candidates must be an array of objects with keys toneLabel and text. "
            + "If you return at least three candidates, candidate 1 must be neutral everyday chat with toneLabel 'normal', "
            + "candidate 2 must be casual and slang-friendly with toneLabel 'casual', "
            + "and candidate 3 must be polite or formal with toneLabel 'formal'. "
            + "Return exactly " + request.candidateCount() + " natural chat-friendly alternatives whenever possible.";
    }

    private static String buildUserPrompt(TranslationRequest request) {
        return "Translate this Minecraft chat message. Preserve meaning, tone, and lightweight slang where reasonable. "
            + "Do not add explanations. Make each candidate clearly different in register. Message: " + request.sourceText();
    }

    private String validateHttpResponse(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }

        String message = "OpenAI request failed with status " + response.statusCode() + ".";
        try {
            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject error = root.getAsJsonObject("error");
            if (error != null && error.get("message") != null) {
                message = error.get("message").getAsString();
            }
        } catch (RuntimeException ignored) {
            ChatTranslateClient.LOGGER.debug("Failed to parse OpenAI error payload: {}", response.body());
        }
        throw new TranslationException(message);
    }

    private static String extractModelContent(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new TranslationException("OpenAI returned no choices.");
            }

            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null) {
                throw new TranslationException("OpenAI response did not contain a message.");
            }

            JsonElement content = message.get("content");
            if (content == null || content.isJsonNull()) {
                throw new TranslationException("OpenAI response did not contain message content.");
            }
            if (content.isJsonPrimitive()) {
                return content.getAsString();
            }
            if (content.isJsonArray()) {
                StringBuilder combined = new StringBuilder();
                for (JsonElement part : content.getAsJsonArray()) {
                    JsonObject object = part.getAsJsonObject();
                    JsonObject text = object.getAsJsonObject("text");
                    if (text != null && text.get("value") != null) {
                        combined.append(text.get("value").getAsString());
                    }
                }
                return combined.toString();
            }
            throw new TranslationException("OpenAI response content format was unsupported.");
        } catch (RuntimeException exception) {
            if (exception instanceof TranslationException translationException) {
                throw translationException;
            }
            throw new TranslationException("Failed to parse OpenAI translation response.", exception);
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
