# Meteor settings index

Source: [Meteor registry at `e3a2e7796762`](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/Modules.java).

This is a source index of registered modules and declared setting names/defaults. A blank default means the value is supplied by the setting type or another expression. Conditional and computed controls require source review. It is a reference for independent Wanderlust implementations, not a feature-completeness claim.

Registry entries indexed: **169**. This file lists setting identifiers and source defaults, including controls whose behavior still needs manual review.

## Combat

### [AnchorAura](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AnchorAura.java)

| Setting | Source default |
| --- | --- |
| `target-range` | `10` |
| `target-priority` | `SortPriority.LowestHealth` |
| `min-damage` | `7` |
| `max-self-damage` | `7` |
| `anti-suicide` | `true` |
| `swap-back` | `true` |
| `rotate` | `true` |
| `place` | `true` |
| `place-delay` | `5` |
| `place-range` | `4` |
| `walls-range` | `4` |
| `air-place` | `true` |
| `charge-delay` | `1` |
| `break-delay` | `1` |
| `break-range` | `4.5` |
| `walls-range` | `4.5` |
| `pause-on-use` | `true` |
| `pause-on-mine` | `true` |
| `pause-on-CA` | `true` |
| `swing` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(15, 255, 211, 41)` |
| `line-color` | `new SettingColor(15, 255, 211)` |

### [AntiAnvil](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AntiAnvil.java)

| Setting | Source default |
| --- | --- |
| `swing` | `true` |
| `rotate` | `true` |

### [AntiBed](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AntiBed.java)

| Setting | Source default |
| --- | --- |
| `place-string-top` | `false` |
| `place-string-middle` | `true` |
| `place-string-bottom` | `false` |
| `only-in-hole` | `true` |

### [ArrowDodge](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/ArrowDodge.java)

| Setting | Source default |
| --- | --- |
| `move-type` | `MoveType.Velocity` |
| `move-speed` | `1` |
| `distance-check` | `1` |
| `ground-check` | `true` |
| `all-projectiles` | `false` |
| `ignore-own` | `true` |
| `simulation-steps` | `500` |

### [AttributeSwap](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AttributeSwap.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Simple` |
| `target-slot` | `1` |
| `swap-on-miss` | `false` |
| `swap-back` | `true` |
| `swap-back-delay` | `2` |
| `shield-breaker` | `true` |
| `durability-saver` | `true` |
| `sword-swapping` | `true` |
| `mace-swapping` | `true` |
| `spear-swapping` | `true` |
| `other-swapping` | `true` |
| `fire-aspect` | `true` |
| `looting` | `true` |
| `sharpness` | `true` |
| `smite` | `true` |
| `bane-of-arthropods` | `true` |
| `sweeping-edge` | `true` |
| `regular-mace` | `true` |
| `density` | `true` |
| `breach` | `true` |
| `wind-burst` | `true` |
| `impaling` | `true` |
| `lunge` | `true` |
| `hitbox` | `true` |
| `exclude-lunge-from-hitbox` | `true` |
| `only-on-weapon` | `false` |
| `sword` | `true` |
| `axe` | `true` |
| `pickaxe` | `true` |
| `shovel` | `true` |
| `hoe` | `true` |
| `mace` | `true` |
| `spear` | `true` |
| `trident` | `true` |

### [AutoAnvil](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoAnvil.java)

| Setting | Source default |
| --- | --- |
| `target-range` | `4` |
| `target-priority` | `SortPriority.LowestHealth` |
| `height` | `2` |
| `delay` | `10` |
| `place-at-feet` | `true` |
| `multi-place` | `true` |
| `toggle-on-break` | `false` |
| `rotate` | `true` |

### [AutoArmor](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoArmor.java)

| Setting | Source default |
| --- | --- |
| `preferred-protection` | `Protection.Protection` |
| `swap-delay` | `1` |
| `avoided-enchantments` | `Enchantments.BINDING_CURSE, Enchantments.FROST_WALKER` |
| `blast-prot-leggings` | `true` |
| `anti-break` | `false` |
| `ignore-elytra` | `true` |

### [AutoCity](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoCity.java)

| Setting | Source default |
| --- | --- |
| `target-range` | `5.5` |
| `break-range` | `4.5` |
| `switch-mode` | `SwitchMode.Normal` |
| `support` | `true` |
| `place-range` | `4.5` |
| `rotate` | `true` |
| `chat-info` | `true` |
| `swing-hand` | `false` |
| `render-block` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(225, 0, 0, 75)` |
| `line-color` | `new SettingColor(225, 0, 0, 255)` |

### [AutoEXP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoEXP.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Both` |
| `replenish` | `true` |
| `only-on-ground` | `false` |
| `exp-slot` | `6` |
| `min-threshold` | `30` |
| `max-threshold` | `80` |

### [AutoLog](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoLog.java)

| Setting | Source default |
| --- | --- |
| `health` | `6` |
| `predict-incoming-damage` | `true` |
| `totem-pops` | `0` |
| `only-trusted` | `false` |
| `32K` | `false` |
| `smart-toggle` | `false` |
| `toggle-off` | `true` |
| `toggle-auto-reconnect` | `true` |
| `entities` | `EntityTypes.END_CRYSTAL` |
| `use-total-count` | `true` |
| `combined-entity-threshold` | `10` |
| `individual-entity-threshold` | `2` |
| `range` | `5` |

### [AutoTotem](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoTotem.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Smart` |
| `delay` | `0` |
| `health` | `10` |
| `elytra` | `true` |
| `fall` | `true` |
| `explosion` | `true` |

### [AutoTrap](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoTrap.java)

| Setting | Source default |
| --- | --- |
| `whitelist` | `Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN` |
| `place-range` | `4` |
| `walls-range` | `4` |
| `target-priority` | `SortPriority.LowestHealth` |
| `target-range` | `3` |
| `place-delay` | `1` |
| `blocks-per-tick` | `1` |
| `top-blocks` | `TopMode.Full` |
| `bottom-blocks` | `BottomMode.Platform` |
| `self-toggle` | `true` |
| `rotate` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(197, 137, 232, 10)` |
| `line-color` | `new SettingColor(197, 137, 232)` |
| `next-side-color` | `new SettingColor(227, 196, 245, 10)` |
| `next-line-color` | `new SettingColor(5, 139, 221)` |

### [AutoWeapon](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoWeapon.java)

| Setting | Source default |
| --- | --- |
| `weapon` | `Weapon.Sword` |
| `threshold` | `4` |
| `anti-break` | `false` |

### [AutoWeb](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/AutoWeb.java)

| Setting | Source default |
| --- | --- |
| `place-range` | `4` |
| `walls-range` | `4` |
| `target-priority` | `SortPriority.LowestDistance` |
| `target-range` | `10` |
| `predict-movement` | `true` |
| `ticks-to-predict` | `10` |
| `doubles` | `false` |
| `rotate` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(239, 231, 244, 31)` |
| `line-color` | `new SettingColor(255, 255, 255)` |

### [BedAura](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/BedAura.java)

| Setting | Source default |
| --- | --- |
| `delay` | `9` |
| `strict-direction` | `false` |
| `target-range` | `4` |
| `target-priority` | `SortPriority.LowestHealth` |
| `min-damage` | `7` |
| `max-self-damage` | `7` |
| `anti-suicide` | `true` |
| `auto-move` | `false` |
| `auto-move-slot` | `9` |
| `auto-switch` | `true` |
| `pause-on-eat` | `true` |
| `pause-on-drink` | `true` |
| `pause-on-mine` | `true` |
| `swing` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(15, 255, 211, 75)` |
| `line-color` | `new SettingColor(15, 255, 211)` |

### [BowAimbot](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/BowAimbot.java)

| Setting | Source default |
| --- | --- |
| `range` | `20` |
| `entities` | `—` |
| `priority` | `SortPriority.LowestHealth` |
| `babies` | `true` |
| `nametagged` | `false` |
| `pause-on-combat` | `false` |

### [BowSpam](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/BowSpam.java)

| Setting | Source default |
| --- | --- |
| `charge` | `5` |
| `when-holding-right-click` | `false` |
| `crossbow-delay` | `10` |
| `search-inventory` | `true` |

### [Burrow](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/Burrow.java)

| Setting | Source default |
| --- | --- |
| `block-to-use` | `Block.EChest` |
| `instant` | `true` |
| `automatic` | `true` |
| `trigger-height` | `1.12` |
| `rubberband-height` | `12` |
| `timer` | `1` |
| `only-in-holes` | `false` |
| `center` | `true` |
| `rotate` | `true` |

### [Criticals](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/Criticals.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Packet` |
| `only-killaura` | `false` |
| `smash-attack` | `true` |
| `additional-height` | `0.0` |

### [CrystalAura](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/CrystalAura.java)

