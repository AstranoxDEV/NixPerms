package de.astranox.nixperms.core.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MojangProfileService {

    private static final String URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern UUID_PATTERN = Pattern.compile("\"id\":\\s*\"([0-9a-f]{32})\"");
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private MojangProfileService() {}

    public static UUID getUniqueId(String name) {
        try {
            HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder().uri(URI.create(URL + name)).build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            Matcher matcher = UUID_PATTERN.matcher(response.body());
            if (!matcher.find()) return null;
            String raw = matcher.group(1);
            return UUID.fromString(raw.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        } catch (IOException | InterruptedException e) { return null; }
    }
}
