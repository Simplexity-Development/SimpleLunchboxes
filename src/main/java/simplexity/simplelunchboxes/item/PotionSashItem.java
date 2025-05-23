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
import simplexity.simplelunchboxes.inventory.PotionSashInventory;

import java.util.UUID;

public class PotionSashItem extends CustomItem {

    public static final NamespacedKey key = new NamespacedKey(SimpleLunchboxes.namespace, "potion_sash");
    public static final NamespacedKey uuidNsk = new NamespacedKey(SimpleLunchboxes.namespace, "uuid");
    private static ItemStack potionSashItem;

    private static PotionSashItem instance;

    public static PotionSashItem getInstance() {
        if (instance == null) instance = new PotionSashItem();
        return instance;
    }

    private PotionSashItem() {
        constructItems();
    }

    public @NotNull ItemStack getPotionSashItem(int tier, @Nullable UUID uuid) {
        ItemStack potionSashItem = PotionSashItem.potionSashItem.asOne();
        LunchboxInventory.getInstance().initializeInventory(potionSashItem, tier, uuid);
        return potionSashItem;
    }


    @Override
    public void handleConsumption(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        assert uuidString != null;
        ItemStack potion = PotionSashInventory.getInstance().selectPotion(UUID.fromString(uuidString));
        if (potion == null) {
            event.setCancelled(true);
            return;
        }
        event.setItem(potion);
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
        // Potion Sash
        potionSashItem = new ItemStack(Material.POTION);
        ItemMeta potionItemMeta = potionSashItem.getItemMeta();
        potionItemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
        potionItemMeta.displayName(SimpleLunchboxes.getMiniMessage().deserialize("Potion Sash"));
        // TODO: Custom Model Data Stuff
        potionSashItem.setItemMeta(potionItemMeta);
    }
}
