package simplexity.simplelunchboxes.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;

import java.util.UUID;

public class LunchboxItem extends CustomItem {

    public static final NamespacedKey key = new NamespacedKey(SimpleLunchboxes.namespace, "lunchbox");
    public static final NamespacedKey uuidNsk = new NamespacedKey(SimpleLunchboxes.namespace, "uuid");
    private static ItemStack lunchboxItem;
    private static ItemStack gluttonousLunchboxItem;

    private static LunchboxItem instance;

    public static LunchboxItem getInstance() {
        if (instance == null) instance = new LunchboxItem();
        return instance;
    }

    private LunchboxItem() {
        constructItems();
    }

    public @NotNull ItemStack getLunchboxItem(int tier, boolean gluttonous, @Nullable UUID uuid) {
        ItemStack lunchbox = (gluttonous ? gluttonousLunchboxItem.asOne() : lunchboxItem.asOne());
        LunchboxInventory.getInstance().initializeInventory(lunchbox, tier, uuid);
        return lunchbox;
    }

    @Override
    public void handleConsumption(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        assert uuidString != null;
        ItemStack food = LunchboxInventory.getInstance().selectLunchboxFood(UUID.fromString(uuidString));
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
        // Lunchbox
        lunchboxItem = new ItemStack(Material.MELON_SLICE);
        ItemMeta foodItemMeta = lunchboxItem.getItemMeta();
        foodItemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        foodItemMeta.displayName(SimpleLunchboxes.getMiniMessage().deserialize("Lunchbox"));
        foodItemMeta.setCustomModelData(100); // TODO: Custom Model Data Stuff
        lunchboxItem.setItemMeta(foodItemMeta);

        // Gluttonous Lunchbox
        gluttonousLunchboxItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta gluttonousFoodItemMeta = gluttonousLunchboxItem.getItemMeta();
        gluttonousFoodItemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        gluttonousFoodItemMeta.displayName(SimpleLunchboxes.getMiniMessage().deserialize("<aqua>Gluttonous Lunchbox</aqua>"));
        foodItemMeta.setCustomModelData(100); // TODO: Custom Model Data Stuff
        gluttonousLunchboxItem.setItemMeta(gluttonousFoodItemMeta);
    }
}
