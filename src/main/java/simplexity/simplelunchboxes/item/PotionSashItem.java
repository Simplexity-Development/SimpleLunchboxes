package simplexity.simplelunchboxes.item;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.UseRemainder;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.config.LocaleHandler;
import simplexity.simplelunchboxes.config.LocaleMessage;
import simplexity.simplelunchboxes.inventory.PotionSashInventory;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class PotionSashItem extends CustomItem {

    public static final NamespacedKey key = new NamespacedKey(SimpleLunchboxes.namespace, "potion_sash");
    public static final NamespacedKey uuidNsk = new NamespacedKey(SimpleLunchboxes.namespace, "uuid");
    private static ItemStack potionSashItem;

    private static PotionSashItem instance;

    public static PotionSashItem getInstance() {
        if (instance == null) instance = new PotionSashItem();
        return instance;
    }

    private PotionSashItem() {
        constructItems();
    }

    public @NotNull ItemStack getPotionSashItem(int tier, @Nullable UUID uuid) {
        ItemStack potionSash = PotionSashItem.potionSashItem.asOne();
        PotionSashInventory.getInstance().initializeInventory(potionSash, tier, uuid);
        // initializeInventory calls setItemMeta which resets the DataComponent patch — re-apply
        potionSash.setData(DataComponentTypes.CONSUMABLE, drinkConsumable());
        return potionSash;
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
        ItemStack potion = PotionSashInventory.getInstance().selectPotion(uuid);
        if (potion == null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(LocaleHandler.getInstance().get(LocaleMessage.POTION_SASH_EMPTY));
            return;
        }
        UseRemainder useRemainder = potion.getData(DataComponentTypes.USE_REMAINDER);
        ItemStack remainder = useRemainder != null ? useRemainder.transformInto() : null;
        event.setItem(potion);
        event.setReplacement(item);
        PotionSashInventory.getInstance().returnRemainder(uuid, event.getPlayer(), remainder);
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

        potionSashItem = new ItemStack(Material.STICK);
        potionSashItem.editMeta(meta -> meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true));
        applyItemConfig(potionSashItem, config.getPotionSashConfig());
        potionSashItem.setData(DataComponentTypes.CONSUMABLE, drinkConsumable());
    }

    /** Builds a {@link Consumable} that plays the drinking animation and sound. */
    public static @NotNull Consumable drinkConsumable() {
        return Consumable.consumable()
                .animation(ItemUseAnimation.DRINK)
                .sound(Key.key("entity.generic.drink"))
                .build();
    }
}
