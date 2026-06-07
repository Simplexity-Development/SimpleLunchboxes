package simplexity.simplelunchboxes.config;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Immutable snapshot of a custom item's configurable properties.
 *
 * @param displayName raw MiniMessage display name string
 * @param itemModel   three-state model config (empty / low / high); all keys {@code ""} = unset
 * @param glint       whether the item has an enchantment glint
 * @param lore        raw MiniMessage lore lines
 */
public record ItemConfig(
        @NotNull String displayName,
        @NotNull ItemModelConfig itemModel,
        boolean glint,
        @NotNull List<String> lore
) {}
