package simplexity.simplelunchboxes.config;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.SimpleLunchboxes;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LocaleHandler {

    private static LocaleHandler instance;

    public static LocaleHandler getInstance() {
        if (instance == null) instance = new LocaleHandler();
        return instance;
    }

    private final File localeFile;
    private final YamlConfiguration yml = new YamlConfiguration();

    private LocaleHandler() {
        localeFile = new File(SimpleLunchboxes.getPlugin().getDataFolder(), "locale.yml");
        load();
    }

    public void load() {
        try {
            if (localeFile.getParentFile() != null) localeFile.getParentFile().mkdirs();
            localeFile.createNewFile();
            yml.load(localeFile);
        } catch (IOException | InvalidConfigurationException e) {
            SimpleLunchboxes.getPlugin().getLogger().severe("Failed to load locale.yml");
            SimpleLunchboxes.getPlugin().getLogger().severe(e.getMessage());
        }
        populateLocale();
        save();
    }

    private void populateLocale() {
        Set<LocaleMessage> missing = new HashSet<>(Arrays.asList(LocaleMessage.values()));
        for (LocaleMessage message : LocaleMessage.values()) {
            if (yml.contains(message.getPath())) {
                message.setMessage(yml.getString(message.getPath(), message.getDefaultMessage()));
                missing.remove(message);
            }
        }
        for (LocaleMessage message : missing) {
            yml.set(message.getPath(), message.getDefaultMessage());
            message.setMessage(message.getDefaultMessage());
        }
    }

    private void save() {
        try {
            yml.save(localeFile);
        } catch (IOException e) {
            SimpleLunchboxes.getPlugin().getLogger().severe("Failed to save locale.yml");
            SimpleLunchboxes.getPlugin().getLogger().severe(e.getMessage());
        }
    }

    /**
     * Returns the formatted {@link Component} for the given locale key.
     *
     * @param message the locale message to retrieve
     * @return the parsed {@link Component}
     */
    public @NotNull Component get(@NotNull LocaleMessage message) {
        return SimpleLunchboxes.getMiniMessage().deserialize(message.getMessage());
    }

    /**
     * Returns the raw MiniMessage string for the given key, for use with placeholder substitution.
     *
     * @param message the locale message to retrieve
     * @return the raw string
     */
    public @NotNull String getRaw(@NotNull LocaleMessage message) {
        return message.getMessage();
    }
}
