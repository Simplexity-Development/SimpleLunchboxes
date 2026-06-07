package simplexity.simplelunchboxes.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import simplexity.simplelunchboxes.commands.subcommands.GiveSubcommand;
import simplexity.simplelunchboxes.commands.subcommands.RefreshSubcommand;
import simplexity.simplelunchboxes.commands.subcommands.ReloadSubcommand;

import java.util.List;

public class SimpleLunchboxesCommand {

    private static final List<SubCommand> subcommands = List.of(
            new GiveSubcommand(),
            new ReloadSubcommand(),
            new RefreshSubcommand()
    );

    /**
     * Builds the root {@code /simplelunchboxes} command with all subcommands attached.
     *
     * @return the root {@link LiteralArgumentBuilder}
     */
    public static @NotNull LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("simplelunchboxes");
        for (SubCommand sub : subcommands) {
            root.then(sub.build());
        }
        return root;
    }
}
