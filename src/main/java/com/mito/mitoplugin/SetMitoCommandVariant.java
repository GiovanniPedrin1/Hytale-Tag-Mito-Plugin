package com.mito.mitoplugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SetMitoCommandVariant extends AbstractCommand {

    private final MitoManager manager;
    private final RequiredArg<PlayerRef> playerArg;

    public SetMitoCommandVariant(MitoManager manager) {
        super("Set mito for another player"); // variante não precisa de nome, só descrição
        this.manager = manager;

        playerArg = withRequiredArg("player", "Player alvo", ArgTypes.PLAYER_REF);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // não deixa o framework barrar
    }

    @Override
    protected @NonNull CompletableFuture<Void> execute(@NonNull CommandContext ctx) {
        UUID senderUuid = ctx.sender().getUuid();

        Color mitoSysColor = manager.config.mitoSysColor;
        String mitoSysTag = manager.config.mitoSysTag;

        Color mitoColor = manager.config.mitoColor;
        String mitoTag = manager.config.mitoTag;

        boolean canUse = PermissionsModule.get().hasPermission(senderUuid, "mito.admin", false);
        if (!ctx.isPlayer()) { canUse = true; }
        if (!canUse) {
            ctx.sendMessage(Message.join(
                    Message.raw(mitoSysTag).color(mitoSysColor),
                    Message.raw("Sem permissão para executar!")
            ));
            return CompletableFuture.completedFuture(null);
        }

        PlayerRef target = ctx.get(playerArg);

        manager.setMito(true, target.getUuid());

        ctx.sendMessage(Message.join(
                Message.raw(mitoSysTag).color(mitoSysColor),
                Message.raw(mitoTag).color(mitoColor),
                Message.raw(" definido para: "),
                Message.raw(target.getUsername()).color(mitoSysColor)
        ));

        target.sendMessage(Message.join(
                Message.raw(mitoSysTag).color(mitoSysColor),
                Message.raw("Você foi definido como "),
                Message.raw(mitoTag).color(mitoColor),
                Message.raw("!")
        ));

        return CompletableFuture.completedFuture(null);
    }
}
