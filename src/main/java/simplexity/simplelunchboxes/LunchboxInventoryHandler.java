package simplexity.simplelunchboxes;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

// TODO: Divide up class into more smaller and specialized classes (ie: LunchboxHandler and LunchboxInventoryHandler).
public class LunchboxInventoryHandler {

    public static final NamespacedKey uuidNsk = new NamespacedKey(SimpleLunchboxes.namespace, "uuid");

    private final HashMap<UUID, Inventory> openInventories = new HashMap<>();

    private final String fileName = "lunchboxes.yml";
    private final File dataFile = new File(SimpleLunchboxes.getPlugin().getDataFolder(), fileName);
    private final FileConfiguration yml = new YamlConfiguration();

    public static LunchboxInventoryHandler instance;

    private LunchboxInventoryHandler() {
        try {
            if (!dataFile.exists()) {
                SimpleLunchboxes.getPlugin().getDataFolder().mkdir();
                dataFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadYml();
    }

    public static LunchboxInventoryHandler getInstance() {
        if (instance == null) instance = new LunchboxInventoryHandler();
        return instance;
    }

    public boolean openInventory(@Nullable ItemStack lunchbox, @NotNull Player player) {
        if (!isLunchbox(lunchbox)) return false;
        assert lunchbox != null;

        String uuidString = lunchbox.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        assert uuidString != null;
        UUID uuid = UUID.fromString(uuidString);

        Inventory inventory = loadInventory(uuid);
        player.openInventory(inventory);
        return true;
    }

    private Inventory loadInventory(UUID uuid) {
        if (openInventories.containsKey(uuid)) return openInventories.get(uuid);
        return createInventory(uuid);
    }

    private Inventory createInventory(UUID uuid) {
        ConfigurationSection items = getLunchboxItems(uuid);

        // TODO: Make name configurable.
        // TODO: Make Tier Slots configurable (ie Tier 1 has only 3 available slots).
        Inventory inv = Bukkit.createInventory(null, getLunchboxTier(uuid)*9, Component.text("Lunchbox"));

        for (String key : items.getKeys(false)) {
            int position = Integer.parseInt(key);
            ItemStack itemStack = items.getItemStack(key);
            if (position >= inv.getSize()) {
                SimpleLunchboxes.getPlugin().getLogger().warning("Lunchbox (UUID: " + uuid + ") attempted to place an item stack in position " + position + " but the lunchbox is too small.");
                continue;
            }
            inv.setItem(position, itemStack);
        }

        openInventories.put(uuid, inv);
        return inv;
    }

    public ItemStack newLunchbox(int tier) {
        ItemStack lunchbox = SimpleLunchboxes.lunchboxItem.asOne();
        initializeLunchbox(lunchbox, tier);
        return lunchbox;
    }

    public ItemStack newGluttonousLunchbox(int tier) {
        ItemStack lunchbox = SimpleLunchboxes.gluttonousLunchboxItem.asOne();
        initializeLunchbox(lunchbox, tier);
        return lunchbox;
    }

    public ItemStack newEnderLunchbox() {
        return SimpleLunchboxes.enderLunchboxItem.asOne();
    }

    public ItemStack newGluttonousEnderLunchbox() {
        return SimpleLunchboxes.gluttonousEnderLunchboxItem.asOne();
    }

    private void initializeLunchbox(ItemStack lunchbox, int tier) {
        ItemMeta meta = lunchbox.getItemMeta();
        UUID uuid = UUID.randomUUID();
        meta.getPersistentDataContainer().set(uuidNsk, PersistentDataType.STRING, uuid.toString());
        lunchbox.setItemMeta(meta);
        if (tier > 6) tier = 6;
        if (tier < 1) tier = 1;
        setLunchboxTier(uuid, tier);
    }

    public boolean isCustomItem(@Nullable ItemStack item) {
        if (item == null) return false;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        return container.has(LunchboxListeners.lunchboxNsk) ||
                container.has(LunchboxListeners.enderLunchboxNsk);
    }

    public boolean isLunchbox(@Nullable ItemStack item) {
        if (item == null) return false;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        return container.has(LunchboxListeners.lunchboxNsk);
    }

    public boolean isEnderLunchbox(@Nullable ItemStack item) {
        if (item == null) return false;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        return container.has(LunchboxListeners.enderLunchboxNsk);
    }

    public void handleEating(@NotNull PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (isLunchbox(item)) handleEatingLunchbox(event);
        if (isEnderLunchbox(item)) handleEatingEnderLunchbox(event);
    }

    private void handleEatingLunchbox(@NotNull PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        assert uuidString != null;
        ItemStack food = selectLunchboxFood(UUID.fromString(uuidString));
        if (food == null) {
            event.setCancelled(true);
            return;
        }
        event.setItem(food);
        event.setReplacement(item);
    }

    private void handleEatingEnderLunchbox(@NotNull PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemStack food = selectEnderFood(event.getPlayer());
        if (food == null) {
            event.setCancelled(true);
            return;
        }
        event.setItem(food);
        event.setReplacement(item);
    }

    private @Nullable ItemStack selectLunchboxFood(@NotNull UUID uuid) {
        if (openInventories.containsKey(uuid)) {
            Inventory inventory = openInventories.get(uuid);
            for (ItemStack item : inventory.getContents()) {
                if (item == null) continue;
                if (!item.getType().isEdible()) continue;
                if (isCustomItem(item)) continue;

                item.subtract();
                return item.asOne();
            }
        }
        else {
            ConfigurationSection items = getLunchboxItems(uuid);
            int size = 9*getLunchboxTier(uuid);
            Set<String> keys = items.getKeys(false);
            for (int i = 0; i < size; i++) {
                String key = Integer.toString(i);
                if (!keys.contains(key)) continue;

                ItemStack item = items.getItemStack(key);
                if (item == null) continue;
                if (!item.getType().isEdible()) continue;
                if (isCustomItem(item)) continue;

                item.subtract();
                items.set(key, item);
                saveYml();
                return item.asOne();
            }
        }
        return null;
    }

    private @Nullable ItemStack selectEnderFood(@NotNull Player player) {
        for (ItemStack item : player.getEnderChest().getContents()) {
            if (item == null) continue;
            if (!item.getType().isEdible()) continue;
            if (isCustomItem(item)) continue;

            item.subtract();
            return item.asOne();
        }
        return null;
    }

    public boolean isLunchboxInventory(Inventory inv) {
        return openInventories.containsValue(inv);
    }

    private @NotNull ConfigurationSection getLunchbox(@NotNull UUID uuid) {
        if (!yml.contains(uuid.toString())) {
            return yml.createSection(uuid.toString());
        }
        ConfigurationSection section = yml.getConfigurationSection(uuid.toString());
        assert section != null;
        return section;
    }

    private @NotNull ConfigurationSection getLunchboxItems(@NotNull UUID uuid) {
        ConfigurationSection section = getLunchbox(uuid);
        if (!section.contains("items")) {
            return section.createSection("items");
        }
        ConfigurationSection items = section.getConfigurationSection("items");
        assert items != null;
        return items;
    }

    private int getLunchboxTier(@NotNull UUID uuid) {
        ConfigurationSection section = getLunchbox(uuid);
        if (!section.contains("tier")) section.set("tier", 1);
        return section.getInt("tier");
    }

    private void setLunchboxTier(@NotNull UUID uuid, int tier) {
        ConfigurationSection section = getLunchbox(uuid);
        if (!section.contains("tier")) section.set("tier", tier);
    }

    private boolean upgradeLunchboxTier(@NotNull UUID uuid) {
        if (getLunchboxTier(uuid) > 6) return false;
        if (openInventories.containsKey(uuid)) {
            Inventory inv = openInventories.get(uuid);
            inv.close();
            saveInventory(inv, uuid);
            openInventories.remove(uuid);
        }
        setLunchboxTier(uuid, getLunchboxTier(uuid) + 1);
        return true;
    }

    public void saveInventory(Inventory inv, UUID uuid) {
        ConfigurationSection section = getLunchbox(uuid);
        section.set("items", null);
        ConfigurationSection items = getLunchboxItems(uuid);
        int index = 0;
        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack == null) {
                index++;
                continue;
            }
            items.set(Integer.toString(index++), itemStack);
        }
        saveYml();
    }

    public void saveYml() {
        try { yml.save(dataFile); }
        catch (IOException e) { e.printStackTrace(); }
    }

    public void reloadYml() {
        try { yml.load(dataFile); }
        catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
    }

    public void closeLunchbox(Inventory inv) {
        if (!isLunchboxInventory(inv)) return;
        if (inv.getViewers().size() > 1) return;    // (?) Viewers == 1 when last person is closing the inventory.
        UUID uuid = null;
        for (UUID search : openInventories.keySet()) {
            if (openInventories.get(search).equals(inv)) {
                uuid = search;
                break;
            }
        }
        saveInventory(inv, uuid);
    }

    public void closeAll() {
        for (UUID uuid : openInventories.keySet()) {
            Inventory inv = openInventories.get(uuid);
            inv.close();
            saveInventory(inv, uuid);
        }
        openInventories.clear();
    }

}
