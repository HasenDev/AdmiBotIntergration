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
        printStream.println(message);
    }
    @Override
    public void sendMessage(@NotNull String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String message) {
        sendMessage(message);
    }
    @Override
    public void sendMessage(@Nullable UUID uuid, @NotNull String[] messages) {
        for (String message : messages) {
            sendMessage(uuid, message);
        }
    }
    @Override
    public @NotNull Server getServer() {
        return consoleSender.getServer();
    }
    @Override
    public String getName() {
        return consoleSender.getName();
    }
    @Override
    public @NotNull Spigot spigot() {
        return consoleSender.spigot();
    }
    @Override
    public boolean isOp() {
        return consoleSender.isOp(); 
    }
    @Override
    public void setOp(boolean value) {
        consoleSender.setOp(value);
    }
    public ConsoleCommandSender getConsoleSender() {
        return consoleSender;
    }
    public String getOutput() {
        return outputStream.toString();
    }
    public void resetOutput() {
        outputStream.reset();
    }
    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return consoleSender.isPermissionSet(name);
    }
    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return consoleSender.isPermissionSet(permission);
    }
    @Override
    public boolean hasPermission(@NotNull String name) {
        return consoleSender.hasPermission(name);
    }
    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return consoleSender.hasPermission(permission);
    }
    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return consoleSender.addAttachment(plugin, name, value);
    }
    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return consoleSender.addAttachment(plugin);
    }
    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return consoleSender.addAttachment(plugin, name, value, ticks);
    }
    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return consoleSender.addAttachment(plugin, ticks);
    }
    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        consoleSender.removeAttachment(attachment);
    }
    @Override
    public void recalculatePermissions() {
        consoleSender.recalculatePermissions();
    }
    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return consoleSender.getEffectivePermissions();
    }
}
