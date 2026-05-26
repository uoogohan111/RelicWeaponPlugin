package dev.relicweapon.abilities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

/**
 * Contract every modular ability must implement.
 * Default methods are no-ops so abilities only override what they care about.
 */
public interface Ability {

    /** Right-click interaction with the relic weapon in hand. */
    default void onRightClick(Player player, ItemStack item) {}

    /** Left-click / swing with the relic weapon in hand. */
    default void onLeftClick(Player player, ItemStack item) {}

    /**
     * The relic weapon scored a melee hit.
     *
     * @param attacker     the attacking player
     * @param victim       the entity that was hit
     * @param weapon       the weapon ItemStack
     * @param fallDistance blocks fallen before the hit (mace mechanic)
     */
    default void onHitEntity(Player attacker, Entity victim,
                              ItemStack weapon, double fallDistance) {}

    /**
     * The thrown relic weapon (as a Trident entity) hit something.
     *
     * @param owner      the player who threw it
     * @param projectile the Trident entity
     * @param hitEntity  the entity it hit (may be null for block hits)
     */
    default void onProjectileHit(Player owner, Trident projectile,
                                  Entity hitEntity) {}
}
