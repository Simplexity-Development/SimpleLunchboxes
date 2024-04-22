package simplexity.simplelunchboxes.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;
import simplexity.simplelunchboxes.inventory.PotionSashInventory;
import simplexity.simplelunchboxes.item.PotionSashItem;

public class PotionSashListeners implements Listener {

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (PotionSashItem.getInstance().isThisItem(item)) PotionSashItem.getInstance().handleConsumption(event);
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
}
