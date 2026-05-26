package dev.relicweapon.listeners;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Intercepts melee damage events where the attacker holds the Relic Weapon.
 * Applies the base damage multiplier then delegates to the ability system.
 */
public final class DamageListener implements Listener {

    private final RelicWeaponPlugin plugin;

    public DamageListener(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        ItemStack mainHand = attacker.getInventory().getItemInMainHand();
        if (!plugin.items().isRelicWeapon(mainHand)) return;

        // ── Base damage multiplier ───────────────────────────────────────────
        double base       = event.getDamage();
        double multiplied = base * plugin.cfg().getDamageMultiplier();
        event.setDamage(multiplied);

        // ── Capture fall distance before it resets ───────────────────────────
        double fallDistance = attacker.getFallDistance();

        Entity victim = event.getEntity();

        // Delegate to ability modules
        plugin.abilities().onHitEntity(attacker, victim, mainHand, fallDistance);
    }
}
