# Candidate verification — 10 November 2026

This is an incremental repair candidate, not a launch-ready release. The [module audit](../../MODULE-AUDIT.md) records 32 modules with a passed behavior scenario, 3 partial and 190 pending. These numbers do not establish complete Meteor parity or multiplayer compatibility.

Local build: `gradlew.bat build --offline --console=plain` — PASS.

JAR SHA-256: `277ee373844de061241e32008ed85593d3fe375f7b104db988eb42f56bab69a7`.

`python qa/verify_candidate.py` completed all nine suites with zero failures. Each evidence file records the same JAR hash and a clean mod set: Wanderlust, Fabric API and the QA probe. Earlier runs also passed with optional installed mods present; those are not substituted for this clean run.

| Suite | Passing checks | Evidence |
| --- | ---: | --- |
| Inventory queue/replenishment | 10 | [Assertions](candidate-inventory-parity.txt) |
| AutoEXP, AutoArmor, AutoTotem, KillAura | 10 | [Assertions](candidate-release-modules.txt) |
| HighJump, SafeWalk and Step behavior | 10 | [Assertions](candidate-movement-parity.txt) |
| Core gameplay | 7 | [Assertions](candidate-core.txt) |
| World interactions | 11 | [Assertions](candidate-world.txt) |
| Combat and camera/movement regression | 15 | [Assertions](candidate-combat-motion.txt) |
| Xray/XP outcomes and setting registration | 122 | [Assertions](candidate-repair.txt) |
| Module activation/disable runtime scan | 225 | [Results](candidate-all-modules.txt) |
| UI geometry, routing, persistence and loopback proxy tests | 659 | [Assertions](candidate-ui.txt) |

The runtime scan is not gameplay verification. The repair suite includes 101 numeric-setting registrations; UI geometry assertions are not module behavior tests. The UI suite covers the existing 15-viewport matrix; screenshots at 1080p and 640×480 were inspected for bounds and readability. CPU extraction p95 was 1.1785 ms in 60 measured samples on this machine; this excludes GPU presentation and is not an FPS guarantee.

The first sandboxed UI run failed because loopback sockets were denied. The complete run above used the needed permission and passed the same proxy fixtures. No production workaround was added for that environment restriction.

Fresh inspected Xray scenes: [enabled](candidate-xray-on.png), [disabled](candidate-xray-off.png). Gold/diamond textures remain visible with Xray on; the opaque stone wall and water return when it is off.

No personal worlds or account vaults were used. This candidate has not been installed in the personal Minecraft mods folder. Server/service-dependent integrations remain deferred at the user's request.
