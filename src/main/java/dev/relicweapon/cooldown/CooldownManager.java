package dev.relicweapon.cooldown;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks per-player, per-ability cooldowns.
 * Abilities are identified by a String key so the system is fully extensible.
 */
public final class CooldownManager {

    private final RelicWeaponPlugin plugin;

    /** player-uuid → (ability-key → expiry-ms) */
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    /** Tracked BukkitTasks (for cleanup on disable) */
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    public CooldownManager(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns true if the player is on cooldown for the given ability.
     */
    public boolean isOnCooldown(Player player, String abilityKey) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return false;
        Long expiry = map.get(abilityKey);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            map.remove(abilityKey);
            return false;
        }
        return true;
    }

    /**
     * Sets a cooldown for the player on the given ability.
     *
     * @param seconds number of seconds for the cooldown
     */
    public void setCooldown(Player player, String abilityKey, int seconds) {
        cooldowns
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(abilityKey, System.currentTimeMillis() + (seconds * 1000L));
    }

    /**
     * Returns remaining cooldown in seconds (0 if none).
     */
    public int getRemainingSeconds(Player player, String abilityKey) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        Long expiry = map.get(abilityKey);
        if (expiry == null) return 0;
        long remaining = expiry - System.currentTimeMillis();
        return remaining <= 0 ? 0 : (int) Math.ceil(remaining / 1000.0);
    }

    /**
     * Clears a specific cooldown for a player.
     */
    public void clearCooldown(Player player, String abilityKey) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map != null) map.remove(abilityKey);
    }

    /**
     * Register a scheduled BukkitTask associated with a player (e.g. return timer).
     * Cancels any existing task for that player first.
     */
    public void registerTask(UUID playerUuid, BukkitTask task) {
        BukkitTask existing = tasks.put(playerUuid, task);
        if (existing != null) existing.cancel();
    }

    /**
     * Cancel and remove a registered task for a player.
     */
    public void cancelTask(UUID playerUuid) {
        BukkitTask task = tasks.remove(playerUuid);
        if (task != null) task.cancel();
    }

    /**
     * Cancel all registered tasks (called on plugin disable).
     */
    public void cancelAll() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
        cooldowns.clear();
    }
}
