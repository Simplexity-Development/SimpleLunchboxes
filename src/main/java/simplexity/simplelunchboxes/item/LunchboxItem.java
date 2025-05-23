package simplexity.simplelunchboxes.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;

import java.util.UUID;

public class LunchboxItem extends CustomItem {

    public static final NamespacedKey key = new NamespacedKey(SimpleLunchboxes.namespace, "lunchbox");
    public static final NamespacedKey uuidNsk = new NamespacedKey(SimpleLunchboxes.namespace, "uuid");

    private ItemStack lunchboxItem;
    private ItemStack gluttonousLunchboxItem;

    @SuppressWarnings({"UnstableApiUsage"})
    private Consumable consumable;
    @SuppressWarnings({"UnstableApiUsage"})
    private FoodProperties normalFoodProperties;
    @SuppressWarnings({"UnstableApiUsage"})
    private FoodProperties gluttonousFoodProperties;
    private Component lunchboxName;
    private Component gluttonousLunchboxName;

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
        return lunchbox;
    }

    @Override
    public void handleConsumption(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        String uuidString = item.getItemMeta().getPersistentDataContainer().get(uuidNsk, PersistentDataType.STRING);
        assert uuidString != null;
        ItemStack food = LunchboxInventory.getInstance().selectLunchboxFood(UUID.fromString(uuidString));
        if (food == null) {
            event.setCancelled(true);
            return;
        }
        event.setItem(food);
        event.setReplacement(item);
    }

    @Override
    public boolean isThisItem(@Nullable ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key);
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage", "DataFlowIssue"})
    public void constructItems() {
        constructDataComponents();

        // Lunchbox
        lunchboxItem = new ItemStack(Material.STICK);
        lunchboxItem.setData(DataComponentTypes.CONSUMABLE, consumable);
        lunchboxItem.setData(DataComponentTypes.FOOD, normalFoodProperties);
        lunchboxItem.setData(DataComponentTypes.CUSTOM_NAME, lunchboxName);
        lunchboxItem.setData(DataComponentTypes.ITEM_MODEL, Registry.ITEM.getKey(ItemType.BAMBOO_RAFT));
        lunchboxItem.editMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            // TODO: Custom Model Data Stuff
        });

        // Gluttonous Lunchbox
        gluttonousLunchboxItem = new ItemStack(Material.STICK);
        gluttonousLunchboxItem.setData(DataComponentTypes.CONSUMABLE, consumable);
        gluttonousLunchboxItem.setData(DataComponentTypes.FOOD, gluttonousFoodProperties);
        gluttonousLunchboxItem.setData(DataComponentTypes.CUSTOM_NAME, gluttonousLunchboxName);
        gluttonousLunchboxItem.setData(DataComponentTypes.ITEM_MODEL, Registry.ITEM.getKey(ItemType.BAMBOO_RAFT));
        gluttonousLunchboxItem.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        gluttonousLunchboxItem.editMeta(itemMeta -> {
            itemMeta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
            // TODO: Custom Model Data Stuff
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private void constructDataComponents() {
        consumable = Consumable.consumable().build();

        normalFoodProperties = FoodProperties.food().canAlwaysEat(false).build();
        lunchboxName = Component.text("Lunchbox", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false);

        gluttonousFoodProperties = FoodProperties.food().canAlwaysEat(true).build();
        gluttonousLunchboxName = Component.text("Gluttonous Lunchbox", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false);
    }
}
