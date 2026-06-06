package simplexity.simplelunchboxes.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public interface SubCommand {

    /**
     * Builds and returns the literal command node for this subcommand.
     *
     * @return the built {@link LiteralCommandNode}
     */
    @NotNull LiteralCommandNode<CommandSourceStack> build();
}
