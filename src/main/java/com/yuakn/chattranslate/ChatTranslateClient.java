package com.yuakn.chattranslate;

import com.yuakn.chattranslate.chat.IncomingChatPipeline;
import com.yuakn.chattranslate.chat.OutgoingChatPipeline;
import com.yuakn.chattranslate.config.ClientConfigManager;
import com.yuakn.chattranslate.translation.DeepLTranslationProvider;
import com.yuakn.chattranslate.translation.OpenAiTranslationProvider;
import com.yuakn.chattranslate.translation.TranslationBackend;
import com.yuakn.chattranslate.translation.TranslationService;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChatTranslateClient implements ClientModInitializer {
    public static final String MOD_ID = "chattranslate";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ChatTranslateClient instance;

    private ClientConfigManager configManager;
    private TranslationService translationService;
    private OutgoingChatPipeline outgoingChatPipeline;
    private IncomingChatPipeline incomingChatPipeline;

    public static ChatTranslateClient get() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        this.configManager = new ClientConfigManager();
        this.configManager.load();
        this.translationService = new TranslationService(this.configManager::getSnapshot, Map.of(
            TranslationBackend.OPENAI, new OpenAiTranslationProvider(),
            TranslationBackend.DEEPL, new DeepLTranslationProvider()
        ));
        this.outgoingChatPipeline = new OutgoingChatPipeline(this.translationService);
        this.incomingChatPipeline = new IncomingChatPipeline(this.translationService);
        LOGGER.info("Initialized {}", MOD_ID);
    }

    public ClientConfigManager configManager() {
        return this.configManager;
    }

    public TranslationService translationService() {
        return this.translationService;
    }

    public OutgoingChatPipeline outgoingChatPipeline() {
        return this.outgoingChatPipeline;
    }

    public IncomingChatPipeline incomingChatPipeline() {
        return this.incomingChatPipeline;
    }

    public void showClientMessage(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(message, false);
            } else {
                LOGGER.warn("Dropping client message because no player is active: {}", message.getString());
            }
        });
    }
}
