package simplexity.simplelunchboxes;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import simplexity.simplelunchboxes.commands.SimpleLunchboxesCommand;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.config.LocaleHandler;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;
import simplexity.simplelunchboxes.inventory.PotionSashInventory;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.item.PotionSashItem;
import simplexity.simplelunchboxes.listener.LunchboxListeners;
import simplexity.simplelunchboxes.listener.PotionSashListeners;
import simplexity.simplelunchboxes.util.SimpleLunchboxesPermission;

@SuppressWarnings("UnstableApiUsage")
public final class SimpleLunchboxes extends JavaPlugin {

    public static final String namespace = "simplelunchboxes";
    private static SimpleLunchboxes plugin;
    private static MiniMessage miniMessage;

    @Override
    public void onEnable() {
        plugin = this;
        miniMessage = MiniMessage.miniMessage();
        registerPermissions();
        ConfigHandler.getInstance();
        LocaleHandler.getInstance();
        this.getServer().getPluginManager().registerEvents(new LunchboxListeners(), this);
        this.getServer().getPluginManager().registerEvents(new PotionSashListeners(), this);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                commands.registrar().register(SimpleLunchboxesCommand.createCommand().build())
        );
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

    private void registerPermissions() {
        for (SimpleLunchboxesPermission perm : SimpleLunchboxesPermission.values()) {
            this.getServer().getPluginManager().addPermission(perm.getPermission());
        }
    }

    public static SimpleLunchboxes getPlugin() { return plugin; }
    public static MiniMessage getMiniMessage() { return miniMessage; }
}
