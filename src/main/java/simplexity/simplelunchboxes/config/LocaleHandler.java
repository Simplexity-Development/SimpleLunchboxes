package simplexity.simplelunchboxes.config;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.SimpleLunchboxes;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

public class LocaleHandler {

    private static LocaleHandler instance;

    public static LocaleHandler getInstance() {
        if (instance == null) instance = new LocaleHandler();
        return instance;
    }

    private final File localeFile;
    private final YamlConfiguration yml = new YamlConfiguration();
    private final EnumMap<LocaleMessage, Component> cache = new EnumMap<>(LocaleMessage.class);

    private LocaleHandler() {
        localeFile = new File(SimpleLunchboxes.getPlugin().getDataFolder(), "locale.yml");
        SimpleLunchboxes.getPlugin().saveResource("locale.yml", false);
        load();
    }

    public void load() {
        try {
            yml.load(localeFile);
        } catch (IOException | InvalidConfigurationException e) {
            SimpleLunchboxes.getPlugin().getLogger().severe("Failed to load locale.yml");
            SimpleLunchboxes.getPlugin().getLogger().severe(e.getMessage());
        }
        cache.clear();
        for (LocaleMessage message : LocaleMessage.values()) {
            String raw = yml.getString(message.getKey(), "<red>Missing locale key: " + message.getKey());
            cache.put(message, SimpleLunchboxes.getMiniMessage().deserialize(raw));
        }
    }

    /**
     * Returns the cached component for the given locale key.
     *
     * @param message the locale message to retrieve
     * @return the formatted {@link Component}
     */
    public @NotNull Component get(@NotNull LocaleMessage message) {
        return cache.getOrDefault(message, Component.text("Missing: " + message.getKey()));
    }

    /**
     * Returns the raw MiniMessage string for the given key, for use with placeholder substitution.
     *
     * @param message the locale message to retrieve
     * @return the raw string from locale.yml
     */
    public @NotNull String getRaw(@NotNull LocaleMessage message) {
        return yml.getString(message.getKey(), "<red>Missing locale key: " + message.getKey());
    }
}
