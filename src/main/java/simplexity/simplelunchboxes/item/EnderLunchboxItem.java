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

@SuppressWarnings("UnstableApiUsage")
public class EnderLunchboxItem extends CustomItem {

    public static final NamespacedKey key = new NamespacedKey(SimpleLunchboxes.namespace, "ender_lunchbox");
    private static ItemStack enderLunchboxItem;
    private static ItemStack gluttonousEnderLunchboxItem;

    private static EnderLunchboxItem instance;

    public static EnderLunchboxItem getInstance() {
        if (instance == null) instance = new EnderLunchboxItem();
        return instance;
    }

    private EnderLunchboxItem() {
        constructItems();
    }

    public @NotNull ItemStack newItem(boolean gluttonous) {
        return (gluttonous ? gluttonousEnderLunchboxItem.asOne() : enderLunchboxItem.asOne());
    }

    @Override
    public void handleConsumption(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemStack food = LunchboxInventory.getInstance().selectEnderFood(event.getPlayer());
        if (food == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(LocaleHandler.getInstance().get(LocaleMessage.ENDER_LUNCHBOX_EMPTY));
            return;
        }
        event.setItem(food);
        boolean gluttonous = item.getItemMeta().getPersistentDataContainer().has(gluttonousNsk);
        ItemStack nextFood = LunchboxInventory.getInstance().peekEnderFood(event.getPlayer());
        item.setData(DataComponentTypes.FOOD, buildFoodFor(nextFood, gluttonous));
        float fill = LunchboxInventory.getInstance().getEnderFoodFill(event.getPlayer());
        ItemConfig config = gluttonous
                ? ConfigHandler.getInstance().getGluttonousEnderLunchboxConfig()
                : ConfigHandler.getInstance().getEnderLunchboxConfig();
        applyItemModel(item, config, fill);
        event.setReplacement(item);
        LunchboxInventory.getInstance().returnEnderRemainder(event.getPlayer(), food);
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
        ItemConfig enderConfig = config.getEnderLunchboxConfig();
        ItemConfig gluttonousConfig = config.getGluttonousEnderLunchboxConfig();
        Consumable consumable = Consumable.consumable().build();

        // Ender Lunchbox
        enderLunchboxItem = new ItemStack(Material.STICK);
        enderLunchboxItem.editMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true));
        applyItemConfig(enderLunchboxItem, enderConfig);
        enderLunchboxItem.setData(DataComponentTypes.CONSUMABLE, consumable);
        enderLunchboxItem.setData(DataComponentTypes.FOOD, FoodProperties.food().canAlwaysEat(false).build());

        // Gluttonous Ender Lunchbox
        gluttonousEnderLunchboxItem = new ItemStack(Material.STICK);
        gluttonousEnderLunchboxItem.editMeta(meta -> {
            meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(gluttonousNsk, PersistentDataType.BOOLEAN, true);
        });
        applyItemConfig(gluttonousEnderLunchboxItem, gluttonousConfig);
        gluttonousEnderLunchboxItem.setData(DataComponentTypes.CONSUMABLE, consumable);
        gluttonousEnderLunchboxItem.setData(DataComponentTypes.FOOD, FoodProperties.food().canAlwaysEat(true).build());
    }
}
