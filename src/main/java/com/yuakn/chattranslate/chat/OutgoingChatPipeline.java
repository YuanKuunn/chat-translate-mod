package com.yuakn.chattranslate.chat;

import com.yuakn.chattranslate.translation.TranslationResult;
import com.yuakn.chattranslate.translation.TranslationService;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

public final class OutgoingChatPipeline {
    private final TranslationService translationService;
    private final ScheduledExecutorService previewExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "chattranslate-preview");
        thread.setDaemon(true);
        return thread;
    });

    public OutgoingChatPipeline(TranslationService translationService) {
        this.translationService = translationService;
    }

    public boolean isEligibleMessage(String message) {
        return !StringUtil.isBlank(message) && !message.startsWith("/");
    }

    public boolean isEnabled() {
        return this.translationService.isEnabled();
    }

    public Optional<Component> getBlockingReason() {
        return this.translationService.getConfigurationError();
    }

    public int debounceMillis() {
        return this.translationService.debounceMillis();
    }

    public ScheduledExecutorService previewExecutor() {
        return this.previewExecutor;
    }

    public CompletableFuture<TranslationResult> requestCandidates(String sourceText) {
        return this.translationService.requestOutgoingCandidates(sourceText);
    }
}
