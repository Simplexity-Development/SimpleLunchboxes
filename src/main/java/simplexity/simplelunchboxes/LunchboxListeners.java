package simplexity.simplelunchboxes;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class LunchboxListeners implements Listener {

    // TODO: Consolidate NSKs to a utility class, especially cause they are not used in this class anymore.
    public static final NamespacedKey lunchboxNsk = new NamespacedKey(SimpleLunchboxes.namespace, "lunchbox");
    public static final NamespacedKey enderLunchboxNsk = new NamespacedKey(SimpleLunchboxes.namespace, "ender_lunchbox");

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        LunchboxInventoryHandler.getInstance().handleEating(event);
    }

    @EventHandler
    public void onSneakUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSneaking()) return;
        boolean success = LunchboxInventoryHandler.getInstance().openInventory(event.getItem(), event.getPlayer());
        if (success) event.setCancelled(true);
    }

    @EventHandler
    public void onLunchboxClose(InventoryCloseEvent event) {
        LunchboxInventoryHandler.getInstance().closeLunchbox(event.getInventory());
    }

}