package com.mito.mitoplugin;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.hypixel.hytale.logger.HytaleLogger;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class MitoConfig {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private transient final File folder;
    private transient final File file;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Color.class, (JsonSerializer<Color>) (src, typeOfSrc, context) -> {
                String hex = String.format("#%02X%02X%02X", src.getRed(), src.getGreen(), src.getBlue());
                return new JsonPrimitive(hex);
            })
            .registerTypeAdapter(Color.class, (JsonDeserializer<Color>) (json, typeOfT, context) -> {
                if (json.isJsonPrimitive()) {
                    String s = json.getAsString().trim();
                    return Color.decode(s);
                }
                throw new JsonParseException("mitoColor deve ser uma string hex, ex: \"#FFD700\"");
            })
            .create();

    @SerializedName("tag")
    public String mitoTag = "[MITO] ";

    @SerializedName("sys_tag")
    public String mitoSysTag = "[MITO] ";

    @SerializedName("color")
    public Color mitoColor = Color.decode("#FFD700");

    @SerializedName("sys_color")
    public Color mitoSysColor = Color.decode("#FFD700");

    @SerializedName("victim_color")
    public Color victimColor = Color.decode("#D30000");

    @SerializedName("announce_kill")
    public String announce_kill = "{sysTag}{new} matou {old} e é o novo {tag} do servidor!";

    @SerializedName("announce_command")
    public String announce_command = "{sysTag}O {new} é o novo {tag} do servidor!";

    public MitoConfig(File folder) {
        if (folder == null) throw new IllegalArgumentException("folder não pode ser null");

        this.folder = folder;
        this.file = new File(folder, "mito_config.json");

        ensureFolder();

        // Se ainda não existe, cria com defaults
        if (!file.exists()) {
            save();
        } else {
            // opcional: já carrega automaticamente
            reload();
        }

        LOGGER.atInfo().log("Config em: " + file.getAbsolutePath());
    }

    private void ensureFolder() {
        if (!folder.exists()) {
            boolean ok = folder.mkdirs();
            if (!ok && !folder.exists()) {
                LOGGER.atSevere().log("Não foi possível criar a pasta de dados: " + folder.getAbsolutePath());
            }
        }
    }

    public void reload() {
        if (!file.exists()) {
            save();
            return;
        }

        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            MitoConfig loaded = gson.fromJson(r, MitoConfig.class);
            if (loaded != null) {
                this.mitoTag = loaded.mitoTag;
                this.mitoSysTag = loaded.mitoSysTag;
                this.mitoColor = loaded.mitoColor;
                this.mitoSysColor = loaded.mitoSysColor;
                this.victimColor = loaded.victimColor;
                this.announce_kill = loaded.announce_kill;
                this.announce_command = loaded.announce_command;
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Erro ao carregar mito_config.json");
        }
    }

    public void save() {
        ensureFolder();

        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(this, w);
            w.flush();
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("Erro ao salvar em mito_config.json");
        }
    }
}

