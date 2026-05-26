package dev.relicweapon.listeners;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class DamageListener implements Listener {

    private final RelicWeaponPlugin plugin;

    /**
     * Guards against re-entrant damage events.
     * When MaceSmashAbility calls le.damage(), Paper fires EntityDamageByEntityEvent
     * again on the same thread — this set breaks that infinite loop.
     */
    private final Set<UUID> processing = Collections.synchronizedSet(new HashSet<>());

    public DamageListener(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        // If we're already processing a hit for this player, this is a
        // recursive call from le.damage() inside an ability — skip it entirely.
        if (processing.contains(attacker.getUniqueId())) return;

        ItemStack mainHand = attacker.getInventory().getItemInMainHand();
        if (!plugin.items().isRelicWeapon(mainHand)) return;

        // Apply base damage multiplier
        double base       = event.getDamage();
        double multiplied = base * plugin.cfg().getDamageMultiplier();
        event.setDamage(multiplied);

        // Capture fall distance before abilities reset it
        double fallDistance = attacker.getFallDistance();
        Entity victim = event.getEntity();

        // Lock, run abilities, unlock — any le.damage() calls inside will be ignored
        processing.add(attacker.getUniqueId());
        try {
            plugin.abilities().onHitEntity(attacker, victim, mainHand, fallDistance);
        } finally {
            processing.remove(attacker.getUniqueId());
        }
    }
}
