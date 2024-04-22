package simplexity.simplelunchboxes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.item.PotionSashItem;

public class LunchboxListeners implements Listener {

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (LunchboxItem.getInstance().isThisItem(item)) LunchboxItem.getInstance().handleConsumption(event);
        if (EnderLunchboxItem.getInstance().isThisItem(item)) EnderLunchboxItem.getInstance().handleConsumption(event);
        if (PotionSashItem.getInstance().isThisItem(item)) PotionSashItem.getInstance().handleConsumption(event);
    }

    @EventHandler
    public void onSneakUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;
        boolean success = LunchboxInventory.getInstance().openInventory(event.getItem(), event.getPlayer());
        if (success) event.setCancelled(true);
    }

    @EventHandler
    public void onLunchboxClose(InventoryCloseEvent event) {
        LunchboxInventory.getInstance().closeInventory(event.getInventory());
    }

}