package simplexity.simplelunchboxes.inventory;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.item.PotionSashItem;
import simplexity.simplelunchboxes.util.Constants;
import simplexity.simplelunchboxes.util.CustomItemUtil;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
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
        if (item == null) return false;

        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        if (uuidString == null) {
            SimpleLunchboxes.getPlugin().getLogger().warning("Potion sash item missing UUID in persistent data.");
            return false;
        }
        UUID uuid = UUID.fromString(uuidString);

        Inventory inventory = loadInventory(uuid);
        player.openInventory(inventory);
        return true;
    }

    @Override
    protected Inventory createInventory(@NotNull UUID uuid) {
        Inventory inv = super.createInventory(uuid);
        int maxStack = ConfigHandler.getInstance().getPotionSashMaxStack();
        for (ItemStack item : inv.getContents()) {
            if (item != null && Constants.potionSashAllowedMaterials.contains(item.getType())) {
                item.setData(DataComponentTypes.MAX_STACK_SIZE, maxStack);
            }
        }
        return inv;
    }

    @Override
    protected void saveInventory(@NotNull Inventory inv, @NotNull UUID uuid) {
        // Strip MAX_STACK_SIZE from potions so they revert to vanilla behavior outside the sash
        for (ItemStack item : inv.getContents()) {
            if (item != null && Constants.potionSashAllowedMaterials.contains(item.getType())) {
                item.unsetData(DataComponentTypes.MAX_STACK_SIZE);
            }
        }
        super.saveInventory(inv, uuid);
    }

    public @Nullable ItemStack selectPotion(@NotNull UUID uuid) {
        if (openInventories.containsKey(uuid)) {
            Inventory inventory = openInventories.get(uuid);
            for (ItemStack item : inventory.getContents()) {
                if (item == null) continue;
                if (!Constants.potionSashDrinkableMaterials.contains(item.getType())) continue;
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
                if (!Constants.potionSashDrinkableMaterials.contains(item.getType())) continue;
                if (CustomItemUtil.isCustomItem(item)) continue;

                ItemStack returnItem = item.asOne();
                item.subtract();
                if (item.isEmpty()) item = null;
                items.set(key, item);
                saveYml();
                return returnItem;
            }
        }
        return null;
    }

    /**
     * Attempts to return the remainder item (e.g. glass bottle from a potion) to the sash after
     * consumption. If the sash is open, adds to the live inventory. Falls back to YAML if closed.
     * Drops at the player's location if no room is available.
     *
     * @param uuid      the sash UUID
     * @param player    the player who consumed the item
     * @param remainder the item to return, sourced from {@link io.papermc.paper.datacomponent.DataComponentTypes#USE_REMAINDER}
     */
    public void returnRemainder(@NotNull UUID uuid, @NotNull Player player, @Nullable ItemStack remainder) {
        if (remainder == null) return;
        if (openInventories.containsKey(uuid)) {
            Map<Integer, ItemStack> leftover = openInventories.get(uuid).addItem(remainder);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), remainder);
            }
            return;
        }
        // Sash is closed — work directly with YAML
        ConfigurationSection items = getInventoryItems(uuid);
        int size = 9 * getInventoryTier(uuid);
        Set<String> keys = items.getKeys(false);
        // Try to merge into an existing stack of the same item
        for (int i = 0; i < size; i++) {
            String k = Integer.toString(i);
            if (!keys.contains(k)) continue;
            ItemStack slot = items.getItemStack(k);
            if (slot == null || !slot.isSimilar(remainder)) continue;
            if (slot.getAmount() >= slot.getMaxStackSize()) continue;
            slot.setAmount(slot.getAmount() + 1);
            items.set(k, slot);
            saveYml();
            return;
        }
        // Try to find an empty slot
        for (int i = 0; i < size; i++) {
            String k = Integer.toString(i);
            if (keys.contains(k)) continue;
            items.set(k, remainder);
            saveYml();
            return;
        }
        // No room — drop at player's feet
        player.getWorld().dropItemNaturally(player.getLocation(), remainder);
    }
}
