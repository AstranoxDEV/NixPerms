package de.astranox.nixperms.paper;

import de.astranox.nixperms.api.user.INixUser;
import de.astranox.nixperms.api.user.IUserSnapshot;
import de.astranox.nixperms.core.NixPermsCore;
import org.bukkit.entity.Player;
import org.bukkit.permissions.*;
import java.util.UUID;

public final class BukkitPermissibleBase extends PermissibleBase {

    private final NixPermsCore core;
    private final UUID uniqueId;

    public BukkitPermissibleBase(Player player, NixPermsCore core) {
        super(player);
        this.core = core;
        this.uniqueId = player.getUniqueId();
    }

    @Override
    public boolean hasPermission(String inName) {
        if (inName == null) return false;
        INixUser user = (INixUser) core.users().getUser(uniqueId);
        if (user != null) return user.hasPermission(inName.toLowerCase());
        IUserSnapshot snap = core.users().fetchSnapshot(uniqueId).join();
        if (snap != null) return snap.permissions().decision(inName.toLowerCase()).toBoolean();
        return super.hasPermission(inName);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return hasPermission(perm.getName());
    }

    @Override
    public boolean isPermissionSet(String name) {
        INixUser user = (INixUser) core.users().getUser(uniqueId);
        if (user != null) return user.ownPermissions().containsKey(name.toLowerCase()) || user.primary().permissions().contains(name.toLowerCase());
        return super.isPermissionSet(name);
    }
}
