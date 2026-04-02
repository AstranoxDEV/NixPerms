package de.astranox.nixperms.core.command.subcommand;

import de.astranox.nixperms.api.annotation.command.*;
import de.astranox.nixperms.core.NixPermsCore;
import de.astranox.nixperms.core.command.NixCommandContext;
import java.nio.file.Path;

@Subcommand(label = "reload", aliases = {"rl"})
public final class ReloadSubcommand {

    private final NixPermsCore core;
    private final Path dataFolder;

    public ReloadSubcommand(NixPermsCore core, Path dataFolder) {
        this.core = core;
        this.dataFolder = dataFolder;
    }

    @Action("config")
    public void reloadConfig(NixCommandContext ctx) {
        core.reload(dataFolder);
        ctx.reply("commands.reload.success").send();
    }

    @Action("groups")
    public void reloadGroups(NixCommandContext ctx) {
        core.groups().loadAll().thenRun(() -> ctx.reply("commands.reload.success").send());
    }

    @Action("all")
    public void reloadAll(NixCommandContext ctx) {
        core.reload(dataFolder);
        core.groups().loadAll().thenRun(() -> ctx.reply("commands.reload.success").send());
    }
}
