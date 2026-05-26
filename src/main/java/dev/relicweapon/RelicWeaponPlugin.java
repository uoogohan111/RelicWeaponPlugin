package dev.relicweapon;

import dev.relicweapon.config.ConfigHandler;
import dev.relicweapon.cooldown.CooldownManager;
import dev.relicweapon.items.ItemManager;
import dev.relicweapon.listeners.DamageListener;
import dev.relicweapon.listeners.InteractListener;
import dev.relicweapon.listeners.ProjectileListener;
import dev.relicweapon.managers.AbilityManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.logging.Logger;

public final class RelicWeaponPlugin extends JavaPlugin {

    private static RelicWeaponPlugin instance;

    private ConfigHandler configHandler;
    private CooldownManager cooldownManager;
    private ItemManager itemManager;
    private AbilityManager abilityManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialise managers (order matters)
        configHandler   = new ConfigHandler(this);
        cooldownManager = new CooldownManager(this);
        itemManager     = new ItemManager(this);
        abilityManager  = new AbilityManager(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new InteractListener(this),    this);
        getServer().getPluginManager().registerEvents(new DamageListener(this),      this);
        getServer().getPluginManager().registerEvents(new ProjectileListener(this),  this);

        Logger log = getLogger();
        log.info("RelicWeaponPlugin enabled! Paper 26.1 / Java 25");
        log.info("Model IDs: idle=" + configHandler.getModelId()
                + " charged=" + configHandler.getModelIdCharged()
                + " thrown=" + configHandler.getModelIdThrown());
    }

    @Override
    public void onDisable() {
        if (cooldownManager != null) cooldownManager.cancelAll();
        getLogger().info("RelicWeaponPlugin disabled.");
    }

    // ─── Command handler ────────────────────────────────────────────────────────

    @Override
    public boolean onCommand(@NonNull CommandSender sender,
                             @NonNull Command command,
                             @NonNull String label,
                             String @NonNull [] args) {

        if (!command.getName().equalsIgnoreCase("relicweapon")) return false;

        if (args.length == 0 || args[0].equalsIgnoreCase("give")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can receive the Relic Weapon.");
                return true;
            }
            if (!player.hasPermission("relicweapon.give")) {
                player.sendMessage("§cYou don't have permission.");
                return true;
            }
            player.getInventory().addItem(itemManager.createRelicWeapon());
            player.sendMessage("§5§lRelic Weapon §7has been added to your inventory!");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("relicweapon.reload")) {
                sender.sendMessage("§cYou don't have permission.");
                return true;
            }
            reloadConfig();
            configHandler.reload();
            sender.sendMessage("§aRelicWeapon config reloaded.");
            return true;
        }

        sender.sendMessage("§eUsage: /relicweapon [give|reload]");
        return true;
    }

    // ─── Static accessor ────────────────────────────────────────────────────────

    public static RelicWeaponPlugin get() { return instance; }

    public ConfigHandler   cfg()       { return configHandler; }
    public CooldownManager cooldowns() { return cooldownManager; }
    public ItemManager     items()     { return itemManager; }
    public AbilityManager  abilities() { return abilityManager; }
}
