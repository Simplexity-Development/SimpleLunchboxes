package simplexity.simplelunchboxes.inventory;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseRemainder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.util.CustomItemUtil;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
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
        if (!LunchboxItem.getInstance().isThisItem(item)) return false;
        if (item == null) return false;

        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        if (uuidString == null) {
            SimpleLunchboxes.getPlugin().getLogger().warning("Lunchbox item missing UUID in persistent data.");
            return false;
        }
        UUID uuid = UUID.fromString(uuidString);

        Inventory inventory = loadInventory(uuid);
        player.openInventory(inventory);
        return true;
    }

    public @Nullable ItemStack selectLunchboxFood(@NotNull UUID uuid) {
        if (openInventories.containsKey(uuid)) {
            Inventory inventory = openInventories.get(uuid);
            for (ItemStack item : inventory.getContents()) {
                if (item == null) continue;
                if (!item.getType().isEdible()) continue;
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
                if (!item.getType().isEdible()) continue;
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

    public @Nullable ItemStack selectEnderFood(@NotNull Player player) {
        for (ItemStack item : player.getEnderChest().getContents()) {
            if (item == null) continue;
            if (!item.getType().isEdible()) continue;
            if (CustomItemUtil.isCustomItem(item)) continue;

            ItemStack enderReturnItem = item.asOne();
            item.subtract();
            return enderReturnItem;
        }
        return null;
    }

    /**
     * Returns the next food item that would be dispensed from this lunchbox without consuming it.
     * Used to update the lunchbox's FOOD tooltip after each eat.
     *
     * @param uuid the lunchbox UUID
     * @return the next food item, or {@code null} if the lunchbox is empty
     */
    public @Nullable ItemStack peekLunchboxFood(@NotNull UUID uuid) {
        if (openInventories.containsKey(uuid)) {
            for (ItemStack item : openInventories.get(uuid).getContents()) {
                if (item == null) continue;
                if (!item.getType().isEdible()) continue;
                if (CustomItemUtil.isCustomItem(item)) continue;
                return item.asOne();
            }
        } else {
            ConfigurationSection items = getInventoryItems(uuid);
            int size = 9 * getInventoryTier(uuid);
            Set<String> keys = items.getKeys(false);
            for (int i = 0; i < size; i++) {
                String key = Integer.toString(i);
                if (!keys.contains(key)) continue;
                ItemStack item = items.getItemStack(key);
                if (item == null) continue;
                if (!item.getType().isEdible()) continue;
                if (CustomItemUtil.isCustomItem(item)) continue;
                return item.asOne();
            }
        }
        return null;
    }

    /**
     * Returns the next food item that would be dispensed from a player's ender chest without consuming it.
     *
     * @param player the player whose ender chest to peek
     * @return the next food item, or {@code null} if the ender chest has no food
     */
    public @Nullable ItemStack peekEnderFood(@NotNull Player player) {
        for (ItemStack item : player.getEnderChest().getContents()) {
            if (item == null) continue;
            if (!item.getType().isEdible()) continue;
            if (CustomItemUtil.isCustomItem(item)) continue;
            return item.asOne();
        }
        return null;
    }

    /**
     * Returns the remainder item (e.g. bowl from stew) back into the lunchbox after consumption.
     * If the lunchbox is open, adds to the live inventory. Falls back to YAML if closed.
     * Drops at the player's location if no room is available.
     * Does nothing if the consumed item has no {@link DataComponentTypes#USE_REMAINDER}.
     *
     * @param uuid         the lunchbox UUID
     * @param player       the player who ate the food
     * @param consumedFood the food item that was consumed
     */
    public void returnRemainder(@NotNull UUID uuid, @NotNull Player player, @NotNull ItemStack consumedFood) {
        UseRemainder useRemainder = consumedFood.getData(DataComponentTypes.USE_REMAINDER);
        if (useRemainder == null) return;
        ItemStack remainder = useRemainder.transformInto();
        if (openInventories.containsKey(uuid)) {
            Map<Integer, ItemStack> leftover = openInventories.get(uuid).addItem(remainder);
            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), remainder);
            }
            return;
        }
        ConfigurationSection items = getInventoryItems(uuid);
        int size = 9 * getInventoryTier(uuid);
        Set<String> keys = items.getKeys(false);
        for (int i = 0; i < size; i++) {
            String key = Integer.toString(i);
            if (!keys.contains(key)) continue;
            ItemStack slot = items.getItemStack(key);
            if (slot == null || !slot.isSimilar(remainder)) continue;
            if (slot.getAmount() >= slot.getMaxStackSize()) continue;
            slot.setAmount(slot.getAmount() + 1);
            items.set(key, slot);
            saveYml();
            return;
        }
        for (int i = 0; i < size; i++) {
            String key = Integer.toString(i);
            if (keys.contains(key)) continue;
            items.set(key, remainder);
            saveYml();
            return;
        }
        player.getWorld().dropItemNaturally(player.getLocation(), remainder);
    }

    /**
     * Returns the remainder item back into the player's ender chest after consuming food from an
     * ender lunchbox. Drops at the player's location if the ender chest is full.
     * Does nothing if the consumed item has no {@link DataComponentTypes#USE_REMAINDER}.
     *
     * @param player       the player who ate the food
     * @param consumedFood the food item that was consumed
     */
    public void returnEnderRemainder(@NotNull Player player, @NotNull ItemStack consumedFood) {
        UseRemainder useRemainder = consumedFood.getData(DataComponentTypes.USE_REMAINDER);
        if (useRemainder == null) return;
        ItemStack remainder = useRemainder.transformInto();
        Map<Integer, ItemStack> leftover = player.getEnderChest().addItem(remainder);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), remainder);
        }
    }

    /**
     * Computes the fill fraction of a lunchbox inventory using the same formula as a comparator:
     * {@code sum(count / maxStackSize) / totalSlots}.
     *
     * @param uuid the lunchbox UUID
     * @return fill fraction in {@code [0, 1]}
     */
    public float getFoodFill(@NotNull UUID uuid) {
        int totalSlots = 9 * getInventoryTier(uuid);
        float sum = 0f;
        if (openInventories.containsKey(uuid)) {
            for (ItemStack item : openInventories.get(uuid).getContents()) {
                if (item == null || !item.getType().isEdible() || CustomItemUtil.isCustomItem(item)) continue;
                sum += (float) item.getAmount() / item.getType().getMaxStackSize();
            }
        } else {
            ConfigurationSection items = getInventoryItems(uuid);
            Set<String> keys = items.getKeys(false);
            for (int i = 0; i < totalSlots; i++) {
                String key = Integer.toString(i);
                if (!keys.contains(key)) continue;
                ItemStack item = items.getItemStack(key);
                if (item == null || !item.getType().isEdible() || CustomItemUtil.isCustomItem(item)) continue;
                sum += (float) item.getAmount() / item.getType().getMaxStackSize();
            }
        }
        return sum / totalSlots;
    }

    /**
     * Computes the fill fraction of food in a player's ender chest using the same formula as a
     * comparator: {@code sum(count / maxStackSize for food slots) / 27}.
     *
     * @param player the player whose ender chest to measure
     * @return fill fraction in {@code [0, 1]}
     */
    public float getEnderFoodFill(@NotNull Player player) {
        float sum = 0f;
        for (ItemStack item : player.getEnderChest().getContents()) {
            if (item == null || !item.getType().isEdible() || CustomItemUtil.isCustomItem(item)) continue;
            sum += (float) item.getAmount() / item.getType().getMaxStackSize();
        }
        return sum / 27f;
    }

}
