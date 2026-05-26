package dev.relicweapon.listeners;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * Listens for Trident projectile hit events.
 * Only processes projectiles launched by a player holding / having thrown
 * the Relic Weapon — identified by the shooter being a Player.
 *
 * Note: Because we spawn the Trident via {@code Player#launchProjectile} the
 * item data is embedded in the Trident entity's item stack via PersistentData
 * set at spawn time in ThrowReturnAbility.
 */
public final class ProjectileListener implements Listener {

    private final RelicWeaponPlugin plugin;

    public ProjectileListener(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player owner)) return;

        // We only care about tridents whose item matches the relic weapon
        // Paper's Trident entity exposes getItem() in 1.21+
        if (!plugin.items().isRelicWeapon(trident.getItem())) return;

        Entity hitEntity = event.getHitEntity(); // null = block hit

        // Cancel vanilla pickup so our return system handles the item
        event.setCancelled(true);

        plugin.abilities().onProjectileHit(owner, trident, hitEntity);
    }
}
