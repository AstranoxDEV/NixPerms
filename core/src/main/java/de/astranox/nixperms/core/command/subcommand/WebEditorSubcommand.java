// core/command/subcommand/WebEditorSubcommand.java
package de.astranox.nixperms.core.command.subcommand;

import de.astranox.nixperms.api.annotation.command.*;
import de.astranox.nixperms.core.command.NixCommandContext;
import de.astranox.nixperms.core.web.WebEditorBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Subcommand(label = "webeditor", aliases = {"web", "editor"})
public final class WebEditorSubcommand {

    private final WebEditorBridge bridge;

    public WebEditorSubcommand(WebEditorBridge bridge) {
        this.bridge = bridge;
    }

    @Action("open")
    public void open(NixCommandContext ctx) {
        if (!ctx.sender().isPlayer()) { ctx.reply("commands.webeditor.no-console").send(); return; }
        if (bridge == null || !bridge.isRunning()) { ctx.reply("commands.webeditor.disabled").send(); return; }

        String token = bridge.createSessionToken(ctx.sender().uniqueId());
        String url = "http://" + bridge.serverAddress() + ":" + bridge.port() + "/webeditor?token=" + token;

        ctx.sendRaw(buildLink(url));
        ctx.reply("commands.webeditor.link-sent").with("expiry", "30min").send();
    }

    private Component buildLink(String url) {
        return MiniMessage.miniMessage().deserialize(
                "<hover:show_text:'<#4ADE80>Click to open the NixPerms Web Editor'>" +
                        "<click:open_url:'" + url + "'>" +
                        "<#00D4FF><underlined>→ Open Web Editor</underlined></click></hover>"
        );
    }
}
