package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Mace-style smash ability (Minecraft 1.21 mechanic).
 * <ul>
 *   <li>Bonus damage based on blocks fallen before the hit.</li>
 *   <li>Wind-burst AOE knockback on impact (like the Wind Burst enchantment).</li>
 *   <li>Resets the attacker's fall distance so they take no fall damage.</li>
 * </ul>
 * The DamageListener has already applied {@code damageMultiplier}; this class
 * adds the fall-height component on top.
 */
public final class MaceSmashAbility implements Ability {

    private final RelicWeaponPlugin plugin;

    public MaceSmashAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onHitEntity(Player attacker, Entity victim,
                             ItemStack weapon, double fallDistance) {
        if (fallDistance <= 0) return;

        // ── Fall-height bonus damage ─────────────────────────────────────────
        double bonus = fallDistance * plugin.cfg().getFallDamagePerBlock();
        double cap   = plugin.cfg().getFallDamageCap();
        if (cap > 0) bonus = Math.min(bonus, cap);

        if (victim instanceof LivingEntity le) {
            le.damage(bonus, attacker);
        }

        // ── Wind-burst AOE knockback ─────────────────────────────────────────
        double radius = plugin.cfg().getWindBurstRadius();
        double power  = plugin.cfg().getWindBurstPower();

        if (radius > 0 && fallDistance > 1.5) {
            Collection<Entity> nearby = victim.getWorld()
                .getNearbyEntities(victim.getLocation(), radius, radius, radius);

            for (Entity nearby_entity : nearby) {
                if (nearby_entity.equals(attacker)) continue;
                Vector away = nearby_entity.getLocation()
                                           .toVector()
                                           .subtract(victim.getLocation().toVector())
                                           .normalize()
                                           .multiply(power)
                                           .setY(0.5);
                nearby_entity.setVelocity(away);
            }

            victim.getWorld().playSound(
                victim.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1f, 0.9f);
        }

        // ── Prevent attacker taking their own fall damage ────────────────────
        attacker.setFallDistance(0f);
    }
}
