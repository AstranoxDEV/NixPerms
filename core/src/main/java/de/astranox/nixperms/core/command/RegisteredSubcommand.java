package de.astranox.nixperms.core.command;

import de.astranox.nixperms.api.INixPermsAPI;
import de.astranox.nixperms.api.annotation.command.Action;
import de.astranox.nixperms.api.annotation.command.Arg;
import de.astranox.nixperms.api.annotation.command.ArgType;
import de.astranox.nixperms.api.annotation.command.Subcommand;
import de.astranox.nixperms.api.annotation.command.Usage;
import de.astranox.nixperms.api.command.NixCommandSender;
import de.astranox.nixperms.api.group.GroupRole;
import de.astranox.nixperms.api.group.IPermissionGroup;
import de.astranox.nixperms.api.user.INixUser;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class RegisteredSubcommand {

    private final Object instance;
    private final Subcommand annotation;
    private final INixPermsAPI api;
    private final Object2ObjectOpenHashMap<String, Method> actions = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, Method> aliases = new Object2ObjectOpenHashMap<>();

    RegisteredSubcommand(Object instance, Subcommand annotation, INixPermsAPI api) {
        this.instance = instance;
        this.annotation = annotation;
        this.api = api;

        for (Method method : instance.getClass().getDeclaredMethods()) {
            Action action = method.getAnnotation(Action.class);
            if (action == null) continue;

            actions.put(action.value().toLowerCase(), method);
            for (String alias : action.aliases()) {
                aliases.put(alias.toLowerCase(), method);
            }
        }
    }

    void execute(NixCommandSender sender, String[] args) {
        if (!sender.hasPermission(annotation.permission())) {
            api.messages().send(sender, "commands.no-permission", Map.of());
            return;
        }

        if (args.length < 2) {
            api.messages().send(sender, "commands." + annotation.label() + ".usage", Map.of());
            return;
        }

        ResolvedAction resolvedAction = resolveAction(args);
        if (resolvedAction == null) {
            api.messages().send(sender, "commands.unknown-action", Map.of("input", args.length > 1 ? args[1] : "?"));
            return;
        }

        NixCommandContext ctx = new NixCommandContext(sender, args, api);
        Object[] resolvedArgs = resolveArgs(resolvedAction.method(), ctx, args, resolvedAction.actionIndex());

        if (resolvedArgs == null) {
            Usage usage = resolvedAction.method().getAnnotation(Usage.class);
            api.messages().send(
                    sender,
                    "commands." + annotation.label() + "." + resolvedAction.actionName() + ".usage",
                    usage != null ? Map.of("usage", usage.value()) : Map.of()
            );
            return;
        }

        try {
            resolvedAction.method().setAccessible(true);
            resolvedAction.method().invoke(instance, resolvedArgs);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            api.messages().send(sender, "commands.error", Map.of(
                    "error", cause.getMessage() != null ? cause.getMessage() : "unknown"
            ));
            cause.printStackTrace();
        } catch (Exception e) {
            api.messages().send(sender, "commands.error", Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "unknown"
            ));
            e.printStackTrace();
        }
    }

    List<String> suggest(NixCommandSender sender, String[] args) {
        if (args.length <= 1) {
            return List.of();
        }

        if (args.length == 2) {
            return NixCommandSuggestions.filter(actions.keySet(), args[1]);
        }

        if (args.length == 3 && !isAction(args[1])) {
            return NixCommandSuggestions.filter(actions.keySet(), args[2]);
        }

        ResolvedAction resolvedAction = resolveAction(args);
        if (resolvedAction == null) {
            return List.of();
        }

        return suggestArg(resolvedAction.method(), args, resolvedAction.actionIndex());
    }

    private ResolvedAction resolveAction(String[] args) {
        Method methodAt1 = findMethod(args[1]);
        if (methodAt1 != null) {
            return new ResolvedAction(args[1].toLowerCase(), methodAt1, 1);
        }

        if (args.length >= 3) {
            Method methodAt2 = findMethod(args[2]);
            if (methodAt2 != null) {
                return new ResolvedAction(args[2].toLowerCase(), methodAt2, 2);
            }
        }

        return null;
    }

    private Method findMethod(String input) {
        String key = input.toLowerCase();
        Method method = actions.get(key);
        if (method != null) return method;
        return aliases.get(key);
    }

    private boolean isAction(String input) {
        return findMethod(input) != null;
    }

    private Object[] resolveArgs(Method method, NixCommandContext ctx, String[] rawArgs, int actionIndex) {
        Parameter[] params = method.getParameters();
        Object[] resolved = new Object[params.length];
        int logicalArgPosition = 0;

        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];

            if (NixCommandContext.class.isAssignableFrom(param.getType())) {
                resolved[i] = ctx;
                continue;
            }

            Arg argAnnotation = param.getAnnotation(Arg.class);
            if (argAnnotation == null) {
                return null;
            }

            int rawIndex = mapRawIndex(actionIndex, logicalArgPosition);
            logicalArgPosition++;

            String raw = rawIndex < rawArgs.length ? rawArgs[rawIndex] : null;
            if (raw == null && argAnnotation.required() && argAnnotation.def().isEmpty()) {
                return null;
            }

            String value = raw != null ? raw : argAnnotation.def();
            Object resolvedValue = resolveArgValue(param.getType(), argAnnotation, value, ctx);

            if (resolvedValue == null && argAnnotation.required()) {
                return null;
            }

            resolved[i] = resolvedValue;
        }

        return resolved;
    }

    private int mapRawIndex(int actionIndex, int logicalArgPosition) {
        if (actionIndex == 1) {
            return 2 + logicalArgPosition;
        }

        if (logicalArgPosition == 0) {
            return 1;
        }

        return 2 + logicalArgPosition;
    }

    private Object resolveArgValue(Class<?> type, Arg annotation, String value, NixCommandContext ctx) {
        ArgType argType = annotation.type();
        if (argType == ArgType.AUTO) {
            argType = detectType(type);
        }

        return switch (argType) {
            case STRING, PERMISSION_NODE -> value;
            case INT -> {
                try {
                    yield Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            case BOOLEAN -> Boolean.parseBoolean(value);
            case DOUBLE -> {
                try {
                    yield Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            case GROUP -> ctx.api().groups().group(value);
            case USER -> {
                UUID uuid = uuidOrNull(value);
                if (uuid != null) {
                    yield ctx.api().users().getUser(uuid);
                }

                yield ctx.api().users().loaded().stream()
                        .filter(user -> user.name() != null)
                        .filter(user -> user.name().equalsIgnoreCase(value))
                        .findFirst()
                        .orElse(null);
            }
            case GROUP_ROLE -> {
                try {
                    yield GroupRole.valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    yield null;
                }
            }
            default -> value;
        };
    }

    private ArgType detectType(Class<?> type) {
        if (type == String.class) return ArgType.STRING;
        if (type == int.class || type == Integer.class) return ArgType.INT;
        if (type == boolean.class || type == Boolean.class) return ArgType.BOOLEAN;
        if (type == double.class || type == Double.class) return ArgType.DOUBLE;
        if (IPermissionGroup.class.isAssignableFrom(type)) return ArgType.GROUP;
        if (INixUser.class.isAssignableFrom(type)) return ArgType.USER;
        if (GroupRole.class.isAssignableFrom(type)) return ArgType.GROUP_ROLE;
        return ArgType.STRING;
    }

    private List<String> suggestArg(Method method, String[] args, int actionIndex) {
        Parameter[] params = method.getParameters();
        int logicalArgPosition = 0;

        for (Parameter param : params) {
            if (NixCommandContext.class.isAssignableFrom(param.getType())) {
                continue;
            }

            Arg argAnnotation = param.getAnnotation(Arg.class);
            if (argAnnotation == null) {
                continue;
            }

            int rawIndex = mapRawIndex(actionIndex, logicalArgPosition);
            logicalArgPosition++;

            if (rawIndex != args.length - 1) {
                continue;
            }

            ArgType argType = argAnnotation.type() == ArgType.AUTO
                    ? detectType(param.getType())
                    : argAnnotation.type();

            return switch (argType) {
                case GROUP -> NixCommandSuggestions.filter(
                        api.groups().loaded().stream().map(IPermissionGroup::name).toList(),
                        args[rawIndex]
                );
                case USER -> NixCommandSuggestions.filter(
                        api.users().loaded().stream()
                                .map(user -> user.name() != null ? user.name() : user.uniqueId().toString())
                                .toList(),
                        args[rawIndex]
                );
                case GROUP_ROLE -> NixCommandSuggestions.filter(List.of("PRIMARY", "SECONDARY"), args[rawIndex]);
                case BOOLEAN -> NixCommandSuggestions.filter(List.of("true", "false"), args[rawIndex]);
                default -> List.of();
            };
        }

        return List.of();
    }

    private UUID uuidOrNull(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private record ResolvedAction(String actionName, Method method, int actionIndex) {}
}
