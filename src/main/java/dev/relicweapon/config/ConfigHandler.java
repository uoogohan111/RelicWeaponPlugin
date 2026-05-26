package dev.relicweapon.config;

import dev.relicweapon.RelicWeaponPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Centralises access to all config.yml values.
 * Call reload() after reloadConfig() to refresh cached values.
 */
public final class ConfigHandler {

    private final RelicWeaponPlugin plugin;

    // ── Cached values ───────────────────────────────────────────────────────
    private int    cooldown;
    private double dashPower;
    private double dashVertical;
    private boolean dashParticles;

    private boolean throwEnabled;
    private double  throwPower;
    private int     returnDelayTicks;
    private boolean returnOnHit;

    private double  damageMultiplier;
    private double  fallDamagePerBlock;
    private double  fallDamageCap;
    private double  windBurstRadius;
    private double  windBurstPower;

    private double  extraReach;
    private int     chargeLungeTicks;
    private double  lungeSpeedDamageMultiplier;

    private boolean riptideEnabled;
    private double  riptidePower;
    private boolean channelingEnabled;
    private int     loyaltyLevel;

    private int modelId;
    private int modelIdCharged;
    private int modelIdThrown;

    private String   displayName;
    private List<String> lore;

    // ────────────────────────────────────────────────────────────────────────

    public ConfigHandler(RelicWeaponPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        FileConfiguration c = plugin.getConfig();

        cooldown        = c.getInt("relic_weapon.cooldown", 8);
        dashPower       = c.getDouble("relic_weapon.dash_power", 1.4);
        dashVertical    = c.getDouble("relic_weapon.dash_vertical", 0.3);
        dashParticles   = c.getBoolean("relic_weapon.dash_particles", true);

        throwEnabled      = c.getBoolean("relic_weapon.throw_enabled", true);
        throwPower        = c.getDouble("relic_weapon.throw_power", 2.5);
        returnDelayTicks  = c.getInt("relic_weapon.return_delay_ticks", 60);
        returnOnHit       = c.getBoolean("relic_weapon.return_on_hit", true);

        damageMultiplier        = c.getDouble("relic_weapon.damage_multiplier", 2.0);
        fallDamagePerBlock      = c.getDouble("relic_weapon.fall_damage_per_block", 0.5);
        fallDamageCap           = c.getDouble("relic_weapon.fall_damage_cap", 20.0);
        windBurstRadius         = c.getDouble("relic_weapon.wind_burst_radius", 3.0);
        windBurstPower          = c.getDouble("relic_weapon.wind_burst_power", 1.5);

        extraReach                  = c.getDouble("relic_weapon.extra_reach", 1.0);
        chargeLungeTicks            = c.getInt("relic_weapon.charge_lunge_ticks", 20);
        lungeSpeedDamageMultiplier  = c.getDouble("relic_weapon.lunge_speed_damage_multiplier", 1.8);

        riptideEnabled    = c.getBoolean("relic_weapon.riptide_enabled", true);
        riptidePower      = c.getDouble("relic_weapon.riptide_power", 3.0);
        channelingEnabled = c.getBoolean("relic_weapon.channeling_enabled", true);
        loyaltyLevel      = c.getInt("relic_weapon.loyalty_level", 2);

        modelId        = c.getInt("relic_weapon.model_id",         1001);
        modelIdCharged = c.getInt("relic_weapon.model_id_charged", 1002);
        modelIdThrown  = c.getInt("relic_weapon.model_id_thrown",  1003);

        displayName = c.getString("relic_weapon.display_name", "&5&l⚡ Relic Weapon");
        lore        = c.getStringList("relic_weapon.lore");
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public int     getCooldown()                   { return cooldown; }
    public double  getDashPower()                  { return dashPower; }
    public double  getDashVertical()               { return dashVertical; }
    public boolean isDashParticles()               { return dashParticles; }

    public boolean isThrowEnabled()                { return throwEnabled; }
    public double  getThrowPower()                 { return throwPower; }
    public int     getReturnDelayTicks()           { return returnDelayTicks; }
    public boolean isReturnOnHit()                 { return returnOnHit; }

    public double  getDamageMultiplier()           { return damageMultiplier; }
    public double  getFallDamagePerBlock()         { return fallDamagePerBlock; }
    public double  getFallDamageCap()              { return fallDamageCap; }
    public double  getWindBurstRadius()            { return windBurstRadius; }
    public double  getWindBurstPower()             { return windBurstPower; }

    public double  getExtraReach()                 { return extraReach; }
    public int     getChargeLungeTicks()           { return chargeLungeTicks; }
    public double  getLungeSpeedDamageMultiplier() { return lungeSpeedDamageMultiplier; }

    public boolean isRiptideEnabled()              { return riptideEnabled; }
    public double  getRiptidePower()               { return riptidePower; }
    public boolean isChannelingEnabled()           { return channelingEnabled; }
    public int     getLoyaltyLevel()               { return loyaltyLevel; }

    public int     getModelId()                    { return modelId; }
    public int     getModelIdCharged()             { return modelIdCharged; }
    public int     getModelIdThrown()              { return modelIdThrown; }

    public Component getDisplayNameComponent() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
    }

    public List<Component> getLoreComponents() {
        return lore.stream()
                   .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                   .toList();
    }
}
