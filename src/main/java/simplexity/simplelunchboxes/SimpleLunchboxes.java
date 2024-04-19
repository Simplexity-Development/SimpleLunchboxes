package simplexity.simplelunchboxes;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleLunchboxes extends JavaPlugin {

    public static final String namespace = "simplelunchboxes";
    public static ItemStack foodItem;
    public static ItemStack gluttonousFoodItem;
    private static SimpleLunchboxes plugin;
    private MiniMessage miniMessage;

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
        foodItem = new ItemStack(Material.MELON_SLICE);
        ItemMeta foodItemMeta = foodItem.getItemMeta();
        foodItemMeta.getPersistentDataContainer().set(LunchboxListeners.lunchboxNsk, PersistentDataType.BOOLEAN, true);
        foodItemMeta.displayName(miniMessage.deserialize("Lunchbox"));
        foodItemMeta.setCustomModelData(100); // TODO: Custom Model Data Stuff
        foodItem.setItemMeta(foodItemMeta);

        // Gluttonous Lunchbox
        gluttonousFoodItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta gluttonousFoodItemMeta = gluttonousFoodItem.getItemMeta();
        gluttonousFoodItemMeta.getPersistentDataContainer().set(LunchboxListeners.lunchboxNsk, PersistentDataType.BOOLEAN, true);
        gluttonousFoodItemMeta.displayName(miniMessage.deserialize("<aqua>Gluttonous Lunchbox</aqua>"));
        foodItemMeta.setCustomModelData(100); // TODO: Custom Model Data Stuff
        gluttonousFoodItem.setItemMeta(gluttonousFoodItemMeta);
    }

    public static SimpleLunchboxes getPlugin() { return plugin; }
}
