package com.yuakn.chattranslate.chat;

import com.yuakn.chattranslate.translation.TranslationCandidate;
import java.util.List;
import net.minecraft.network.chat.Component;

public record ChatPreviewState(Status status, long requestId, String sourceText, List<TranslationCandidate> candidates, String primaryText, Component message) {
    public static ChatPreviewState idle() {
        return new ChatPreviewState(Status.IDLE, 0L, "", List.of(), "", Component.empty());
    }

    public static ChatPreviewState loading(long requestId, String sourceText) {
        return new ChatPreviewState(Status.LOADING, requestId, sourceText, List.of(), "", Component.translatable("chattranslate.preview.loading"));
    }

    public static ChatPreviewState ready(long requestId, String sourceText, List<TranslationCandidate> candidates, String primaryText) {
        return new ChatPreviewState(Status.READY, requestId, sourceText, List.copyOf(candidates), primaryText, Component.translatable("chattranslate.preview.candidates"));
    }

    public static ChatPreviewState error(long requestId, String sourceText, Component message) {
        return new ChatPreviewState(Status.ERROR, requestId, sourceText, List.of(), "", message);
    }

    public enum Status {
        IDLE,
        LOADING,
        READY,
        ERROR
    }
}
