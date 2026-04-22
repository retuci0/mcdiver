package me.retucio.diver.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.retucio.diver.Diver;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


public class ConfigManager {

    private static ConfigManager INSTANCE;

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path PATH = Paths.get("diver", "config.json");

    private Config config = null;


    private ConfigManager() {}

    public static ConfigManager getInstance() {
        if (INSTANCE == null) INSTANCE = new ConfigManager();
        return INSTANCE;
    }


    /* macro management */

    public void setMacro(String name, String command) {
        ensureConfig();
        Macro macro = config.macros.get(name);
        if (macro == null) {
            macro = new Macro(name, command);
            config.macros.put(name, macro);
        } else {
            macro.setCommand(command);
        }
        save();
    }

    public void removeMacro(String name) {
        ensureConfig();
        config.macros.remove(name);
        save();
    }

    public String getCommand(String name) {
        ensureConfig();
        Macro macro = config.macros.get(name);
        return macro != null ? macro.getCommand() : null;
    }

    public Macro getMacro(String name) {
        ensureConfig();
        return config.macros.get(name);
    }

    public Map<String, Macro> getAllMacros() {
        ensureConfig();
        return config.macros;
    }

    public Macro getMacroByKey(int keyCode) {
        ensureConfig();
        for (Macro macro : config.macros.values()) {
            if (macro.getKeybind() == keyCode) return macro;
        }
        return null;
    }

    public void bindKey(String macroName, int keyCode) {
        ensureConfig();
        Macro macro = config.macros.get(macroName);
        if (macro != null) {
            macro.setKeybind(keyCode);
            save();
        }
    }

    public void unbindKey(String macroName) {
        ensureConfig();
        Macro macro = config.macros.get(macroName);
        if (macro != null) {
            macro.setKeybind(-1);
            save();
        }
    }


    /* loading and saving */

    private void ensureConfig() {
        if (config == null) {
            config = new Config();
            load();
        }
    }

    public void load() {
        if (!PATH.toFile().exists()) {
            save();
            return;
        }
        try (Reader reader = new FileReader(PATH.toFile())) {
            config = GSON.fromJson(reader, Config.class);
            if (config == null) config = new Config();
            Diver.LOGGER.info("config loaded from {}", PATH);
        } catch (IOException e) {
            Diver.LOGGER.error("failed to load config: ", e);
            config = new Config();
        }
    }

    public void save() {
        ensureConfig();
        try {
            if (!PATH.getParent().toFile().mkdirs() && !PATH.getParent().toFile().exists()) {
                Diver.LOGGER.error("failed to create config dir");
                return;
            }
            try (Writer writer = new FileWriter(PATH.toFile())) {
                GSON.toJson(config, writer);
                Diver.LOGGER.info("config saved to {}", PATH);
            }
        } catch (IOException e) {
            Diver.LOGGER.error("failed to save config: ", e);
        }
    }



    public Config getConfig() {
        ensureConfig();
        return config;
    }
}