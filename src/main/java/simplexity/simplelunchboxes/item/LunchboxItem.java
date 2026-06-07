package simplexity.simplelunchboxes.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.config.ItemConfig;
import simplexity.simplelunchboxes.config.LocaleHandler;
import simplexity.simplelunchboxes.config.LocaleMessage;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class LunchboxItem extends CustomItem {

    public static final NamespacedKey key = new NamespacedKey(SimpleLunchboxes.namespace, "lunchbox");
    public static final NamespacedKey uuidNsk = new NamespacedKey(SimpleLunchboxes.namespace, "uuid");

    private ItemStack lunchboxItem;
    private ItemStack gluttonousLunchboxItem;

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
        // initializeInventory calls setItemMeta which resets the DataComponent patch — re-apply
        lunchbox.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().build());
        String uuidStr = lunchbox.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        UUID actualUuid = uuidStr != null ? UUID.fromString(uuidStr) : null;
        ItemStack nextFood = actualUuid != null ? LunchboxInventory.getInstance().peekLunchboxFood(actualUuid) : null;
        lunchbox.setData(DataComponentTypes.FOOD, buildFoodFor(nextFood, gluttonous));
        if (actualUuid != null) {
            float fill = LunchboxInventory.getInstance().getFoodFill(actualUuid);
            ItemConfig config = gluttonous
                    ? ConfigHandler.getInstance().getGluttonousLunchboxConfig()
                    : ConfigHandler.getInstance().getLunchboxConfig();
            applyItemModel(lunchbox, config, fill);
        }
        return lunchbox;
    }

    @Override
    public void handleConsumption(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        if (uuidString == null) {
            event.setCancelled(true);
            return;
        }
        UUID uuid = UUID.fromString(uuidString);
        ItemStack food = LunchboxInventory.getInstance().selectLunchboxFood(uuid);
        if (food == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(LocaleHandler.getInstance().get(LocaleMessage.LUNCHBOX_EMPTY));
            return;
        }
        event.setItem(food);
        boolean gluttonous = item.getItemMeta().getPersistentDataContainer().has(gluttonousNsk);
        ItemStack nextFood = LunchboxInventory.getInstance().peekLunchboxFood(uuid);
        item.setData(DataComponentTypes.FOOD, buildFoodFor(nextFood, gluttonous));
        float fill = LunchboxInventory.getInstance().getFoodFill(uuid);
        ItemConfig config = gluttonous
                ? ConfigHandler.getInstance().getGluttonousLunchboxConfig()
                : ConfigHandler.getInstance().getLunchboxConfig();
        applyItemModel(item, config, fill);
        event.setReplacement(item);
        LunchboxInventory.getInstance().returnRemainder(uuid, event.getPlayer(), food);
    }

    @Override
    public boolean isThisItem(@Nullable ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key);
    }

    @Override
    public void constructItems() {
        ConfigHandler config = ConfigHandler.getInstance();
        ItemConfig lunchboxConfig = config.getLunchboxConfig();
        ItemConfig gluttonousConfig = config.getGluttonousLunchboxConfig();
        Consumable consumable = Consumable.consumable().build();

        // Lunchbox
        lunchboxItem = new ItemStack(Material.STICK);
        lunchboxItem.editMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true));
        applyItemConfig(lunchboxItem, lunchboxConfig);
        lunchboxItem.setData(DataComponentTypes.CONSUMABLE, consumable);
        lunchboxItem.setData(DataComponentTypes.FOOD, FoodProperties.food().canAlwaysEat(false).build());

        // Gluttonous Lunchbox
        gluttonousLunchboxItem = new ItemStack(Material.STICK);
        gluttonousLunchboxItem.editMeta(meta -> {
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(gluttonousNsk, PersistentDataType.BOOLEAN, true);
        });
        applyItemConfig(gluttonousLunchboxItem, gluttonousConfig);
        gluttonousLunchboxItem.setData(DataComponentTypes.CONSUMABLE, consumable);
        gluttonousLunchboxItem.setData(DataComponentTypes.FOOD, FoodProperties.food().canAlwaysEat(true).build());
    }
}
