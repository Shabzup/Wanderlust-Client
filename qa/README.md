# Controlled Minecraft checks

These probes run a separate Minecraft instance with a synthetic offline identity. They are development tools, excluded from the production mod. Runtime activation and wiring checks do not establish correct gameplay behavior; see [MODULE-AUDIT.md](../MODULE-AUDIT.md) for the current coverage and known gaps.

## Requirements

The current launcher requires Windows, Python 3, Java/Javac 25, and an existing Minecraft 26.2 Fabric profile `fabric-loader-0.19.5-26.2` with its downloaded libraries and assets. It expects Fabric API `fabric-api-0.161.0+26.2.jar` in that instance's `mods` folder. It does not download or redistribute Minecraft. Override `WANDERLUST_MINECRAFT_DIR` and `WANDERLUST_FABRIC_PROFILE` to select another installation/profile.

Build first with `gradlew.bat build`. From the project root, run one suite at a time:

```powershell
python qa/launch.py --clean --polish
python qa/launch.py --clean --replenish
python qa/launch.py --clean --all-modules
python qa/launch.py --clean --repair
python qa/launch.py --clean --core
python qa/launch.py --clean --world-modules
python qa/launch.py --clean --extended --combatonly
python qa/launch.py --clean --upgrade
python qa/launch.py --clean --workspace
python qa/launch.py --memory-write
python qa/launch.py --memory-read
```

Output goes to `build/qa/launch.log`; instances and generated classes also remain under `build/qa`. The `--clean` flag selects the isolated directory and resets Wanderlust properties; it does not delete that directory. `--extras` copies other installed mods into the isolated instance; they remain for subsequent runs. Review this directory before changing compatibility baselines. Never point the generated test instance at personal worlds or account vaults.

The probes report `PASS`, `FAIL` and completion totals in the log. Some probes stop Minecraft after a failed assertion while its process still exits successfully, so inspect assertion totals as well as the process exit code. A timeout is not a pass. Some older probes use historical UI coordinates and need maintenance before reuse. GitHub Actions currently checks compilation and packaging; it does not run these Windows gameplay scenarios.

## Retained evidence

`evidence/` contains the 225-module runtime CSV, selected assertion output extracted from the original test runs, and inspected Xray scene screenshots. Raw logs, credentials, private installation paths and game data are excluded. `extended-bow-cleanup.txt` intentionally retains the earlier CrystalAura failure; `crystal-recovery-passed.txt` records the later passing combat check. These are historical results, not new test runs performed during the repository upload.

The `--polish` suite exercises menu keyboard routing, responsive layouts, module and entity-picker interactions, size presets, masked proxy editing, encrypted proxy persistence and real TCP SOCKS5 routing using loopback fixtures. It also opens an integrated world for popup and reduced-motion checks. This suite returns a failure exit code if assertions fail or its completion marker is absent. See [UI-VERIFICATION.md](../UI-VERIFICATION.md) for the display matrix and the limits of the CPU extraction benchmark.
