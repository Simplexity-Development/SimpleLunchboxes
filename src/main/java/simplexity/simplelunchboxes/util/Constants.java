package simplexity.simplelunchboxes.util;

import org.bukkit.Material;

import java.util.Set;

public class Constants {

    // Can be put into the potion sash, bound by the max-stack-size
    public static final Set<Material> potionSashAllowedMaterials = Set.of(
            Material.POTION,
            Material.SUSPICIOUS_STEW,
            Material.MILK_BUCKET
    );

    // Exempted from the potion sash stack logic, will be treated like a normal item stack
    public static final Set<Material> potionSashExemptedMaterials = Set.of(
            Material.GLASS_BOTTLE,
            Material.BOWL,
            Material.BUCKET,
            Material.OMINOUS_BOTTLE
    );

    // Can be consumed in the potion sash
    public static final Set<Material> potionSashDrinkableMaterials = Set.of(
            Material.POTION,
            Material.OMINOUS_BOTTLE,
            Material.SUSPICIOUS_STEW,
            Material.MILK_BUCKET
    );

    // Non-edible materials that are still allowed in lunchboxes
    public static final Set<Material> lunchboxExemptedMaterials = Set.of(
            Material.GLASS_BOTTLE,
            Material.BOWL,
            Material.BUCKET
    );
}
