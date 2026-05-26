package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Impact ability: plays particles and sounds when the relic weapon scores a
 * melee hit. Works alongside the MaceSmashAbility for the visual layer.
 */
public final class ImpactAbility implements Ability {

    private final RelicWeaponPlugin plugin;

    public ImpactAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onHitEntity(Player attacker, Entity victim,
                             ItemStack weapon, double fallDistance) {
        // Spark burst at the victim's location
        victim.getWorld().spawnParticle(
            Particle.CRIT,
            victim.getLocation().add(0, 1, 0),
            20, 0.4, 0.4, 0.4, 0.2
        );
        victim.getWorld().spawnParticle(
            Particle.ENCHANTED_HIT,
            victim.getLocation().add(0, 1, 0),
            12, 0.3, 0.3, 0.3, 0.1
        );

        // Impact sound; pitch increases with fall height (mace feel)
        float pitch = (float) Math.min(2.0, 1.0 + fallDistance * 0.05);
        victim.getWorld().playSound(
            victim.getLocation(), Sound.ITEM_MACE_SMASH_AIR, 1f, pitch);

        // If we fell a significant distance, add landing shockwave particles
        if (fallDistance > 3.0) {
            victim.getWorld().spawnParticle(
                Particle.EXPLOSION,
                victim.getLocation(),
                1, 0, 0, 0, 0
            );
            victim.getWorld().playSound(
                victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.6f);
        }
    }
}
