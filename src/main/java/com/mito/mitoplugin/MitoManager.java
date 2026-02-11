package com.mito.mitoplugin;


import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

public class MitoManager {
    public final MitoConfig config;
    public final MitoRepository repository;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public MitoManager(MitoConfig config, MitoRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    public boolean isMito(UUID uuid) {
        return uuid != null && uuid.equals(repository.getMitoUUID());
    }

    public void getMito(@Nonnull CommandContext ctx) {
        repository.load();
        UUID mito = repository.getMitoUUID();
        if (mito == null){
            ctx.sendMessage(Message.join(
                    Message.raw(config.mitoSysTag).color(config.mitoSysColor),
                    Message.raw("Ninguém é"),
                    Message.raw(config.mitoTag).color(config.mitoColor),
                    Message.raw(" ainda")
            ));
            return;
        }

        PlayerRef playerRefNew = Universe.get().getPlayer(mito);
        String mitoName;
        if (playerRefNew == null){
            mitoName = repository.getMitoName();
            if (mitoName == null) {
                ctx.sendMessage(Message.join(
                        Message.raw(config.mitoSysTag).color(config.mitoSysColor),
                        Message.raw("Ninguém é"),
                        Message.raw(config.mitoTag).color(config.mitoColor),
                        Message.raw(" ainda")
                ));
                return;
            }
        }else{
            mitoName = playerRefNew.getUsername();
        }
        Color mitoColor = config.mitoColor;
        ctx.sendMessage(Message.join(
                Message.raw(config.mitoSysTag).color(config.mitoSysColor),
                Message.raw("O "),
                Message.raw(config.mitoTag).color(config.mitoColor),
                Message.raw(" atual é "),
                Message.raw(mitoName).color(mitoColor)
        ));
    }


    public void setMito(boolean isCommand ,UUID newMito) {
        repository.load();
        UUID old = repository.getMitoUUID();

        notifyChange(isCommand, old, newMito);

        repository.setMitoUUID(newMito);
    }

    private Message buildAnnounceMessage(String template, String oldName, String newName) {
        Message out = Message.empty();

        int i = 0;
        while (i < template.length()) {
            int next = template.indexOf('{', i);
            if (next == -1) {
                out = Message.join(out, Message.raw(template.substring(i)));
                break;
            }

            // texto antes do placeholder
            if (next > i) {
                out = Message.join(out, Message.raw(template.substring(i, next)));
            }

            int end = template.indexOf('}', next);
            if (end == -1) { // placeholder mal formado, cola o resto
                out = Message.join(out, Message.raw(template.substring(next)));
                break;
            }

            String key = template.substring(next + 1, end);
            switch (key) {
                case "sysTag" -> out = Message.join(
                        out, Message.raw(config.mitoSysTag).color(config.mitoSysColor)
                );
                case "tag" -> out = Message.join(out,
                        Message.raw(config.mitoTag).color(config.mitoColor)
                );
                case "old" -> out = Message.join(out,
                        Message.raw(oldName).color(config.victimColor)
                );
                case "new" -> out = Message.join(out,
                        Message.raw(newName).color(config.mitoColor)
                );

                default -> out = Message.join(out, Message.raw("{" + key + "}"));
            }

            i = end + 1;
        }

        return out;
    }

    private void notifyChange(boolean isCommand, UUID oldMito, UUID newMito) {
        String texto;

        if(isCommand){
            texto = config.announce_command;
        }else{
            texto = config.announce_kill;
        }

        String oldMitoName = "";
        PlayerRef playerRefOld;
        boolean ok = false;

        if (oldMito != null) { // Tratar caso mito.json estiver vazio
            playerRefOld = Universe.get().getPlayer(oldMito);
            if (playerRefOld != null) { // Tratar Caso o UUID seja invalido
                oldMitoName = playerRefOld.getUsername();
                ok = true;
            }
        }

        if(!ok){
            oldMitoName = repository.getMitoName();
            if (oldMitoName == null){
                oldMitoName = "ninguém";
            }
        }

        PlayerRef playerRefNew = Universe.get().getPlayer(newMito);

        if (playerRefNew == null){
            LOGGER.atWarning().log("O UUID do novo MITO deve ser valido");
            return;
        }


        Message msg = buildAnnounceMessage(texto, oldMitoName, playerRefNew.getUsername());

        // Notificar mudança via comando
        BroadcastClass.broadcast(msg);
    }

    public void reloadConfig(@Nullable CommandContext ctx) {
        config.reload();
        if (ctx != null) {
            ctx.sendMessage(Message.join(
                    Message.raw(config.mitoSysTag).color(config.mitoSysColor),
                    Message.raw("config recarregado!")
            ));
        }
    }

    public void reloadRepository(@Nullable CommandContext ctx) {
        repository.load();
        if (ctx != null) {
            ctx.sendMessage(Message.join(
                    Message.raw(config.mitoSysTag).color(config.mitoSysColor),
                    Message.raw("repository recarregado!")
            ));
        }
    }
}

