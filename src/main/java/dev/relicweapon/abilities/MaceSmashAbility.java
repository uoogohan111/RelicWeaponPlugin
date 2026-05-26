package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Particle;
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
        if (fallDistance < 2.0) return;

        // ── Fall-height bonus damage ─────────────────────────────────────────
        double bonus = fallDistance * plugin.cfg().getFallDamagePerBlock();
        double cap   = plugin.cfg().getFallDamageCap();
        if (cap > 0) bonus = Math.min(bonus, cap);

        if (bonus > 0 && victim instanceof LivingEntity le) {
            AttributeInstance maxHpAttr = le.getAttribute(Attribute.MAX_HEALTH);
            double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
            double newHp = Math.max(0, le.getHealth() - bonus);
            le.setHealth(Math.min(newHp, maxHp));

            le.playHurtAnimation(0f);
            victim.getWorld().playSound(
                victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.8f, 1.0f);
        }

        // ── Wind-burst AOE knockback ─────────────────────────────────────────
        double radius = plugin.cfg().getWindBurstRadius();
        double power  = plugin.cfg().getWindBurstPower();

        if (radius > 0) {
            // Knock back nearby entities away from impact point
            victim.getWorld()
                .getNearbyEntities(victim.getLocation(), radius, radius, radius)
                .stream()
                .filter(e -> !e.equals(attacker) && !e.equals(victim))
                .filter(e -> e instanceof LivingEntity)
                .forEach(e -> {
                    Vector away = e.getLocation()
                                   .toVector()
                                   .subtract(victim.getLocation().toVector());
                    if (away.lengthSquared() > 0) {
                        away.normalize().multiply(power).setY(0.6);
                        e.setVelocity(away);
                    }
                });

            // Also knock back the victim itself
            if (victim instanceof LivingEntity) {
                Vector victimKnock = victim.getLocation()
                    .getDirection().multiply(-power).setY(0.8);
                victim.setVelocity(victimKnock);
            }

            victim.getWorld().playSound(
                victim.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 1f, 0.9f);
            victim.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                victim.getLocation().add(0, 1, 0),
                20, radius * 0.5, 0.5, radius * 0.5, 0.1
            );
        }

        // ── Prevent attacker taking their own fall damage ────────────────────
        attacker.setFallDistance(0f);
    }
}
