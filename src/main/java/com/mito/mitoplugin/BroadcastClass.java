package com.mito.mitoplugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class BroadcastClass {
    public static void broadcast(Message msg) {
        for (PlayerRef p : Universe.get().getPlayers()) {
            p.sendMessage(msg);
        }
    }


}
