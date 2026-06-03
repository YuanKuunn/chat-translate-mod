package com.yuakn.chattranslate.translation;

public final class TranslationException extends RuntimeException {
    private final String userMessage;

    public TranslationException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    public TranslationException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }

    public String userMessage() {
        return this.userMessage;
    }
}
