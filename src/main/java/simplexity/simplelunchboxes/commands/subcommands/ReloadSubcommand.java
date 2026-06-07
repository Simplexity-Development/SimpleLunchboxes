package simplexity.simplelunchboxes.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.SimpleLunchboxes;
import simplexity.simplelunchboxes.commands.SubCommand;
import simplexity.simplelunchboxes.config.ConfigHandler;
import simplexity.simplelunchboxes.config.LocaleHandler;
import simplexity.simplelunchboxes.config.LocaleMessage;
import simplexity.simplelunchboxes.util.SimpleLunchboxesPermission;

public class ReloadSubcommand implements SubCommand {

    @Override
    public @NotNull LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("reload")
                .requires(source -> SimpleLunchboxesPermission.RELOAD.hasPermission(source.getSender()))
                .executes(this::execute)
                .build();
    }

    private int execute(@NotNull CommandContext<CommandSourceStack> ctx) {
        ConfigHandler.getInstance().load();
        LocaleHandler.getInstance().load();
        SimpleLunchboxes.getPlugin().constructCustomItems();
        ctx.getSource().getSender().sendMessage(LocaleHandler.getInstance().get(LocaleMessage.RELOAD_SUCCESS));
        return 1;
    }
}