| Setting | Source default |
| --- | --- |
| `target-range` | `10` |
| `predict-movement` | `false` |
| `min-damage` | `6` |
| `max-damage` | `6` |
| `anti-suicide` | `true` |
| `ignore-nakeds` | `false` |
| `rotate` | `true` |
| `yaw-steps-mode` | `YawStepMode.Break` |
| `yaw-steps` | `180` |
| `entities` | `EntityTypes.PLAYER, EntityTypes.WARDEN, EntityTypes.WITHER` |
| `auto-switch` | `AutoSwitchMode.Normal` |
| `switch-delay` | `0` |
| `no-gap-switch` | `true` |
| `no-bow-switch` | `true` |
| `anti-weakness` | `true` |
| `place` | `true` |
| `place-delay` | `0` |
| `place-range` | `4.5` |
| `walls-range` | `4.5` |
| `1.12-placement` | `false` |
| `support` | `SupportMode.Disabled` |
| `support-delay` | `1` |
| `face-place` | `true` |
| `face-place-health` | `8` |
| `face-place-durability` | `2` |
| `face-place-missing-armor` | `false` |
| `force-face-place` | `Keybind.none()` |
| `break` | `true` |
| `break-delay` | `0` |
| `smart-delay` | `false` |
| `break-range` | `4.5` |
| `walls-range` | `4.5` |
| `only-own` | `false` |
| `break-attempts` | `2` |
| `ticks-existed` | `0` |
| `attack-frequency` | `25` |
| `fast-break` | `true` |
| `pause-on-use` | `PauseMode.Place` |
| `pause-on-mine` | `PauseMode.None` |
| `pause-on-lag` | `true` |
| `pause-modules` | `BedAura.class` |
| `pause-health` | `5` |
| `swing-mode` | `SwingMode.Both` |
| `render-mode` | `RenderMode.Normal` |
| `render-place` | `true` |
| `place-time` | `10` |
| `render-break` | `false` |
| `break-time` | `13` |
| `smoothness` | `10` |
| `height` | `0.7` |
| `render-time` | `10` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 255, 255, 45)` |
| `line-color` | `new SettingColor(255, 255, 255)` |
| `damage` | `true` |
| `damage-color` | `new SettingColor(255, 255, 255)` |
| `damage-scale` | `1.25` |

### [Hitboxes](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/Hitboxes.java)

| Setting | Source default |
| --- | --- |
| `entities` | `EntityTypes.PLAYER` |
| `expand` | `0.5` |
| `ignore-friends` | `true` |
| `only-on-weapon` | `false` |
| `sword` | `true` |
| `axe` | `true` |
| `pickaxe` | `true` |
| `shovel` | `true` |
| `hoe` | `true` |
| `mace` | `true` |
| `spear` | `true` |
| `trident` | `true` |

### [HoleFiller](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/HoleFiller.java)

| Setting | Source default |
| --- | --- |
| `blocks` | `Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK, Blocks.RESPAWN_ANCHOR,...` |
| `search-radius` | `5` |
| `place-range` | `4.5` |
| `walls-range` | `4.5` |
| `doubles` | `true` |
| `rotate` | `false` |
| `place-delay` | `1` |
| `blocks-per-tick` | `3` |
| `smart` | `true` |
| `force-fill` | `Keybind.none()` |
| `predict-movement` | `true` |
| `ticks-to-predict` | `10` |
| `ignore-safe` | `true` |
| `only-moving` | `true` |
| `target-range` | `7` |
| `feet-range` | `1.5` |
| `swing` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(197, 137, 232, 10)` |
| `line-color` | `new SettingColor(197, 137, 232)` |
| `next-side-color` | `new SettingColor(227, 196, 245, 10)` |
| `next-line-color` | `new SettingColor(5, 139, 221)` |

### [KillAura](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/KillAura.java)

| Setting | Source default |
| --- | --- |
| `attack-when-holding` | `AttackItems.Weapons` |
| `selected-weapon-types` | `Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.TRIDENT` |
| `rotate` | `RotationMode.Always` |
| `auto-switch` | `false` |
| `swap-back` | `false` |
| `shield-mode` | `ShieldMode.None` |
| `only-on-click` | `false` |
| `only-on-look` | `false` |
| `pause-baritone` | `true` |
| `entities` | `EntityTypes.PLAYER` |
| `priority` | `SortPriority.ClosestAngle` |
| `max-targets` | `1` |
| `range` | `4.5` |
| `walls-range` | `3.5` |
| `passive-mob-age-filter` | `EntityAgeTest.Adult` |
| `hostile-mob-age-filter` | `EntityAgeTest.Both` |
| `ignore-named` | `false` |
| `ignore-passive` | `true` |
| `ignore-tamed` | `false` |
| `pause-on-lag` | `true` |
| `pause-on-use` | `false` |
| `pause-on-CA` | `true` |
| `TPS-sync` | `true` |
| `custom-delay` | `false` |
| `hit-delay` | `11` |
| `switch-delay` | `0` |

### [Offhand](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/Offhand.java)

| Setting | Source default |
| --- | --- |
| `item-switch-delay` | `0` |
| `item` | `Item.Crystal` |
| `hotbar` | `false` |
| `right-gapple` | `false` |
| `sword-gapple` | `false` |
| `always-gap-on-sword` | `false` |
| `always-pot-on-sword` | `false` |
| `sword-pot` | `false` |
| `min-health` | `10` |
| `elytra` | `false` |
| `falling` | `false` |
| `explosion` | `true` |

### [Quiver](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/Quiver.java)

| Setting | Source default |
| --- | --- |
| `effects` | `MobEffects.STRENGTH.value()` |
| `cooldown` | `10` |
| `check-effects` | `true` |
| `silent-bow` | `true` |
| `chat-info` | `false` |
| `only-in-holes` | `true` |
| `only-on-ground` | `true` |
| `min-health` | `10` |

### [SelfAnvil](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/SelfAnvil.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [SelfTrap](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/SelfTrap.java)

| Setting | Source default |
| --- | --- |
| `whitelist` | `Blocks.OBSIDIAN, Blocks.NETHERITE_BLOCK` |
| `top-mode` | `TopMode.Top` |
| `bottom-mode` | `BottomMode.None` |
| `place-delay` | `1` |
| `center` | `true` |
| `turn-off` | `true` |
| `rotate` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(204, 0, 0, 10)` |
| `line-color` | `new SettingColor(204, 0, 0, 255)` |

### [SelfWeb](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/SelfWeb.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Normal` |
| `range` | `3` |
| `double-place` | `false` |
| `auto-toggle` | `true` |
| `rotate` | `true` |

### [Surround](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/combat/Surround.java)

| Setting | Source default |
| --- | --- |
| `blocks` | `Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK` |
| `delay` | `0` |
| `blocks-per-tick` | `1` |
| `center` | `Center.Incomplete` |
| `double-height` | `false` |
| `only-on-ground` | `true` |
| `air-place` | `true` |
| `toggle-modules` | `false` |
| `toggle-back-on` | `false` |
| `modules` | `—` |
| `rotate` | `true` |
| `protect` | `true` |
| `toggle-on-y-change` | `true` |
| `toggle-on-complete` | `false` |
| `toggle-on-death` | `true` |
| `swing` | `true` |
| `render` | `true` |
| `below` | `false` |
| `shape-mode` | `ShapeMode.Both` |
| `safe-side-color` | `new SettingColor(13, 255, 0, 0)` |
| `safe-line-color` | `new SettingColor(13, 255, 0, 0)` |
| `normal-side-color` | `new SettingColor(0, 255, 238, 12)` |
| `normal-line-color` | `new SettingColor(0, 255, 238, 100)` |
| `unsafe-side-color` | `new SettingColor(204, 0, 0, 12)` |
| `unsafe-line-color` | `new SettingColor(204, 0, 0, 100)` |

## Player

### [AirPlace](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AirPlace.java)

