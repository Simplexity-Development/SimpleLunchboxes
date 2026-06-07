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
        File parent = dataFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            SimpleLunchboxes.getPlugin().getLogger().severe("Failed to create data folder for: " + fileName);
            return;
        }
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                SimpleLunchboxes.getPlugin().getLogger().severe("Failed to create data file: " + fileName);
                SimpleLunchboxes.getPlugin().getLogger().severe(e.getMessage());
                return;
            }
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

        // TODO: Make Tier Slots configurable (ie Tier 1 has only 3 available slots).
        Inventory inv = Bukkit.createInventory(null, getInventoryTier(uuid)*9, Component.text(inventoryName));

        for (String key : items.getKeys(false)) {
            int position = Integer.parseInt(key);
            ItemStack itemStack = items.getItemStack(key);
            if (position >= inv.getSize()) {
                SimpleLunchboxes.getPlugin().getLogger().warning("Inventory (UUID: " + uuid + ") attempted to place an item in position " + position + " but the inventory is too small.");
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
        tier = Math.clamp(tier, 1, 6);
        setInventoryTier(uuid, tier);
    }

    protected @NotNull ConfigurationSection getInventory(@NotNull UUID uuid) {
        if (!yml.contains(uuid.toString())) {
            return yml.createSection(uuid.toString());
        }
        ConfigurationSection section = yml.getConfigurationSection(uuid.toString());
        if (section == null) {
            SimpleLunchboxes.getPlugin().getLogger().warning("Inventory section for UUID " + uuid + " was null; recreating.");
            return yml.createSection(uuid.toString());
        }
        return section;
    }

    protected @NotNull ConfigurationSection getInventoryItems(@NotNull UUID uuid) {
        ConfigurationSection section = getInventory(uuid);
        if (!section.contains("items")) {
            return section.createSection("items");
        }
        ConfigurationSection items = section.getConfigurationSection("items");
        if (items == null) {
            SimpleLunchboxes.getPlugin().getLogger().warning("Items section for UUID " + uuid + " was null; recreating.");
            return section.createSection("items");
        }
        return items;
    }

    public int getInventoryTier(@NotNull UUID uuid) {
        ConfigurationSection section = getInventory(uuid);
        if (!section.contains("tier")) {
            section.set("tier", 1);
        }
        return section.getInt("tier");
    }

    protected void setInventoryTier(@NotNull UUID uuid, int tier) {
        ConfigurationSection section = getInventory(uuid);
        section.set("tier", tier);
        saveYml();
    }

    public @Nullable UUID getUuidForInventory(@NotNull Inventory inv) {
        for (UUID search : openInventories.keySet()) {
            if (openInventories.get(search).equals(inv)) return search;
        }
        return null;
    }

    public void closeInventory(@NotNull Inventory inv) {
        if (!isThisInventory(inv)) return;
        if (inv.getViewers().size() > 1) return;    // (?) Viewers == 1 when last person is closing the inventory.
        UUID uuid = null;
        for (UUID search : openInventories.keySet()) {
            if (openInventories.get(search).equals(inv)) {
                uuid = search;
                break;
            }
        }
        if (uuid == null) return;
        saveInventory(inv, uuid);
        openInventories.remove(uuid);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isThisInventory(@NotNull Inventory inv) {
        return openInventories.containsValue(inv);
    }

    protected void saveInventory(@NotNull Inventory inv, @NotNull UUID uuid) {
        ConfigurationSection section = getInventory(uuid);
        section.set("items", null);
        ConfigurationSection items = getInventoryItems(uuid);
        int index = 0;
        for (ItemStack itemStack : inv.getContents()) {
            if (itemStack == null || itemStack.getType().isAir()) {
                index++;
                continue;
            }
            items.set(Integer.toString(index++), itemStack);
        }
        saveYml();
    }

    protected void saveYml() {
        try { yml.save(dataFile); }
        catch (IOException e) {
            SimpleLunchboxes.getPlugin().getLogger().severe("Failed to save " + fileName);
            SimpleLunchboxes.getPlugin().getLogger().severe(e.getMessage());
        }
    }

    protected void reloadYml() {
        try { yml.load(dataFile); }
        catch (IOException | InvalidConfigurationException e) {
            SimpleLunchboxes.getPlugin().getLogger().severe("Failed to load " + fileName);
            SimpleLunchboxes.getPlugin().getLogger().severe(e.getMessage());
        }
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
