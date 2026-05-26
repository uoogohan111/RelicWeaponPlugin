package dev.relicweapon;

import dev.relicweapon.config.ConfigHandler;
import dev.relicweapon.cooldown.CooldownManager;
import dev.relicweapon.items.ItemManager;
import dev.relicweapon.listeners.DamageListener;
import dev.relicweapon.listeners.InteractListener;
import dev.relicweapon.listeners.ProjectileListener;
import dev.relicweapon.managers.AbilityManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
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

        // Modern Paper Command Registration
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            commands.register(
                Commands.literal("relicweapon")
                    // /relicweapon give
                    .then(Commands.literal("give").executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage("§cOnly players can receive the Relic Weapon.");
                            return 1;
                        }
                        if (!player.hasPermission("relicweapon.give")) {
                            player.sendMessage("§cYou don't have permission.");
                            return 1;
                        }
                        player.getInventory().addItem(itemManager.createRelicWeapon());
                        player.sendMessage("§5§lRelic Weapon §7has been added to your inventory!");
                        return 1;
                    }))
                    // /relicweapon reload
                    .then(Commands.literal("reload").executes(ctx -> {
                        CommandSender sender = ctx.getSource().getSender();
                        if (!sender.hasPermission("relicweapon.reload")) {
                            sender.sendMessage("§cYou don't have permission.");
                            return 1;
                        }
                        reloadConfig();
                        configHandler.reload();
                        sender.sendMessage("§aRelicWeapon config reloaded.");
                        return 1;
                    }))
                    .executes(ctx -> {
                        // Default fallback when typing just /relicweapon with no arguments
                        ctx.getSource().getSender().sendMessage("§eUsage: /relicweapon [give|reload]");
                        return 1;
                    })
                    .build(),
                "Main command for RelicWeaponPlugin",
                List.of("rw") // Handles the /rw shorthand alias automatically
            );
        });

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

    // ─── Static accessor ────────────────────────────────────────────────────────

    public static RelicWeaponPlugin get() { return instance; }

    public ConfigHandler   cfg()       { return configHandler; }
    public CooldownManager cooldowns() { return cooldownManager; }
    public ItemManager     items()     { return itemManager; }
    public AbilityManager  abilities() { return abilityManager; }
}
