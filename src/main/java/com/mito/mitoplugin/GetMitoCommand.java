package com.mito.mitoplugin;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import org.jspecify.annotations.NonNull;

public class GetMitoCommand extends CommandBase {
    private final MitoManager manager;

    public GetMitoCommand(MitoManager manager) {
        super("getmito", "Get the current MITO");
        this.manager = manager;
    }

    @Override
    protected boolean canGeneratePermission() {
        return false; // não deixa o framework barrar
    }

    @Override
    protected void executeSync(@NonNull CommandContext ctx) {
        manager.getMito(ctx);
    }
}
