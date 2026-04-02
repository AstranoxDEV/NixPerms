package de.astranox.nixperms.paper;

import de.astranox.nixperms.api.command.NixCommandSender;
import de.astranox.nixperms.api.platform.Platform;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public final class BukkitCommandSender implements NixCommandSender {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private final CommandSender handle;

    public BukkitCommandSender(CommandSender handle) {
        this.handle = handle;
    }

    @Override public String name() { return handle.getName(); }
    @Override public UUID uniqueId() { return handle instanceof Player p ? p.getUniqueId() : CONSOLE_UUID; }
    @Override public boolean isPlayer() { return handle instanceof Player; }
    @Override public boolean hasPermission(String node) {
        if (platform() == Platform.CONSOLE) return true;
        return handle.hasPermission(node);
    }
    @Override public String locale() { return handle instanceof Player p ? p.getLocale().toLowerCase().replace("-", "_") : "en_us"; }
    @Override public Platform platform() { return Platform.BUKKIT; }
    @Override public void sendComponent(Component component) { handle.sendMessage(LegacyComponentSerializer.legacySection().serialize(component)); }
}
