package com.mito.mitoplugin;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.jspecify.annotations.NonNull;

public final class DeathMitoEvent extends DeathSystems.OnDeathSystem {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static MitoManager manager;

    public DeathMitoEvent(MitoManager manager){
        DeathMitoEvent.manager = manager;
        LOGGER.atInfo().log("DeathMito foi iniciado"); // Debug
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(DeathComponent.getComponentType());
    }

    @Override
    public void onComponentAdded(@NonNull Ref<EntityStore> ref,
                                 @NonNull DeathComponent deathComponent,
                                 @NonNull Store<EntityStore> store,
                                 @NonNull CommandBuffer<EntityStore> commandBuffer) {
        LOGGER.atInfo().log("Morte detectada..."); // Debug
        PlayerRef victim = store.getComponent(ref, PlayerRef.getComponentType());
        if (victim == null) {
            LOGGER.atInfo().log("DeathMitoEvent: victim PlayerRef null"); // Debug
            return;
        }
        if (!manager.isMito(victim.getUuid())) return;
        LOGGER.atInfo().log("Mito morreu: " + victim.getUsername()); // Debug

        Damage deathInfo = deathComponent.getDeathInfo();
        if (deathInfo == null) {
            LOGGER.atInfo().log("DeathMitoEvent: deathInfo null"); // Debug
            return;
        }

        Damage.Source source = deathInfo.getSource();
        LOGGER.atInfo().log("Death source = " + source.getClass().getName()); // Debug

        PlayerRef killer = null;

        if (source instanceof Damage.EntitySource entitySource) {
            killer = store.getComponent(entitySource.getRef(), PlayerRef.getComponentType());
            LOGGER.atInfo().log("EntitySource killerRef=" + (killer == null ? "null" : killer.getUsername())); // Debug
        }

        if (killer == null) return;

        manager.setMito(false, killer.getUuid());
        LOGGER.atInfo().log("DeathMitoEvent aplicado no killer: " + killer.getUsername()); // Debug
    }
}