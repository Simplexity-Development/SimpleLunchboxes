package simplexity.simplelunchboxes.util;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public enum SimpleLunchboxesPermission {
    GIVE("simplelunchboxes.give", PermissionDefault.OP),
    RELOAD("simplelunchboxes.reload", PermissionDefault.OP);

    private final Permission permission;

    SimpleLunchboxesPermission(String node, PermissionDefault defaultValue) {
        this.permission = new Permission(node, defaultValue);
    }

    public @NotNull Permission getPermission() {
        return permission;
    }

    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.hasPermission(permission);
    }
}
