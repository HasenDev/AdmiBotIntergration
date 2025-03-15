package xyz.admibot;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class WebSocketHandler extends WebSocketClient {

    private static final int INITIAL_RECONNECT_DELAY = 5000; 
    private static final int MAX_RECONNECT_DELAY = 60000;
    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long COMMAND_TIMEOUT_MS = 4000;

    private final String secureKey;
    private final Logger logger = AdmiBotIntegration.getInstance().getLogger();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private int reconnectAttempts = 0;
    private long currentReconnectDelay = INITIAL_RECONNECT_DELAY;

    public WebSocketHandler(String uri, String secureKey) throws Exception {
        super(new URI(uri), getHeaders(secureKey));
        this.secureKey = secureKey;
    }

    private static Map<String, String> getHeaders(String secureKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Admibot-Key", secureKey);
        return headers;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        logger.info("Connected to AdmiBot WebSocket.");
        reconnectAttempts = 0;
        currentReconnectDelay = INITIAL_RECONNECT_DELAY;
        authenticate();
    }

    private void authenticate() {
        JSONObject auth = new JSONObject();
        auth.put("action", "authenticate");
        auth.put("key", secureKey);
        send(auth.toString());
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String action = json.optString("action", "").toLowerCase();
            handleAction(action, json);
        } catch (Exception e) {
            logger.warning("Error parsing incoming message: " + e.getMessage());
        }
    }

    private void handleAction(String action, JSONObject json) {
        String correlationId = json.optString("correlation_id", "");
        switch (action) {
            case "shutdown":
                Bukkit.shutdown();
                sendResponse("shutdown_response", "Server is shutting down", correlationId);
                break;
            case "console_command":
                handleConsoleCommand(json);
                break;
            case "chat_message":
                handleChatMessage(json, correlationId);
                break;
            case "ping":
                JSONObject pong = new JSONObject();
                pong.put("action", "pong");
                pong.put("date", System.currentTimeMillis());
                if (!correlationId.isEmpty()) {
                    pong.put("correlation_id", correlationId);
                }
                send(pong.toString());
                break;
            case "players_list":
                handlePlayersList(correlationId);
                break;
            case "server_status":
                handleServerStatus(correlationId);
                break;
            case "server_time":
                handleServerTime(correlationId);
                break;
            case "player_count":
                handlePlayerCount(correlationId);
                break;
            default:
                // Process as custom action if not one of the known actions.
                handleCustomAction(action, json, correlationId);
                break;
        }
    }

    /**
     * Handles custom actions as defined in actions.yml.
     * The action name (in lowercase) is used as the key in the actions configuration.
     * The command associated with the action should include a placeholder "{param}" for substitution.
     *
     * @param action        The custom action name.
     * @param json          The JSON payload.
     * @param correlationId The correlation id for the response.
     */
    private void handleCustomAction(String action, JSONObject json, String correlationId) {
        // Ensure the JSON contains an 'action' field and it is not empty.
        if (action == null || action.trim().isEmpty()) {
            sendResponse("custom_action_response", "No custom action specified", correlationId);
            return;
        }

        // Retrieve the parameter from the JSON payload.
        String parameter = "";
        if (json.has("parameters")) {
            JSONObject params = json.optJSONObject("parameters");
            if (params != null) {
                parameter = params.optString("parameter", "").trim();
            }
        } else {
            parameter = json.optString("parameter", "").trim();
        }

        // Retrieve the custom command template from actions.yml.
        String customCommand = AdmiBotIntegration.getInstance().getActionsConfig().getString(action, null);
        if (customCommand == null || customCommand.isEmpty()) {
            sendResponse(action + "_response", "Custom action " + action + " does not exist", correlationId);
            return;
        }

        // Replace the {param} placeholder with the provided parameter.
        String commandToExecute = customCommand.replace("{param}", parameter);
        logger.info("[AdmiBot] Executing custom action: " + action + " -> " + commandToExecute);

        // Check if the plugin is still enabled before scheduling the synchronous task.
        if (!AdmiBotIntegration.getInstance().isEnabled()) {
            logger.warning("[AdmiBot] Plugin is disabled; cannot execute custom action: " + action);
            sendResponse(action + "_response", "Plugin is disabled, custom action " + action + " was not executed", correlationId);
            return;
        }

        // Execute the command synchronously on the main thread.
        try {
            Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(
                    AdmiBotIntegration.getInstance(),
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute)
            );
            Boolean result = future.get(COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!result) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
            }
        } catch (TimeoutException te) {
            logger.warning("[AdmiBot] Custom action command timeout, executing directly: " + commandToExecute);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
        } catch (Exception ex) {
            logger.severe("[AdmiBot] Exception executing custom action: " + commandToExecute);
            ex.printStackTrace();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecute);
        }

        sendResponse(action + "_response", "Custom action " + action + " executed with parameter: " + parameter, correlationId);
    }

    private void handleConsoleCommand(JSONObject json) {
        String cmd = "";
        JSONObject params = json.optJSONObject("parameters");
        if (params != null) {
            cmd = params.optString("command", "").trim();
        }
        if (cmd.isEmpty()) {
            cmd = json.optString("command", "").trim();
        }
        if (cmd.isEmpty()) return;

        String correlationId = json.optString("correlation_id", "");
        if (cmd.startsWith("say ") && cmd.length() > 4) {
            cmd = "say " + cmd.substring(4);
        }

        final String finalCmd = cmd;
        logger.info("[AdmiBot] Admin executed command from Discord: " + finalCmd);

        try {
            Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(
                    AdmiBotIntegration.getInstance(),
                    () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd)
            );
            Boolean result = future.get(COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (!result) {
                logger.warning("[AdmiBot] Command returned false, executing directly: " + finalCmd);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
            }
        } catch (TimeoutException te) {
            logger.warning("[AdmiBot] Command timeout, executing directly: " + finalCmd);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        } catch (Exception ex) {
            logger.severe("[AdmiBot] Exception executing command: " + finalCmd);
            ex.printStackTrace();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }

        JSONObject response = new JSONObject();
        response.put("action", "console_command_response");
        response.put("command", finalCmd);
        response.put("output", "Command executed successfully. Logs are in console.");
        if (!correlationId.isEmpty()) {
            response.put("correlation_id", correlationId);
        }
        send(response.toString());
    }

    private void handleChatMessage(JSONObject json, String correlationId) {
        String chatMsg = json.optString("message", "").trim();
        if (!chatMsg.isEmpty()) {
            Bukkit.broadcastMessage(chatMsg);
        }
        sendResponse("chat_message_response", chatMsg, correlationId);
    }

    private void handlePlayersList(String correlationId) {
        StringBuilder playerList = new StringBuilder();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerList.append(player.getName()).append(", ");
        }
        if (playerList.length() > 0) {
            playerList.setLength(playerList.length() - 2);  // Remove trailing comma.
        }
        sendResponse("players_list_response", playerList.toString(), correlationId);
    }

    private void handleServerStatus(String correlationId) {
        sendResponse("server_status_response", "Running", correlationId);
    }

    private void handleServerTime(String correlationId) {
        long currentTime = System.currentTimeMillis();
        sendResponse("server_time_response", String.valueOf(currentTime), correlationId);
    }

    private void handlePlayerCount(String correlationId) {
        int playerCount = Bukkit.getOnlinePlayers().size();
        sendResponse("player_count_response", String.valueOf(playerCount), correlationId);
    }

    private void sendResponse(String action, String output, String correlationId) {
        JSONObject response = new JSONObject();
        response.put("action", action);
        response.put("output", output);
        if (!correlationId.isEmpty()) {
            response.put("correlation_id", correlationId);
        }
        send(response.toString());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        scheduleReconnect();
    }

    @Override
    public void onError(Exception ex) {
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) return;
        reconnectAttempts++;
        scheduler.schedule(() -> {
            try {
                reconnectBlocking();
            } catch (InterruptedException ignored) {
            }
        }, currentReconnectDelay, TimeUnit.MILLISECONDS);
        currentReconnectDelay = Math.min(currentReconnectDelay * 2, MAX_RECONNECT_DELAY);
    }

    public void connectWithRetry() {
        scheduler.execute(() -> {
            while (!isOpen() && !isClosing() && !isClosed()) {
                try {
                    connectBlocking();
                } catch (InterruptedException ignored) {
                }
                if (!isOpen()) {
                    try {
                        Thread.sleep(currentReconnectDelay);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        });
    }

    public void shutdown() {
        scheduler.shutdown();
        close();
    }
}
