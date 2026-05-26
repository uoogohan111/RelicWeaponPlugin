package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Channeling mechanic: hitting an entity during a thunderstorm summons a
 * lightning bolt on them, matching vanilla Channeling trident behaviour.
 */
public final class TridentChannelingAbility implements Ability {

    private final RelicWeaponPlugin plugin;

    public TridentChannelingAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onHitEntity(Player attacker, Entity victim,
                             ItemStack weapon, double fallDistance) {
        if (!plugin.cfg().isChannelingEnabled()) return;
        if (!attacker.getWorld().isThundering()) return;
        // Only strike if the victim is exposed to the sky
        if (victim.getLocation().getBlock().getLightFromSky() < 15) return;

        // Summon lightning (effect only — no additional fire damage to keep balance)
        LightningStrike bolt = victim.getWorld()
            .strikeLightningEffect(victim.getLocation());

        // Deal 1 heart bonus damage to simulate the lightning hit
        if (victim instanceof LivingEntity le) {
            le.damage(2.0, attacker);
        }

        attacker.getWorld().playSound(
            attacker.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1f, 1f);
    }
}
