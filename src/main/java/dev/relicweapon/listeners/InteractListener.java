package dev.relicweapon.listeners;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles right-click and left-click interactions with the Relic Weapon.
 */
public final class InteractListener implements Listener {

    private final RelicWeaponPlugin plugin;

    public InteractListener(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!plugin.items().isRelicWeapon(item)) return;

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            // Prevent placing / using the item as a block
            event.setCancelled(true);
            plugin.abilities().onRightClick(event.getPlayer(), item);
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            plugin.abilities().onLeftClick(event.getPlayer(), item);
        }
    }
}
