package de.astranox.nixperms.core.command;

import de.astranox.nixperms.core.util.Levenshtein;
import java.util.*;

public final class NixCommandSuggestions {

    private static final int MAX_DISTANCE = 3;

    private NixCommandSuggestions() {}

    public static List<String> filter(Collection<String> candidates, String input) {
        String lower = input == null ? "" : input.toLowerCase();
        if (lower.isBlank()) return candidates.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
        return candidates.stream()
                .filter(c -> c.toLowerCase().startsWith(lower) || Levenshtein.distance(c.toLowerCase(), lower) <= MAX_DISTANCE)
                .sorted(Comparator.comparingInt(c -> Levenshtein.distance(c.toLowerCase(), lower)))
                .toList();
    }

    public static List<String> filterPermissionTree(Collection<String> candidates, String input) {
        String lower = input == null ? "" : input.toLowerCase();
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (lower.isBlank()) return candidates.stream().map(p -> p.toLowerCase().split("\\.")[0]).distinct().sorted().toList();
        boolean endsWithDot = lower.endsWith(".");
        int lastDot = endsWithDot ? lower.length() - 1 : lower.lastIndexOf('.');
        String base = lastDot <= 0 ? "" : lower.substring(0, lastDot);
        String currentPart = endsWithDot ? "" : lastDot == -1 ? lower : lower.substring(lastDot + 1);
        String prefix = base.isBlank() ? "" : base + ".";
        for (String candidate : candidates) {
            String permission = candidate.toLowerCase();
            if (!permission.startsWith(prefix)) continue;
            String remainder = permission.substring(prefix.length());
            if (remainder.isBlank()) continue;
            int nextDot = remainder.indexOf('.');
            String nextPart = nextDot == -1 ? remainder : remainder.substring(0, nextDot);
            if (!currentPart.isBlank() && !nextPart.startsWith(currentPart)) continue;
            result.add(prefix + nextPart);
        }
        return List.copyOf(result);
    }
}
