package dev.relicweapon.abilities;

import dev.relicweapon.RelicWeaponPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Riptide mechanic: right-click while in water or during rain/thunder launches
 * the player in the direction they're looking, just like the Riptide enchantment
 * on a vanilla trident.
 */
public final class TridentRiptideAbility implements Ability {

    private static final String COOLDOWN_KEY = "relic_riptide";

    private final RelicWeaponPlugin plugin;

    public TridentRiptideAbility(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRightClick(Player player, ItemStack item) {
        if (!plugin.cfg().isRiptideEnabled()) return;
        if (player.isSneaking()) return; // sneak is throw

        boolean inWater  = player.isInWater();
        boolean inRain   = player.getWorld().hasStorm()
                        && !player.getWorld().isClearWeather()
                        && player.getLocation().getBlock().getLightFromSky() >= 15;

        if (!inWater && !inRain) return;

        if (plugin.cooldowns().isOnCooldown(player, COOLDOWN_KEY)) {
            int remaining = plugin.cooldowns().getRemainingSeconds(player, COOLDOWN_KEY);
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§3Riptide on cooldown: §e" + remaining + "s"));
            return;
        }

        double power = plugin.cfg().getRiptidePower();
        Vector dir   = player.getLocation().getDirection().normalize().multiply(power);
        player.setVelocity(dir);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1f, 1f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 0.5f, 1f);

        plugin.cooldowns().setCooldown(player, COOLDOWN_KEY, plugin.cfg().getCooldown());
    }
}
