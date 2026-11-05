# Module coverage

Wanderlust currently has **225 menu entries**, including names from the supplied screenshot. Functional verification and repairs are in progress; see [MODULE-AUDIT.md](MODULE-AUDIT.md). The inventory below describes the current code paths, including simplified and shared engines. An entry in this list is not evidence of a completed or verified implementation.

## Specific limits

- ZKillAura, ZAutoTotem and AutoTrap+ share the corresponding Wanderlust engines and settings.
- OreSim inspects generated ore blocks from loaded integrated-server terrain. Remote server seed simulation is unavailable.
- MinescriptIntegration prepares a script command in chat for the installed Minescript mod; press Enter to run it. InfinityMiner and local Swarm navigation require Baritone.
- Swarm relays Baritone commands only between local clients on the same computer.
- PotionSaver suppresses idle movement packets. It does not stop modern servers from expiring potion effects. Packet-based and movement features remain subject to server validation.
- AutoFeed, AutoHeal, AutoRepair, AutoSell, AutoEndorse, AutoLogin, SmiteAura, DeathCommands and RangeActions require the corresponding server commands or user configuration. AutoLogin commands stay in memory and are excluded from saved properties.
- DiscordPresence requires a running local Discord desktop client and your Discord application ID. This integration and authenticated server-command workflows were not end-to-end exercised.
- Chams renders filled entity bounds, StashFinder marks loaded storage, and BetterBeacons marks loaded beacons. These are the functions implemented here. Notebot plays configured pitches with nearby note blocks in Survival.
- Placement, mining, farming and inventory tools use available items and loaded terrain. Many supplies must be in the hotbar.

## Screenshot inventory

