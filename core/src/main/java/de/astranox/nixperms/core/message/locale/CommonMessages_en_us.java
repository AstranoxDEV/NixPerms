package de.astranox.nixperms.core.message.locale;

import de.astranox.nixperms.api.annotation.message.*;
import de.astranox.nixperms.api.message.MessageProvider;
import de.astranox.nixperms.core.message.NixPermsStyle;

@Locale("en_us")
public final class CommonMessages_en_us {

    @Message("commands.usage") public static MessageProvider usage = () -> NixPermsStyle.MUTED + "Usage: <white>/nixperms <subcommand>";
    @Message("commands.no-permission") public static MessageProvider noPermission = () -> NixPermsStyle.ERROR + "You don't have permission to do that.";
    @Message("commands.unknown") public static MessageProvider unknown = () -> NixPermsStyle.ERROR + "Unknown command: " + NixPermsStyle.PRIMARY + "<input>";
    @Message("commands.unknown-did-you-mean") public static MessageProvider didYouMean = () -> NixPermsStyle.ERROR + "Unknown command: " + NixPermsStyle.PRIMARY + "<input>" + NixPermsStyle.MUTED + " — did you mean " + NixPermsStyle.WHITE + "<suggestion>" + NixPermsStyle.MUTED + "?";
    @Message("commands.unknown-action") public static MessageProvider unknownAction = () -> NixPermsStyle.ERROR + "Unknown action: " + NixPermsStyle.PRIMARY + "<input>";
    @Message("commands.error") public static MessageProvider error = () -> NixPermsStyle.ERROR + "An error occurred: " + NixPermsStyle.MUTED + "<error>";
    @Message("commands.reload.success") public static MessageProvider reloadSuccess = () -> NixPermsStyle.SUCCESS + "NixPerms reloaded successfully.";
    @Message("commands.webeditor.link-sent") public static MessageProvider webEditorLink = () -> NixPermsStyle.SUCCESS + "Web Editor link generated. Expires in " + NixPermsStyle.PRIMARY + "<expiry>";
    @Message("commands.webeditor.no-console") public static MessageProvider webEditorNoConsole = () -> NixPermsStyle.ERROR + "This command can only be used by players.";
    @Message("commands.webeditor.disabled") public static MessageProvider webEditorDisabled = () -> NixPermsStyle.ERROR + "The Web Editor is not enabled. Set " + NixPermsStyle.PRIMARY + "web.enabled: true" + NixPermsStyle.WHITE + " in config.yml.";
}