| Setting | Source default |
| --- | --- |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(204, 0, 0, 10)` |
| `line-color` | `new SettingColor(204, 0, 0, 255)` |
| `custom-range` | `false` |
| `range` | `5` |

### [AntiAFK](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AntiAFK.java)

| Setting | Source default |
| --- | --- |
| `jump` | `true` |
| `swing` | `false` |
| `sneak` | `false` |
| `sneak-time` | `5` |
| `spin` | `true` |
| `spin-mode` | `SpinMode.Server` |
| `speed` | `7` |
| `pitch` | `0` |
| `send-messages` | `false` |
| `random` | `false` |
| `delay` | `15` |
| `messages` | `"Meteor on top!", "Meteor on crack!"` |

### [AntiHunger](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AntiHunger.java)

| Setting | Source default |
| --- | --- |
| `sprint` | `true` |
| `on-ground` | `true` |

### [AutoEat](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoEat.java)

| Setting | Source default |
| --- | --- |
| `blacklist` | `Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE, Items.CHORUS_FRUIT, Items.POISONOUS_P...` |
| `pause-auras` | `true` |
| `pause-baritone` | `true` |
| `search-inventory` | `false` |
| `food-priority` | `Priority.Saturation` |
| `threshold-mode` | `ThresholdMode.Any` |
| `health-threshold` | `10` |
| `hunger-threshold` | `16` |

### [AutoClicker](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoClicker.java)

| Setting | Source default |
| --- | --- |
| `while-in-screens` | `true` |
| `mode-left` | `Mode.Press` |
| `delay-left` | `2` |
| `mode-right` | `Mode.Press` |
| `delay-right` | `2` |

### [AutoFish](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoFish.java)

| Setting | Source default |
| --- | --- |
| `auto-switch` | `true` |
| `anti-break` | `true` |
| `auto-cast` | `true` |
| `cast-delay` | `14` |
| `cast-delay-variance` | `0` |
| `catch-delay` | `6` |
| `catch-delay-variance` | `0` |

### [AutoGap](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoGap.java)

| Setting | Source default |
| --- | --- |
| `allow-egap` | `true` |
| `always` | `false` |
| `pause-auras` | `true` |
| `pause-baritone` | `true` |
| `before-expiry` | `false` |
| `expiry-threshold` | `60` |
| `potions-regeneration` | `false` |
| `potions-fire-resistance` | `true` |
| `potions-absorption` | `false` |
| `health-enabled` | `true` |
| `health-threshold` | `20` |

### [AutoMend](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoMend.java)

| Setting | Source default |
| --- | --- |
| `blacklist` | `—` |
| `force` | `false` |
| `auto-disable` | `true` |

### [AutoReplenish](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoReplenish.java)

| Setting | Source default |
| --- | --- |
| `min-count` | `8` |
| `delay` | `1` |
| `offhand` | `true` |
| `unstackable` | `true` |
| `same-enchants` | `true` |
| `search-hotbar` | `false` |
| `excluded-items` | `—` |

### [AutoRespawn](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoRespawn.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [AutoTool](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/AutoTool.java)

| Setting | Source default |
| --- | --- |
| `prefer` | `EnchantPreference.Fortune` |
| `silk-touch-for-ender-chest` | `true` |
| `fortune-for-ores-and-crops` | `false` |
| `anti-break` | `false` |
| `anti-break-percentage` | `10` |
| `switch-back` | `false` |
| `switch-delay` | `0` |
| `list-mode` | `ListMode.Blacklist` |
| `whitelist` | `—` |
| `blacklist` | `—` |

### [BreakDelay](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/BreakDelay.java)

| Setting | Source default |
| --- | --- |
| `cooldown` | `0` |
| `no-insta-break` | `false` |

### [ChestSwap](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/ChestSwap.java)

| Setting | Source default |
| --- | --- |
| `chestplate` | `Chestplate.PreferNetherite` |
| `stay-on` | `false` |
| `close-inventory` | `true` |

### [EXPThrower](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/EXPThrower.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [FakePlayer](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/FakePlayer.java)

| Setting | Source default |
| --- | --- |
| `name` | `"seasnail8169"` |
| `copy-inv` | `true` |
| `health` | `20` |

### [FastUse](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/FastUse.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.All` |
| `items` | `—` |
| `blocks` | `false` |
| `cooldown` | `0` |

### [GhostHand](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/GhostHand.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [InstantRebreak](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/InstantRebreak.java)

| Setting | Source default |
| --- | --- |
| `delay` | `0` |
| `only-pick` | `true` |
| `rotate` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(204, 0, 0, 10)` |
| `line-color` | `new SettingColor(204, 0, 0, 255)` |

### [LiquidInteract](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/LiquidInteract.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [MiddleClickExtra](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/MiddleClickExtra.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Pearl` |
| `send-message` | `false` |
| `message-to-send` | `"/msg %player I just friended you on Meteor."` |
| `quick-swap` | `false` |
| `swap-back` | `false` |
| `disable-in-creative` | `true` |
| `notify` | `true` |

### [Multitask](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/Multitask.java)

| Setting | Source default |
| --- | --- |
| `attacking-entities` | `true` |

### [NameProtect](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/NameProtect.java)

| Setting | Source default |
| --- | --- |
| `name-protect` | `true` |
| `name` | `"seasnail"` |
| `skin-protect` | `true` |

### [NoInteract](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/NoInteract.java)

| Setting | Source default |
| --- | --- |
| `block-mine` | `—` |
| `block-mine-mode` | `ListMode.Blacklist` |
| `block-interact` | `—` |
| `block-interact-mode` | `ListMode.Blacklist` |
| `block-interact-hand` | `HandMode.None` |
| `entity-hit` | `—` |
| `entity-hit-mode` | `ListMode.Blacklist` |
| `entity-interact` | `—` |
| `entity-interact-mode` | `ListMode.Blacklist` |
| `entity-interact-hand` | `HandMode.None` |
| `friends` | `InteractMode.None` |
| `babies` | `InteractMode.None` |
| `nametagged` | `InteractMode.None` |

### [NoMiningTrace](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/NoMiningTrace.java)

| Setting | Source default |
| --- | --- |
| `blacklisted-entities` | `` |
| `only-when-holding-a-pickaxe` | `true` |

### [NoRotate](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/NoRotate.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [NoStatusEffects](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/NoStatusEffects.java)

| Setting | Source default |
| --- | --- |
| `blocked-effects` | `LEVITATION.value(), JUMP_BOOST.value(), SLOW_FALLING.value(), DOLPHINS_GRACE.value()` |

### [Portals](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/Portals.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [PotionSaver](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/PotionSaver.java)

| Setting | Source default |
| --- | --- |
| `effects` | `STRENGTH.value(), ABSORPTION.value(), RESISTANCE.value(), FIRE_RESISTANCE.value(), SPEE...` |
| `only-when-stationary` | `false` |

### [Reach](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/Reach.java)

| Setting | Source default |
| --- | --- |
| `extra-block-reach` | `—` |
| `extra-entity-reach` | `—` |

### [Rotation](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/Rotation.java)

| Setting | Source default |
| --- | --- |
| `yaw-lock-mode` | `LockMode.Simple` |
| `yaw-angle` | `0` |
| `pitch-lock-mode` | `LockMode.Simple` |
| `pitch-angle` | `0` |

### [SpeedMine](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/player/SpeedMine.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Damage` |
| `blocks` | `—` |
| `blocks-filter` | `ListMode.Blacklist` |
| `modifier` | `1.4` |
| `haste-amplifier` | `2` |
| `instamine` | `true` |
| `grim-bypass` | `false` |

## Movement

### [AirJump](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/AirJump.java)

| Setting | Source default |
| --- | --- |
| `maintain-level` | `false` |

### [Anchor](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Anchor.java)

| Setting | Source default |
| --- | --- |
| `max-height` | `10` |
| `min-pitch` | `0` |
| `cancel-jump-in-hole` | `false` |
| `pull` | `false` |
| `pull-speed` | `0.3` |

### [AntiVoid](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/AntiVoid.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Jump` |

### [AutoJump](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/AutoJump.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Jump` |
| `jump-if` | `JumpWhen.Always` |
| `velocity-height` | `0.25` |

### [AutoWalk](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/AutoWalk.java)

| Setting | Source default |
| --- | --- |
| `disable-on-input` | `false` |
| `disable-on-y-change` | `false` |
| `no-unloaded-chunks` | `true` |

### [AutoWasp](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/AutoWasp.java)

| Setting | Source default |
| --- | --- |
| `horizontal-speed` | `2.0` |
| `vertical-speed` | `3.0` |
| `avoid-landing` | `true` |
| `predict-movement` | `true` |
| `only-friends` | `false` |
| `action-on-target-loss` | `Action.TOGGLE` |
| `offset` | `0, 0, 0` |

### [Blink](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Blink.java)

| Setting | Source default |
| --- | --- |
| `render-original` | `true` |
| `pulse-delay` | `0` |

### [ClickTP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/ClickTP.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [ElytraBoost](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/ElytraBoost.java)

| Setting | Source default |
| --- | --- |
| `anti-consume` | `true` |
| `firework-duration` | `0` |
| `play-sound` | `true` |
| `keybind` | `—` |

### [ElytraFly](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/elytrafly/ElytraFly.java)

| Setting | Source default |
| --- | --- |
| `mode` | `ElytraFlightModes.Vanilla` |
| `auto-take-off` | `false` |
| `fall-multiplier` | `0.01` |
| `horizontal-speed` | `1` |
| `vertical-speed` | `1` |
| `acceleration` | `false` |
| `acceleration-step` | `1` |
| `acceleration-start` | `0` |
| `stop-in-water` | `true` |
| `no-unloaded-chunks` | `true` |
| `auto-hover` | `false` |
| `no-crash` | `false` |
| `crash-look-ahead` | `5` |
| `insta-drop` | `false` |
| `pitch40-lower-bounds` | `180` |
| `pitch40-upper-bounds` | `220` |
| `pitch40-rotate-speed-up` | `5.45` |
| `pitch40-rotate-speed-down` | `0.90` |
| `auto-jump` | `true` |
| `yaw-lock` | `Rotation.LockMode.Smart` |
| `yaw` | `0` |
| `pitch-lock` | `true` |
| `pitch` | `85` |
| `restart` | `true` |
| `restart-delay` | `7` |
| `sprint-constantly` | `true` |
| `manual-takeoff` | `false` |
| `elytra-replace` | `false` |
| `replace-durability` | `2` |
| `chest-swap` | `ChestSwapMode.Never` |
| `replenish-fireworks` | `false` |
| `replenish-slot` | `9` |
| `auto-pilot` | `false` |
| `use-fireworks` | `false` |
| `firework-delay` | `8` |
| `minimum-height` | `120` |

