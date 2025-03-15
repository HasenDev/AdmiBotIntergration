package xyz.admibot;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AdmiBotIntegration extends JavaPlugin {

    private static AdmiBotIntegration instance;
    private WebSocketHandler wsHandler;
    private String secureKey;
    private FileConfiguration actionsConfig; // Stores custom actions loaded from actions.yml

    public static AdmiBotIntegration getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Save default config.yml if it does not exist.
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        // Generate and store secure key if not present.
        if (!config.contains("secureKey")) {
            secureKey = KeyGenerator.generateKey();
            config.set("secureKey", secureKey);
            saveConfig();
            printKey(secureKey, true); // First-time generation
        } else {
            secureKey = config.getString("secureKey");
            printKey(secureKey, false); // Loaded from config
        }

        // Load custom actions from actions.yml.
        // This file defines your custom actions (keys) and their corresponding command templates.
        File actionsFile = new File(getDataFolder(), "actions.yml");
        if (!actionsFile.exists()) {
            // Save the default actions.yml bundled with the jar to the plugin folder.
            saveResource("actions.yml", false);
        }
        actionsConfig = YamlConfiguration.loadConfiguration(actionsFile);

        // Initialize and connect the WebSocket handler.
        try {
            wsHandler = new WebSocketHandler("wss://ws.admibot.xyz", secureKey);
            wsHandler.connectWithRetry();
        } catch (Exception e) {
            getLogger().severe("Failed to initialize WebSocket: " + e.getMessage());
        }

        // Register the admibot command.
        PluginCommand command = getCommand("admibot");
        if (command != null) {
            command.setExecutor(new AdmibotCommand());
        } else {
            getLogger().severe("Command admibot not defined in plugin.yml");
        }
    }

    @Override
    public void onDisable() {
        if (wsHandler != null) {
            wsHandler.close();
        }
    }

    /**
     * Returns the WebSocket handler.
     *
     * @return WebSocketHandler instance.
     */
    public WebSocketHandler getWebSocketHandler() {
        return wsHandler;
    }

    /**
     * Returns the secure key used for WebSocket authentication.
     *
     * @return Secure key string.
     */
    public String getSecureKey() {
        return secureKey;
    }
    public FileConfiguration getActionsConfig() {
        return actionsConfig;
    }
    private void printKey(String key, boolean isNewKey) {
        String title = "========[ AdmiBot Integration 1.4 ]========";
        String footer = "===========================================";

        String message = "- Your " + (isNewKey ? "newly generated" : "loaded") + " secure key:";
        String keyDisplay = key;

        String instruction = "=> Use this key with the Discord bot using:";
        String command = "/minecraft add " + key;

        String warning = "⚠ IMPORTANT: Never share this key with anyone!";

        getLogger().info(title);
        getLogger().info(message);
        getLogger().info(keyDisplay);
        getLogger().info("");
        getLogger().info(instruction);
        getLogger().info(command);
        getLogger().info("");
        getLogger().info(warning);
        getLogger().info(footer);
    }
}
