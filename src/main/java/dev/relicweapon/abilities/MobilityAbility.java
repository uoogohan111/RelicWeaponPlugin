package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Dash / boost ability.
 * Right-click (non-sneaking) to dash in the direction the player is looking.
 */
public final class MobilityAbility implements Ability {

    private static final String COOLDOWN_KEY = "relic_dash";

    private final RelicWeaponPlugin plugin;

    public MobilityAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(Player player, ItemStack item) {
        // Sneak + right-click is reserved for throw
        if (player.isSneaking()) return;
        if (plugin.cooldowns().isOnCooldown(player, COOLDOWN_KEY)) {
            int remaining = plugin.cooldowns().getRemainingSeconds(player, COOLDOWN_KEY);
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§cDash on cooldown: §e" + remaining + "s"));
            return;
        }

        double power    = plugin.cfg().getDashPower();
        double vertical = plugin.cfg().getDashVertical();

        Vector dir = player.getLocation().getDirection().normalize();
        Vector vel = dir.multiply(power).setY(Math.max(dir.getY(), vertical));

        player.setVelocity(vel);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_SHOOT, 1f, 1.2f);

        if (plugin.cfg().isDashParticles()) {
            player.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                player.getLocation().add(0, 1, 0),
                10, 0.3, 0.3, 0.3, 0.1
            );
        }

        plugin.cooldowns().setCooldown(player, COOLDOWN_KEY, plugin.cfg().getCooldown());
    }
}