### [EntityControl](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/EntityControl.java)

| Setting | Source default |
| --- | --- |
| `entities` | `list.toArray(EntityType[]::new)` |
| `spoof-saddle*` | `false` |
| `max-jump` | `true` |
| `lock-yaw` | `true` |
| `cancel-server-packets` | `false` |
| `speed` | `false` |
| `horizontal-speed` | `10` |
| `only-on-ground` | `false` |
| `in-water` | `true` |
| `fly` | `false` |
| `vertical-speed` | `6` |
| `fall-speed` | `0` |
| `anti-fly-kick` | `true` |
| `delay` | `40` |

### [FastClimb](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/FastClimb.java)

| Setting | Source default |
| --- | --- |
| `timer-mode` | `false` |
| `climb-speed` | `0.2872` |
| `timer` | `1.436` |

### [Flight](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Flight.java)

| Setting | Source default |
| --- | --- |
| `speed` | `0.1` |
| `vertical-speed-match` | `false` |
| `no-sneak` | `false` |
| `mode` | `AntiKickMode.Packet` |
| `delay` | `20` |
| `off-time` | `1` |

### [GUIMove](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/GUIMove.java)

| Setting | Source default |
| --- | --- |
| `guis` | `Screens.Inventory` |
| `arrows-rotate` | `true` |
| `rotate-speed` | `4` |

### [HighJump](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/HighJump.java)

| Setting | Source default |
| --- | --- |
| `jump-multiplier` | `1` |

### [Jesus](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Jesus.java)

| Setting | Source default |
| --- | --- |
| `powder-snow` | `true` |
| `ncp-bypass` | `false` |
| `slow-down` | `false` |
| `mode` | `Mode.Solid` |
| `dip-if-burning` | `true` |
| `dip-on-sneak` | `true` |
| `dip-on-fall` | `true` |
| `dip-fall-height` | `4` |
| `mode` | `Mode.Solid` |
| `dip-if-resistant` | `true` |
| `dip-on-sneak` | `true` |
| `dip-on-fall` | `true` |
| `dip-fall-height` | `4` |

### [LongJump](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/LongJump.java)

| Setting | Source default |
| --- | --- |
| `mode` | `JumpMode.Vanilla` |
| `vanilla-boost-factor` | `1.261` |
| `burst-initial-speed` | `6` |
| `burst-boost-factor` | `2.149` |
| `only-on-ground` | `true` |
| `on-jump` | `false` |
| `glide-multiplier` | `1` |
| `timer` | `1` |
| `auto-disable` | `true` |
| `disable-on-rubberband` | `true` |

### [NoFall](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/NoFall.java)

| Setting | Source default |
| --- | --- |
| `reset-on-disable` | `true` |
| `placed-item` | `PlacedItem.Bucket` |
| `air-place-mode` | `PlaceMode.BeforeDeath` |
| `rotate` | `true` |
| `anchor` | `true` |
| `anti-bounce` | `true` |
| `pause-on-mace` | `true` |

### [NoSlow](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/NoSlow.java)

| Setting | Source default |
| --- | --- |
| `items` | `true` |
| `web` | `WebMode.Vanilla` |
| `web-timer` | `10` |
| `honey-block` | `true` |
| `soul-sand` | `true` |
| `slime-block` | `true` |
| `berry-bush` | `true` |
| `air-strict` | `false` |
| `fluid-drag` | `false` |
| `sneaking` | `false` |
| `hunger` | `false` |
| `slowness` | `false` |

### [Parkour](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Parkour.java)

| Setting | Source default |
| --- | --- |
| `edge-distance` | `0.001` |

### [ReverseStep](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/ReverseStep.java)

| Setting | Source default |
| --- | --- |
| `fall-speed` | `3` |
| `fall-distance` | `3` |
| `vehicles` | `false` |

### [SafeWalk](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/SafeWalk.java)

| Setting | Source default |
| --- | --- |
| `minimum-fall-distance` | `1` |
| `sneak` | `false` |
| `safe-sneak` | `true` |
| `sneak-on-sprint` | `true` |
| `edge-distance` | `0.30` |
| `render` | `false` |
| `render-player-box` | `false` |

### [Scaffold](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Scaffold.java)

| Setting | Source default |
| --- | --- |
| `blocks` | `—` |
| `blocks-filter` | `ListMode.Blacklist` |
| `fast-tower` | `false` |
| `tower-speed` | `0.5` |
| `while-moving` | `false` |
| `only-on-click` | `false` |
| `swing` | `false` |
| `auto-switch` | `true` |
| `rotate` | `true` |
| `air-place` | `false` |
| `ahead-distance` | `0` |
| `closest-block-range` | `4` |
| `radius` | `0` |
| `blocks-per-tick` | `3` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(197, 137, 232, 10)` |
| `line-color` | `new SettingColor(197, 137, 232)` |

### [Slippy](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Slippy.java)

| Setting | Source default |
| --- | --- |
| `friction` | `1` |
| `list-mode` | `ListMode.Blacklist` |
| `ignored-blocks` | `—` |
| `allowed-blocks` | `—` |

### [Sneak](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Sneak.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Vanilla` |

### [Speed](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/speed/Speed.java)

| Setting | Source default |
| --- | --- |
| `mode` | `SpeedModes.Vanilla` |
| `vanilla-speed` | `5.6` |
| `strafe-speed` | `1.6` |
| `speed-limit` | `false` |
| `timer` | `1` |
| `in-liquids` | `false` |
| `when-sneaking` | `false` |
| `only-on-ground` | `false` |

### [Spider](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Spider.java)

| Setting | Source default |
| --- | --- |
| `climb-speed` | `0.2` |

### [Sprint](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Sprint.java)

| Setting | Source default |
| --- | --- |
| `sprint-mode` | `Mode.Strict` |
| `keep-sprint` | `false` |
| `unsprint-on-hit` | `false` |
| `unsprint-in-water` | `true` |
| `sprint-while-stationary` | `false` |

### [Step](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Step.java)

| Setting | Source default |
| --- | --- |
| `height` | `1.25` |
| `active-when` | `ActiveWhen.Always` |
| `safe-step` | `false` |
| `step-health` | `5` |

### [TridentBoost](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/TridentBoost.java)

| Setting | Source default |
| --- | --- |
| `boost` | `2` |
| `out-of-water` | `true` |

### [Velocity](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/movement/Velocity.java)

| Setting | Source default |
| --- | --- |
| `knockback` | `true` |
| `knockback-horizontal` | `0` |
| `knockback-vertical` | `0` |
| `explosions` | `true` |
| `explosions-horizontal` | `0` |
| `explosions-vertical` | `0` |
| `liquids` | `true` |
| `liquids-horizontal` | `0` |
| `liquids-vertical` | `0` |
| `entity-push` | `true` |
| `entity-push-amount` | `0` |
| `blocks` | `true` |
| `sinking` | `false` |
| `fishing` | `false` |

## Render

### [BetterTab](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/BetterTab.java)

| Setting | Source default |
| --- | --- |
| `tablist-size` | `100` |
| `column-height` | `20` |
| `highlight-self` | `true` |
| `self-color` | `new SettingColor(250, 130, 30)` |
| `highlight-friends` | `true` |
| `accurate-latency` | `true` |
| `gamemode` | `false` |

### [BetterTooltips](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/BetterTooltips.java)

| Setting | Source default |
| --- | --- |
| `display-when` | `DisplayWhen.Keybind` |
| `keybind` | `Keybind.fromKey(KEY_LALT)` |
| `open-contents` | `true` |
| `keybind` | `Keybind.fromButton(MOUSE_BUTTON_MIDDLE)` |
| `pause-in-creative` | `true` |
| `containers` | `true` |
| `compact-shulker-tooltip` | `true` |
| `echests` | `true` |
| `maps` | `true` |
| `map-scale` | `1` |
| `books` | `true` |
| `banners` | `true` |
| `entities-in-buckets` | `true` |
| `bundles` | `true` |
| `food-info` | `true` |
| `byte-size` | `true` |
| `byte-size-format` | `SortSize.Dynamic` |
| `status-effects` | `true` |
| `tooltip` | `false` |
| `tooltip-components` | `false` |

### [BlockESP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/blockesp/BlockESP.java)

