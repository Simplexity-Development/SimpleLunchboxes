package simplexity.simplelunchboxes.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.config.ItemConfig;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public abstract class CustomItem {

    public static final NamespacedKey gluttonousNsk = new NamespacedKey(SimpleLunchboxes.namespace, "gluttonous");

    public abstract void handleConsumption(PlayerItemConsumeEvent event);

    public abstract boolean isThisItem(@Nullable ItemStack item);

    public abstract void constructItems();

    /**
     * Applies all configurable display properties (name, lore, model, glint) from an
     * {@link ItemConfig} onto an existing item stack.
     *
     * @param item   the item to update
     * @param config the config to read from
     * @param fill   comparator-style fill fraction {@code [0, 1]} used to select the model state
     */
    public static void applyItemConfig(@NotNull ItemStack item, @NotNull ItemConfig config, float fill) {
        // editMeta resets the DataComponent patch, wiping any setData overrides. Save and restore them.
        Consumable consumable = item.getData(DataComponentTypes.CONSUMABLE);
        FoodProperties food = item.getData(DataComponentTypes.FOOD);

        List<Component> lore = config.lore().stream()
                .map(line -> SimpleLunchboxes.getMiniMessage().deserialize(line))
                .toList();
        String modelKey = config.itemModel().forFill(fill);
        item.editMeta(meta -> {
            meta.customName(SimpleLunchboxes.getMiniMessage().deserialize(config.displayName()));
            meta.lore(lore.isEmpty() ? null : lore);
            meta.setItemModel(modelKey.isBlank() ? null : NamespacedKey.fromString(modelKey));
            meta.setEnchantmentGlintOverride(config.glint() ? true : null);
        });

        if (consumable != null) item.setData(DataComponentTypes.CONSUMABLE, consumable);
        if (food != null) item.setData(DataComponentTypes.FOOD, food);
    }

    /**
     * Overload for use during {@code constructItems()} where fill is unknown.
     * Defaults to fill {@code 0} (empty state) so template items show the empty model.
     */
    public static void applyItemConfig(@NotNull ItemStack item, @NotNull ItemConfig config) {
        applyItemConfig(item, config, 0f);
    }

    /**
     * Updates only the {@code ITEM_MODEL} component based on the current fill fraction.
     * Lighter-weight than {@link #applyItemConfig} — use this after eat/close events
     * where only the model needs refreshing, not the full display.
     *
     * @param item   the item to update
     * @param config the config providing model keys
     * @param fill   comparator-style fill fraction {@code [0, 1]}
     */
    public static void applyItemModel(@NotNull ItemStack item, @NotNull ItemConfig config, float fill) {
        Consumable consumable = item.getData(DataComponentTypes.CONSUMABLE);
        FoodProperties food = item.getData(DataComponentTypes.FOOD);
        String modelKey = config.itemModel().forFill(fill);
        item.editMeta(meta -> meta.setItemModel(modelKey.isBlank() ? null : NamespacedKey.fromString(modelKey)));
        if (consumable != null) item.setData(DataComponentTypes.CONSUMABLE, consumable);
        if (food != null) item.setData(DataComponentTypes.FOOD, food);
    }

    /**
     * Builds a {@link FoodProperties} for use as the lunchbox tooltip, reflecting
     * the nutrition and saturation of the next food that will be dispensed.
     *
     * @param nextFood     the next food item, or {@code null} if the lunchbox is empty
     * @param canAlwaysEat whether the lunchbox variant can always eat (gluttony)
     * @return a {@link FoodProperties} instance
     */
    public static @NotNull FoodProperties buildFoodFor(@Nullable ItemStack nextFood, boolean canAlwaysEat) {
        FoodProperties.Builder builder = FoodProperties.food();
        if (nextFood != null) {
            FoodProperties nextProps = nextFood.getData(DataComponentTypes.FOOD);
            if (nextProps != null) {
                builder.nutrition(nextProps.nutrition()).saturation(nextProps.saturation());
                canAlwaysEat = canAlwaysEat || nextProps.canAlwaysEat();
            }
        }
        return builder.canAlwaysEat(canAlwaysEat).build();
    }
}
