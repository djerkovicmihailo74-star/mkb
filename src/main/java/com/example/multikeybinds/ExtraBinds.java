package com.example.multikeybinds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Extra binds, stored as translationKey -> list of binds in config/multikeybinds.json. */
public final class ExtraBinds {
    public static final int MAX_PER_ACTION = 3;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, List<Bind>>>() {}.getType();
    private static final Map<String, List<Bind>> BINDS = new LinkedHashMap<>();

    private ExtraBinds() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("multikeybinds.json");
    }

    public static void load() {
        try {
            if (Files.exists(file())) {
                try (Reader r = Files.newBufferedReader(file())) {
                    Map<String, List<Bind>> m = GSON.fromJson(r, TYPE);
                    if (m != null) { BINDS.clear(); BINDS.putAll(m); }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(BINDS, TYPE, w);
        } catch (Exception ignored) {}
    }

    public static Map<String, List<Bind>> all() { return BINDS; }

    public static List<Bind> get(String actionKey) {
        return BINDS.getOrDefault(actionKey, List.of());
    }

    public static void add(String actionKey, Bind b) {
        List<Bind> list = BINDS.computeIfAbsent(actionKey, k -> new ArrayList<>());
        if (list.size() < MAX_PER_ACTION && !list.contains(b)) list.add(b);
        save();
    }

    public static void remove(String actionKey, Bind b) {
        List<Bind> list = BINDS.get(actionKey);
        if (list != null) {
            list.remove(b);
            if (list.isEmpty()) BINDS.remove(actionKey);
        }
        save();
    }
}
