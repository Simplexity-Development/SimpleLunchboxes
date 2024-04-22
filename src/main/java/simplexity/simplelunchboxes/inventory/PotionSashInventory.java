package simplexity.simplelunchboxes.inventory;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.item.PotionSashItem;
import simplexity.simplelunchboxes.util.CustomItemUtil;

import java.util.Set;
import java.util.UUID;

public class PotionSashInventory extends CustomInventory {

    private static PotionSashInventory instance;

    public static PotionSashInventory getInstance() {
        if (instance == null) instance = new PotionSashInventory();
        return instance;
    }

    private PotionSashInventory() {
        super("potion_sashes.yml", "Potion Sash");
    }

    @Override
    public boolean openInventory(@Nullable ItemStack item, @NotNull Player player) {
        if (!PotionSashItem.getInstance().isThisItem(item)) return false;
        assert item != null;

        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        assert uuidString != null;
        UUID uuid = UUID.fromString(uuidString);

        Inventory inventory = loadInventory(uuid);
        player.openInventory(inventory);
        return true;
    }

    public @Nullable ItemStack selectPotion(@NotNull UUID uuid) {
        if (openInventories.containsKey(uuid)) {
            Inventory inventory = openInventories.get(uuid);
            for (ItemStack item : inventory.getContents()) {
                if (item == null) continue;
                if (item.getType() != Material.POTION) continue;
                if (CustomItemUtil.isCustomItem(item)) continue;

                ItemStack returnItem = item.asOne();
                item.subtract();
                return returnItem;
            }
        }
        else {
            ConfigurationSection items = getInventoryItems(uuid);
            int size = 9*getInventoryTier(uuid);
            Set<String> keys = items.getKeys(false);
            for (int i = 0; i < size; i++) {
                String key = Integer.toString(i);
                if (!keys.contains(key)) continue;

                ItemStack item = items.getItemStack(key);
                if (item == null) continue;
                if (item.getType() != Material.POTION) continue;
                if (CustomItemUtil.isCustomItem(item)) continue;

                ItemStack returnItem = item.asOne();
                item.subtract();
                items.set(key, item);
                saveYml();
                return returnItem;
            }
        }
        return null;
    }
}
