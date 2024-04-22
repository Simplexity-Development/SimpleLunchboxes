package simplexity.simplelunchboxes;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.item.PotionSashItem;

public class GiveQoLFoodCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        player.getInventory().addItem(LunchboxItem.getInstance().getLunchboxItem(1, false, null));
        player.getInventory().addItem(LunchboxItem.getInstance().getLunchboxItem(1, true, null));
        player.getInventory().addItem(EnderLunchboxItem.getInstance().newItem(false));
        player.getInventory().addItem(EnderLunchboxItem.getInstance().newItem(true));
        player.getInventory().addItem(PotionSashItem.getInstance().getPotionSashItem(1, null));
        return true;
    }

}