| Setting | Source default |
| --- | --- |
| `default-block-config` | `new ESPBlockData( ShapeMode.Lines, new SettingColor(0, 255, 200), new SettingColor(0, 2...` |
| `block-configs` | `—` |
| `tracers` | `false` |

### [BlockSelection](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/BlockSelection.java)

| Setting | Source default |
| --- | --- |
| `advanced` | `true` |
| `single-side` | `false` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 255, 255, 50)` |
| `line-color` | `new SettingColor(255, 255, 255, 255)` |
| `hide-when-inside-block` | `true` |

### [Blur](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Blur.java)

| Setting | Source default |
| --- | --- |
| `strength` | `5` |
| `fade-time` | `100` |
| `meteor` | `true` |
| `inventories` | `true` |
| `chat` | `false` |
| `other` | `true` |

### [BossStack](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/BossStack.java)

| Setting | Source default |
| --- | --- |
| `stack` | `true` |
| `hide-name` | `false` |
| `bar-spacing` | `10` |

### [Breadcrumbs](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Breadcrumbs.java)

| Setting | Source default |
| --- | --- |
| `color` | `new SettingColor(225, 25, 25)` |
| `max-sections` | `1000` |
| `section-length` | `0.5` |

### [BreakIndicators](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/BreakIndicators.java)

| Setting | Source default |
| --- | --- |
| `shape-mode` | `ShapeMode.Both` |
| `packet-mine` | `true` |
| `start-side-color` | `new SettingColor(25, 252, 25, 150)` |
| `start-line-color` | `new SettingColor(25, 252, 25, 150)` |
| `end-side-color` | `new SettingColor(255, 25, 25, 150)` |
| `end-line-color` | `new SettingColor(255, 25, 25, 150)` |

### [CameraTweaks](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/CameraTweaks.java)

| Setting | Source default |
| --- | --- |
| `clip` | `true` |
| `camera-distance` | `4` |
| `scrolling` | `true` |
| `bind` | `Keybind.fromKey(InputConstants.KEY_LALT)` |
| `sensitivity` | `1` |

### [Chams](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Chams.java)

| Setting | Source default |
| --- | --- |
| `entities` | `—` |
| `shader` | `Shader.Image` |
| `color` | `new SettingColor(255, 255, 255, 150)` |
| `ignore-self` | `true` |
| `players` | `false` |
| `ignore-self` | `false` |
| `texture` | `false` |
| `color` | `new SettingColor(198, 135, 254, 150)` |
| `scale` | `1.0` |
| `crystals` | `false` |
| `scale` | `0.6` |
| `bounce` | `0.6` |
| `rotation-speed` | `0.3` |
| `texture` | `true` |
| `crystal-color` | `new SettingColor(198, 135, 254, 255)` |
| `enabled` | `false` |
| `texture` | `false` |
| `hand-color` | `new SettingColor(198, 135, 254, 150)` |

### [CityESP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/CityESP.java)

| Setting | Source default |
| --- | --- |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(225, 0, 0, 75)` |
| `line-color` | `new SettingColor(225, 0, 0, 255)` |

### [EntityOwner](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/EntityOwner.java)

| Setting | Source default |
| --- | --- |
| `scale` | `1` |

### [ESP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/ESP.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Shader` |
| `highlight-target` | `false` |
| `target-hitbox` | `true` |
| `outline-width` | `2` |
| `glow-multiplier` | `3.5` |
| `ignore-self` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `fill-opacity` | `0.3` |
| `fade-distance` | `3` |
| `entities` | `EntityTypes.PLAYER` |
| `color-mode` | `ESPColorMode.EntityType` |
| `show-friend-colors` | `true` |
| `non-living-entity-color` | `new SettingColor(25, 25, 25)` |
| `players-color` | `new SettingColor(255, 255, 255)` |
| `animals-color` | `new SettingColor(25, 255, 25, 255)` |
| `water-animals-color` | `new SettingColor(25, 25, 255, 255)` |
| `monsters-color` | `new SettingColor(255, 25, 25, 255)` |
| `ambient-color` | `new SettingColor(25, 25, 25, 255)` |
| `misc-color` | `new SettingColor(175, 175, 175, 255)` |
| `target-color` | `new SettingColor(200, 200, 200, 255)` |
| `target-hitbox-color` | `new SettingColor(100, 200, 200, 255)` |

### [Freecam](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Freecam.java)

| Setting | Source default |
| --- | --- |
| `speed` | `1.0` |
| `speed-scroll-sensitivity` | `0` |
| `stay-sneaking` | `true` |
| `toggle-on-damage` | `false` |
| `toggle-on-death` | `false` |
| `toggle-on-log` | `true` |
| `reload-chunks` | `true` |
| `show-hands` | `true` |
| `rotate` | `false` |
| `static` | `true` |
| `click-to-path` | `false` |
| `double-click` | `false` |

### [FreeLook](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/FreeLook.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Player` |
| `toggle-perspective` | `true` |
| `camera-sensitivity` | `8` |
| `arrows-control-opposite` | `true` |
| `arrow-speed` | `4` |

### [Fullbright](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Fullbright.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [HandView](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/HandView.java)

| Setting | Source default |
| --- | --- |
| `server-rotations` | `false` |
| `old-animations` | `false` |
| `skip-swapping-animation` | `false` |
| `disable-eating-animation` | `false` |
| `sword-slash` | `false` |
| `swing-mode` | `SwingMode.None` |
| `swing-speed` | `6` |
| `main-hand-progress` | `0` |
| `off-hand-progress` | `0` |
| `scale` | `1, 1, 1` |
| `position` | `0, 0, 0` |
| `rotation` | `0, 0, 0` |
| `scale` | `1, 1, 1` |
| `position` | `0, 0, 0` |
| `rotation` | `0, 0, 0` |
| `scale` | `1, 1, 1` |
| `position` | `0, 0, 0` |
| `rotation` | `0, 0, 0` |

### [HoleESP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/HoleESP.java)

| Setting | Source default |
| --- | --- |
| `horizontal-radius` | `10` |
| `vertical-radius` | `5` |
| `min-height` | `3` |
| `doubles` | `true` |
| `ignore-own` | `false` |
| `webs` | `false` |
| `shape-mode` | `ShapeMode.Both` |
| `height` | `0.2` |
| `top-quad` | `true` |
| `bottom-quad` | `false` |
| `bedrock-top` | `new SettingColor(100, 255, 0, 200)` |
| `bedrock-bottom` | `new SettingColor(100, 255, 0, 0)` |
| `obsidian-top` | `new SettingColor(255, 0, 0, 200)` |
| `obsidian-bottom` | `new SettingColor(255, 0, 0, 0)` |
| `mixed-top` | `new SettingColor(255, 127, 0, 200)` |
| `mixed-bottom` | `new SettingColor(255, 127, 0, 0)` |

### [ItemPhysics](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/ItemPhysics.java)

| Setting | Source default |
| --- | --- |
| `random-rotation` | `true` |

### [ItemHighlight](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/ItemHighlight.java)

| Setting | Source default |
| --- | --- |
| `items` | `—` |
| `color` | `new SettingColor(225, 25, 255, 50)` |

### [LightOverlay](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/LightOverlay.java)

| Setting | Source default |
| --- | --- |
| `horizontal-range` | `8` |
| `vertical-range` | `4` |
| `see-through-blocks` | `false` |
| `light-level` | `0` |
| `color` | `new SettingColor(225, 25, 25)` |
| `potential-color` | `new SettingColor(225, 225, 25)` |

### [LogoutSpots](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/LogoutSpots.java)

