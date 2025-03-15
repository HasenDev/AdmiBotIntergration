package xyz.admibot;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.UUID;

public class CapturingCommandSender implements CommandSender {
    private final ConsoleCommandSender consoleSender;
    private final ByteArrayOutputStream outputStream;
    private final PrintStream printStream;
    private String output;

    public CapturingCommandSender(ConsoleCommandSender consoleSender) {
        this.consoleSender = consoleSender;
        this.outputStream = new ByteArrayOutputStream();
        this.printStream = new PrintStream(outputStream);
        this.output = "";
    }

    @Override
    public void sendMessage(String message) {
        // Capture the output sent to the console
        printStream.println(message);  // Use printStream to capture the message
    }

    @Override
    public void sendMessage(@NotNull String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String message) {
        sendMessage(message); // For UUID-based messages, just send the message
    }

    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String[] messages) {
        for (String message : messages) {
            sendMessage(uuid, message);
        }
    }

    @Override
    public @NotNull Server getServer() {
        return consoleSender.getServer();  // Return the server from the console sender
    }

    @Override
    public String getName() {
        return consoleSender.getName();  // Get the name of the console sender
    }

    @Override
    public @NotNull Spigot spigot() {
        return consoleSender.spigot();  // Get the Spigot API object from the console sender
    }

    @Override
    public boolean isOp() {
        return consoleSender.isOp();  // Check if the console sender has operator status
    }

    @Override
    public void setOp(boolean value) {
        consoleSender.setOp(value);  // Set the operator status for the console sender
    }

    // Remove the @Override annotation, because it's not part of the CommandSender interface
    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;  // Return the original console sender
    }

    public String getOutput() {
        return outputStream.toString();  // Get the captured output from the console
    }

    public void resetOutput() {
        outputStream.reset();  // Reset the captured output
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return consoleSender.isPermissionSet(name);  // Check permission for the name
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return consoleSender.isPermissionSet(permission);  // Check permission for the Permission object
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return consoleSender.hasPermission(name);  // Check if the console sender has the permission by name
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return consoleSender.hasPermission(permission);  // Check if the console sender has the permission
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return consoleSender.addAttachment(plugin, name, value);  // Add a permission attachment
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return consoleSender.addAttachment(plugin);  // Add a permission attachment without a name/value pair
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return consoleSender.addAttachment(plugin, name, value, ticks);  // Add a permission attachment with a timeout
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return consoleSender.addAttachment(plugin, ticks);  // Add a permission attachment with a timeout
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        consoleSender.removeAttachment(attachment);  // Remove a permission attachment
    }

    @Override
    public void recalculatePermissions() {
        consoleSender.recalculatePermissions();  // Recalculate the permissions for the console sender
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return consoleSender.getEffectivePermissions();  // Return the effective permissions for the console sender
    }
}
