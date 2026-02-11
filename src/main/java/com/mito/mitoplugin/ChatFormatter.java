package com.mito.mitoplugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.awt.*;


public final class ChatFormatter {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final MitoManager manager;
    private final LuckPermsService lpService; // pode ser null

    public ChatFormatter(MitoManager manager, LuckPermsService lpService){
        this.manager = manager;
        this.lpService = lpService;
    }

    public void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        boolean isMito = manager.isMito(sender.getUuid());
        if (!isMito) return;

        event.setCancelled(true);

        String mitoTag = manager.config.mitoTag;
        Color mitoColor = manager.config.mitoColor;

        String content = event.getContent();

        LuckPermsHook lp = lpService.get();
        String lpPrefixRaw = (lp != null) ? lp.getPrefix(sender) : "";

        Message prefixMsg = LPcolors.parseLegacy(lpPrefixRaw); // aplica cores do &prefix
        Message mitoMsg = Message.raw(mitoTag).color(mitoColor);

        Message head = Message.empty();

        if (lpPrefixRaw != null && !lpPrefixRaw.isBlank()) {
            head = Message.join(head, prefixMsg, Message.raw(" "));
        }

        head = Message.join(
                mitoMsg,
                head,
                Message.raw(sender.getUsername()).color(mitoColor),
                Message.raw(": ").color(Color.white)
        );

        Message out = Message.join(head, Message.raw(content).color(Color.white));

        for (PlayerRef target : event.getTargets()) {
            target.sendMessage(out);
        }
    }



    private static String sanitizePrefix(String prefix) {
        if (prefix == null) return "";
        // remove códigos legacy &x / §x
        return prefix.replaceAll("(?i)[&§][0-9A-FK-ORX]", "").trim();
    }

}