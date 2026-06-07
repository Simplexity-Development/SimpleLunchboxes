package simplexity.simplelunchboxes.listener;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.inventory.PotionSashInventory;
import simplexity.simplelunchboxes.item.PotionSashItem;
import simplexity.simplelunchboxes.util.Constants;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class PotionSashListeners implements Listener {

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (PotionSashItem.getInstance().isThisItem(item)) PotionSashItem.getInstance().handleConsumption(event);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getPlayer().isSneaking()) return;
        if (!PotionSashItem.getInstance().isThisItem(event.getItem())) return;
        event.setUseInteractedBlock(Result.DENY);
    }

    @EventHandler
    public void onSneakUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;
        boolean success = PotionSashInventory.getInstance().openInventory(event.getItem(), event.getPlayer());
        if (success) event.setCancelled(true);
    }

    @EventHandler
    public void onPotionSashClose(InventoryCloseEvent event) {
        PotionSashInventory.getInstance().closeInventory(event.getInventory());
    }

    /**
     * Manages all inventory interactions with the potion sash:
     * <ul>
     *   <li>Blocks non-POTION items from entering.</li>
     *   <li>Shift-click INTO sash: manually stacks potions with custom max-stack-size.</li>
     *   <li>Shift-click / pickup FROM sash: limits extraction to one potion at a time.</li>
     *   <li>Direct cursor placement INTO sash: custom stacking up to max-stack-size.</li>
     * </ul>
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (!PotionSashInventory.getInstance().isThisInventory(topInv)) return;

        int maxStack = ConfigHandler.getInstance().getPotionSashMaxStack();
        InventoryAction action = event.getAction();
        Inventory clickedInv = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;
        // If these are exempted materials, just let them be
        if (Constants.potionSashExemptedMaterials.contains(clickedItem.getType())) {
            return;
        }
        // If these are not allowed, cancel action
        if (!Constants.potionSashAllowedMaterials.contains(clickedItem.getType())) {
            event.setCancelled(true);
            return;
        }

        // Shift-click from player inventory INTO sash
        if (clickedInv != topInv && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            int remaining = clickedItem.getAmount();
            // Fill existing partial stacks first
            for (int i = 0; i < topInv.getSize() && remaining > 0; i++) {
                ItemStack slot = topInv.getItem(i);
                if (slot == null || slot.getType().isAir() || !areSamePotion(slot, clickedItem)) continue;
                int canAdd = maxStack - slot.getAmount();
                if (canAdd <= 0) continue;
                int transfer = Math.min(canAdd, remaining);
                slot.setAmount(slot.getAmount() + transfer);
                slot.setData(DataComponentTypes.MAX_STACK_SIZE, maxStack);
                topInv.setItem(i, slot);
                remaining -= transfer;
            }
            // Then fill empty slots
            for (int i = 0; i < topInv.getSize() && remaining > 0; i++) {
                ItemStack slot = topInv.getItem(i);
                if (slot != null && !slot.getType().isAir()) continue;
                int toPlace = Math.min(remaining, maxStack);
                ItemStack placing = clickedItem.clone();
                placing.setAmount(toPlace);
                placing.setData(DataComponentTypes.MAX_STACK_SIZE, maxStack);
                topInv.setItem(i, placing);
                remaining -= toPlace;
            }
            if (remaining <= 0) {
                event.setCurrentItem(new ItemStack(Material.AIR));
            } else {
                clickedItem.setAmount(remaining);
                event.setCurrentItem(clickedItem);
            }
            return;
        }

        // Shift-click FROM sash to player inventory: give exactly 1 potion
        if (clickedInv == topInv && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            ItemStack onePotion = clickedItem.asOne();
            onePotion.resetData(DataComponentTypes.MAX_STACK_SIZE);
            Map<Integer, ItemStack> leftover = event.getWhoClicked().getInventory().addItem(onePotion);
            if (leftover.isEmpty()) {
                int newAmount = clickedItem.getAmount() - 1;
                if (newAmount <= 0) {
                    topInv.setItem(event.getSlot(), new ItemStack(Material.AIR));
                } else {
                    clickedItem.setAmount(newAmount);
                }
            }
            return;
        }

        // Pickup (left/right click) FROM sash: give exactly 1 when stack > 1
        if (clickedInv == topInv
                && (action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_HALF)) {
            ItemStack slotItem = event.getCurrentItem();
            if (slotItem == null || slotItem.getType().isAir() || slotItem.getAmount() <= 1) return;
            ItemStack cursor = event.getCursor();
            if (!cursor.getType().isAir()) return;
            event.setCancelled(true);
            ItemStack onePotion = slotItem.asOne();
            onePotion.resetData(DataComponentTypes.MAX_STACK_SIZE);
            event.getView().setCursor(onePotion);
            slotItem.setAmount(slotItem.getAmount() - 1);
            return;
        }

        // Remaining cases: only relevant when the click is in the sash
        if (clickedInv != topInv) return;

        ItemStack toPlace;
        boolean directPlacement;
        if (action == InventoryAction.HOTBAR_SWAP) {
            int hotbarSlot = event.getHotbarButton();
            toPlace = hotbarSlot == -1
                    ? event.getWhoClicked().getInventory().getItemInOffHand()
                    : event.getWhoClicked().getInventory().getItem(hotbarSlot);
            directPlacement = false;
        } else {
            toPlace = event.getCursor();
            directPlacement = true;
        }

        if (toPlace == null || toPlace.getType().isAir()) return;

        if (!Constants.potionSashAllowedMaterials.contains(toPlace.getType())) {
            event.setCancelled(true);
            return;
        }

        if (!directPlacement) return; // hotbar-swap POTION: allow default behavior

        // Direct POTION placement — apply custom stacking
        event.setCancelled(true);
        ItemStack cursor = event.getCursor();
        ItemStack slot = event.getCurrentItem();
        if (slot != null && !slot.getType().isAir() && areSamePotion(slot, cursor)) {
            int canAdd = maxStack - slot.getAmount();
            int transfer = Math.min(canAdd, cursor.getAmount());
            if (transfer <= 0) return;
            slot.setAmount(slot.getAmount() + transfer);
            slot.setData(DataComponentTypes.MAX_STACK_SIZE, maxStack);
            topInv.setItem(event.getSlot(), slot);
            int remaining = cursor.getAmount() - transfer;
            if (remaining <= 0) {
                event.getView().setCursor(null);
            } else {
                cursor.setAmount(remaining);
                event.getView().setCursor(cursor);
            }
        } else if (slot == null || slot.getType().isAir()) {
            ItemStack placing = cursor.clone();
            placing.setAmount(Math.min(cursor.getAmount(), maxStack));
            placing.setData(DataComponentTypes.MAX_STACK_SIZE, maxStack);
            topInv.setItem(event.getSlot(), placing);
            int remaining = cursor.getAmount() - placing.getAmount();
            if (remaining <= 0) {
                event.getView().setCursor(null);
            } else {
                cursor.setAmount(remaining);
                event.getView().setCursor(cursor);
            }
        }
    }

    /**
     * Compares two potions for stacking purposes, ignoring the {@code MAX_STACK_SIZE} component
     * that the sash sets internally. Without this, a vanilla potion (no component) and a sash
     * potion (component set) would not be considered similar by {@link ItemStack#isSimilar}.
     */
    private boolean areSamePotion(@NotNull ItemStack a, @NotNull ItemStack b) {
        if (a.getType() != b.getType()) return false;
        ItemStack aCopy = a.clone();
        ItemStack bCopy = b.clone();
        aCopy.resetData(DataComponentTypes.MAX_STACK_SIZE);
        bCopy.resetData(DataComponentTypes.MAX_STACK_SIZE);
        return aCopy.isSimilar(bCopy);
    }
}
