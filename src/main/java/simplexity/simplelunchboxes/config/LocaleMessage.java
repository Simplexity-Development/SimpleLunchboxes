package simplexity.simplelunchboxes.config;

public enum LocaleMessage {
    LUNCHBOX_EMPTY("lunchbox-empty"),
    ENDER_LUNCHBOX_EMPTY("ender-lunchbox-empty"),
    POTION_SASH_EMPTY("potion-sash-empty"),
    GIVE_SUCCESS("give-success"),
    GIVE_NO_PLAYER("give-no-player"),
    GIVE_INVALID_ITEM("give-invalid-item"),
    RELOAD_SUCCESS("reload-success");

    private final String key;

    LocaleMessage(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
