package simplexity.simplelunchboxes.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum LocaleMessage {
    LUNCHBOX_EMPTY("lunchbox.empty", "<red>Your lunchbox is empty!"),
    ENDER_LUNCHBOX_EMPTY("ender-lunchbox.empty", "<red>Your ender chest has no food!"),
    POTION_SASH_EMPTY("potion-sash.empty", "<red>Your potion sash is empty!"),
    GIVE_SUCCESS("command.give.success", "<green>Gave <white><item></white> to <white><player></white>."),
    GIVE_NO_PLAYER("command.give.no-player", "<red>You must specify a player when running this from console."),
    GIVE_INVALID_ITEM("command.give.invalid-item", "<red>Unknown item type. Valid types: lunchbox, gluttonous_lunchbox, ender_lunchbox, gluttonous_ender_lunchbox, potion_sash."),
    RELOAD_SUCCESS("command.reload.success", "<green>SimpleLunchboxes config and locale reloaded.");

    private final String path;
    private final String defaultMessage;
    private String message;

    LocaleMessage(String path, String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
        this.message = defaultMessage;
    }

    /**
     * Returns the YAML path for this locale entry.
     *
     * @return dot-separated path
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * Returns the live message string (raw MiniMessage format).
     * Falls back to an empty string if somehow unset.
     *
     * @return the current message string
     */
    @NotNull
    public String getMessage() {
        if (message == null) return "";
        return message;
    }

    /**
     * Returns the hardcoded default as declared in the enum.
     * Used when writing missing keys back to locale.yml.
     *
     * @return the default message string
     */
    @NotNull
    public String getDefaultMessage() {
        return defaultMessage;
    }

    public void setMessage(@Nullable String message) {
        if (message == null) message = defaultMessage;
        this.message = message;
    }
}
