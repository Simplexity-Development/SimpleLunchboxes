package simplexity.simplelunchboxes.inventory;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.util.CustomItemUtil;

import java.util.Set;
import java.util.UUID;

public class LunchboxInventory extends CustomInventory {

    private static LunchboxInventory instance;

    public static LunchboxInventory getInstance() {
        if (instance == null) instance = new LunchboxInventory();
        return instance;
    }

    private LunchboxInventory() {
        super("lunchboxes.yml", "Lunchbox");
    }

    @Override
    public boolean openInventory(@Nullable ItemStack item, @NotNull Player player) {
        if (!isLunchbox(item)) return false;
        assert item != null;

        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        assert uuidString != null;
        UUID uuid = UUID.fromString(uuidString);

        Inventory inventory = loadInventory(uuid);
        player.openInventory(inventory);
        return true;
    }

    public boolean isLunchbox(@Nullable ItemStack item) {
        if (item == null) return false;
        return EnderLunchboxItem.getInstance().isThisItem(item) || LunchboxItem.getInstance().isThisItem(item);
    }

    public @Nullable ItemStack selectLunchboxFood(@NotNull UUID uuid) {
        if (openInventories.containsKey(uuid)) {
            Inventory inventory = openInventories.get(uuid);
            for (ItemStack item : inventory.getContents()) {
                if (item == null) continue;
                if (!item.getType().isEdible()) continue;
                if (CustomItemUtil.isCustomItem(item)) continue;

                item.subtract();
                return item.asOne();
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
                if (!item.getType().isEdible()) continue;
                if (CustomItemUtil.isCustomItem(item)) continue;

                item.subtract();
                items.set(key, item);
                saveYml();
                return item.asOne();
            }
        }
        return null;
    }

    public @Nullable ItemStack selectEnderFood(@NotNull Player player) {
        for (ItemStack item : player.getEnderChest().getContents()) {
            if (item == null) continue;
            if (!item.getType().isEdible()) continue;
            if (CustomItemUtil.isCustomItem(item)) continue;

            item.subtract();
            return item.asOne();
        }
        return null;
    }

}
