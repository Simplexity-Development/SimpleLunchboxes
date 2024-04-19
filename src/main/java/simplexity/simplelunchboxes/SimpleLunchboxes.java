package simplexity.simplelunchboxes;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleLunchboxes extends JavaPlugin {

    public static final String namespace = "simplelunchboxes";
    private static SimpleLunchboxes plugin;
    private MiniMessage miniMessage;

    public static ItemStack lunchboxItem;
    public static ItemStack gluttonousLunchboxItem;
    public static ItemStack enderLunchboxItem;
    public static ItemStack gluttonousEnderLunchboxItem;

    @Override
    public void onEnable() {
        plugin = this;
        miniMessage = MiniMessage.miniMessage();
        this.getServer().getPluginManager().registerEvents(new LunchboxListeners(), this);
        this.getCommand("giveqolfood").setExecutor(new GiveQoLFoodCommand());
        constructQoLFood();
    }

    @Override
    public void onDisable() {
        LunchboxInventoryHandler.getInstance().closeAll();
    }

    public void constructQoLFood() {

        // Lunchbox
        lunchboxItem = new ItemStack(Material.MELON_SLICE);
        ItemMeta foodItemMeta = lunchboxItem.getItemMeta();
        foodItemMeta.getPersistentDataContainer().set(LunchboxListeners.lunchboxNsk, PersistentDataType.BOOLEAN, true);
        foodItemMeta.displayName(miniMessage.deserialize("Lunchbox"));
        foodItemMeta.setCustomModelData(100); // TODO: Custom Model Data Stuff
        lunchboxItem.setItemMeta(foodItemMeta);

        // Gluttonous Lunchbox
        gluttonousLunchboxItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta gluttonousFoodItemMeta = gluttonousLunchboxItem.getItemMeta();
        gluttonousFoodItemMeta.getPersistentDataContainer().set(LunchboxListeners.lunchboxNsk, PersistentDataType.BOOLEAN, true);
        gluttonousFoodItemMeta.displayName(miniMessage.deserialize("<aqua>Gluttonous Lunchbox</aqua>"));
        foodItemMeta.setCustomModelData(100); // TODO: Custom Model Data Stuff
        gluttonousLunchboxItem.setItemMeta(gluttonousFoodItemMeta);

        // Ender Lunchbox
        enderLunchboxItem = new ItemStack(Material.MELON_SLICE);
        ItemMeta enderLunchboxItemMeta = enderLunchboxItem.getItemMeta();
        enderLunchboxItemMeta.getPersistentDataContainer().set(LunchboxListeners.enderLunchboxNsk, PersistentDataType.BOOLEAN, true);
        enderLunchboxItemMeta.displayName(miniMessage.deserialize("Ender Lunchbox"));
        enderLunchboxItemMeta.setCustomModelData(101); // TODO: Custom Model Data Stuff
        enderLunchboxItem.setItemMeta(enderLunchboxItemMeta);

        // Gluttonous Ender Lunchbox
        gluttonousEnderLunchboxItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta enderGluttonousLunchboxItemMeta = gluttonousEnderLunchboxItem.getItemMeta();
        enderGluttonousLunchboxItemMeta.getPersistentDataContainer().set(LunchboxListeners.enderLunchboxNsk, PersistentDataType.BOOLEAN, true);
        enderGluttonousLunchboxItemMeta.displayName(miniMessage.deserialize("<aqua>Gluttonous Ender Lunchbox</aqua>"));
        enderGluttonousLunchboxItemMeta.setCustomModelData(101); // TODO: Custom Model Data Stuff
        gluttonousEnderLunchboxItem.setItemMeta(enderGluttonousLunchboxItemMeta);

        // TODO: Potion Sash
    }

    public static SimpleLunchboxes getPlugin() { return plugin; }
}
