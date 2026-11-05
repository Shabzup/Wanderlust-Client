# Module repair audit

This file tracks functional verification, not just whether a module appears in the menu. An enabled toggle, a successful build, or a source reference does not establish that a module works.

## Current repair batch

Installed repair build: `C2A5F86B77FAF6184E725D8DAF1906041228915C5A23D29E5CEED0FDCFEA9A62` (SHA-256). The previous installed JAR is backed up under `.minecraft/backup/WanderlustClient`.

- AutoEXP: removed the undocumented mandatory damaged-equipment gate; repair-only behavior is now an explicit option. XP use supports offhand bottles and restores the selected slot and view angle.
- Xray: registered ore face culling and section visibility hooks; switched to the complete Minecraft 26.2 extraction/rebuild path; corrected vanilla and Indigo lighting; filtered unselected plant and fluid geometry; expanded the target-list edit field; refreshes geometry after editing targets.
- CrystalAura: checks for placement confirmation and temporarily avoids rejected positions instead of retrying the same position forever. A controlled server fixture reproduced client success/server rejection before the fix and confirmed placement plus breaking afterward.
- BowSpam: releases a module-owned bow draw when disabled.
- Optional Baritone: stops its executor after Minecraft closes. Subsequent test processes with the installed optional mods exit with code 0 instead of hitting the shutdown watchdog.

Current scope: **225 individual runtime tests passed**, with selected disable/cleanup checks; **29 modules have passed a specific behavioral scenario**, **5 have partial checks**, and **191 still need dedicated behavior scenarios**. This is not a claim that every setting, multiplayer environment or all 225 implementations is fully verified.

## Known gaps to repair in later batches

- AutoArmor scores equipment by maximum durability rather than protection, enchantments or curse constraints.
- AutoCity currently mines a neighboring block without a full PvP target/block selection strategy.
- Several server command integrations have configuration and dispatch code but no verified compatible server behavior.
- Shared engines for ZKillAura, ZAutoTotem and AutoTrapPlus are aliases, not independently implemented versions.

These are code-review findings, not failed gameplay tests. They remain incomplete until a suitable functional test passes.

## Remaining functional verification

Each module below needs a realistic setup, action/result checks, disable cleanup and relevant settings checks. External service and server command integrations require a controlled compatible service/server. They will not be marked passed from local toggle tests.