| Screenshot column | Visible name | Wanderlust module | Implemented behavior |
| --- | --- | --- | --- |
| Combat | Anchor Aura | `anchor-aura` | Place, charge and detonate respawn anchors in the Overworld. |
| Combat | Anti Anvil | `anti-anvil` | Mine nearby anvils. |
| Combat | Anti Bed | `anti-bed` | Mine nearby beds. |
| Combat | Arrow Dodge | `arrow-dodge` | Move sideways from approaching arrows. |
| Combat | Attribute Swap | `attribute-swap` | Use a configured hotbar slot for each attack. |
| Combat | Auto Anvil | `auto-anvil` | Place a hotbar anvil above a nearby player when supported. |
| Combat | Auto Armor | `auto-armor` | Equip armor from the inventory. |
| Combat | Auto City | `auto-city` | Mine blocks beside nearby opposing players. |
| Combat | Auto Exp | `auto-exp` | Use XP bottles while equipped items are damaged. |
| Combat | Auto Log | `auto-log` | Disconnect from multiplayer below your health threshold. |
| Combat | Auto Totem | `auto-totem` | Equip a spare totem from your inventory in your offhand. |
| Combat | Auto Trap | `auto-trap` | Place hotbar blocks around the nearest opposing player. |
| Combat | Auto Weapon | `auto-weapon` | Select a preferred hotbar weapon. |
| Combat | Auto Web | `auto-web` | Place cobwebs at a nearby opposing player. |
| Combat | Bed Aura | `bed-aura` | Place and explode beds in the Nether or the End. |
| Combat | Bow Aimbot | `bow-aimbot` | Aim a drawn bow at a nearby target. |
| Combat | Bow Spam | `bow-spam` | Draw and release a held bow repeatedly. |
| Combat | Burrow | `burrow` | Attempt to place a block at your feet. |
| Combat | Criticals | `criticals` | Attempt a critical hit when attacking from the ground. |
| Combat | Crystal Aura | `crystal-aura` | Place and break end crystals near opposing players. |
| Combat | Hitboxes | `hitboxes` | Expand local targeting selection margins. |
| Combat | Hole Filler | `hole-filler` | Fill nearby single-block holes with obsidian. |
| Combat | Kill Aura | `kill-aura` | Attack nearby targets within reach. |
| Combat | Offhand | `offhand` | Equip a configured item in the offhand. |
| Combat | Quiver | `quiver` | Draw a bow upward for a self-shot. |
| Combat | Self Anvil | `self-anvil` | Place a supported anvil above you. |
| Combat | Self Trap | `self-trap` | Place blocks around and above you. |
| Combat | Self Web | `self-web` | Place a cobweb at your feet. |
| Combat | Surround | `surround` | Place hotbar blocks around your feet. |
| Player | Air Place | `air-place` | Repeat held-block placement at the crosshair. |
| Player | Anti Afk | `anti-afk` | Periodically jump and turn. |
| Player | Anti Hunger | `anti-hunger` | Attempt to reduce hunger caused by movement packets. |
| Player | Auto Clicker | `auto-clicker` | Repeat attacks at a configurable interval. |
| Player | Auto Eat | `auto-eat` | Eat food from your hotbar when hunger falls. |
| Player | Auto Fish | `auto-fish` | Cast a held fishing rod, reel on a bite and cast again. |
| Player | Auto Gap | `auto-gap` | Eat a golden apple from your hotbar when health falls. |
| Player | Auto Mend | `auto-mend` | Throw hotbar XP bottles at your feet to repair worn Mending armor. |
| Player | Auto Replenish | `auto-replenish` | Replace depleted hotbar stacks and broken tools from your inventory. |
| Player | Auto Respawn | `auto-respawn` | Respawn automatically after death in a non-hardcore world. |
| Player | Auto Tool | `auto-tool` | Select the fastest hotbar tool for the targeted block. |
| Player | Break Delay | `break-delay` | Remove the client delay between broken blocks. |
| Player | Chest Swap | `chest-swap` | Swap between a hotbar or inventory elytra and chest armor. |
| Player | Exp Thrower | `exp-thrower` | Throw XP bottles at your feet. |
| Player | Fake Player | `fake-player` | Spawn a local practice dummy. |
| Player | Fast Use | `fast-use` | Remove the client right-click delay. |
| Player | Ghost Hand | `ghost-hand` | Target through configured block outlines. |
| Player | Instant Rebreak | `instant-rebreak` | Retry mining the block under the crosshair. |
| Player | Liquid Interact | `liquid-interact` | Allow the crosshair to target liquid surfaces. |
| Player | Middle Click Extra | `middle-click-extra` | Throw a hotbar ender pearl with middle-click. |
| Player | Multitask | `multitask` | Allow attacking while using an item. |
| Player | Name Protect | `name-protect` | Replace your local displayed name. |
| Player | No Interact | `no-interact` | Block right-click interactions while enabled. |
| Player | No Mining Trace | `no-mining-trace` | Use a block raycast while holding attack. |
| Player | No Rotate | `no-rotate` | Keep your view direction after server position corrections. |
| Player | No Status Effects | `no-status-effects` | Clear local blindness, nausea and darkness visuals. |
| Player | Portals | `portals` | Allow menus to remain open in portals. |
| Player | Potion Saver | `potion-saver` | Suppress idle movement packets; modern servers still tick effects. |
| Player | Reach | `reach` | Adjust local interaction reach; servers still validate actions. |
| Player | Rotation | `rotation` | Hold a configurable yaw and pitch. |
| Player | Speed Mine | `speed-mine` | Continue mining the crosshair block each tick. |
| Movement | Air Jump | `air-jump` | Jump again while airborne. |
| Movement | Anchor | `anchor` | Stop horizontal movement while descending over a hole. |
| Movement | Anti Void | `anti-void` | Boost upward near the world bottom. |
| Movement | Auto Jump | `auto-jump` | Jump continuously while walking. |
| Movement | Auto Walk | `auto-walk` | Hold forward automatically. |
| Movement | Auto Wasp | `auto-wasp` | Follow the nearest player while gliding. |
| Movement | Blink | `blink` | Hold movement packets temporarily, then send them in order. |
| Movement | Click Tp | `click-tp` | Attempt to move to the block you right-click. |
| Movement | Elytra Boost | `elytra-boost` | Accelerate while gliding with an elytra. |
| Movement | Elytra Fly | `elytra-fly` | Maintain controlled flight while gliding. |
| Movement | Entity Control | `entity-control` | Steer the mount you are riding. |
| Movement | Fast Climb | `fast-climb` | Ascend ladders and vines faster. |
| Movement | Flight | `flight` | Move freely in the air. |
| Movement | Gui Move | `gui-move` | Move with WASD in menus when no text field is focused. |
| Movement | High Jump | `high-jump` | Increase jump height. |
| Movement | Jesus | `jesus` | Stand and move across water or lava. |
| Movement | Long Jump | `long-jump` | Boost forward while jumping. |
| Movement | No Fall | `no-fall` | Attempt to prevent fall damage while descending. |
| Movement | No Slow | `no-slow` | Compensate for movement slowdown while using items. |
| Movement | Parkour | `parkour` | Jump when approaching a ledge. |
| Movement | Reverse Step | `reverse-step` | Descend faster when walking off a ledge. |
| Movement | Safe Walk | `safe-walk` | Stop horizontal travel over unsupported edges. |
| Movement | Scaffold | `scaffold` | Place hotbar blocks beneath you while moving. |
| Movement | Slippy | `slippy` | Slide faster on the ground. |
| Movement | Sneak | `sneak` | Hold sneak automatically. |
| Movement | Speed | `speed` | Move faster on the ground or in water. |
| Movement | Spider | `spider` | Climb when touching a wall. |
| Movement | Sprint | `sprint` | Sprint while moving. |
| Movement | Step | `step` | Walk up full blocks without jumping. |
| Movement | Trident Boost | `trident-boost` | Boost Riptide travel. |
| Movement | Velocity | `velocity` | Scale received player knockback. |
| Render | Better Tab | `better-tab` | Add latency to player list names. |
| Render | Better Tooltips | `better-tooltips` | Show advanced vanilla item tooltips. |
| Render | Block Esp | `block-esp` | Outline selected loaded block IDs. |
| Render | Block Selection | `block-selection` | Highlight the block under the crosshair. |
| Render | Blur | `blur` | Blur the world behind the client menu. |
| Render | Boss Stack | `boss-stack` | Combine boss bars with matching names. |
| Render | Breadcrumbs | `breadcrumbs` | Show points along your traveled path. |
| Render | Break Indicators | `break-indicators` | Highlight the block you are mining. |
| Render | Camera Tweaks | `camera-tweaks` | Disable view bobbing while enabled. |
| Render | Chams | `chams` | Show filled entity bounds through walls. |
| Render | City Esp | `city-esp` | Highlight blocks next to nearby players. |
| Render | Entity Owner | `entity-owner` | Show owners of tamed animals. |
| Render | Esp | `esp` | Highlight nearby players and dropped items through walls. |
| Render | Free Look | `free-look` | Look around without turning your player. |
| Render | Freecam | `freecam` | Scout with a detached camera while your body stays behind. |
| Render | Fullbright | `fullbright` | Apply local night vision while enabled. |
| Render | Hand View | `hand-view` | Adjust first-person hand position and scale. |
| Render | Hole Esp | `hole-esp` | Highlight nearby single-block holes. |
| Render | Item Highlight | `item-highlight` | Highlight dropped items. |
| Render | Item Physics | `item-physics` | Lay dropped item models flat. |
| Render | Light Overlay | `light-overlay` | Show nearby low block-light positions. |
| Render | Logout Spots | `logout-spots` | Mark where loaded players disappear. |
| Render | Marker | `marker` | Show a configurable world coordinate marker. |
| Render | Nametags | `nametags` | Show readable names above nearby players. |
| Render | No Render | `no-render` | Hide configured entity types. |
| Render | Pop Chams | `pop-chams` | Show a temporary outline where a totem activates. |
| Render | Storage Esp | `storage-esp` | Highlight loaded storage by container type. |
| Render | Time Changer | `time-changer` | Override displayed world clock time. |
| Render | Tracers | `tracers` | Draw lines toward nearby entities. |
| Render | Trail | `trail` | Show a line along your traveled path. |
| Render | Trajectories | `trajectories` | Show the estimated held projectile path. |
| Render | Tunnel Esp | `tunnel-esp` | Highlight narrow passages in loaded blocks. |
| Render | Void Esp | `void-esp` | Highlight empty columns toward the world bottom. |
| World | Ambience | `ambience` | Override local rain and thunder. |
| World | Auto Breed | `auto-breed` | Feed nearby adult animals using breeding items in your hotbar. |
| World | Auto Brewer | `auto-brewer` | Transfer configured ingredients and fuel in a brewing screen. |
| World | Auto Mount | `auto-mount` | Interact with nearby boats and horses. |
| World | Auto Nametag | `auto-nametag` | Use hotbar name tags on nearby unnamed mobs. |
| World | Auto Shearer | `auto-shearer` | Shear nearby sheep using shears from your hotbar. |
| World | Auto Sign | `auto-sign` | Write four preset lines when a sign editor opens. |
| World | Auto Smelter | `auto-smelter` | Transfer configured input and fuel in a furnace screen. |
| World | Build Height | `build-height` | Redirect upward interactions at the build limit to a side. |
| World | Collisions | `collisions` | Disable local block collisions; servers may correct movement. |
| World | Echest Farmer | `e-chest-farmer` | Place and mine nearby ender chests. |
| World | Enderman Look | `enderman-look` | Avoid or stare at nearby endermen. |
| World | Excavator | `excavator` | Mine a nearby volume at player height. |
| World | Flamethrower | `flamethrower` | Use hotbar flint and steel at the crosshair. |
| World | Highway Builder | `highway-builder` | Place a path below you and clear the next head block. |
| World | Infinity Miner | `infinity-miner` | Restart configured Baritone mining tasks. |
| World | Liquid Filler | `liquid-filler` | Place hotbar blocks into nearby liquid. |
| World | No Ghost Blocks | `no-ghost-blocks` | Request block correction at the crosshair. |
| World | Nuker | `nuker` | Mine nearby breakable blocks within reach. |
| World | Packet Mine | `packet-mine` | Continue mining the targeted block. |
| World | Spawn Proofer | `spawn-proofer` | Place hotbar torches in dark nearby spaces. |
| World | Stash Finder | `stash-finder` | Highlight loaded storage locations. |
| World | Timer | `timer` | Adjust local tick timing. |
| World | Vein Miner | `vein-miner` | Mine nearby matching blocks after selecting one. |
| Misc | Anti Packet Kick | `anti-packet-kick` | Ignore packet decoder exceptions instead of closing immediately. |
| Misc | Auto Reconnect | `auto-reconnect` | Reconnect after a configurable disconnect delay. |
| Misc | Better Beacons | `better-beacons` | Mark loaded beacons. |
| Misc | Better Chat | `better-chat` | Add timestamps to displayed chat messages. |
| Misc | Book Bot | `book-bot` | Fill a held writable book from configured text. |
| Misc | Discord Presence | `discord-presence` | Publish a Discord activity using your application ID. |
| Misc | Inventory Tweaks | `inventory-tweaks` | Merge matching inventory stacks in container screens. |
| Misc | Message Aura | `message-aura` | Send a configured private message once to each loaded player. |
| Misc | Notebot | `notebot` | Tune and play nearby note blocks in Survival. |
| Misc | Notifier | `notifier` | Notify when players enter the loaded area. |
| Misc | Packet Canceller | `packet-canceller` | Cancel configured outgoing packet class names. |
| Misc | Packet Logger | `packet-logger` | Log outgoing packet types without packet payloads. |
| Misc | Server Spoof | `server-spoof` | Set the client brand payload. |
| Misc | Sound Blocker | `sound-blocker` | Mute configured sound IDs. |
| Misc | Spam | `spam` | Send configured chat text at an interval. |
| Misc | Swarm | `swarm` | Relay Baritone commands between local Wanderlust clients. |
| Meteorist | Auto Crafter | `auto-crafter` | Place a recipe for the configured output in a crafting screen. |
| Meteorist | Auto Feed | `auto-feed` | Run a server feed command when hunger is low. |
| Meteorist | Auto Heal | `auto-heal` | Run a server heal command when health is low. |
| Meteorist | Auto Interact | `auto-interact` | Repeat interaction at the crosshair. |
| Meteorist | Auto Leave | `auto-leave` | Disconnect at a configurable health threshold. |
| Meteorist | Auto Mud | `auto-mud` | Use hotbar water bottles on dirt. |
| Meteorist | Auto Repair | `auto-repair` | Run a server repair command below a durability threshold. |
| Meteorist | Auto Sleep | `auto-sleep` | Interact with nearby beds in the Overworld. |
| Meteorist | Auto Sneak | `auto-sneak` | Hold sneak automatically. |
| Meteorist | Auto Trade | `auto-trade` | Repeat a selected merchant offer. |
| Meteorist | Boat Control | `boat-control` | Steer a boat with a configurable speed. |
| Meteorist | Disconnect Sound | `disconnect-sound` | Play a notification sound when disconnected. |
| Meteorist | Dm Spam | `dm-spam` | Repeat a configured private message. |
| Meteorist | Double Doors Interact | `double-doors-interact` | Use the neighboring door when opening a door. |
| Meteorist | Entity Interact | `entity-interact` | Interact with nearby entities. |
| Meteorist | Grid | `grid` | Draw a block grid around you. |
| Meteorist | Instructions | `instructions` | Run a configured sequence of bounded game actions. |
| Meteorist | Item Sucker | `item-sucker` | Walk or use Baritone toward nearby dropped items. |
| Meteorist | Jump Flight | `jump-flight` | Rise while holding Jump. |
| Meteorist | Jumps | `jumps` | Adjust the height of repeated jumps. |
| Meteorist | Minescript Integration | `minescript-integration` | Prepare a configured command in chat for an installed Minescript mod. |
| Meteorist | Nerd Vision | `nerd-vision` | Show entity types and runtime IDs. |
| Meteorist | Placer | `placer` | Repeat block interaction with the held item. |
| Meteorist | Range Actions | `range-actions` | Run a configured command when entering a region. |
| Meteorist | Sign Search | `sign-search` | Highlight loaded signs matching a text query. |
| Meteorist | Slot Click | `slot-click` | Repeat a configured container slot action. |
| Meteorist | Z Aimbot | `z-aimbot` | Aim at the nearest visible living target. |
| Meteorist | Z Auto Totem | `z-auto-totem` | Use the Wanderlust AutoTotem engine and its settings. |
| Meteorist | Z Kill Aura | `z-kill-aura` | Use the Wanderlust KillAura engine and entity checklist. |
| Meteorist | Zoom Plus | `zoom-plus` | Set a lower field of view while enabled. |
| Nora Tweaks | Auto Dirt Path | `auto-dirt-path` | Use a hotbar shovel on dirt or grass. |
| Nora Tweaks | Auto Farm Land | `auto-farm-land` | Use a hotbar hoe to create farmland. |
| Nora Tweaks | Auto Farming | `auto-farming` | Harvest mature crops and plant hotbar seeds. |
| Nora Tweaks | Auto Log Strip | `auto-log-strip` | Use a hotbar axe on logs. |
| Nora Tweaks | Auto Trap+ | `auto-trap-plus` | Use the Wanderlust trap placement engine. |
| Nora Tweaks | Better Locator | `better-locator` | Draw lines to loaded players. |
| Nora Tweaks | Category Manager | `category-manager` | Move a module to a selected existing category. |
| Nora Tweaks | Chat Utility | `chat-utility` | Add a configurable prefix to outgoing chat. |
| Nora Tweaks | Deepslate Esp | `deepslate-esp` | Outline loaded deepslate blocks. |
| Nora Tweaks | Hotkey Utility | `hotkey-utility` | Open Minecraft module keybind settings. |
| Nora Tweaks | Legit Mace Kill | `legit-mace-kill` | Attack with a mace when falling within reach. |
| Nora Tweaks | Mace Combo | `mace-combo` | Combine wind charge jumps with falling mace attacks. |
| Nora Tweaks | Ore Sim | `ore-sim` | Inspect generated ores in loaded integrated-server terrain; remote seed prediction is unavailable. |
| Nora Tweaks | Pearl Checker | `pearl-checker` | Notify about newly loaded ender pearls. |
| Nora Tweaks | Wind Charge Jump | `wind-charge-jump` | Use a hotbar wind charge downward when jumping. |
| PowHax | Armor Buster | `armor-buster` | Attack nearby armored player targets. |
| PowHax | Auto Endorse | `auto-endorse` | Run a configured server command periodically. |
| PowHax | Auto Login | `auto-login` | Use an in-memory login command when prompted. |
| PowHax | Auto Sell | `auto-sell` | Run a configured sell command periodically. |
| PowHax | Bedrock Printer | `bedrock-printer` | Export loaded nearby bedrock coordinates to CSV. |
| PowHax | Blaze Form | `blaze-form` | Attack nearby blazes while farming. |
| PowHax | Death Commands | `death-commands` | Run configured server commands after respawning. |
| PowHax | Fly Speed | `fly-speed` | Set creative flight speed. |
| PowHax | Hand Derp | `hand-derp` | Alternate hand swing animations. |
| PowHax | Smite Aura | `smite-aura` | Run the server smite command on nearby players; server permission is required. |
| PowHax | Trajectories+ | `trajectories-plus` | Show estimated projectile trajectories. |

## Validation

The isolated Minecraft checks cover defaults, scaled left/right clicking, the entity picker, module/key registration, FakePlayer lifecycle, Fullbright restoration, HighJump motion, AutoTool selection, server-confirmed Scaffold placement, packet filtering and exclusion of AutoLogin secrets from configuration. They are representative checks, not an exhaustive multiplayer trial of every module.

## Session memory and feedback

ActiveModules is an optional top-left HUD, enabled from Visuals. Module toggles and the HUD toggle save immediately. Toggle notices use Minecraft client system messages, with no outgoing chat packet. Offline names and Microsoft authentication snapshots are restored from a Windows DPAPI-encrypted account vault; existing offline names migrate automatically. Vault serialization and restoration are tested with synthetic authentication data across separate Minecraft processes. Live Microsoft authentication requires the user's own account and is not part of these offline tests.
