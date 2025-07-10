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
    private FileConfiguration actionsConfig;

    public static AdmiBotIntegration getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        if (!config.contains("secureKey")) {
            secureKey = KeyGenerator.generateKey();
            config.set("secureKey", secureKey);
            saveConfig();
            printKey(secureKey, true);
        } else {
            secureKey = config.getString("secureKey");
            printKey(secureKey, false);
        }
        File actionsFile = new File(getDataFolder(), "actions.yml");
        if (!actionsFile.exists()) {
            saveResource("actions.yml", false);
        }
        actionsConfig = YamlConfiguration.loadConfiguration(actionsFile);
        try {
            wsHandler = new WebSocketHandler("wss://ws.admibot.xyz", secureKey);
            wsHandler.connectWithRetry();
        } catch (Exception e) {
            getLogger().severe("Failed to initialize WebSocket: " + e.getMessage());
        }
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
    public WebSocketHandler getWebSocketHandler() {
        return wsHandler;
    }
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

        String instruction = "=> Use the command below to add your server to AdmiBot:";
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
