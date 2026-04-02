package de.astranox.nixperms.core.command.subcommand;

import de.astranox.nixperms.api.annotation.command.Action;
import de.astranox.nixperms.api.annotation.command.Arg;
import de.astranox.nixperms.api.annotation.command.ArgType;
import de.astranox.nixperms.api.annotation.command.Subcommand;
import de.astranox.nixperms.api.annotation.command.Usage;
import de.astranox.nixperms.api.group.IPermissionGroup;
import de.astranox.nixperms.api.user.INixUser;
import de.astranox.nixperms.core.command.NixCommandContext;
import de.astranox.nixperms.core.util.MojangProfileService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Subcommand(label = "user", aliases = {"u"})
public final class UserSubcommand {

    @Action("addperm")
    @Usage("<user> <node> [value]")
    public void addPerm(NixCommandContext ctx,
                        @Arg("user") String name,
                        @Arg(value = "node", type = ArgType.PERMISSION_NODE) String node,
                        @Arg(value = "value", def = "true", required = false) boolean value) {
        resolveUser(ctx, name).thenAccept(user -> {
            if (user == null) {
                ctx.reply("commands.user.not-found").with("user", name).send();
                return;
            }

            user.setPermission(node, value).thenRun(() ->
                    ctx.reply("commands.user.addperm.success")
                            .with("user", displayName(user))
                            .with("node", node)
                            .with("value", String.valueOf(value))
                            .send()
            );
        });
    }

    @Action(value = "delperm", aliases = {"removeperm"})
    @Usage("<user> <node>")
    public void delPerm(NixCommandContext ctx,
                        @Arg("user") String name,
                        @Arg(value = "node", type = ArgType.PERMISSION_NODE) String node) {
        resolveUser(ctx, name).thenAccept(user -> {
            if (user == null) {
                ctx.reply("commands.user.not-found").with("user", name).send();
                return;
            }

            user.unsetPermission(node).thenRun(() ->
                    ctx.reply("commands.user.delperm.success")
                            .with("user", displayName(user))
                            .with("node", node)
                            .send()
            );
        });
    }

    @Action("setgroup")
    @Usage("<user> <group>")
    public void setGroup(NixCommandContext ctx,
                         @Arg("user") String name,
                         @Arg(value = "group", type = ArgType.GROUP) IPermissionGroup group) {
        if (group == null) {
            ctx.reply("commands.group.not-found").with("group", ctx.arg(3).orElse("?")).send();
            return;
        }

        resolveUser(ctx, name).thenAccept(user -> {
            if (user == null) {
                ctx.reply("commands.user.not-found").with("user", name).send();
                return;
            }

            user.setPrimary(group)
                    .thenCompose(v -> ctx.api().users().saveUser(user))
                    .thenRun(() -> ctx.reply("commands.user.setgroup.success")
                            .with("user", displayName(user))
                            .with("group", group.name())
                            .send())
                    .exceptionally(ex -> {
                        ctx.reply("commands.error").with("error", message(ex)).send();
                        return null;
                    });
        });
    }

    @Action("setsecondary")
    @Usage("<user> <group>")
    public void setSecondary(NixCommandContext ctx,
                             @Arg("user") String name,
                             @Arg(value = "group", type = ArgType.GROUP) IPermissionGroup group) {
        if (group == null) {
            ctx.reply("commands.group.not-found").with("group", ctx.arg(3).orElse("?")).send();
            return;
        }

        resolveUser(ctx, name).thenAccept(user -> {
            if (user == null) {
                ctx.reply("commands.user.not-found").with("user", name).send();
                return;
            }

            user.setSecondary(group)
                    .thenCompose(v -> ctx.api().users().saveUser(user))
                    .thenRun(() -> ctx.reply("commands.user.setsecondary.success")
                            .with("user", displayName(user))
                            .with("group", group.name())
                            .send())
                    .exceptionally(ex -> {
                        ctx.reply("commands.error").with("error", message(ex)).send();
                        return null;
                    });
        });
    }

    @Action("clearsecondary")
    @Usage("<user>")
    public void clearSecondary(NixCommandContext ctx,
                               @Arg("user") String name) {
        resolveUser(ctx, name).thenAccept(user -> {
            if (user == null) {
                ctx.reply("commands.user.not-found").with("user", name).send();
                return;
            }

            user.setSecondary(null)
                    .thenCompose(v -> ctx.api().users().saveUser(user))
                    .thenRun(() -> ctx.reply("commands.user.setsecondary.success")
                            .with("user", displayName(user))
                            .with("group", "none")
                            .send())
                    .exceptionally(ex -> {
                        ctx.reply("commands.error").with("error", message(ex)).send();
                        return null;
                    });
        });
    }

    @Action("info")
    @Usage("<user>")
    public void info(NixCommandContext ctx,
                     @Arg("user") String name) {
        resolveUser(ctx, name).thenAccept(user -> {
            if (user == null) {
                ctx.reply("commands.user.not-found").with("user", name).send();
                return;
            }

            ctx.reply("commands.user.info")
                    .with("user", displayName(user))
                    .with("primary", user.primary().name())
                    .with("secondary", user.secondaryEffective() != null ? user.secondaryEffective().name() : "none")
                    .send();

            user.ownPermissions().forEach((node, value) ->
                    ctx.reply("commands.group.perm-entry")
                            .with("node", node)
                            .with("value", String.valueOf(value))
                            .send()
            );
        });
    }

    @Action("resolve")
    @Usage("<name>")
    public void resolve(NixCommandContext ctx, @Arg("name") String name) {
        resolveUser(ctx, name).thenAccept(user -> {
            if (user == null) {
                ctx.reply("commands.user.not-found").with("user", name).send();
                return;
            }

            ctx.reply("commands.user.info")
                    .with("user", displayName(user))
                    .with("primary", user.primary().name())
                    .with("secondary", user.secondaryEffective() != null ? user.secondaryEffective().name() : "none")
                    .send();
        });
    }

    private CompletableFuture<INixUser> resolveUser(NixCommandContext ctx, String input) {
        INixUser loadedByName = ctx.api().users().loaded().stream()
                .filter(user -> user.name() != null)
                .filter(user -> user.name().equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);
        if (loadedByName != null) return CompletableFuture.completedFuture(loadedByName);

        UUID directUuid = uuidOrNull(input);
        if (directUuid != null) {
            INixUser byUuid = ctx.api().users().getUser(directUuid);
            if (byUuid != null) return CompletableFuture.completedFuture(byUuid);
        }

        return CompletableFuture.supplyAsync(() -> MojangProfileService.getUniqueId(input))
                .thenApply(uuid -> {
                    if (uuid == null) return null;
                    return ctx.api().users().getUser(uuid);
                });
    }

    private UUID uuidOrNull(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String displayName(INixUser user) {
        return user.name() != null ? user.name() : user.uniqueId().toString();
    }

    private String message(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null && cause.getMessage() != null) return cause.getMessage();
        if (throwable.getMessage() != null) return throwable.getMessage();
        return "unknown";
    }
}
