package com.mito.mitoplugin;
import com.hypixel.hytale.server.core.Message;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public final class LPcolors {
    private LPcolors() {}

    public static Message parseLegacy(String input) {
        if (input == null || input.isEmpty()) return Message.empty();

        List<Message> parts = new ArrayList<>();
        StringBuilder buf = new StringBuilder();

        Color current = null; // null = default
        // Se você tiver API de estilo no Message, dá pra guardar flags aqui (bold etc.)
        // boolean bold=false, italic=false, underline=false, strike=false, obf=false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if ((c == '&' || c == '§') && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));

                // flush buffer antes de aplicar o novo estilo
                if (!buf.isEmpty()) {
                    parts.add(applyStyle(Message.raw(buf.toString()), current /*, flags*/));
                    buf.setLength(0);
                }

                if (code == 'r') {
                    current = null;
                    // reset flags também
                    i++;
                    continue;
                }

                Color maybeColor = mapColor(code);
                if (maybeColor != null) {
                    current = maybeColor;
                    i++;
                    continue;
                }

                // estilos (se quiser suportar; senão ignore)
                // switch (code) { case 'l': bold=true; break; ... }
                // i++; continue;

                // Se não reconheceu, trata como texto literal
            }

            buf.append(c);
        }

        if (!buf.isEmpty()) {
            parts.add(applyStyle(Message.raw(buf.toString()), current /*, flags*/));
        }

        if (parts.isEmpty()) return Message.empty();
        Message out = parts.getFirst();
        for (int i = 1; i < parts.size(); i++) {
            out = Message.join(out, parts.get(i));
        }
        return out;
    }

    private static Message applyStyle(Message msg, Color color /*, flags*/) {
        if (color != null) msg = msg.color(color);
        // Se tiver métodos de estilo no seu Message, aplique aqui.
        return msg;
    }

    private static Color mapColor(char code) {
        // Minecraft-like palette
        return switch (code) {
            case '0' -> new Color(0x000000);
            case '1' -> new Color(0x0000AA);
            case '2' -> new Color(0x00AA00);
            case '3' -> new Color(0x00AAAA);
            case '4' -> new Color(0xAA0000);
            case '5' -> new Color(0xAA00AA);
            case '6' -> new Color(0xFFAA00);
            case '7' -> new Color(0xAAAAAA);
            case '8' -> new Color(0x555555);
            case '9' -> new Color(0x5555FF);
            case 'a' -> new Color(0x55FF55);
            case 'b' -> new Color(0x55FFFF);
            case 'c' -> new Color(0xFF5555);
            case 'd' -> new Color(0xFF55FF);
            case 'e' -> new Color(0xFFFF55);
            case 'f' -> new Color(0xFFFFFF);
            default -> null;
        };
    }
}
