package com.mito.mitoplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.io.*;
import java.util.UUID;

public class MitoRepository {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private UUID mitoUUID;
    private String mitoDisplayName;

    public MitoRepository(File dataFolder) {
        // Garante que a pasta existe
        if (dataFolder != null && !dataFolder.exists()) {
            boolean ok = dataFolder.mkdirs();
            if (!ok) {
                LOGGER.atWarning().log("Não foi possível criar a pasta de dados: " + dataFolder.getAbsolutePath());
            }
        }

        this.file = new File(dataFolder, "mito.json");
        LOGGER.atInfo().log("Repository em: " + file.getAbsolutePath());
    }

    public void load() {
        if (!file.exists()) {
            mitoUUID = null;
            mitoDisplayName = null;
            return;
        }

        try (Reader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json == null || !json.has("mito_uuid") || json.get("mito_uuid").isJsonNull()) {
                mitoUUID = null;
                mitoDisplayName = null;
                return;
            }
            mitoUUID = UUID.fromString(json.get("mito_uuid").getAsString());
            mitoDisplayName = json.get("mito_name").getAsString();
        } catch (IOException e) {
            LOGGER.atSevere().withCause(e).log("Falha ao carregar mito.json");
            mitoUUID = null;
            mitoDisplayName = null;
        }
    }

    public void save() {
        // Backup: garante que a pasta pai do arquivo existe
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean ok = parent.mkdirs();
            if (!ok) {
                LOGGER.atSevere().log("Não foi possível criar a pasta para salvar: " + parent.getAbsolutePath());
                return;
            }
        }

        JsonObject json = new JsonObject();
        json.addProperty("mito_uuid", mitoUUID == null ? null : mitoUUID.toString());
        json.addProperty("mito_name", mitoDisplayName == null ? null : mitoDisplayName);
        
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            LOGGER.atSevere().withCause(e).log("Falha ao escrever em mito.json");
        }
    }

    public UUID getMitoUUID() {
        return mitoUUID;
    }

    public String getMitoName(){
        return mitoDisplayName;
    }
    public void setMitoUUID(UUID uuid) {
        this.mitoUUID = uuid;
        PlayerRef playerRefTemp = Universe.get().getPlayer(uuid);
        if (playerRefTemp == null){
            this.mitoDisplayName = null;
            save();
            return;
        }
        this.mitoDisplayName = playerRefTemp.getUsername();
        save();
    }
}
