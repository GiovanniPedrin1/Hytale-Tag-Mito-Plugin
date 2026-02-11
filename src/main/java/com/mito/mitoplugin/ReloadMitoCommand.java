package com.mito.mitoplugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.UUID;

public class ReloadMitoCommand extends CommandBase {
    MitoManager manager;
    public ReloadMitoCommand(MitoManager manager) {
        super("reloadmito", "Reload the plugin MITO");
        this.manager = manager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // não deixa o framework barrar antes
    }

    @Override
    protected void executeSync(@NonNull CommandContext ctx) {
        UUID playerUUID = ctx.sender().getUuid();

        Color mitoColor = manager.config.mitoSysColor;
        String mitoTag = manager.config.mitoSysTag;

        boolean canUse = PermissionsModule.get().hasPermission(playerUUID, "mito.admin", false);
        if (!ctx.isPlayer()) { canUse = true; }
        if (canUse){
            manager.reloadConfig(ctx);
            manager.reloadRepository(ctx);
        }else{
            ctx.sendMessage(Message.join(
                    Message.raw(mitoTag).color(mitoColor),
                    Message.raw("Sem permissão para executar!")
            ));
        }
    }
}
