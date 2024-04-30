package simplexity.simplelunchboxes.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public abstract class CustomInventory {

    public static final NamespacedKey uuidNsk = new NamespacedKey(SimpleLunchboxes.namespace, "uuid");

    protected final HashMap<UUID, Inventory> openInventories = new HashMap<>();

    protected final String fileName;
    protected final File dataFile;
    protected final FileConfiguration yml = new YamlConfiguration();
    protected final String inventoryName;

    protected CustomInventory(String fileName, String inventoryName) {
        this.fileName = fileName;
        this.dataFile = new File(SimpleLunchboxes.getPlugin().getDataFolder(), fileName);
        this.inventoryName = inventoryName;
        init();
    }

    private void init() {
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

    public abstract boolean openInventory(@Nullable ItemStack item, @NotNull Player player);

    protected Inventory loadInventory(UUID uuid) {
        if (openInventories.containsKey(uuid)) return openInventories.get(uuid);
        return createInventory(uuid);
    }

    protected Inventory createInventory(UUID uuid) {
        ConfigurationSection items = getInventoryItems(uuid);

        // TODO: Make name configurable.
        // TODO: Make Tier Slots configurable (ie Tier 1 has only 3 available slots).
        Inventory inv = Bukkit.createInventory(null, getInventoryTier(uuid)*9, Component.text(inventoryName));

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

    public void initializeInventory(@NotNull ItemStack customItem, int tier, @Nullable UUID existing_uuid) {
        ItemMeta meta = customItem.getItemMeta();
        UUID uuid = (existing_uuid == null ? UUID.randomUUID() : existing_uuid);
        meta.getPersistentDataContainer().set(uuidNsk, PersistentDataType.STRING, uuid.toString());
        customItem.setItemMeta(meta);
        if (tier > 6) tier = 6;
        if (tier < 1) tier = 1;
        setInventoryTier(uuid, tier);
    }

    protected ConfigurationSection getInventory(UUID uuid) {
        if (!yml.contains(uuid.toString())) {
            return yml.createSection(uuid.toString());
        }
        ConfigurationSection section = yml.getConfigurationSection(uuid.toString());
        assert section != null;
        return section;
    }

    protected ConfigurationSection getInventoryItems(UUID uuid) {
        ConfigurationSection section = getInventory(uuid);
        if (!section.contains("items")) {
            return section.createSection("items");
        }
        ConfigurationSection items = section.getConfigurationSection("items");
        assert items != null;
        return items;
    }

    protected int getInventoryTier(@NotNull UUID uuid) {
        ConfigurationSection section = getInventory(uuid);
        if (!section.contains("tier")) {
            section.set("tier", 1);
        }
        return section.getInt("tier");
    }

    protected void setInventoryTier(@NotNull UUID uuid, int tier) {
        ConfigurationSection section = getInventory(uuid);
        if (!section.contains("tier")) section.set("tier", tier);
        saveYml();
    }

    protected boolean upgradeInventoryTier(@NotNull UUID uuid) {
        if (getInventoryTier(uuid) >= 6) return false;
        if (openInventories.containsKey(uuid)) {
            Inventory inv = openInventories.get(uuid);
            inv.close();
            saveInventory(inv, uuid);
            openInventories.remove(uuid);
        }
        setInventoryTier(uuid, getInventoryTier(uuid) + 1);
        return true;
    }

    public void closeInventory(Inventory inv) {
        if (!isThisInventory(inv)) return;
        if (inv.getViewers().size() > 1) return;    // (?) Viewers == 1 when last person is closing the inventory.
        UUID uuid = null;
        for (UUID search : openInventories.keySet()) {
            if (openInventories.get(search).equals(inv)) {
                uuid = search;
                break;
            }
        }
        saveInventory(inv, uuid);
        openInventories.remove(uuid);
    }

    public boolean isThisInventory(Inventory inv) {
        return openInventories.containsValue(inv);
    }

    protected void saveInventory(Inventory inv, UUID uuid) {
        ConfigurationSection section = getInventory(uuid);
        section.set("items", null);
        ConfigurationSection items = getInventoryItems(uuid);
        int index = 0;
        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack == null || itemStack.getType().isEmpty()) {
                index++;
                continue;
            }
            items.set(Integer.toString(index++), itemStack);
        }
        saveYml();
    }

    protected void saveYml() {
        try { yml.save(dataFile); }
        catch (IOException e) { e.printStackTrace(); }
    }

    protected void reloadYml() {
        try { yml.load(dataFile); }
        catch (IOException | InvalidConfigurationException e) { e.printStackTrace(); }
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
