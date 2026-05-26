package dev.relicweapon.items;

import dev.relicweapon.RelicWeaponPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Creates, tags, and identifies Relic Weapon ItemStacks.
 *
 * The weapon is based on TRIDENT so it inherits vanilla throw/loyalty/riptide
 * mechanics at the engine level. Our listeners augment those behaviours.
 */
public final class ItemManager {

    /** PDC key used to mark an item as a Relic Weapon. */
    public static final String PDC_KEY = "relic_weapon";

    private final RelicWeaponPlugin plugin;
    private final NamespacedKey relicKey;

    public ItemManager(RelicWeaponPlugin plugin) {
        this.plugin   = plugin;
        this.relicKey = new NamespacedKey(plugin, PDC_KEY);
    }

    // ─── Factory ────────────────────────────────────────────────────────────

    /**
     * Creates a fully configured Relic Weapon ItemStack.
     */
    public ItemStack createRelicWeapon() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta  = item.getItemMeta();

        if (meta == null) return item; // should never happen for TRIDENT

        // Display
        meta.displayName(plugin.cfg().getDisplayNameComponent());
        List<Component> lore = plugin.cfg().getLoreComponents();
        meta.lore(lore);

        // Custom model data (resource-pack switching)
        meta.setCustomModelData(plugin.cfg().getModelId());

        // PDC marker so we can identify this item in listeners
        meta.getPersistentDataContainer()
            .set(relicKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    // ─── Identification ──────────────────────────────────────────────────────

    /**
     * Returns true if the given ItemStack is a Relic Weapon.
     */
    public boolean isRelicWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null &&
               meta.getPersistentDataContainer()
                   .has(relicKey, PersistentDataType.BYTE);
    }

    /**
     * Updates the CustomModelData of a Relic Weapon to reflect a new state.
     * States: "idle", "charged", "thrown"
     */
    public void setModelState(ItemStack item, String state) {
        if (!isRelicWeapon(item)) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        int modelId = switch (state) {
            case "charged" -> plugin.cfg().getModelIdCharged();
            case "thrown"  -> plugin.cfg().getModelIdThrown();
            default        -> plugin.cfg().getModelId();
        };
        meta.setCustomModelData(modelId);
        item.setItemMeta(meta);
    }

    public NamespacedKey getRelicKey() { return relicKey; }
}
