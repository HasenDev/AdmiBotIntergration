package xyz.admibot;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
public class AdmibotCommand implements CommandExecutor {
    private static final String PREFIX = ChatColor.WHITE + "[" + ChatColor.AQUA + "Admi" + ChatColor.DARK_AQUA + "Bot" + ChatColor.WHITE + "] " + ChatColor.RESET;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("status")) {
            showStatus(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("ping")) {
            sender.sendMessage(PREFIX + ChatColor.GREEN + "Pong!");
            return true;
        }
        if (args[0].equalsIgnoreCase("guide")) {
            sendGuide(sender);
            return true;
        }
        sender.sendMessage(PREFIX + ChatColor.RED + "Unknown command. Use /admibot help for assistance.");
        return true;
    }
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Admibot Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/admibot help" + ChatColor.WHITE + " - Show help message");
        sender.sendMessage(ChatColor.YELLOW + "/admibot status" + ChatColor.WHITE + " - Show WebSocket connection status and secure key");
        sender.sendMessage(ChatColor.YELLOW + "/admibot ping" + ChatColor.WHITE + " - Simple pong response");
        sender.sendMessage(ChatColor.YELLOW + "/admibot guide" + ChatColor.WHITE + " - Learn how to use Admibot from the official guide");
        sender.sendMessage(ChatColor.GRAY + "Visit the official documentation for more advanced features and setup at https://docs.admibot.xyz/minecraft-link/introduction");
    }
    private void showStatus(CommandSender sender) {
        WebSocketHandler wsHandler = AdmiBotIntegration.getInstance().getWebSocketHandler();
        String status = (wsHandler != null && wsHandler.isOpen()) ? "Connected" : "Disconnected";
        String secureKey = AdmiBotIntegration.getInstance().getSecureKey();

        sender.sendMessage(PREFIX + ChatColor.GREEN + "WebSocket Status: " + ChatColor.YELLOW + status);
    }
    private void sendGuide(CommandSender sender) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + "To learn more about how to use Admibot, visit the official guide:");
        sender.sendMessage(ChatColor.YELLOW + "https://docs.admibot.xyz/minecraft-link/introduction");
        sender.sendMessage(ChatColor.GRAY + "This guide will help you understand all features and configurations of the plugin.");
    }
    private String formatUptime(long uptimeMillis) {
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return String.format("%d day(s), %d hour(s), %d minute(s)", days, hours % 24, minutes % 60);
    }
}