| Setting | Source default |
| --- | --- |
| `scale` | `1` |
| `full-height` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 0, 255, 55)` |
| `line-color` | `new SettingColor(255, 0, 255)` |
| `name-color` | `new SettingColor(255, 255, 255)` |
| `name-background-color` | `new SettingColor(0, 0, 0, 75)` |

### [Marker](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/marker/Marker.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [Nametags](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Nametags.java)

| Setting | Source default |
| --- | --- |
| `entities` | `EntityTypes.PLAYER, EntityTypes.ITEM` |
| `scale` | `1.1` |
| `ignore-self` | `true` |
| `ignore-friends` | `false` |
| `ignore-bots` | `true` |
| `culling` | `false` |
| `culling-range` | `20` |
| `culling-count` | `50` |
| `health` | `true` |
| `use-display-name` | `false` |
| `gamemode` | `false` |
| `distance` | `false` |
| `ping` | `true` |
| `items` | `true` |
| `item-spacing` | `2` |
| `ignore-empty-slots` | `true` |
| `durability` | `Durability.None` |
| `display-enchants` | `false` |
| `shown-enchantments` | `Enchantments.PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, E...` |
| `enchantment-position` | `Position.Above` |
| `enchant-name-length` | `3` |
| `enchant-text-scale` | `1` |
| `show-count` | `true` |
| `background-color` | `new SettingColor(0, 0, 0, 75)` |
| `name-color` | `new SettingColor()` |
| `ping-color` | `new SettingColor(20, 170, 170)` |
| `gamemode-color` | `new SettingColor(232, 185, 35)` |
| `distance-color-mode` | `DistanceColorMode.Gradient` |
| `distance-color` | `new SettingColor(150, 150, 150)` |

### [NoRender](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/NoRender.java)

| Setting | Source default |
| --- | --- |
| `portal-overlay` | `false` |
| `spyglass-overlay` | `false` |
| `nausea` | `false` |
| `pumpkin-overlay` | `false` |
| `powdered-snow-overlay` | `false` |
| `fire-overlay` | `false` |
| `liquid-overlay` | `false` |
| `in-wall-overlay` | `false` |
| `vignette` | `false` |
| `gui-background` | `false` |
| `totem-animation` | `false` |
| `eating-particles` | `false` |
| `enchantment-glint` | `false` |
| `boss-bar` | `false` |
| `scoreboard` | `false` |
| `crosshair` | `false` |
| `title` | `false` |
| `held-item-name` | `false` |
| `obfuscation` | `false` |
| `potion-icons` | `false` |
| `message-signature-indicator` | `false` |
| `weather` | `false` |
| `world-border` | `false` |
| `blindness` | `false` |
| `darkness` | `false` |
| `fog` | `false` |
| `enchantment-table-book` | `false` |
| `sign-text` | `false` |
| `block-break-overlay` | `false` |
| `beacon-beams` | `false` |
| `falling-blocks` | `false` |
| `cave-culling` | `false` |
| `map-markers` | `false` |
| `map-contents` | `false` |
| `banners` | `BannerRenderMode.Everything` |
| `firework-explosions` | `false` |
| `particles` | `—` |
| `barrier-invisibility` | `false` |
| `texture-rotations` | `false` |
| `block-entities` | `—` |
| `entities` | `—` |
| `drop-spawn-packets` | `false` |
| `armor` | `false` |
| `invisibility` | `false` |
| `glowing` | `false` |
| `spawner-entities` | `false` |
| `dead-entities` | `false` |
| `nametags` | `false` |

### [PopChams](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/PopChams.java)

| Setting | Source default |
| --- | --- |
| `only-one` | `false` |
| `render-time` | `1` |
| `y-modifier` | `0.75` |
| `scale-modifier` | `-0.25` |
| `fade-out` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 255, 255, 25)` |
| `line-color` | `new SettingColor(255, 255, 255, 127)` |

### [StorageESP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/StorageESP.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Shader` |
| `storage-blocks` | `StorageBlockListSetting.STORAGE_BLOCKS` |
| `tracers` | `false` |
| `shape-mode` | `ShapeMode.Both` |
| `fill-opacity` | `50` |
| `width` | `1` |
| `glow-multiplier` | `3.5` |
| `chest` | `new SettingColor(255, 160, 0, 255)` |
| `trapped-chest` | `new SettingColor(255, 0, 0, 255)` |
| `barrel` | `new SettingColor(255, 160, 0, 255)` |
| `shulker` | `new SettingColor(255, 160, 0, 255)` |
| `ender-chest` | `new SettingColor(120, 0, 255, 255)` |
| `other` | `new SettingColor(140, 140, 140, 255)` |
| `fade-distance` | `6` |
| `hide-opened` | `false` |
| `opened-color` | `—` |

### [TimeChanger](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/TimeChanger.java)

| Setting | Source default |
| --- | --- |
| `time` | `0` |

### [Tracers](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Tracers.java)

| Setting | Source default |
| --- | --- |
| `entities` | `EntityTypes.PLAYER` |
| `ignore-self` | `false` |
| `ignore-friends` | `false` |
| `show-invisible` | `true` |
| `style` | `TracerStyle.Lines` |
| `target` | `Target.Body` |
| `stem` | `true` |
| `max-distance` | `256` |
| `distance-offscreen` | `200` |
| `size-offscreen` | `10` |
| `blink-offscreen` | `true` |
| `blink-offscreen-speed` | `4` |
| `distance-colors` | `false` |
| `show-friend-colors` | `true` |
| `players-colors` | `new SettingColor(205, 205, 205, 127)` |
| `animals-color` | `new SettingColor(145, 255, 145, 127)` |
| `water-animals-color` | `new SettingColor(145, 145, 255, 127)` |
| `monsters-color` | `new SettingColor(255, 145, 145, 127)` |
| `ambient-color` | `new SettingColor(75, 75, 75, 127)` |
| `misc-color` | `new SettingColor(145, 145, 145, 127)` |

### [Trail](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Trail.java)

| Setting | Source default |
| --- | --- |
| `particles` | `ParticleTypes.DRIPPING_OBSIDIAN_TEAR, ParticleTypes.CAMPFIRE_COSY_SMOKE` |
| `pause-when-stationary` | `true` |

### [Trajectories](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Trajectories.java)

| Setting | Source default |
| --- | --- |
| `items` | `getDefaultItems()` |
| `other-players` | `true` |
| `fired-projectiles` | `false` |
| `ignore-wither-skulls` | `false` |
| `accurate` | `false` |
| `simulation-steps` | `500` |
| `ignore-rendering-first-ticks` | `3` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 150, 0, 35)` |
| `line-color` | `new SettingColor(255, 150, 0)` |
| `render-position-boxes` | `false` |
| `position-box-size` | `0.02` |
| `position-side-color` | `new SettingColor(255, 150, 0, 35)` |
| `position-line-color` | `new SettingColor(255, 150, 0)` |

### [TunnelESP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/TunnelESP.java)

| Setting | Source default |
| --- | --- |
| `height` | `0.1` |
| `connected` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 175, 25, 50)` |
| `line-color` | `new SettingColor(255, 175, 25, 255)` |

### [VoidESP](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/VoidESP.java)

| Setting | Source default |
| --- | --- |
| `air-only` | `false` |
| `horizontal-radius` | `64` |
| `hole-height` | `1` |
| `nether-roof` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `fill-color` | `new SettingColor(225, 25, 25, 50)` |
| `line-color` | `new SettingColor(225, 25, 255)` |

### [WallHack](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/WallHack.java)

| Setting | Source default |
| --- | --- |
| `occlude-chunks` | `false` |

### [WaypointsModule](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/WaypointsModule.java)

| Setting | Source default |
| --- | --- |
| `text-render-distance` | `100` |
| `waypoint-fade-distance` | `20` |
| `max-death-positions` | `0` |
| `chat` | `false` |

### [WeatherChanger](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/WeatherChanger.java)

| Setting | Source default |
| --- | --- |
| `rain-level` | `0` |
| `thunder-level` | `0` |

### [Xray](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Xray.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [Zoom](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/render/Zoom.java)

| Setting | Source default |
| --- | --- |
| `zoom` | `6` |
| `scroll-sensitivity` | `1` |
| `smooth` | `true` |
| `cinematic` | `false` |
| `hide-HUD` | `false` |
| `show-hands` | `false` |

## World

### [Ambience](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Ambience.java)

| Setting | Source default |
| --- | --- |
| `end-sky` | `false` |
| `custom-sky-color` | `false` |
| `overworld-sky-color` | `new SettingColor(0, 125, 255)` |
| `nether-sky-color` | `new SettingColor(102, 0, 0)` |
| `end-sky-color` | `new SettingColor(65, 30, 90)` |
| `custom-cloud-color` | `false` |
| `cloud-color` | `new SettingColor(102, 0, 0)` |
| `custom-lightning-color` | `false` |
| `lightning-color` | `new SettingColor(102, 0, 0)` |
| `custom-grass-color` | `false` |
| `grass-color` | `new SettingColor(102, 0, 0)` |
| `custom-foliage-color` | `false` |
| `foliage-color` | `new SettingColor(102, 0, 0)` |
| `custom-water-color` | `false` |
| `water-color` | `new SettingColor(102, 0, 0)` |
| `custom-lava-color` | `false` |
| `lava-color` | `new SettingColor(102, 0, 0)` |
| `custom-fog-color` | `false` |
| `fog-color` | `new SettingColor(102, 0, 0)` |

### [AutoBreed](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/AutoBreed.java)

| Setting | Source default |
| --- | --- |
| `entities` | `EntityTypes.HORSE, EntityTypes.DONKEY, EntityTypes.COW, EntityTypes.MOOSHROOM, EntityTy...` |
| `range` | `4.5` |
| `hand-for-breeding` | `InteractionHand.MAIN_HAND` |
| `mob-age-filter` | `EntityAgeTest.Adult` |
| `continuous-breeding` | `false` |
| `breeding-interval` | `6600) // 30s in love mode and the 5-minute breeding cooldown .visible(continuousBreedin...` |

### [AutoBrewer](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/AutoBrewer.java)

| Setting | Source default |
| --- | --- |
| `potion` | `MyPotion.Strength` |

### [AutoMount](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/AutoMount.java)

