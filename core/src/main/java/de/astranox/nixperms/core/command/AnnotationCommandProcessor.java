package de.astranox.nixperms.core.command;

import de.astranox.nixperms.api.annotation.command.*;
import de.astranox.nixperms.api.INixPermsAPI;
import de.astranox.nixperms.api.command.NixCommandSender;
import de.astranox.nixperms.api.group.GroupRole;
import de.astranox.nixperms.api.group.IPermissionGroup;
import de.astranox.nixperms.api.user.INixUser;
import de.astranox.nixperms.core.util.Levenshtein;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.reflect.*;
import java.util.*;

public final class AnnotationCommandProcessor {

    private static final int MAX_SUGGEST_DISTANCE = 3;

    private final INixPermsAPI api;
    private final Object2ObjectOpenHashMap<String, RegisteredSubcommand> subcommands = new Object2ObjectOpenHashMap<>();

    public AnnotationCommandProcessor(INixPermsAPI api) {
        this.api = api;
    }

    public void register(Object subcommand) {
        Subcommand annotation = subcommand.getClass().getAnnotation(Subcommand.class);
        if (annotation == null) throw new IllegalArgumentException("Class " + subcommand.getClass().getSimpleName() + " is not annotated with @Subcommand");
        RegisteredSubcommand registered = new RegisteredSubcommand(subcommand, annotation, api);
        subcommands.put(annotation.label().toLowerCase(), registered);
        for (String alias : annotation.aliases()) subcommands.put(alias.toLowerCase(), registered);
    }

    public void execute(NixCommandSender sender, String[] args) {
        if (args.length == 0) {
            api.messages().send(sender, "commands.usage", Map.of());
            return;
        }

        System.out.println("DEBUG: Searching for subcommand: '" + args[0] + "'");
        System.out.println("DEBUG: Available subcommands: " + subcommands.keySet());

        String label = args[0].toLowerCase();
        RegisteredSubcommand subcommand = subcommands.get(label);
        if (subcommand != null) {
            System.out.println("DEBUG: Found subcommand: " + label);
            subcommand.execute(sender, args);
            return;
        }
        String closest = findClosest(label);
        if (closest != null) { api.messages().send(sender, "commands.unknown-did-you-mean", Map.of("input", args[0], "suggestion", closest)); return; }
        api.messages().send(sender, "commands.unknown", Map.of("input", args[0]));
    }

    public List<String> suggest(NixCommandSender sender, String[] args) {
        if (args == null || args.length <= 1) return NixCommandSuggestions.filter(subcommands.keySet(), args == null || args.length == 0 ? "" : args[0]);
        RegisteredSubcommand subcommand = subcommands.get(args[0].toLowerCase());
        if (subcommand == null) return List.of();
        return subcommand.suggest(sender, args);
    }

    private String findClosest(String input) {
        return subcommands.keySet().stream()
                .filter(k -> Levenshtein.distance(k, input) <= MAX_SUGGEST_DISTANCE)
                .min(Comparator.comparingInt(k -> Levenshtein.distance(k, input)))
                .orElse(null);
    }
}
