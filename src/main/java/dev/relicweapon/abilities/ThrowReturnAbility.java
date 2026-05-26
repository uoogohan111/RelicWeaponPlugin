package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ThrowReturnAbility implements Ability {

    private static final String COOLDOWN_KEY = "relic_throw";

    private final RelicWeaponPlugin plugin;

    /**
     * UUIDs of players whose throw was launched by US (not vanilla).
     * InteractListener checks this to allow our projectile through.
     */
    public final Set<UUID> ourThrows = new HashSet<>();

    public ThrowReturnAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(Player player, ItemStack item) {
        if (!player.isSneaking()) return;
        if (!plugin.cfg().isThrowEnabled()) return;

        if (plugin.cooldowns().isOnCooldown(player, COOLDOWN_KEY)) {
            int remaining = plugin.cooldowns().getRemainingSeconds(player, COOLDOWN_KEY);
            player.sendActionBar(Component.text("§cThrow on cooldown: §e" + remaining + "s"));
            return;
        }

        // Remove one item from hand
        item.subtract(1);

        // Flag that the next trident launch from this player is ours
        ourThrows.add(player.getUniqueId());

        Trident trident = player.launchProjectile(Trident.class);
        trident.setVelocity(
            player.getLocation().getDirection()
                  .multiply(plugin.cfg().getThrowPower())
        );
        trident.setLoyaltyLevel(0);
        trident.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

        // Clear flag immediately after launch
        ourThrows.remove(player.getUniqueId());

        // Update model state on a new item if any remain
        ItemStack held = player.getInventory().getItemInMainHand();
        if (plugin.items().isRelicWeapon(held)) {
            plugin.items().setModelState(held, "thrown");
        }

        player.getWorld().playSound(
            player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1f, 1f);
        player.getWorld().spawnParticle(
            Particle.SWEEP_ATTACK,
            player.getLocation().add(0, 1, 0),
            8, 0.3, 0.3, 0.3, 0.05
        );

        plugin.cooldowns().setCooldown(player, COOLDOWN_KEY, plugin.cfg().getCooldown());

        int delay = plugin.cfg().getReturnDelayTicks();
        if (delay > 0) {
            scheduleReturn(player, trident, delay);
        }
    }

    @Override
    public void onProjectileHit(Player owner, Trident projectile,
                                 org.bukkit.entity.Entity hitEntity) {
        if (!plugin.cfg().isThrowEnabled()) return;
        if (!plugin.cfg().isReturnOnHit()) return;

        plugin.cooldowns().cancelTask(owner.getUniqueId());
        scheduleReturn(owner, projectile, 5);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

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

        BukkitTask flyTask = plugin.getServer().getScheduler()
            .runTaskTimer(plugin, new Runnable() {
                private int ticks = 0;

                @Override
                public void run() {
                    ticks++;
                    if (!player.isOnline() || trident.isDead()
                            || !trident.isValid() || ticks > 80) {
                        trident.remove();
                        restoreItem(player);
                        plugin.cooldowns().cancelTask(player.getUniqueId());
                        return;
                    }

                    Vector toPlayer = player.getLocation().add(0, 1, 0)
                            .toVector()
                            .subtract(trident.getLocation().toVector());

                    if (toPlayer.length() < 1.5) {
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

                    trident.setVelocity(toPlayer.normalize()
                        .multiply(plugin.cfg().getLoyaltyLevel() + 1.5));
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
