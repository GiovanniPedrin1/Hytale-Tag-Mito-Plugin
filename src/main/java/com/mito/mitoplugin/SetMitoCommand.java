package com.mito.mitoplugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SetMitoCommand extends AbstractCommand {

    private final MitoManager manager;

    public SetMitoCommand(MitoManager manager) {
        super("setmito", "Set a new MITO player");
        this.manager = manager;

        // Variante: /setmito <player>
        addUsageVariant(new SetMitoCommandVariant(manager));
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // não deixa o framework barrar
    }

    @Override
    protected @NonNull CompletableFuture<Void> execute(@NonNull CommandContext ctx) {
        UUID senderUuid = ctx.sender().getUuid();

        Color mitoColor = manager.config.mitoSysColor;
        String mitoTag = manager.config.mitoSysTag;

        boolean canUse = PermissionsModule.get().hasPermission(senderUuid, "mito.admin", false);
        if (!canUse) {
            ctx.sendMessage(Message.join(
                    Message.raw(mitoTag).color(mitoColor),
                    Message.raw("Sem permissão para executar!")
            ));
            return CompletableFuture.completedFuture(null);
        }

        // /setmito (self) — precisa ser player online
        PlayerRef self = Universe.get().getPlayer(senderUuid);
        if (self == null) {
            ctx.sendMessage(Message.join(
                    Message.raw(mitoTag).color(mitoColor),
                    Message.raw("Esse comando precisa ser executado por um player (ou use /setmito <player>).")
            ));
            return CompletableFuture.completedFuture(null);
        }

        manager.setMito(true, self.getUuid());
        ctx.sendMessage(Message.join(
                Message.raw(mitoTag).color(mitoColor),
                Message.raw("Agora você é o "),
                Message.raw(manager.config.mitoTag).color(manager.config.mitoColor),
                Message.raw("!")
        ));
        return CompletableFuture.completedFuture(null);
    }
}
