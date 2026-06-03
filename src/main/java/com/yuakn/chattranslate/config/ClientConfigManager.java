package com.yuakn.chattranslate.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yuakn.chattranslate.ChatTranslateClient;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientConfigManager {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private final Path configPath;
    private ClientTranslationConfig currentConfig = new ClientTranslationConfig();

    public ClientConfigManager() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve("chattranslate.json");
    }

    public synchronized void load() {
        if (!Files.exists(this.configPath)) {
            this.currentConfig = new ClientTranslationConfig().sanitize();
            this.save(this.currentConfig);
            return;
        }

        try (Reader reader = Files.newBufferedReader(this.configPath, StandardCharsets.UTF_8)) {
            ClientTranslationConfig loaded = GSON.fromJson(reader, ClientTranslationConfig.class);
            this.currentConfig = loaded == null ? new ClientTranslationConfig() : loaded;
            this.currentConfig.sanitize();
        } catch (IOException | RuntimeException exception) {
            ChatTranslateClient.LOGGER.warn("Failed to load config, using defaults", exception);
            this.currentConfig = new ClientTranslationConfig().sanitize();
        }
    }

    public synchronized ClientTranslationConfig getSnapshot() {
        return this.currentConfig.copy().sanitize();
    }

    public synchronized void save(ClientTranslationConfig config) {
        this.currentConfig = config.copy().sanitize();

        try {
            Files.createDirectories(this.configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(this.configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(this.currentConfig, writer);
            }
        } catch (IOException exception) {
            ChatTranslateClient.LOGGER.error("Failed to save config", exception);
        }
    }
}
