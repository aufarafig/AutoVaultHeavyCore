package me.ggthfn.heavycore.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class HeavyCoreConfig {
    public boolean enabled = true;
    public Set<String> filter = new LinkedHashSet<>();
    public boolean requireWindBurstOnBook = false;
    public boolean openOminous = true;
    public boolean openNormal = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HeavyCoreConfig INSTANCE;

    public static HeavyCoreConfig get() {
        if (INSTANCE == null) INSTANCE = new HeavyCoreConfig();
        return INSTANCE;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("tukangkunci.json");
    }

    public static void load() {
        Path path = file();
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path);
                HeavyCoreConfig loaded = GSON.fromJson(json, HeavyCoreConfig.class);
                if (loaded != null) INSTANCE = loaded;
            } else {
                INSTANCE = defaults();
                save();
            }
        } catch (IOException e) {
            INSTANCE = defaults();
        }
        if (INSTANCE.filter == null) INSTANCE.filter = new LinkedHashSet<>();
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(get()));
        } catch (IOException ignored) {}
    }

    public static void resetToDefaults() {
        INSTANCE = defaults();
        save();
    }

    private static HeavyCoreConfig defaults() {
        HeavyCoreConfig c = new HeavyCoreConfig();
        c.filter.add("minecraft:heavy_core");
        c.filter.add("minecraft:enchanted_book");
        return c;
    }
}
