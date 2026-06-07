package simplexity.simplelunchboxes.listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Player;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.config.ItemConfig;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;
import simplexity.simplelunchboxes.item.CustomItem;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.util.Constants;
import simplexity.simplelunchboxes.util.CustomItemUtil;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class LunchboxListeners implements Listener {

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (CustomItemUtil.isCustomItem(ingredient)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (LunchboxItem.getInstance().isThisItem(item)) LunchboxItem.getInstance().handleConsumption(event);
        if (EnderLunchboxItem.getInstance().isThisItem(item)) EnderLunchboxItem.getInstance().handleConsumption(event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getPlayer().isSneaking()) return;
        ItemStack item = event.getItem();
        if (!LunchboxItem.getInstance().isThisItem(item) && !EnderLunchboxItem.getInstance().isThisItem(item)) return;
        event.setUseInteractedBlock(Result.DENY);
    }

    @EventHandler
    public void onSneakUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;
        boolean success = LunchboxInventory.getInstance().openInventory(event.getItem(), event.getPlayer());
        if (success) event.setCancelled(true);
    }

    @EventHandler
    public void onLunchboxInventoryClick(InventoryClickEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!LunchboxInventory.getInstance().isThisInventory(topInv)) return;

        ItemStack toPlace;
        if (event.getClickedInventory() == topInv) {
            if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
                int hotbarSlot = event.getHotbarButton();
                toPlace = hotbarSlot == -1
                        ? event.getWhoClicked().getInventory().getItemInOffHand()
                        : event.getWhoClicked().getInventory().getItem(hotbarSlot);
            } else {
                toPlace = event.getCursor();
            }
        } else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            toPlace = event.getCurrentItem();
        } else {
            return;
        }

        if (toPlace == null || toPlace.getType().isAir()) return;
        if ((!toPlace.getType().isEdible() && !Constants.lunchboxExemptedMaterials.contains(toPlace.getType()))
                || CustomItemUtil.isCustomItem(toPlace)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLunchboxClose(InventoryCloseEvent event) {
        UUID uuid = LunchboxInventory.getInstance().getUuidForInventory(event.getInventory());
        LunchboxInventory.getInstance().closeInventory(event.getInventory());
        if (uuid == null) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        ItemStack nextFood = LunchboxInventory.getInstance().peekLunchboxFood(uuid);
        float fill = LunchboxInventory.getInstance().getFoodFill(uuid);
        String uuidStr = uuid.toString();
        for (ItemStack item : player.getInventory().getContents()) {
            if (!LunchboxItem.getInstance().isThisItem(item)) continue;
            assert item != null; // isThisItem performs a null check on item.
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            String itemUuid = meta.getPersistentDataContainer()
                    .get(LunchboxItem.uuidNsk, PersistentDataType.STRING);
            if (!uuidStr.equals(itemUuid)) continue;
            boolean gluttonous = meta.getPersistentDataContainer().has(CustomItem.gluttonousNsk);
            item.setData(DataComponentTypes.FOOD, CustomItem.buildFoodFor(nextFood, gluttonous));
            ItemConfig config = gluttonous
                    ? ConfigHandler.getInstance().getGluttonousLunchboxConfig()
                    : ConfigHandler.getInstance().getLunchboxConfig();
            CustomItem.applyItemModel(item, config, fill);
            break;
        }
    }

    @EventHandler
    public void onEnderChestClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.ENDER_CHEST) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        ItemStack nextFood = LunchboxInventory.getInstance().peekEnderFood(player);
        float fill = LunchboxInventory.getInstance().getEnderFoodFill(player);
        for (ItemStack item : player.getInventory().getContents()) {
            if (!EnderLunchboxItem.getInstance().isThisItem(item)) continue;
            assert item != null; // isThisItem performs a null check on item.
            ItemMeta enderMeta = item.getItemMeta();
            if (enderMeta == null) continue;
            boolean gluttonous = enderMeta.getPersistentDataContainer().has(CustomItem.gluttonousNsk);
            item.setData(DataComponentTypes.FOOD, CustomItem.buildFoodFor(nextFood, gluttonous));
            ItemConfig config = gluttonous
                    ? ConfigHandler.getInstance().getGluttonousEnderLunchboxConfig()
                    : ConfigHandler.getInstance().getEnderLunchboxConfig();
            CustomItem.applyItemModel(item, config, fill);
        }
    }
}
