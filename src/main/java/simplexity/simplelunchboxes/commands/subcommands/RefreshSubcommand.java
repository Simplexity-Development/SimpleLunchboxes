package simplexity.simplelunchboxes.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.commands.SubCommand;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.config.LocaleHandler;
import simplexity.simplelunchboxes.config.LocaleMessage;
import simplexity.simplelunchboxes.inventory.LunchboxInventory;
import simplexity.simplelunchboxes.item.CustomItem;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.item.PotionSashItem;

import java.util.UUID;

public class RefreshSubcommand implements SubCommand {

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("refresh")
                .executes(this::execute)
                .build();
    }

    private int execute(@NotNull CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleHandler.getInstance().get(LocaleMessage.GIVE_NO_PLAYER));
            return 0;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        ConfigHandler config = ConfigHandler.getInstance();

        if (LunchboxItem.getInstance().isThisItem(item)) {
            boolean gluttonous = item.getItemMeta().getPersistentDataContainer().has(CustomItem.gluttonousNsk);
            String uuidStr = item.getItemMeta().getPersistentDataContainer().get(LunchboxItem.uuidNsk, PersistentDataType.STRING);
            UUID uuid = uuidStr != null ? UUID.fromString(uuidStr) : null;
            float fill = uuid != null ? LunchboxInventory.getInstance().getFoodFill(uuid) : 0f;
            CustomItem.applyItemConfig(item,
                    gluttonous ? config.getGluttonousLunchboxConfig() : config.getLunchboxConfig(), fill);
        } else if (EnderLunchboxItem.getInstance().isThisItem(item)) {
            boolean gluttonous = item.getItemMeta().getPersistentDataContainer().has(CustomItem.gluttonousNsk);
            float fill = LunchboxInventory.getInstance().getEnderFoodFill(player);
            CustomItem.applyItemConfig(item,
                    gluttonous ? config.getGluttonousEnderLunchboxConfig() : config.getEnderLunchboxConfig(), fill);
        } else if (PotionSashItem.getInstance().isThisItem(item)) {
            CustomItem.applyItemConfig(item, config.getPotionSashConfig(), 1f);
        } else {
            player.sendMessage(LocaleHandler.getInstance().get(LocaleMessage.REFRESH_NOT_CUSTOM_ITEM));
            return 0;
        }

        player.getInventory().setItemInMainHand(item);
        player.sendEquipmentChange(player, EquipmentSlot.HAND, item);
        player.sendMessage(LocaleHandler.getInstance().get(LocaleMessage.REFRESH_SUCCESS));
        return 1;
    }
}
