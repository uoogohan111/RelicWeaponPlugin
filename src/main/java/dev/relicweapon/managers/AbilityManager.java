package dev.relicweapon.managers;

import dev.relicweapon.RelicWeaponPlugin;
import dev.relicweapon.abilities.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Central ability registry.
 * <p>
 * Abilities are registered in order. Each ability independently decides whether
 * it should fire; AbilityManager just iterates them and lets them handle the
 * trigger event.  Adding new abilities requires only implementing {@link Ability}
 * and registering it here.
 */
public final class AbilityManager {

    private final List<Ability> abilities = new ArrayList<>();

    public AbilityManager(RelicWeaponPlugin plugin) {
        // Register all modular abilities
        register(new MobilityAbility(plugin));
        register(new ImpactAbility(plugin));
        register(new ThrowReturnAbility(plugin));
        register(new MaceSmashAbility(plugin));
        register(new SpearLungeAbility(plugin));
        register(new TridentRiptideAbility(plugin));
        register(new TridentChannelingAbility(plugin));
    }

    public void register(Ability ability) {
        abilities.add(ability);
    }

    // ─── Trigger entry points called by Listeners ───────────────────────────

    /** Called by InteractListener on RIGHT_CLICK_AIR / RIGHT_CLICK_BLOCK */
    public void onRightClick(Player player, ItemStack item) {
        abilities.forEach(a -> a.onRightClick(player, item));
    }

    /** Called by InteractListener on LEFT_CLICK (swing) */
    public void onLeftClick(Player player, ItemStack item) {
        abilities.forEach(a -> a.onLeftClick(player, item));
    }

    /** Called by DamageListener when relic weapon hits an entity */
    public void onHitEntity(Player attacker, org.bukkit.entity.Entity victim,
                            ItemStack weapon, double fallDistance) {
        abilities.forEach(a -> a.onHitEntity(attacker, victim, weapon, fallDistance));
    }

    /** Called by ProjectileListener when a thrown relic weapon projectile lands */
    public void onProjectileHit(Player owner, org.bukkit.entity.Trident projectile,
                                 org.bukkit.entity.Entity hitEntity) {
        abilities.forEach(a -> a.onProjectileHit(owner, projectile, hitEntity));
    }

    public List<Ability> getAbilities() { return List.copyOf(abilities); }
}
