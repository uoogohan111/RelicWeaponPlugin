package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * Throw / Return ability.
 * Sneak + Right-click: launches the weapon as a Trident entity.
 * After {@code returnDelayTicks} ticks (or on hit, if configured), the trident
 * flies back to the player using a simple lerp-towards approach.
 */
public final class ThrowReturnAbility implements Ability {

    private static final String COOLDOWN_KEY = "relic_throw";

    private final RelicWeaponPlugin plugin;

    public ThrowReturnAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(Player player, ItemStack item) {
        if (!player.isSneaking()) return;
        if (!plugin.cfg().isThrowEnabled()) return;

        if (plugin.cooldowns().isOnCooldown(player, COOLDOWN_KEY)) {
            int remaining = plugin.cooldowns().getRemainingSeconds(player, COOLDOWN_KEY);
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§cThrow on cooldown: §e" + remaining + "s"));
            return;
        }

        // Remove item from hand and spawn a Trident entity
        item.subtract(1);

        Trident trident = player.launchProjectile(Trident.class);
        trident.setVelocity(
            player.getLocation().getDirection()
                  .multiply(plugin.cfg().getThrowPower())
        );
        trident.setLoyaltyLevel(0); // we handle return ourselves
        trident.setPickupStatus(org.bukkit.entity.AbstractArrow.PickupStatus.DISALLOWED);

        // Update model state
        plugin.items().setModelState(item, "thrown");

        // Sound
        player.getWorld().playSound(
            player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);

        plugin.cooldowns().setCooldown(player, COOLDOWN_KEY, plugin.cfg().getCooldown());

        // Schedule return if delay > 0
        int delay = plugin.cfg().getReturnDelayTicks();
        if (delay > 0) {
            scheduleReturn(player, trident, delay);
        }
    }

    @Override
    public void onProjectileHit(Player owner, Trident projectile, org.bukkit.entity.Entity hitEntity) {
        if (!plugin.cfg().isThrowEnabled()) return;
        if (!plugin.cfg().isReturnOnHit()) return;

        // Cancel any pending return task and immediately fly back
        plugin.cooldowns().cancelTask(owner.getUniqueId());
        scheduleReturn(owner, projectile, 5);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void scheduleReturn(Player player, Trident trident, int delayTicks) {
        BukkitTask task = plugin.getServer().getScheduler()
            .runTaskLater(plugin, () -> flyBack(player, trident), delayTicks);
        plugin.cooldowns().registerTask(player.getUniqueId(), task);
    }

    private void flyBack(Player player, Trident trident) {
        if (trident.isDead() || !trident.isValid()) {
            restoreItem(player);
            return;
        }

        // Lerp the trident toward the player every tick
        BukkitTask flyTask = plugin.getServer().getScheduler()
            .runTaskTimer(plugin, new Runnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    ticks++;
                    if (!player.isOnline() || trident.isDead() || !trident.isValid() || ticks > 80) {
                        trident.remove();
                        restoreItem(player);
                        // Cancel self — we need the task reference; use wrapper
                        plugin.cooldowns().cancelTask(player.getUniqueId());
                        return;
                    }

                    Vector toPlayer = player.getLocation().add(0, 1, 0)
                                            .toVector()
                                            .subtract(trident.getLocation().toVector());

                    if (toPlayer.length() < 1.5) {
                        // Caught!
                        trident.remove();
                        restoreItem(player);
                        player.getWorld().playSound(
                            player.getLocation(), Sound.ITEM_TRIDENT_HIT_GROUND, 0.8f, 1.4f);
                        player.getWorld().spawnParticle(
                            Particle.TOTEM_OF_UNDYING,
                            player.getLocation().add(0, 1, 0),
                            6, 0.2, 0.2, 0.2, 0.1
                        );
                        plugin.cooldowns().cancelTask(player.getUniqueId());
                        return;
                    }

                    trident.setVelocity(toPlayer.normalize().multiply(
                        plugin.cfg().getLoyaltyLevel() + 1.5));
                }
            }, 0L, 1L);

        plugin.cooldowns().registerTask(player.getUniqueId(), flyTask);
    }

    private void restoreItem(Player player) {
        ItemStack weapon = plugin.items().createRelicWeapon();
        plugin.items().setModelState(weapon, "idle");
        player.getInventory().addItem(weapon);
        player.getWorld().playSound(
            player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1f, 1f);
    }
}
