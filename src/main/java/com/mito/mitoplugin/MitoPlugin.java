package com.mito.mitoplugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;



import java.io.File;

public class MitoPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final LuckPermsService lpService = new LuckPermsService();

    private static final MitoManager manager = new MitoManager(
            new MitoConfig(new File("data")),
            new MitoRepository(new File("data"))
    );

    public MitoPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void start() {
        super.start();
        this.getEntityStoreRegistry().registerSystem(new DeathMitoEvent(manager));
    }

    @Override
    protected void setup() {
        manager.reloadConfig(null);
        manager.reloadRepository(null);

        this.getCommandRegistry().registerCommand(new SetMitoCommand(manager));
        this.getCommandRegistry().registerCommand(new GetMitoCommand(manager));
        this.getCommandRegistry().registerCommand(new ReloadMitoCommand(manager));
        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, new ChatFormatter(manager, lpService)::onPlayerChat);

    }
}