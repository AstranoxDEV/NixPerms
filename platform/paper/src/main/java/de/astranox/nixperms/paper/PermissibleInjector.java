package de.astranox.nixperms.paper;

import de.astranox.nixperms.core.NixPermsCore;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public final class PermissibleInjector {

    private PermissibleInjector() {}

    public static void inject(Player player, NixPermsCore core, java.util.logging.Logger logger) {
        try {
            Class<?> craftHuman = Class.forName("org.bukkit.craftbukkit.entity.CraftHumanEntity");
            Field permField = craftHuman.getDeclaredField("perm");
            permField.setAccessible(true);
            permField.set(player, new BukkitPermissibleBase(player, core));
        } catch (ClassNotFoundException e) {
            logger.warning("Could not find CraftHumanEntity (is this a Paper/Spigot server?): " + e.getMessage());
        } catch (NoSuchFieldException e) {
            logger.warning("Could not find 'perm' field on CraftHumanEntity: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.warning("Failed to inject permissible for " + player.getName() + ": " + e.getMessage());
        } catch (Throwable t) {
            logger.warning("Unexpected error while injecting permissible for " + player.getName() + ": " + t.getMessage());
        }
    }
}
