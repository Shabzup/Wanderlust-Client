# WanderlustClient

An independently written Fabric client for Minecraft **26.2**, by **Shabzup**. This is a standalone mod, not an addon or source fork. It contains **225 module entries**; see [MODULE-AUDIT.md](MODULE-AUDIT.md) for functional verification progress and [MODULES.md](MODULES.md) for the module inventory. Menu presence does not establish that a module has passed gameplay tests.

## Install

Use Java 25, Fabric Loader 0.19.5 and Fabric API 0.161.0+26.2. Put `WanderlustClient-1.0.0+26.2.jar` in the Minecraft instance's `mods` folder. Back up and remove an older Wanderlust JAR before installing a replacement.

## Interface and controls

Press **Right Shift** during gameplay or from an out-of-game screen to open the animated module workspace. Closing it returns to the originating screen. The interface uses consistent square controls, Manrope Semibold typography, graphite surfaces, violet accents and a gently rotating 3D wireframe ornament. UI Settings controls the module list size (70% by default) and panel background opacity (20% by default), along with module toggle chat messages. The drag workspace always covers the full screen. Size changes only the category windows and their contents; the toolbar, search and settings controls keep their normal size. Smaller lists fit more module rows on screen. UI Settings offers 60%, 70% and 85% size presets, Compact/Comfortable/Spacious row spacing, small-screen fitting, reduced motion and an optional 3D decoration. Ctrl+mouse wheel changes the list size. Defaults remain 70% size and 20% panel opacity, with Comfortable spacing and automatic fitting enabled.

Left-click a module to toggle it. Right-click it, or left-click its visible **…** button, for settings. Use the **+ / −** button beside a category title to collapse or expand it; these choices are saved between sessions. Double-clicking a title also folds it, and the toolbar has Fold all / Expand all. Drag category headers to move windows across the full workspace; dropping one over another swaps their positions. Right-click a header to reset its position. UI Settings has Reset layout to arrange and expand all categories. Changing the list size or GUI dimensions rearranges the windows to fit.

Windows scroll independently with the wheel or page arrows, with a visible position indicator. **Ctrl+F** focuses search, **Clear** removes its text, and **Active only** filters the lists to enabled modules. Search opens matching folded categories. Hover over a module to read its full name, description, on/off state and keybinding. Buttons have high-contrast borders and labels. Escape first returns from settings or clears a filter, then closes the panel; Right Shift closes it directly.

Enable **Visuals → ActiveModules** for the top-left list of enabled modules. Right-click ActiveModules to adjust its position, size, translucent background and visibility in menus. Its toggle and all enabled module states are saved immediately and restored next session. Modules yielding to Baritone are labeled as paused. Toggle messages appear only in your local chat HUD; no chat message is sent to the server.

The multiplayer server list has styled **Accounts**, **UI Settings** and **Proxies** buttons above the server entries. Account and proxy entry points are confined to this screen; the title screen has a UI Settings button. Right Shift opens the module workspace independently from these menus.

Right-click a module to find its **Key** button. You can also assign module hotkeys in Minecraft's Controls settings. Hotkeys are unassigned by default and work during gameplay while screens such as chat are closed. Minecraft saves keybindings in its normal options file. Wanderlust preferences are stored in `config/wanderlust.properties`.

## Accounts

The Accounts screen supports offline usernames, Microsoft device-code sign-in and restoring the original launcher session. Disconnect before switching. Offline usernames work in singleplayer and servers that allow offline accounts.

Saved accounts are restored between sessions from `config/wanderlust-accounts.dat`. This vault uses [Windows DPAPI](https://learn.microsoft.com/en-us/windows/win32/api/dpapi/nf-dpapi-cryptprotectdata) encryption for your Windows login. Microsoft authentication refreshes when you select the saved account. Tokens are excluded from `wanderlust.properties`, and Microsoft passwords are not saved. Keep the Windows profile that created the vault. Accounts signed in through an older memory-only build must be added once after this update; existing saved offline names migrate automatically.

Authentication uses [MinecraftAuth 5.0.2](https://github.com/RaphiMC/MinecraftAuth) and its bundled Commons dependencies, with their required license notices.

## Proxies

Open **Multiplayer → Proxies** to create, edit, select or delete SOCKS5 profiles, with optional username/password authentication. Choose **Use Direct** to disable the selected proxy. Disconnect before changing profiles; a selection applies to new Minecraft game/status TCP connections.

On Windows, profiles and credentials persist in the separate DPAPI-encrypted `config/wanderlust-proxies.dat` vault. Password fields are masked. A selected proxy that cannot resolve or negotiate does not silently fall back to a direct connection. An unreadable vault is preserved and blocks new game TCP connections until you explicitly choose Direct or repair the vault. Other operating systems keep profiles for the current session only.

This feature does not proxy Microsoft authentication HTTP, DNS queries, or separate voice-chat UDP traffic. Proxy routing and credential storage are covered by controlled loopback tests; see [UI verification](UI-VERIFICATION.md).

## Baritone

Baritone is optional. Install a compatible standalone Fabric Baritone JAR for 26.2 alongside Wanderlust to use its navigation integration. The Baritone panel provides goal, mining and follow commands, plus pause, resume and cancel controls. Conflicting movement, combat and world automation yields while Baritone is pathing. Freecam and eating can pause navigation and resume it afterward. Other modules also work without Baritone.

## Using modules

Combat modules require valid targets within the configured range. KillAura's entity picker lists registered hittable types, supports search and has All/None controls; use None and select a single type to target only that type. Explosive modules require their corresponding supplies, generally in the hotbar, and respect dimension restrictions. Bows retain server-side cooldown and inventory limits.

Movement options have individual speed and behavior settings. Inventory, food, XP, fishing and shearing automation needs the appropriate supplies; AutoMend needs Mending equipment. AutoReplenish remembers hotbar items. AutoSign uses configured text, and BuildPos supplies a placement guide. ESP and storage tools inspect loaded data; structure and ore information has additional limits described in MODULES.md.

Server checks can reject movement, combat and packet actions. No module is guaranteed to be undetectable.

## Build

Run `gradlew.bat build` with JDK 25. The distributable is `build/libs/WanderlustClient-1.0.0+26.2.jar`. Manrope Semibold is distributed under the included SIL Open Font License; see its [font notice](src/main/resources/assets/wanderlust/font/manrope-notice.txt).

The canonical repository is [Shabzup/Wanderlust-Client](https://github.com/Shabzup/Wanderlust-Client). GitHub Actions builds the client on pushes and pull requests and provides downloadable JAR artifacts. See [CONTRIBUTING.md](CONTRIBUTING.md) for the repository workflow and [qa/README.md](qa/README.md) for the controlled gameplay probes.

UI screenshots and responsive-layout test scope are documented in [UI-VERIFICATION.md](UI-VERIFICATION.md).
