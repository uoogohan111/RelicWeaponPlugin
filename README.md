# ⚡ RelicWeaponPlugin

A modular Paper plugin (26.1.2 / Java 25) that adds the **Relic Weapon** — a
single custom item combining Spear, Mace, and Trident abilities into one
highly configurable, enchantable weapon.

---

## ✨ Feature Overview

| System | Description |
|---|---|
| **Mobility / Dash** | Right-click to dash in the direction you're looking |
| **Spear Lunge** | Hold right-click to charge; next hit deals speed-bonus damage |
| **Mace Smash** | Fall damage bonus + AOE wind-burst knockback on hit |
| **Trident Riptide** | Right-click in water or rain to launch yourself |
| **Trident Channeling** | Hit an entity during a thunderstorm to summon lightning |
| **Throw / Return** | Sneak + Right-click to throw; auto-returns on hit or after a timer |
| **Enchant Support** | All vanilla trident/weapon enchantments work normally |
| **Resource Pack** | Three model states: `idle`, `charged`, `thrown` via CustomModelData |

---

## 🔧 Requirements

| Requirement | Version |
|---|---|
| Java | **25** |
| Paper | **26.1.2** |
| Gradle | 8.12 (wrapper bundled) |

---

## 🚀 Quick Start

### 1. Clone and Build

```bash
git clone https://github.com/your-username/RelicWeaponPlugin
cd RelicWeaponPlugin
./gradlew build
```

The compiled JAR is in `build/libs/RelicWeaponPlugin-1.0.0.jar`.

### 2. Install

Copy the JAR into your Paper server's `plugins/` folder and restart.

### 3. Get the Weapon In-Game

```
/relicweapon give
# or shorthand:
/rw give
```

### 4. Reload Config

```
/relicweapon reload
```

---

## ⚙️ Configuration (`plugins/RelicWeaponPlugin/config.yml`)

```yaml
relic_weapon:
  cooldown: 8                    # seconds between ability uses
  dash_power: 1.4                # horizontal dash strength
  dash_vertical: 0.3             # upward component of dash
  dash_particles: true

  throw_enabled: true
  throw_power: 2.5
  return_delay_ticks: 60         # ticks before auto-return (0 = off)
  return_on_hit: true            # return immediately on hit

  damage_multiplier: 2.0         # base melee damage multiplier
  fall_damage_per_block: 0.5     # bonus damage per block fallen (mace)
  fall_damage_cap: 20.0          # cap on fall bonus (0 = no cap)
  wind_burst_radius: 3.0         # AOE knockback radius
  wind_burst_power: 1.5

  extra_reach: 1.0               # extra melee reach (spear mechanic)
  charge_lunge_ticks: 20         # ticks to hold for full charge
  lunge_speed_damage_multiplier: 1.8

  riptide_enabled: true
  riptide_power: 3.0
  channeling_enabled: true
  loyalty_level: 2               # return speed (1–3)

  model_id: 1001                 # CustomModelData: idle
  model_id_charged: 1002         # CustomModelData: charged
  model_id_thrown: 1003          # CustomModelData: thrown

  display_name: "&5&l⚡ Relic Weapon"
  lore:
    - "&7A weapon forged from ancient relics."
    - ...
```

---

## 🎮 Controls

| Action | Effect |
|---|---|
| `Right-Click` (ground) | **Dash** in look direction |
| `Right-Click` (hold) | **Charge** lunge; release and attack for bonus damage |
| `Right-Click` (in water/rain) | **Riptide** launch |
| `Sneak + Right-Click` | **Throw** the weapon |
| Melee hit while falling | **Mace smash** — fall bonus + AOE knockback |
| Melee hit in thunderstorm | **Channeling** — lightning bolt on target |

---

## 🗂️ Project Structure

```
RelicWeaponPlugin/
├── .github/
│   └── workflows/
│       └── build.yml              ← GitHub Actions CI
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── src/main/
│   ├── java/dev/relicweapon/
│   │   ├── RelicWeaponPlugin.java  ← Main plugin class
│   │   ├── abilities/
│   │   │   ├── Ability.java        ← Interface (extend this for new abilities)
│   │   │   ├── MobilityAbility.java
│   │   │   ├── ImpactAbility.java
│   │   │   ├── ThrowReturnAbility.java
│   │   │   ├── MaceSmashAbility.java
│   │   │   ├── SpearLungeAbility.java
│   │   │   ├── TridentRiptideAbility.java
│   │   │   └── TridentChannelingAbility.java
│   │   ├── config/
│   │   │   └── ConfigHandler.java
│   │   ├── cooldown/
│   │   │   └── CooldownManager.java
│   │   ├── items/
│   │   │   └── ItemManager.java
│   │   ├── listeners/
│   │   │   ├── InteractListener.java
│   │   │   ├── DamageListener.java
│   │   │   └── ProjectileListener.java
│   │   └── managers/
│   │       └── AbilityManager.java
│   └── resources/
│       ├── paper-plugin.yml
│       └── config.yml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🔌 Adding a Custom Ability

1. Create a class in `dev.relicweapon.abilities` that implements `Ability`.
2. Override only the methods you need (`onRightClick`, `onHitEntity`, etc.).
3. Register it in `AbilityManager`:

```java
register(new MyCustomAbility(plugin));
```

That's it — no other files to change.

---

## 🖼️ Resource Pack (CustomModelData)

The weapon uses three model IDs:

| State | Default ID | Config key |
|---|---|---|
| Idle / held | `1001` | `model_id` |
| Charged (lunge ready) | `1002` | `model_id_charged` |
| Thrown (in flight) | `1003` | `model_id_thrown` |

In your Blockbench model file, create three separate models under
`assets/minecraft/models/item/` and reference them via custom model data
overrides in your `trident.json` override file.

---

## 🏗️ GitHub Actions

Every push triggers `.github/workflows/build.yml`, which:
- Spins up Ubuntu with Java 25 (Temurin)
- Runs `./gradlew build`
- Uploads the compiled JAR as a workflow artifact (kept 14 days)

---

## 📜 License

MIT — do whatever you want, just don't claim you wrote it.
