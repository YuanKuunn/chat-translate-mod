package com.yuakn.chattranslate.chat;

import com.yuakn.chattranslate.translation.TranslationException;
import com.yuakn.chattranslate.translation.TranslationService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

public final class IncomingChatPipeline {
    private final TranslationService translationService;
    private final Minecraft minecraft = Minecraft.getInstance();

    public IncomingChatPipeline(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void handlePlayerMessage(String senderName, String messageText, boolean fromLocalPlayer) {
        if (fromLocalPlayer) {
            return;
        }
        this.translateAndDisplay(messageText, Component.literal("<" + senderName + "> ").append(this.translationPrefix()));
    }

    public void handleSystemMessage(String messageText, boolean overlay) {
        if (overlay) {
            return;
        }
        this.translateAndDisplay(messageText, this.translationPrefix());
    }

    public void handleDisguisedMessage(String messageText) {
        this.translateAndDisplay(messageText, this.translationPrefix());
    }

    private void translateAndDisplay(String messageText, Component prefix) {
        if (!this.translationService.isEnabled() || !this.translationService.isIncomingEnabled() || StringUtil.isBlank(messageText)) {
            return;
        }

        this.translationService.requestIncomingTranslation(messageText)
            .thenAccept(result -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(
                prefix.copy().append(Component.literal(result.primaryText()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
            )))
            .exceptionally(exception -> {
                this.minecraft.execute(() -> this.handleTranslationFailure(exception));
                return null;
            });
    }

    private Component translationPrefix() {
        return Component.translatable("chattranslate.incoming.prefix").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
    }

    private void handleTranslationFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof java.util.concurrent.CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        if (current instanceof TranslationException) {
            return;
        }
    }
}