| Setting | Source default |
| --- | --- |
| `check-saddle` | `false` |
| `rotate` | `true` |
| `entities` | `—` |

### [AutoNametag](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/AutoNametag.java)

| Setting | Source default |
| --- | --- |
| `entities` | `—` |
| `range` | `5` |
| `priority` | `SortPriority.LowestDistance` |
| `renametag` | `true` |
| `rotate` | `true` |

### [AutoShearer](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/AutoShearer.java)

| Setting | Source default |
| --- | --- |
| `distance` | `5.0` |
| `anti-break` | `false` |
| `rotate` | `true` |

### [AutoSign](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/AutoSign.java)

| Setting | Source default |
| --- | --- |
| `delay` | `10` |

### [AutoSmelter](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/AutoSmelter.java)

| Setting | Source default |
| --- | --- |
| `fuel-items` | `Items.COAL, Items.CHARCOAL` |
| `fuel-items-per-refill` | `64` |
| `smeltable-items` | `Items.IRON_ORE, Items.GOLD_ORE, Items.COPPER_ORE, Items.RAW_IRON, Items.RAW_COPPER, Ite...` |
| `disable-when-out-of-items` | `true` |
| `auto-close` | `false` |

### [BuildHeight](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/BuildHeight.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [Collisions](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Collisions.java)

| Setting | Source default |
| --- | --- |
| `blocks` | `—` |
| `magma` | `false` |
| `unloaded-chunks` | `false` |
| `ignore-border` | `false` |

### [EChestFarmer](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/EChestFarmer.java)

| Setting | Source default |
| --- | --- |
| `self-toggle` | `false` |
| `ignore-existing` | `true` |
| `amount` | `64` |
| `swing-hand` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(204, 0, 0, 50)` |
| `line-color` | `new SettingColor(204, 0, 0, 255)` |

### [EndermanLook](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/EndermanLook.java)

| Setting | Source default |
| --- | --- |
| `look-mode` | `Mode.Away` |
| `stun-hostiles` | `true` |

### [Flamethrower](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Flamethrower.java)

| Setting | Source default |
| --- | --- |
| `distance` | `5.0` |
| `anti-break` | `false` |
| `put-out-fire` | `true` |
| `target-babies` | `false` |
| `tick-interval` | `5` |
| `rotate` | `true` |
| `entities` | `EntityTypes.PIG, EntityTypes.COW, EntityTypes.SHEEP, EntityTypes.CHICKEN, EntityTypes.R...` |

### [HighwayBuilder](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/HighwayBuilder.java)

| Setting | Source default |
| --- | --- |
| `width` | `4` |
| `height` | `3` |
| `floor` | `Floor.Replace` |
| `railings` | `true` |
| `corner-support-block` | `true` |
| `mine-above-railings` | `true` |
| `rotation` | `Rotation.Both` |
| `disconnect-on-toggle` | `false` |
| `pause-on-lag` | `true` |
| `destroy-crystal-traps` | `true` |
| `double-mine` | `true` |
| `fast-break` | `true` |
| `dont-break-tools` | `false` |
| `durability-percentage` | `2` |
| `save-pickaxes` | `1` |
| `break-delay` | `0` |
| `blocks-per-tick` | `1` |
| `blocks-to-place` | `Blocks.OBSIDIAN` |
| `place-range` | `4.5` |
| `place-delay` | `0` |
| `placements-per-tick` | `1` |
| `trash-items` | `Items.NETHERRACK, Items.QUARTZ, Items.GOLD_NUGGET, Items.GOLDEN_SWORD, Items.GLOWSTONE_...` |
| `inventory-delay` | `3` |
| `eject-useless-shulkers` | `true` |
| `search-ender-chest` | `false` |
| `search-shulkers` | `true` |
| `minimum-empty-slots` | `3` |
| `mine-ender-chests` | `true` |
| `echest-blockade-type` | `BlockadeType.Full` |
| `save-ender-chests` | `2` |
| `instantly-rebreak-echests` | `false` |
| `rebreak-delay` | `0` |
| `render-blocks-to-mine` | `true` |
| `blocks-to-mine-shape-mode` | `ShapeMode.Both` |
| `blocks-to-mine-side-color` | `new SettingColor(225, 25, 25, 25)` |
| `blocks-to-mine-line-color` | `new SettingColor(225, 25, 25)` |
| `render-blocks-to-place` | `true` |
| `blocks-to-place-shape-mode` | `ShapeMode.Both` |
| `blocks-to-place-side-color` | `new SettingColor(25, 25, 225, 25)` |
| `blocks-to-place-line-color` | `new SettingColor(25, 25, 225)` |

### [LiquidFiller](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/LiquidFiller.java)

| Setting | Source default |
| --- | --- |
| `place-in` | `PlaceIn.Both` |
| `shape` | `Shape.Sphere` |
| `place-range` | `4.5` |
| `walls-range` | `4.5` |
| `delay` | `0` |
| `max-blocks-per-tick` | `1` |
| `sort-mode` | `SortMode.Furthest` |
| `rotate` | `true` |
| `list-mode` | `ListMode.Whitelist` |
| `whitelist` | `Blocks.DIRT, Blocks.COBBLESTONE, Blocks.STONE, Blocks.NETHERRACK, Blocks.DIORITE, Block...` |
| `blacklist` | `—` |

### [NoGhostBlocks](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/NoGhostBlocks.java)

| Setting | Source default |
| --- | --- |
| `breaking` | `true` |
| `placing` | `true` |

### [Nuker](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Nuker.java)

| Setting | Source default |
| --- | --- |
| `shape` | `Shape.Sphere` |
| `mode` | `Mode.Flatten` |
| `range` | `4` |
| `up` | `1` |
| `down` | `1` |
| `left` | `1` |
| `right` | `1` |
| `forward` | `1` |
| `back` | `1` |
| `walls-range` | `4.0` |
| `delay` | `0` |
| `max-blocks-per-tick` | `1` |
| `sort-mode` | `SortMode.Closest` |
| `packet-mine` | `false` |
| `only-suitable-tools` | `false` |
| `interact` | `false` |
| `rotate` | `true` |
| `list-mode` | `ListMode.Blacklist` |
| `blacklist` | `—` |
| `whitelist` | `—` |
| `select-block-bind` | `Keybind.none()` |
| `swing` | `true` |
| `bounding-box` | `true` |
| `nuke-box-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(16, 106, 144, 100)` |
| `line-color` | `new SettingColor(16, 106, 144, 255)` |
| `broken-blocks` | `true` |
| `nuke-block-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 0, 0, 80)` |
| `line-color` | `new SettingColor(255, 0, 0, 255)` |

### [PacketMine](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/PacketMine.java)

| Setting | Source default |
| --- | --- |
| `delay` | `1` |
| `rotate` | `true` |
| `auto-switch` | `false` |
| `not-on-use` | `true` |
| `obscure-breaking-progress` | `false` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `ready-side-color` | `new SettingColor(0, 204, 0, 10)` |
| `ready-line-color` | `new SettingColor(0, 204, 0, 255)` |
| `side-color` | `new SettingColor(204, 0, 0, 10)` |
| `line-color` | `new SettingColor(204, 0, 0, 255)` |

### [StashFinder](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/StashFinder.java)

| Setting | Source default |
| --- | --- |
| `storage-blocks` | `StorageBlockListSetting.STORAGE_BLOCKS` |
| `minimum-storage-count` | `4` |
| `blacklisted-support-blocks` | `DEFAULT_SUPPORT_BLOCK_BLACKLIST` |
| `minimum-distance` | `0` |
| `notifications` | `true` |
| `notification-mode` | `Mode.Both` |
| `render-tracer` | `true` |
| `tracer-color` | `new SettingColor(255, 215, 0, 255)` |
| `tracer-hide-at-distance` | `16` |
| `tracer-max-distance` | `2000` |
| `render-chunk-column` | `false` |
| `chunk-column-color` | `new SettingColor(255, 215, 0, 100)` |
| `clear-traces-bind` | `Keybind.none()` |

### [SpawnProofer](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/SpawnProofer.java)

| Setting | Source default |
| --- | --- |
| `place-delay` | `1` |
| `place-range` | `4.5` |
| `walls-range` | `4.5` |
| `blocks-per-tick` | `1` |
| `light-level` | `0` |
| `blocks` | `Blocks.TORCH, Blocks.STONE_BUTTON, Blocks.STONE_SLAB` |
| `mode` | `Mode.Both` |
| `rotate` | `true` |

### [Timer](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Timer.java)

| Setting | Source default |
| --- | --- |
| `multiplier` | `1` |

### [VeinMiner](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/VeinMiner.java)

| Setting | Source default |
| --- | --- |
| `blocks` | `Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK` |
| `mode` | `ListMode.Blacklist` |
| `depth` | `3` |
| `delay` | `0` |
| `rotate` | `true` |
| `swing-hand` | `true` |
| `render` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(204, 0, 0, 10)` |
| `line-color` | `new SettingColor(204, 0, 0, 255)` |

