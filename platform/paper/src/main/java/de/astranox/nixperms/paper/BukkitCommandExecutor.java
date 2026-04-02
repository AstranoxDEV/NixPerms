package de.astranox.nixperms.paper;

import de.astranox.nixperms.core.NixPermsCore;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public final class BukkitCommandExecutor implements CommandExecutor, TabCompleter {

    private final NixPermsCore core;

    public BukkitCommandExecutor(NixPermsCore core) {
        this.core = core;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        core.commands().execute(new BukkitCommandSender(sender), args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return core.commands().suggest(new BukkitCommandSender(sender), args);
    }

    private String[] buildArgs(String label, String[] args) {
        System.out.println("DEBUG: label='" + label + "' args.length=" + args.length);
        String[] full = new String[args.length + 1];
        full[0] = label;
        System.arraycopy(args, 0, full, 1, args.length);
        System.out.println("DEBUG: full[0]='" + full[0] + "'");
        return full;
    }
}