| Module | Group | Runtime test | Functional status | Source references (navigation only) |
| --- | --- | --- | --- | --- |
| ActiveModules (`active-modules`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ClientFeedback.java |
| KillAura (`kill-aura`) | Combat | PASS: 7 enabled ticks and disable | PASS: Server damage to explicitly selected cow type | BaritoneBridge.java, Combat.java, ControlPanel.java, ExtraAutomation.java, ModuleOptions.java, Wanderlust.java |
| CrystalAura (`crystal-aura`) | Combat | PASS: 7 enabled ticks and disable | PASS: Server placement and break; rejected-candidate recovery | BaritoneBridge.java, Explosives.java, ModuleOptions.java |
| AnchorAura (`anchor-aura`) | Combat | PASS: 7 enabled ticks and disable | PASS: Server placement, charging and detonation | BaritoneBridge.java, Explosives.java, ModuleOptions.java |
| BedAura (`bed-aura`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, Explosives.java, ModuleOptions.java |
| AutoTotem (`auto-totem`) | Combat | PASS: 7 enabled ticks and disable | PASS: Server offhand equip and disabled behavior | BaritoneBridge.java, Combat.java, ControlPanel.java, ModuleOptions.java, Wanderlust.java |
| Criticals (`criticals`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, Combat.java, ModuleOptions.java |
| BowAimbot (`bow-aimbot`) | Combat | PASS: 7 enabled ticks and disable | PASS: Aim at controlled player fixture | BaritoneBridge.java, Combat.java, ModuleOptions.java |
| BowSpam (`bow-spam`) | Combat | PASS: 7 enabled ticks and disable | PASS: Multiple server-spawned arrows | BaritoneBridge.java, Combat.java, ModuleOptions.java |
| AutoClicker (`auto-clicker`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, Combat.java, ModuleOptions.java |
| Flight (`flight`) | Movement | PASS: 7 enabled ticks and disable | PASS: Survival altitude gain | BaritoneBridge.java, ModuleOptions.java, Movement.java |
| BoatFly (`boat-fly`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ModuleOptions.java, Movement.java |
| ElytraFly (`elytra-fly`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, Movement.java |
| ElytraBoost (`elytra-boost`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, Movement.java |
| Speed (`speed`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ModuleOptions.java, Movement.java |
| NoFall (`no-fall`) | Movement | PASS: 7 enabled ticks and disable | PASS: Fall protection plus damaging disabled control | BaritoneBridge.java, ModuleOptions.java, Movement.java |
| Jesus (`jesus`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ModuleOptions.java, Movement.java, LiquidSurfaceMixin.java |
| Step (`step`) | Movement | PASS: 7 enabled ticks and disable | PARTIAL: Step attribute increased and restored; obstacle traversal still pending | BaritoneBridge.java, Movement.java, StepMixin.java |
| Blink (`blink`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ModuleOptions.java, Movement.java |
| Freecam (`freecam`) | Movement | PASS: 7 enabled ticks and disable | PASS: Body suppression, rendered camera movement and restore | BaritoneBridge.java, Combat.java, ModuleOptions.java, Movement.java, PlayerTools.java |
| ESP (`esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ModuleOptions.java, Visuals.java |
| StorageESP (`storage-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ModuleOptions.java, Visuals.java |
| StructureESP (`structure-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ModuleOptions.java, Visuals.java |
| FreeLook (`free-look`) | Visuals | PASS: 7 enabled ticks and disable | PASS: Mouse rotation isolation and restore | BaritoneBridge.java, ModuleOptions.java, Movement.java |
| Tracers (`tracers`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ModuleOptions.java, Visuals.java |
| Nametags (`nametags`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ModuleOptions.java, Visuals.java |
| AutoEat (`auto-eat`) | Player | PASS: 7 enabled ticks and disable | PASS: Server hunger increase and item consumption | PlayerTools.java |
| AutoGap (`auto-gap`) | Player | PASS: 7 enabled ticks and disable | PASS: Server absorption and item consumption | PlayerTools.java |
| AutoLog (`auto-log`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ModuleOptions.java, PlayerTools.java |
| AutoMend (`auto-mend`) | Player | PASS: 7 enabled ticks and disable | PASS: Server Mending repair and XP consumption | BaritoneBridge.java, ModuleOptions.java, PlayerTools.java |
| AutoReplenish (`auto-replenish`) | Player | PASS: 7 enabled ticks and disable | PASS: Server depleted stack replacement | ModuleOptions.java, PlayerTools.java |
| FastClimb (`fast-climb`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, Movement.java |
| AutoRespawn (`auto-respawn`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ModuleOptions.java, PlayerTools.java |
| AutoSign (`auto-sign`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, PlayerTools.java |
| AutoBreed (`auto-breed`) | World | PASS: 7 enabled ticks and disable | PASS: Wheat consumed and sheep entered breeding state on server | BaritoneBridge.java, ModuleOptions.java, PlayerTools.java |
| AutoShearer (`auto-shearer`) | World | PASS: 7 enabled ticks and disable | PASS: Sheep sheared on server | BaritoneBridge.java, ModuleOptions.java, PlayerTools.java |
| AutoFish (`auto-fish`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ModuleOptions.java, PlayerTools.java |
| BuildPos (`build-pos`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, Visuals.java |
| AirJump (`air-jump`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| Anchor (`anchor`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| AntiVoid (`anti-void`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| AutoJump (`auto-jump`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| AutoWalk (`auto-walk`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| AutoWasp (`auto-wasp`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| ClickTp (`click-tp`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| EntityControl (`entity-control`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| GuiMove (`gui-move`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| HighJump (`high-jump`) | Movement | PASS: 7 enabled ticks and disable | PARTIAL: Configured motion transform; complete jump scenario pending | ExtraMovement.java |
| LongJump (`long-jump`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| NoSlow (`no-slow`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| Parkour (`parkour`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| ReverseStep (`reverse-step`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| SafeWalk (`safe-walk`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| Slippy (`slippy`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| Sneak (`sneak`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| Spider (`spider`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| Sprint (`sprint`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| TridentBoost (`trident-boost`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| BoatControl (`boat-control`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| AutoSneak (`auto-sneak`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| JumpFlight (`jump-flight`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| Jumps (`jumps`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| FlySpeed (`fly-speed`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java |
| AntiAnvil (`anti-anvil`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AntiBed (`anti-bed`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| ArrowDodge (`arrow-dodge`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoAnvil (`auto-anvil`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoArmor (`auto-armor`) | Combat | PASS: 7 enabled ticks and disable | PASS: Leather upgraded to netherite on server; enchantment-aware ranking not implemented | BaritoneBridge.java, ExtraActions.java |
| AutoCity (`auto-city`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoExp (`auto-exp`) | Combat | PASS: 7 enabled ticks and disable | PASS: Server bottle use, stop, repair-only mode, offhand | ExtraActions.java |
| AutoTrap (`auto-trap`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ControlPanel.java, ExtraActions.java, Wanderlust.java |
| AutoWeapon (`auto-weapon`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoWeb (`auto-web`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Burrow (`burrow`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| HoleFiller (`hole-filler`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Offhand (`offhand`) | Combat | PASS: 7 enabled ticks and disable | PASS: Configured inventory shield equipped on server | BaritoneBridge.java, ExtraActions.java |
| SelfAnvil (`self-anvil`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| SelfTrap (`self-trap`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| SelfWeb (`self-web`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Surround (`surround`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AirPlace (`air-place`) | Player | PASS: 7 enabled ticks and disable | PASS: Floating block placed from missing ray hit on server | BaritoneBridge.java, ExtraActions.java |
| AntiAfk (`anti-afk`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoTool (`auto-tool`) | Player | PASS: 7 enabled ticks and disable | PARTIAL: Best hotbar tool selection; automatic targeting scenario pending | ExtraActions.java |
| ExpThrower (`exp-thrower`) | Player | PASS: 7 enabled ticks and disable | PASS: Server bottle use | ExtraActions.java |
| NoMiningTrace (`no-mining-trace`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| SpeedMine (`speed-mine`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| InstantRebreak (`instant-rebreak`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Scaffold (`scaffold`) | Movement | PASS: 7 enabled ticks and disable | PASS: Block placed below player on integrated server | ExtraActions.java |
| AutoMount (`auto-mount`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoDirtPath (`auto-dirt-path`) | World | PASS: 7 enabled ticks and disable | PASS: Grass converted to path on server | ExtraActions.java |
| AutoFarmLand (`auto-farm-land`) | World | PASS: 7 enabled ticks and disable | PASS: Ground tilled on server | ExtraActions.java |
| AutoLogStrip (`auto-log-strip`) | World | PASS: 7 enabled ticks and disable | PASS: Oak log stripped on server | ExtraActions.java |
| AutoFarming (`auto-farming`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoMud (`auto-mud`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Placer (`placer`) | World | PASS: 7 enabled ticks and disable | PASS: Repeated held-block placement on server | ExtraActions.java |
| LiquidFiller (`liquid-filler`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Nuker (`nuker`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Excavator (`excavator`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| VeinMiner (`vein-miner`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| SpawnProofer (`spawn-proofer`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| EChestFarmer (`e-chest-farmer`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| PacketMine (`packet-mine`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoSmelter (`auto-smelter`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoBrewer (`auto-brewer`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| EntityInteract (`entity-interact`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| AutoInteract (`auto-interact`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| DoubleDoorsInteract (`double-doors-interact`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraActions.java |
| Xray (`xray`) | Visuals | PASS: 7 enabled ticks and disable | PASS: Ore faces/textures, lighting, scene rebuild and restore | ModuleOptions.java, Xray.java, XrayFaceMixin.java, XrayLightingMixin.java, XrayRenderMixin.java, XrayVisibilityMixin.java |
| Fullbright (`fullbright`) | Visuals | PASS: 7 enabled ticks and disable | PASS: Local night vision applied and restored | ExtraVisuals.java |
| ZoomPlus (`zoom-plus`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| BlockESP (`block-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| BlockSelection (`block-selection`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| Breadcrumbs (`breadcrumbs`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| Trail (`trail`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| CityESP (`city-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| EntityOwner (`entity-owner`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| HoleESP (`hole-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| ItemHighlight (`item-highlight`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| LightOverlay (`light-overlay`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| LogoutSpots (`logout-spots`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| Marker (`marker`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| Trajectories (`trajectories`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| TrajectoriesPlus (`trajectories-plus`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| TunnelESP (`tunnel-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| VoidESP (`void-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| Grid (`grid`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| DeepslateESP (`deepslate-esp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| BetterLocator (`better-locator`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| NerdVision (`nerd-vision`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| BreakIndicators (`break-indicators`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| BetterBeacons (`better-beacons`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| StashFinder (`stash-finder`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraVisuals.java |
| SignSearch (`sign-search`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraVisuals.java |
| ChestSwap (`chest-swap`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| FakePlayer (`fake-player`) | Player | PASS: 7 enabled ticks and disable | PASS: Local dummy created and removed | ExtraPlayer.java |
| FastUse (`fast-use`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | AdditionalMixins.java |
| GhostHand (`ghost-hand`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| LiquidInteract (`liquid-interact`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| MiddleClickExtra (`middle-click-extra`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| NameProtect (`name-protect`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java, AdditionalMixins.java |
| NoInteract (`no-interact`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, AdditionalMixins.java |
| NoRotate (`no-rotate`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, AdditionalMixins.java |
| NoStatusEffects (`no-status-effects`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| Portals (`portals`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | AdditionalMixins.java |
| Reach (`reach`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraPlayer.java |
| Rotation (`rotation`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraPlayer.java |
| BreakDelay (`break-delay`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | AdditionalMixins.java |
| AttributeSwap (`attribute-swap`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraActions.java, ExtraPlayer.java |
| Hitboxes (`hitboxes`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraPlayer.java, AdditionalMixins.java |
| Quiver (`quiver`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| Velocity (`velocity`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMovement.java, AdditionalMixins.java |
| BetterTooltips (`better-tooltips`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| CameraTweaks (`camera-tweaks`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| HandView (`hand-view`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java, AdditionalMixins.java |
| ItemPhysics (`item-physics`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | AdditionalMixins.java |
| NoRender (`no-render`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| HandDerp (`hand-derp`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraPlayer.java |
| AntiHunger (`anti-hunger`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | Movement.java |
| Multitask (`multitask`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | WorldRenderMixins.java |
| PotionSaver (`potion-saver`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | Movement.java |
| BetterTab (`better-tab`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | WorldRenderMixins.java |
| Blur (`blur`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ControlPanel.java |
| BossStack (`boss-stack`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | WorldRenderMixins.java |
| Chams (`chams`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| PopChams (`pop-chams`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraVisuals.java |
| TimeChanger (`time-changer`) | Visuals | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java, WorldRenderMixins.java |
| Ambience (`ambience`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraWorld.java |
| AutoNametag (`auto-nametag`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java |
| BuildHeight (`build-height`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | WorldRenderMixins.java |
| Collisions (`collisions`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java |
| EndermanLook (`enderman-look`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java |
| Flamethrower (`flamethrower`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java |
| HighwayBuilder (`highway-builder`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java |
| InfinityMiner (`infinity-miner`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java |
| NoGhostBlocks (`no-ghost-blocks`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java |
| Timer (`timer`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraWorld.java, WorldRenderMixins.java |
| AntiPacketKick (`anti-packet-kick`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | NetworkUtilityMixins.java |
| AutoReconnect (`auto-reconnect`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| BetterChat (`better-chat`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| BookBot (`book-bot`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| DiscordPresence (`discord-presence`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| InventoryTweaks (`inventory-tweaks`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| MessageAura (`message-aura`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| Notebot (`notebot`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| Notifier (`notifier`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| PacketCanceller (`packet-canceller`) | Misc | PASS: 7 enabled ticks and disable | PARTIAL: Configured packet class filter; complete transport observation pending | ExtraMisc.java |
| PacketLogger (`packet-logger`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| ServerSpoof (`server-spoof`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java, NetworkUtilityMixins.java |
| SoundBlocker (`sound-blocker`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java, NetworkUtilityMixins.java |
| Spam (`spam`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| DisconnectSound (`disconnect-sound`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| DmSpam (`dm-spam`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| AutoEndorse (`auto-endorse`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| AutoLogin (`auto-login`) | Misc | PASS: 7 enabled ticks and disable | PARTIAL: Secret excluded from saved settings; compatible server login scenario pending | ExtraMisc.java |
| AutoSell (`auto-sell`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| ChatUtility (`chat-utility`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraMisc.java |
| AutoCrafter (`auto-crafter`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| AutoFeed (`auto-feed`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraAutomation.java |
| AutoHeal (`auto-heal`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraAutomation.java |
| AutoRepair (`auto-repair`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraAutomation.java |
| AutoSleep (`auto-sleep`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| AutoTrade (`auto-trade`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| BedrockPrinter (`bedrock-printer`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| OreSim (`ore-sim`) | World | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ExtraAutomation.java, ExtraVisuals.java |
| AutoLeave (`auto-leave`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| SlotClick (`slot-click`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| ItemSucker (`item-sucker`) | Player | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| ZAimbot (`z-aimbot`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| ZAutoTotem (`z-auto-totem`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | BaritoneBridge.java, ControlPanel.java, Wanderlust.java |
| ZKillAura (`z-kill-aura`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ControlPanel.java, Wanderlust.java |
| AutoTrapPlus (`auto-trap-plus`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ControlPanel.java, Wanderlust.java |
| LegitMaceKill (`legit-mace-kill`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| MaceCombo (`mace-combo`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| ArmorBuster (`armor-buster`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| BlazeForm (`blaze-form`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| SmiteAura (`smite-aura`) | Combat | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| WindChargeJump (`wind-charge-jump`) | Movement | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java, ExtraMovement.java |
| Swarm (`swarm`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraSwarm.java |
| Instructions (`instructions`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| MinescriptIntegration (`minescript-integration`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| RangeActions (`range-actions`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| CategoryManager (`category-manager`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| HotkeyUtility (`hotkey-utility`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| PearlChecker (`pearl-checker`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |
| DeathCommands (`death-commands`) | Misc | PASS: 7 enabled ticks and disable | Pending scenario verification | ExtraAutomation.java |

## Runtime pass evidence

All 225 modules were activated individually for at least seven client ticks in a fresh integrated world with the installed optional mods present, then disabled and given three cleanup ticks. The repeated pass completed with zero runtime/cleanup failures and a clean process exit. Source: `qa/evidence/all-modules-runtime.csv` and `qa/evidence/all-modules-final.txt`. This is an idle activation test; it does not validate every interaction or configured mode.

101 literal numeric setting references were checked against runtime registration. Core survival inventory/food/repair/targeting checks completed with zero failures.

## Shutdown compatibility repair

After the tests complete, the installed `baritone-meteor-26.2.jar` leaves `baritone.cache.CachedWorld$PackerThread` workers alive. Minecraft's post-main shutdown watchdog eventually terminates the test process. The repair now shuts down Baritone executors after Minecraft closes. Subsequent UI and world/inventory runs with the same optional mods exited with code 0. Old failing runs and the repaired results are retained separately.

## Evidence by suite

- `qa/evidence/all-modules-final.txt` and `qa/evidence/all-modules-runtime.csv`: 225 individual runtime results, zero failures.
- `qa/evidence/repair-final.txt`: 21 rendering/XP outcome assertions plus 101 numeric-setting registration checks, zero failures. The repeated 225 wiring checks in this log are configuration coverage, not additional gameplay proof.
- `qa/evidence/core-modules.txt`: 7 server-side inventory/food/Mending/targeting assertions passed.
- `qa/evidence/world-final.txt`: 11 ground/tool/armor/offhand/animal/placement/step assertions passed.
- `qa/evidence/ui-upgrade-current.txt`: 28 interface, target selection and representative module checks passed.
- `qa/evidence/crystal-recovery-passed.txt`: bow aiming, arrow spawning, crystal placement/breaking and anchor placement/charge/detonation passed after recovery logic was added.
- Movement observations in `qa/evidence/extended-bow-cleanup.txt`: Freecam, FreeLook, Flight and NoFall scenarios passed. That historical combined run still contains the pre-fix CrystalAura failure; it is retained to document the regression rather than presented as a passing full suite.
- `qa/evidence/repair-xray-on.png` and `repair-xray-off.png`: inspected actual scene output, including opaque stone restoration and readable gold/diamond textures.

## Next functional batches

1. Render markers, storage/name labels and loaded-data limits.
2. Survival mining/placement automation, including rejected actions and inventory depletion.
3. Water/lava, boats, mounts, Elytra and movement cleanup under real input.
4. Crafting/container/fishing workflows and combat settings combinations.
5. Server-command, local relay and external-service integrations in compatible controlled environments.
