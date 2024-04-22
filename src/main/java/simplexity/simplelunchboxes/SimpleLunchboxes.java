package simplexity.simplelunchboxes;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;
import simplexity.simplelunchboxes.inventory.PotionSashInventory;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.item.PotionSashItem;
import simplexity.simplelunchboxes.listener.LunchboxListeners;

public final class SimpleLunchboxes extends JavaPlugin {

    public static final String namespace = "simplelunchboxes";
    private static SimpleLunchboxes plugin;
    private static MiniMessage miniMessage;

    @Override
    public void onEnable() {
        plugin = this;
        miniMessage = MiniMessage.miniMessage();
        this.getServer().getPluginManager().registerEvents(new LunchboxListeners(), this);
        this.getCommand("giveqolfood").setExecutor(new GiveQoLFoodCommand());
        constructCustomItems();
    }

    @Override
    public void onDisable() {
        LunchboxInventory.getInstance().closeAll();
        PotionSashInventory.getInstance().closeAll();
    }

    public void constructCustomItems() {
        LunchboxItem.getInstance().constructItems();
        EnderLunchboxItem.getInstance().constructItems();
        PotionSashItem.getInstance().constructItems();
    }

    public static SimpleLunchboxes getPlugin() { return plugin; }
    public static MiniMessage getMiniMessage() { return miniMessage; }
}
