package simplexity.simplelunchboxes.config;

import org.jetbrains.annotations.NotNull;

/**
 * Holds the three item model keys used to reflect a lunchbox's fill level.
 * Fill is calculated the same way a comparator reads a container:
 * {@code sum(count / maxStackSize) / totalSlots}.
 *
 * @param empty model key when the lunchbox has no food (fill == 0)
 * @param low   model key when fill is above 0 but below 50%
 * @param high  model key when fill is at or above 50%
 */
public record ItemModelConfig(
        @NotNull String empty,
        @NotNull String low,
        @NotNull String high
) {
    /**
     * Returns the model key appropriate for the given fill fraction.
     *
     * @param fill a value in {@code [0, 1]} representing how full the container is
     * @return the model key string, or {@code ""} if none is configured for this state
     */
    public @NotNull String forFill(float fill) {
        if (fill <= 0f) return empty;
        if (fill < 0.5f) return low;
        return high;
    }
}
