package simplexity.simplelunchboxes.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;

public class EnderLunchboxItem extends CustomItem {

    public static final NamespacedKey key = new NamespacedKey(SimpleLunchboxes.namespace, "ender_lunchbox");
    private static ItemStack enderLunchboxItem;
    private static ItemStack gluttonousEnderLunchboxItem;

    private static EnderLunchboxItem instance;

    public static EnderLunchboxItem getInstance() {
        if (instance == null) instance = new EnderLunchboxItem();
        return instance;
    }

    private EnderLunchboxItem() {}

    public ItemStack newItem(boolean gluttonous) {
        return (gluttonous ? gluttonousEnderLunchboxItem.asOne() : enderLunchboxItem.asOne());
    }

    @Override
    public void handleConsumption(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemStack food = LunchboxInventory.getInstance().selectEnderFood(event.getPlayer());
        if (food == null) {
            event.setCancelled(true);
            return;
        }
        event.setItem(food);
        event.setReplacement(item);
    }

    @Override
    public boolean isThisItem(@Nullable ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key);
    }

    @Override
    public void constructItems() {
        // Ender Lunchbox
        enderLunchboxItem = new ItemStack(Material.MELON_SLICE);
        ItemMeta enderLunchboxItemMeta = enderLunchboxItem.getItemMeta();
        enderLunchboxItemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        enderLunchboxItemMeta.displayName(SimpleLunchboxes.getMiniMessage().deserialize("Ender Lunchbox"));
        enderLunchboxItemMeta.setCustomModelData(101); // TODO: Custom Model Data Stuff
        enderLunchboxItem.setItemMeta(enderLunchboxItemMeta);

        // Gluttonous Ender Lunchbox
        gluttonousEnderLunchboxItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta enderGluttonousLunchboxItemMeta = gluttonousEnderLunchboxItem.getItemMeta();
        enderGluttonousLunchboxItemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        enderGluttonousLunchboxItemMeta.displayName(SimpleLunchboxes.getMiniMessage().deserialize("<aqua>Gluttonous Ender Lunchbox</aqua>"));
        enderGluttonousLunchboxItemMeta.setCustomModelData(101); // TODO: Custom Model Data Stuff
        gluttonousEnderLunchboxItem.setItemMeta(enderGluttonousLunchboxItemMeta);
    }
}
