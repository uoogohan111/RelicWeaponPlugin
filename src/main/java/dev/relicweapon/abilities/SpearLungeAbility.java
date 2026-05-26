package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Spear lunge ability (inspired by Minecraft 1.21.11 Spear mechanics).
 * <ul>
 *   <li>Hold right-click for {@code chargeLungeTicks} ticks → model switches to "charged".</li>
 *   <li>When the charged model is active and the player lands a hit, bonus damage is
 *       applied proportional to the player's horizontal speed (lunge mechanic).</li>
 *   <li>Extra reach is approximated by expanding the entity's interaction hitbox
 *       via the vanilla reach-distance attribute.</li>
 * </ul>
 */
public final class SpearLungeAbility implements Ability {

    private static final String COOLDOWN_KEY = "relic_lunge";

    private final RelicWeaponPlugin plugin;

    /** tracks players who are currently charging (uuid → charge task) */
    private final Map<UUID, BukkitTask> chargingPlayers = new HashMap<>();
    /** tracks players in "charged" state */
    private final Map<UUID, Boolean> chargedPlayers = new HashMap<>();

    public SpearLungeAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(Player player, ItemStack item) {
        if (player.isSneaking()) return; // sneak is throw
        if (chargingPlayers.containsKey(player.getUniqueId())) return;

        int chargeTicks = plugin.cfg().getChargeLungeTicks();

        // Show charge bar via action bar
        BukkitTask chargeTask = plugin.getServer().getScheduler()
            .runTaskLater(plugin, () -> {
                chargingPlayers.remove(player.getUniqueId());
                chargedPlayers.put(player.getUniqueId(), true);
                plugin.items().setModelState(item, "charged");

                player.sendActionBar(Component.text("§b§lCHARGED! §7Attack to lunge!"));
                player.getWorld().playSound(
                    player.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 0.8f, 1.4f);
                player.getWorld().spawnParticle(
                    Particle.ELECTRIC_SPARK,
                    player.getLocation().add(0, 1, 0),
                    15, 0.3, 0.5, 0.3, 0.1
                );

                // Auto-expire charge after 3 seconds
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (chargedPlayers.remove(player.getUniqueId()) != null) {
                        plugin.items().setModelState(item, "idle");
                        player.sendActionBar(Component.text("§7Charge expired."));
                    }
                }, 60L);
            }, chargeTicks);

        chargingPlayers.put(player.getUniqueId(), chargeTask);
    }

    @Override
    public void onHitEntity(Player attacker, Entity victim,
                             ItemStack weapon, double fallDistance) {
        if (!chargedPlayers.remove(attacker.getUniqueId(), true)) return;

        // Bonus damage scales with the attacker's horizontal speed
        Vector vel = attacker.getVelocity();
        double hSpeed = Math.sqrt(vel.getX() * vel.getX() + vel.getZ() * vel.getZ());

        double multiplier = plugin.cfg().getLungeSpeedDamageMultiplier();
        double bonus = hSpeed * multiplier * 2.0; // 2.0 is a tuning constant

        if (bonus > 0.5 && victim instanceof LivingEntity le) {
            le.damage(bonus, attacker);
        }

        // Reset model
        plugin.items().setModelState(weapon, "idle");

        attacker.getWorld().playSound(
            attacker.getLocation(), Sound.ITEM_MACE_SMASH_GROUND, 0.9f, 1.1f);

        if (!plugin.cooldowns().isOnCooldown(attacker, COOLDOWN_KEY)) {
            plugin.cooldowns().setCooldown(attacker, COOLDOWN_KEY,
                plugin.cfg().getCooldown() / 2); // shorter cooldown for lunge
        }
    }

    /** Clean up if the player logs off mid-charge. */
    public void cancelCharge(UUID uuid) {
        BukkitTask t = chargingPlayers.remove(uuid);
        if (t != null) t.cancel();
        chargedPlayers.remove(uuid);
    }
}
