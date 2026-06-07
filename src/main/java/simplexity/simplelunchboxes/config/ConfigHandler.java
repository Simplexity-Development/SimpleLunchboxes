package simplexity.simplelunchboxes.config;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.SimpleLunchboxes;

import java.util.List;

public class ConfigHandler {

    private static ConfigHandler instance;

    public static ConfigHandler getInstance() {
        if (instance == null) instance = new ConfigHandler();
        return instance;
    }

    private ItemConfig lunchboxConfig;
    private ItemConfig gluttonousLunchboxConfig;
    private ItemConfig enderLunchboxConfig;
    private ItemConfig gluttonousEnderLunchboxConfig;
    private ItemConfig potionSashConfig;
    private int potionSashMaxStack;

    private ConfigHandler() {
        SimpleLunchboxes.getPlugin().saveDefaultConfig();
        load();
    }

    public void load() {
        JavaPlugin plugin = SimpleLunchboxes.getPlugin();
        plugin.reloadConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        lunchboxConfig = readItemConfig("lunchbox", "<!i><aqua>Lunchbox", false,
                List.of("<!i><gray>A trusty companion for long journeys."));
        gluttonousLunchboxConfig = readItemConfig("gluttonous-lunchbox", "<!i><light_purple>Gluttonous Lunchbox", true,
                List.of("<!i><gray>You can <i>always</i> eat more. <i><b>Always</b></i>."));
        enderLunchboxConfig = readItemConfig("ender-lunchbox", "<!i><aqua>Ender Lunchbox", false,
                List.of("<!i><gray>You feel like you could reach into the void to grab a snack."));
        gluttonousEnderLunchboxConfig = readItemConfig("gluttonous-ender-lunchbox", "<!i><light_purple>Gluttonous Ender Lunchbox", false,
                List.of("<!i>Boundless hunger, boundless void."));
        potionSashConfig = readItemConfig("potion-sash", "<!i>Potion Sash", false,
                List.of("<!i><gray>A stylish sash loaded with potions for any occasion."));
        potionSashMaxStack = plugin.getConfig().getInt("items.potion-sash.max-stack-size", 3);
        if (potionSashMaxStack < 1) potionSashMaxStack = 1;
    }

    private @NotNull ItemConfig readItemConfig(@NotNull String key, @NotNull String defaultName,
                                               boolean defaultGlint, @NotNull List<String> defaultLore) {
        JavaPlugin plugin = SimpleLunchboxes.getPlugin();
        String path = "items." + key;
        String displayName = plugin.getConfig().getString(path + ".display-name", defaultName);

        String emptyModel = plugin.getConfig().getString(path + ".item-model.empty", "");
        String lowModel   = plugin.getConfig().getString(path + ".item-model.low", "");
        String highModel  = plugin.getConfig().getString(path + ".item-model.high", "");
        ItemModelConfig itemModel = new ItemModelConfig(emptyModel, lowModel, highModel);

        boolean glint = plugin.getConfig().getBoolean(path + ".glint", defaultGlint);

        List<String> lore = plugin.getConfig().isSet(path + ".lore")
                ? plugin.getConfig().getStringList(path + ".lore")
                : defaultLore;

        return new ItemConfig(displayName, itemModel, glint, lore);
    }

    public @NotNull ItemConfig getLunchboxConfig()                { return lunchboxConfig; }
    public @NotNull ItemConfig getGluttonousLunchboxConfig()      { return gluttonousLunchboxConfig; }
    public @NotNull ItemConfig getEnderLunchboxConfig()           { return enderLunchboxConfig; }
    public @NotNull ItemConfig getGluttonousEnderLunchboxConfig() { return gluttonousEnderLunchboxConfig; }
    public @NotNull ItemConfig getPotionSashConfig()              { return potionSashConfig; }
    public int getPotionSashMaxStack()                            { return potionSashMaxStack; }
}
