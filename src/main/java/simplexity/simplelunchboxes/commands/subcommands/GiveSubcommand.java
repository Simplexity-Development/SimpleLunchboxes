package simplexity.simplelunchboxes.commands.subcommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.commands.SubCommand;
import simplexity.simplelunchboxes.config.LocaleHandler;
import simplexity.simplelunchboxes.config.LocaleMessage;
import simplexity.simplelunchboxes.item.EnderLunchboxItem;
import simplexity.simplelunchboxes.item.LunchboxItem;
import simplexity.simplelunchboxes.item.PotionSashItem;
import simplexity.simplelunchboxes.util.SimpleLunchboxesPermission;

public class GiveSubcommand implements SubCommand {

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("give")
                .requires(source -> SimpleLunchboxesPermission.GIVE.hasPermission(source.getSender()))
                .then(tieredItemNode("lunchbox"))
                .then(tieredItemNode("gluttonous_lunchbox"))
                .then(tieredItemNode("potion_sash"))
                .then(enderItemNode("ender_lunchbox"))
                .then(enderItemNode("gluttonous_ender_lunchbox"))
                .build();
    }

    /** Builds a subcommand node for a tiered item: [tier] [player]. */
    private com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> tieredItemNode(@NotNull String name) {
        return Commands.literal(name)
                .then(Commands.argument("tier", IntegerArgumentType.integer(1, 6))
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .executes(ctx -> give(ctx, name,
                                        IntegerArgumentType.getInteger(ctx, "tier"),
                                        ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                                .resolve(ctx.getSource()).getFirst()
                                ))
                        )
                        .executes(ctx -> give(ctx, name, IntegerArgumentType.getInteger(ctx, "tier"), null))
                )
                .executes(ctx -> give(ctx, name, 1, null));
    }

    /** Builds a subcommand node for an ender item (no tier): [player]. */
    private com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> enderItemNode(@NotNull String name) {
        return Commands.literal(name)
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes(ctx -> give(ctx, name, 1,
                                ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                                        .resolve(ctx.getSource()).getFirst()
                        ))
                )
                .executes(ctx -> give(ctx, name, 1, null));
    }

    private int give(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull String itemType, int tier, @Nullable Player explicitTarget) {
        CommandSender sender = ctx.getSource().getSender();

        Player target = explicitTarget;
        if (target == null) {
            if (sender instanceof Player p) {
                target = p;
            } else {
                sender.sendMessage(LocaleHandler.getInstance().get(LocaleMessage.GIVE_NO_PLAYER));
                return 0;
            }
        }

        ItemStack item = switch (itemType) {
            case "lunchbox" -> LunchboxItem.getInstance().getLunchboxItem(tier, false, null);
            case "gluttonous_lunchbox" -> LunchboxItem.getInstance().getLunchboxItem(tier, true, null);
            case "ender_lunchbox" -> EnderLunchboxItem.getInstance().newItem(false);
            case "gluttonous_ender_lunchbox" -> EnderLunchboxItem.getInstance().newItem(true);
            case "potion_sash" -> PotionSashItem.getInstance().getPotionSashItem(tier, null);
            default -> null;
        };

        if (item == null) {
            sender.sendMessage(LocaleHandler.getInstance().get(LocaleMessage.GIVE_INVALID_ITEM));
            return 0;
        }

        target.getInventory().addItem(item);

        String raw = LocaleHandler.getInstance().getRaw(LocaleMessage.GIVE_SUCCESS)
                .replace("<item>", itemType)
                .replace("<player>", target.getName());
        sender.sendMessage(SimpleLunchboxes.getMiniMessage().deserialize(raw));
        return 1;
    }
}
