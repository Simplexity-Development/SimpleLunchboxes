package simplexity.simplelunchboxes.item;

import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class CustomItem {

    public abstract void handleConsumption(PlayerItemConsumeEvent event);

    public abstract boolean isThisItem(@Nullable ItemStack item);

    public abstract void constructItems();

}
