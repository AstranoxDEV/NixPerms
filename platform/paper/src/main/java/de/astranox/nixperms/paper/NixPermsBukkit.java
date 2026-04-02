package de.astranox.nixperms.paper;

import de.astranox.nixperms.core.NixPermsCore;
import de.astranox.nixperms.core.command.subcommand.*;
import de.astranox.nixperms.core.config.WebConfig;
import de.astranox.nixperms.core.util.ServerAddressResolver;
import de.astranox.nixperms.core.web.WebEditorBridge;
import de.astranox.nixperms.web.NixWebServer;
import de.astranox.nixperms.web.WebEditorBridgeImpl;
import de.astranox.nixperms.web.auth.SessionManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.lang.reflect.Field;

public final class NixPermsBukkit extends JavaPlugin {

    private NixPermsCore core;
    private NixWebServer webServer;
    private WebEditorBridge webBridge;

    @Override
    public void onEnable() {
        NixPermsCore.create(getDataFolder().toPath()).thenAccept(c -> {
            this.core = c;
            startWebServerIfEnabled();
            registerSubcommands();
            registerBukkitCommand();
            registerListeners();
            startWebServer();
            getLogger().info("NixPerms enabled.");
        }).exceptionally(ex -> { getLogger().severe("Failed to enable NixPerms: " + ex.getMessage()); return null; });
    }

    private void startWebServerIfEnabled() {
        WebConfig webCfg = core.config().web;
        if (!webCfg.enabled) return;

        this.webServer = new NixWebServer(core, webCfg);
        this.webServer.start(webCfg.port);

        SessionManager sessions = webServer.sessions();
        this.webBridge = new WebEditorBridgeImpl(webServer, webCfg, sessions);

        getLogger().info("Web Editor running on http://" + webBridge.serverAddress() + ":" + webBridge.port() + "/webeditor");
    }

    @Override
    public void onDisable() {
        if (webServer != null) webServer.stop();
        if (core != null) core.shutdown();
    }

    private void registerSubcommands() {
        core.commands().register(new GroupSubcommand());
        core.commands().register(new UserSubcommand());
        core.commands().register(new ReloadSubcommand(core, getDataFolder().toPath()));
        if (core.config().web.enabled && webServer != null) {
            String address = ServerAddressResolver.resolve(core.config().web);
            core.commands().register(new WebEditorSubcommand(webBridge));
        }
    }

    private void registerBukkitCommand() {
        BukkitCommandExecutor executor = new BukkitCommandExecutor(core);
        PluginCommand command = getCommand("nixperms");
        if (command != null) { command.setExecutor(executor); command.setTabCompleter(executor); }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BukkitPlayerListener(core), this);
        getServer().getOnlinePlayers().forEach(player -> PermissibleInjector.inject(player, core, getLogger()));
    }

    private void startWebServer() {
        WebConfig webConfig = core.config().web;
        if (!webConfig.enabled) return;
        webServer = new NixWebServer(core, webConfig);
        webServer.start(webConfig.port);
        String address = ServerAddressResolver.resolve(webConfig);
        String url = "http://" + address + ":" + webConfig.port + "/webeditor";
        getLogger().info("Web Editor available at: " + url);
        String address2 = ServerAddressResolver.resolve(webConfig);
        core.commands().register(new WebEditorSubcommand(webBridge));
    }
}
