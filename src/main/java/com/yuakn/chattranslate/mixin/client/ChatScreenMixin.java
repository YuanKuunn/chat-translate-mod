package com.yuakn.chattranslate.mixin.client;

import com.yuakn.chattranslate.ChatTranslateClient;
import com.yuakn.chattranslate.chat.ChatPreviewSession;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow
    protected EditBox input;

    @Shadow
    public abstract void handleChatInput(String message, boolean addToHistory);

    @Unique
    private ChatPreviewSession chattranslate$previewSession;

    @Unique
    private boolean chattranslate$bypassTranslation;

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void chattranslate$init(CallbackInfo callbackInfo) {
        this.chattranslate$previewSession = new ChatPreviewSession(ChatTranslateClient.get().outgoingChatPipeline());
        this.chattranslate$previewSession.onTextEdited(this.input.getValue());
    }

    @Inject(method = "onEdited", at = @At("TAIL"))
    private void chattranslate$onEdited(String text, CallbackInfo callbackInfo) {
        if (this.chattranslate$previewSession != null) {
            this.chattranslate$previewSession.onTextEdited(text);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void chattranslate$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo callbackInfo) {
        if (this.chattranslate$previewSession != null && this.input != null) {
            this.chattranslate$previewSession.handleMouseMoved(mouseX, mouseY);
            this.chattranslate$previewSession.render(guiGraphics, this.font, this.input);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void chattranslate$keyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (this.chattranslate$previewSession != null && this.input != null
            && this.chattranslate$previewSession.handleKeyPressed(keyEvent.key(), this::chattranslate$applyCandidateToInput)) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void chattranslate$mouseClicked(MouseButtonEvent mouseButtonEvent, boolean doubleClick, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (this.chattranslate$previewSession != null && this.input != null
            && this.chattranslate$previewSession.handleMouseClicked(
                mouseButtonEvent.x(),
                mouseButtonEvent.y(),
                mouseButtonEvent.button(),
                this::chattranslate$applyCandidateToInput
            )) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void chattranslate$removed(CallbackInfo callbackInfo) {
        if (this.chattranslate$previewSession != null) {
            this.chattranslate$previewSession.close();
            this.chattranslate$previewSession = null;
        }
    }

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void chattranslate$handleChatInput(String message, boolean addToHistory, CallbackInfo callbackInfo) {
        if (this.chattranslate$bypassTranslation || this.chattranslate$previewSession == null) {
            return;
        }

        boolean intercepted = this.chattranslate$previewSession.interceptSend(message, addToHistory, translatedMessage -> {
            this.chattranslate$bypassTranslation = true;
            try {
                this.handleChatInput(translatedMessage, addToHistory);
            } finally {
                this.chattranslate$bypassTranslation = false;
            }
        });
        if (intercepted) {
            callbackInfo.cancel();
        }
    }

    @Unique
    private void chattranslate$applyCandidateToInput(String translatedText) {
        this.input.setValue(translatedText);
        this.input.setCursorPosition(translatedText.length());
        this.minecraft.execute(() -> {
            if (this.input != null) {
                this.input.setValue(translatedText);
                this.input.setCursorPosition(translatedText.length());
            }
        });
    }
}
