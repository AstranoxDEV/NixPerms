package de.astranox.nixperms.paper;

import de.astranox.nixperms.api.user.INixUser;
import de.astranox.nixperms.core.NixPermsCore;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public final class BukkitPlayerListener implements Listener {

    private final NixPermsCore core;

    public BukkitPlayerListener(NixPermsCore core) {
        this.core = core;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLogin(PlayerLoginEvent event) {
        core.users().loadUser(event.getPlayer().getUniqueId()).thenAccept(user -> {
            INixUser nixUser = (INixUser) user;
            nixUser.updateName(event.getPlayer().getName());
            core.users().saveUser(nixUser);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        PermissibleInjector.inject(event.getPlayer(), core, NixPermsBukkit.getProvidingPlugin(NixPermsBukkit.class).getLogger());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        core.users().unloadUser(event.getPlayer().getUniqueId());
    }
}
