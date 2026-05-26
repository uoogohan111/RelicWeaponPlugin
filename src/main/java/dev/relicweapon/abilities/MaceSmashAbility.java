package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;

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
        // We directly subtract health instead of calling le.damage() to avoid
        // re-firing EntityDamageByEntityEvent and causing a StackOverflowError.
        double bonus = fallDistance * plugin.cfg().getFallDamagePerBlock();
        double cap   = plugin.cfg().getFallDamageCap();
        if (cap > 0) bonus = Math.min(bonus, cap);

        if (bonus > 0 && victim instanceof LivingEntity le) {
            AttributeInstance maxHpAttr = le.getAttribute(Attribute.MAX_HEALTH);
            double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
            double newHp = Math.max(0, le.getHealth() - bonus);
            le.setHealth(Math.min(newHp, maxHp));

            // Show damage visually (red flash + hurt sound)
            le.playHurtAnimation(0f);
            victim.getWorld().playSound(
                victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.0f);
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
