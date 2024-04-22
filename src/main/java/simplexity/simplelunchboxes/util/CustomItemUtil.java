package simplexity.simplelunchboxes.util;

import org.bukkit.inventory.ItemStack;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.item.PotionSashItem;

public class CustomItemUtil {

    public static boolean isCustomItem(ItemStack item) {
        return LunchboxItem.getInstance().isThisItem(item) ||
                EnderLunchboxItem.getInstance().isThisItem(item) ||
                PotionSashItem.getInstance().isThisItem(item);
    }

}
