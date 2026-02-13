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
        LOGGER.atInfo().log("DeathMito foi iniciado");
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
        PlayerRef victim = store.getComponent(ref, PlayerRef.getComponentType());
        if (victim == null) {
            return;
        }
        if (!manager.isMito(victim.getUuid())) return;

        Damage deathInfo = deathComponent.getDeathInfo();
        if (deathInfo == null) {
            return;
        }

        Damage.Source source = deathInfo.getSource();

        PlayerRef killer = null;

        if (source instanceof Damage.EntitySource entitySource) {
            killer = store.getComponent(entitySource.getRef(), PlayerRef.getComponentType());
        }

        if (killer == null) return;

        manager.setMito(false, killer.getUuid());
    }
}