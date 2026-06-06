package simplexity.simplelunchboxes.config;

import simplexity.simplelunchboxes.SimpleLunchboxes;

public class ConfigHandler {

    private static ConfigHandler instance;

    public static ConfigHandler getInstance() {
        if (instance == null) instance = new ConfigHandler();
        return instance;
    }

    private ConfigHandler() {
        SimpleLunchboxes.getPlugin().saveDefaultConfig();
        load();
    }

    public void load() {
        SimpleLunchboxes.getPlugin().reloadConfig();
    }
}
