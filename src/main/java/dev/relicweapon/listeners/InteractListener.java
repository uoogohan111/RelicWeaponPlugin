package dev.relicweapon.listeners;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class InteractListener implements Listener {

    private final RelicWeaponPlugin plugin;

    public InteractListener(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Block vanilla trident throws completely.
     * Paper fires ProjectileLaunchEvent when the engine launches the trident —
     * we cancel it here so our ThrowReturnAbility controls throwing instead.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;

        // Check main hand
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off  = player.getInventory().getItemInOffHand();

        if (plugin.items().isRelicWeapon(main) || plugin.items().isRelicWeapon(off)) {
            // Cancel the vanilla throw — our ability handles it on sneak+RMB
            event.setCancelled(true);
        }
    }

    /**
     * Handle right-click and left-click on the Relic Weapon.
     * LOWEST priority so we fire before vanilla processes the trident.
     * ignoreCancelled = false so we still fire even if something else cancelled.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        // Only fire once — ignore off-hand duplicate events
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();

        // Prefer main hand, fall back to off hand
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.items().isRelicWeapon(item)) {
            item = player.getInventory().getItemInOffHand();
            if (!plugin.items().isRelicWeapon(item)) return;
        }

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            // Cancel vanilla interaction (prevents block placement, trident charging, etc.)
            event.setCancelled(true);
            plugin.abilities().onRightClick(player, item);

        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            plugin.abilities().onLeftClick(player, item);
        }
    }
}