### [Excavator](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/Excavator.java)

| Setting | Source default |
| --- | --- |
| `selection-bind` | `Keybind.fromButton(InputConstants.MOUSE_BUTTON_RIGHT)` |
| `log-selection` | `true` |
| `keep-active` | `false` |
| `shape-mode` | `ShapeMode.Both` |
| `side-color` | `new SettingColor(255, 255, 255, 50)` |
| `line-color` | `new SettingColor(255, 255, 255, 255)` |

### [InfinityMiner](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/world/InfinityMiner.java)

| Setting | Source default |
| --- | --- |
| `target-blocks` | `Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE` |
| `target-items` | `Items.DIAMOND` |
| `repair-blocks` | `Blocks.COAL_ORE, Blocks.REDSTONE_ORE, Blocks.NETHER_QUARTZ_ORE` |
| `repair-threshold` | `20` |
| `mine-threshold` | `70` |
| `walk-home` | `false` |
| `log-out` | `false` |

## Misc

### [AntiPacketKick](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/AntiPacketKick.java)

| Setting | Source default |
| --- | --- |
| `catch-exceptions` | `false` |
| `log-exceptions` | `true` |

### [AutoReconnect](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/AutoReconnect.java)

| Setting | Source default |
| --- | --- |
| `delay` | `3.5` |
| `hide-buttons` | `false` |

### [BetterBeacons](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/BetterBeacons.java)

| Setting | Source default |
| --- | --- |
| *(No setting builder found)* | — |

### [BetterChat](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/BetterChat.java)

| Setting | Source default |
| --- | --- |
| `annoy` | `false` |
| `fancy-chat` | `false` |
| `timestamps` | `false` |
| `show-seconds` | `false` |
| `player-heads` | `true` |
| `coords-protection` | `true` |
| `keep-history` | `true` |
| `anti-spam` | `true` |
| `depth` | `20` |
| `anti-clear` | `true` |
| `filter-regex` | `false` |
| `regex-filter` | `—` |
| `infinite-chat-box` | `true` |
| `longer-chat-history` | `true` |
| `extra-lines` | `1000` |
| `prefix` | `false` |
| `random` | `false` |
| `text` | `"> "` |
| `small-caps` | `false` |
| `suffix` | `false` |
| `random` | `false` |
| `text` | `" \| meteor on crack!"` |
| `small-caps` | `true` |

### [BookBot](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/BookBot.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Random` |
| `file` | `DEFAULT_FILE` |
| `random-type` | `RandomType.Utf8` |
| `pages` | `50` |
| `characters` | `128` |
| `delay` | `20` |
| `sign` | `true` |
| `name` | `"Meteor on Crack!"` |
| `append-count` | `true` |
| `word-wrap` | `true` |

### [DiscordPresence](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/DiscordPresence.java)

| Setting | Source default |
| --- | --- |
| `line-1-messages` | `"{player}", "{server}"` |
| `line-1-update-delay` | `200` |
| `line-1-select-mode` | `SelectMode.Sequential` |
| `line-2-messages` | `"Meteor on Crack!", "{round(server.tps, 1)} TPS", "Playing on {server.difficulty} diffi...` |
| `line-2-update-delay` | `60` |
| `line-2-select-mode` | `SelectMode.Sequential` |

### [InventoryTweaks](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/InventoryTweaks.java)

| Setting | Source default |
| --- | --- |
| `mouse-drag-item-move` | `true` |
| `uncap-bundle-scrolling` | `true` |
| `frame-input-handling` | `false` |
| `sorting-enabled` | `true` |
| `sorting-key` | `Keybind.fromButton(MOUSE_BUTTON_MIDDLE)` |
| `sorting-delay` | `1` |
| `disable-in-creative` | `true` |
| `anti-drop-items` | `—` |
| `item-frames` | `true` |
| `override-bind` | `—` |
| `auto-drop-items` | `—` |
| `exclude-equipped` | `true` |
| `exclude-hotbar` | `false` |
| `only-full-stacks` | `false` |
| `steal-screens` | `List.of(MenuType.GENERIC_9x3, MenuType.GENERIC_9x6)` |
| `inventory-buttons` | `true` |
| `steal-drop` | `false` |
| `drop-backwards` | `false` |
| `dump-filter` | `ListMode.None` |
| `dump-items` | `—` |
| `steal-filter` | `ListMode.None` |
| `steal-items` | `—` |
| `auto-steal` | `false` |
| `auto-dump` | `false` |
| `delay` | `20` |
| `initial-delay` | `50` |
| `random` | `50` |

### [MessageAura](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/MessageAura.java)

| Setting | Source default |
| --- | --- |
| `message` | `"Meteor on Crack!"` |
| `ignore-friends` | `false` |

### [Notebot](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/Notebot.java)

| Setting | Source default |
| --- | --- |
| `tick-delay` | `1` |
| `concurrent-tune-blocks` | `1` |
| `mode` | `NotebotUtils.NotebotMode.ExactInstruments` |
| `instrument-detect-mode` | `InstrumentDetectMode.BlockState` |
| `polyphonic` | `true` |
| `auto-rotate` | `true` |
| `auto-play` | `false` |
| `transpose-out-of-range` | `false` |
| `swing-arm` | `true` |
| `check-noteblocks-again-delay` | `10` |
| `render-text` | `true` |
| `render-boxes` | `true` |
| `shape-mode` | `ShapeMode.Both` |
| `untuned-side-color` | `new SettingColor(204, 0, 0, 10)` |
| `untuned-line-color` | `new SettingColor(204, 0, 0, 255)` |
| `tuned-side-color` | `new SettingColor(0, 204, 0, 10)` |
| `tuned-line-color` | `new SettingColor(0, 204, 0, 255)` |
| `hit-side-color` | `new SettingColor(255, 153, 0, 10)` |
| `hit-line-color` | `new SettingColor(255, 153, 0, 255)` |
| `scanned-noteblock-side-color` | `new SettingColor(255, 255, 0, 30)` |
| `scanned-noteblock-line-color` | `new SettingColor(255, 255, 0, 255)` |
| `note-text-scale` | `1.5` |
| `show-scanned-noteblocks` | `false` |

### [Notifier](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/Notifier.java)

| Setting | Source default |
| --- | --- |
| `totem-pops` | `true` |
| `distance-check` | `false` |
| `player-radius` | `30` |
| `ignore-own` | `false` |
| `ignore-friends` | `false` |
| `ignore-others` | `false` |
| `visual-range` | `false` |
| `event` | `Event.Both` |
| `entities` | `EntityTypes.PLAYER` |
| `ignore-friends` | `true` |
| `ignore-fake-players` | `true` |
| `sound` | `true` |
| `pearl` | `true` |
| `ignore-own` | `false` |
| `ignore-friends` | `false` |
| `player-joins-leaves` | `JoinLeaveModes.None` |
| `notification-delay` | `0` |
| `simple-notifications` | `true` |

### [PacketCanceller](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/PacketCanceller.java)

| Setting | Source default |
| --- | --- |
| `S2C-packets` | `—` |
| `C2S-packets` | `—` |

### [PacketLogger](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/PacketLogger.java)

| Setting | Source default |
| --- | --- |
| `S2C-packets` | `—` |
| `C2S-packets` | `—` |
| `show-timestamp` | `true` |
| `show-packet-data` | `false` |
| `show-count` | `true` |
| `show-summary` | `true` |
| `log-to-chat` | `true` |
| `log-to-file` | `false` |
| `flush-interval` | `1` |
| `max-file-size-mb` | `10` |
| `max-total-logs-mb` | `50` |

### [ServerSpoof](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/ServerSpoof.java)

| Setting | Source default |
| --- | --- |
| `spoof-brand` | `true` |
| `brand` | `"vanilla"` |
| `resource-pack` | `false` |
| `block-channels` | `true` |
| `channels` | `"fabric", "minecraft:register"` |

### [SoundBlocker](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/SoundBlocker.java)

| Setting | Source default |
| --- | --- |
| `sounds` | `—` |

### [Spam](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/Spam.java)

| Setting | Source default |
| --- | --- |
| `messages` | `List.of("Meteor on Crack!")` |
| `delay` | `20` |
| `disable-on-leave` | `true` |
| `disable-on-disconnect` | `true` |
| `randomise` | `false` |
| `auto-split-messages` | `false` |
| `split-length` | `256` |
| `split-delay` | `20` |
| `bypass` | `false` |
| `include-uppercase-characters` | `true` |
| `length` | `16` |

### [Swarm](https://github.com/MeteorDevelopment/meteor-client/blob/e3a2e77967629d216e127b5ceecf71fb15f9d2e5/src/main/java/meteordevelopment/meteorclient/systems/modules/misc/swarm/Swarm.java)

| Setting | Source default |
| --- | --- |
| `mode` | `Mode.Host` |
| `ip` | `"localhost"` |
| `port` | `6969` |
