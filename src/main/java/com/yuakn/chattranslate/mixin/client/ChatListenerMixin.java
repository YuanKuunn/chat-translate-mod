package com.yuakn.chattranslate.mixin.client;

import com.mojang.authlib.GameProfile;
import com.yuakn.chattranslate.ChatTranslateClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    @Inject(method = "handlePlayerChatMessage", at = @At("TAIL"))
    private void chattranslate$handlePlayerChatMessage(PlayerChatMessage message, GameProfile profile, ChatType.Bound bound, CallbackInfo callbackInfo) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean fromLocalPlayer = minecraft.player != null && minecraft.player.getUUID().equals(message.sender());
        ChatTranslateClient.get().incomingChatPipeline().handlePlayerMessage(profile.name(), message.decoratedContent().getString(), fromLocalPlayer);
    }

    @Inject(method = "handleDisguisedChatMessage", at = @At("TAIL"))
    private void chattranslate$handleDisguisedChatMessage(Component message, ChatType.Bound bound, CallbackInfo callbackInfo) {
        ChatTranslateClient.get().incomingChatPipeline().handleDisguisedMessage(message.getString());
    }

    @Inject(method = "handleSystemMessage", at = @At("TAIL"))
    private void chattranslate$handleSystemMessage(Component message, boolean overlay, CallbackInfo callbackInfo) {
        ChatTranslateClient.get().incomingChatPipeline().handleSystemMessage(message.getString(), overlay);
    }
}
