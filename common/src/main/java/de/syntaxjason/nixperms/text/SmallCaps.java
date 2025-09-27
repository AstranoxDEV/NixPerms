package de.syntaxjason.nixperms.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class SmallCaps {

    private static final Map<Integer, String> MAP = new HashMap<>();
    private static final String DIAERESIS = "\u0308";

    static {
        MAP.put((int) 'a', "ᴀ"); // U+1D00
        MAP.put((int) 'b', "ʙ"); // U+0299
        MAP.put((int) 'c', "ᴄ"); // U+1D04
        MAP.put((int) 'd', "ᴅ"); // U+1D05
        MAP.put((int) 'e', "ᴇ"); // U+1D07
        MAP.put((int) 'f', "ꜰ"); // U+A730
        MAP.put((int) 'g', "ɢ"); // U+0262
        MAP.put((int) 'h', "ʜ"); // U+029C
        MAP.put((int) 'i', "ɪ"); // U+026A
        MAP.put((int) 'j', "ᴊ"); // U+1D0A
        MAP.put((int) 'k', "ᴋ"); // U+1D0B
        MAP.put((int) 'l', "ʟ"); // U+029F
        MAP.put((int) 'm', "ᴍ"); // U+1D0D
        MAP.put((int) 'n', "ɴ"); // U+0274
        MAP.put((int) 'o', "ᴏ"); // U+1D0F
        MAP.put((int) 'p', "ᴘ"); // U+1D18
        MAP.put((int) 'q', "ꞯ"); // U+A7AF
        MAP.put((int) 'r', "ʀ"); // U+0280

        MAP.put((int) 's', "s");
        MAP.put((int) 't', "ᴛ"); // U+1D1B
        MAP.put((int) 'u', "ᴜ"); // U+1D1C
        MAP.put((int) 'v', "ᴠ"); // U+1D20
        MAP.put((int) 'w', "ᴡ"); // U+1D21
        MAP.put((int) 'x', "x");
        MAP.put((int) 'y', "ʏ"); // U+028F
        MAP.put((int) 'z', "ᴢ"); // U+1D22
        for (char c = 'A'; c <= 'Z'; c++) {
            MAP.put((int) c, MAP.get((int) Character.toLowerCase(c)));
        }
    }

    private SmallCaps() {
    }

    public static String toSmallCaps(String input) {
        return toSmallCaps(input, UmlautMode.KEEP).asComponent().insertion();
    }

    public static @Nullable ComponentLike toSmallCaps(String input, UmlautMode umlauts) {
        if (input == null || input.isEmpty()) return MiniMessage.miniMessage().deserialize("");
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); ) {
            int cp = input.codePointAt(i);
            int next = Character.charCount(cp);

            if (umlauts != UmlautMode.KEEP) {
                if (cp == 'ä') {
                    out.append(umlauts == UmlautMode.COMPOSE ? "ᴀ" + DIAERESIS : "ae");
                    i += next;
                    continue;
                }
                if (cp == 'ö') {
                    out.append(umlauts == UmlautMode.COMPOSE ? "ᴏ" + DIAERESIS : "oe");
                    i += next;
                    continue;
                }
                if (cp == 'ü') {
                    out.append(umlauts == UmlautMode.COMPOSE ? "ᴜ" + DIAERESIS : "ue");
                    i += next;
                    continue;
                }
                if (cp == 'Ä') {
                    out.append(umlauts == UmlautMode.COMPOSE ? "ᴀ" + DIAERESIS : "Ae");
                    i += next;
                    continue;
                }
                if (cp == 'Ö') {
                    out.append(umlauts == UmlautMode.COMPOSE ? "ᴏ" + DIAERESIS : "Oe");
                    i += next;
                    continue;
                }
                if (cp == 'Ü') {
                    out.append(umlauts == UmlautMode.COMPOSE ? "ᴜ" + DIAERESIS : "Ue");
                    i += next;
                    continue;
                }
            }

            String mapped = MAP.get(cp);
            if (mapped != null) {
                out.append(mapped);
                i += next;
                continue;
            }
            out.appendCodePoint(cp);
            i += next;
        }
        return MiniMessage.miniMessage().deserialize(out.toString());
    }

    public static Component toSmallCaps(Component component, UmlautMode umlauts) {
        if (component == null) return null;
        return component.replaceText(TextReplacementConfig.builder()
                .match(Pattern.compile("(?s).+"))
                .replacement((mr, b) -> toSmallCaps(mr.group(), umlauts))
                .build());
    }

    public enum UmlautMode {
        KEEP,
        AE_OE_UE,
        COMPOSE
    }
}

