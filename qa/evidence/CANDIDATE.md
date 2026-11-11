# Candidate verification — 11 November 2026

This is an incremental repair candidate, not a launch-ready release. The [module audit](../../MODULE-AUDIT.md) records 36 modules with a passed behavior scenario, 4 partial and 185 pending. These numbers do not establish complete Meteor parity or multiplayer compatibility.

Local build: `gradlew.bat build --offline --console=plain` — PASS.

Latest JAR SHA-256: `b04ae13b13852d00fba1abb2662755f710dcd0bb31d60a9ff3af68ab34466419`.

The full ten-suite run on `a44dfeaed28f32715d778938a285b682b6b45ecd1bea76a3d0230ac6dcb38c89` passed eight suites. Movement failed its initial platform-readiness assertion, and UI extraction exceeded its elapsed-time limit. After a bounded fixture-readiness wait and production text-cache optimizations, `python qa/verify_candidate.py movement-parity ui` passed both affected suites on the latest JAR. The eight unaffected suites retain their earlier evidence; this is **not** a claim that all ten ran on the final hash. Every evidence file records its actual JAR hash and clean mod set: Wanderlust, Fabric API and the QA probe.

| Suite | Passing checks | Evidence |
| --- | ---: | --- |
| Inventory queue/replenishment | 10 | [Assertions](candidate-inventory-parity.txt) |
| AutoEXP, AutoArmor, AutoTotem, KillAura | 10 | [Assertions](candidate-release-modules.txt) |
| Menu bindings, movement and cleanup | 27 | [Assertions](candidate-menu-movement.txt) |
| HighJump, SafeWalk and Step behavior | 10 | [Assertions](candidate-movement-parity.txt) |
| Core gameplay | 7 | [Assertions](candidate-core.txt) |
| World interactions | 11 | [Assertions](candidate-world.txt) |
| Combat and camera/movement regression | 16 | [Assertions](candidate-combat-motion.txt) |
| Xray/XP outcomes and setting registration | 122 | [Assertions](candidate-repair.txt) |
| Module activation/disable runtime scan | 225 | [Results](candidate-all-modules.txt) |
| UI geometry, routing, persistence, resource reload and loopback proxy tests | 661 | [Assertions](candidate-ui.txt) |
The menu suite substitutes native input sampling through a QA-only mixin; it still exercises the real movement tick and integrated-server sneak state. It is not a hardware input test. An early fixture mixed simulated key states and live polling, so that failed attempt is not presented as a production defect.

The combat fixture resets crystal knockback before testing AnchorAura and records charged-anchor server interactions as well as block removal. [Historical fixture diagnostics](combat-fixture-knockback.txt) explain the preceding failed chained scenario. Raw per-suite logs are retained under ignored `build/qa/suite-logs` for diagnosis.

The runtime scan is not gameplay verification. The repair suite includes 101 numeric-setting registrations; UI geometry assertions are not module behavior tests. The UI suite covers the existing 15-viewport matrix; latest-JAR screenshots at 1080p and 640×480 were inspected for bounds and readability. UI extraction elapsed median was 1.4584 ms and p95 was 5.4046 ms over 60 measured samples. The unchanged elapsed-p95 limit is 16.7 ms. Thread CPU timing was also recorded, but Windows timer granularity makes that diagnostic coarse. These measurements exclude GPU presentation and are not an FPS guarantee.

The [preceding UI benchmark](ui-before-text-cache.txt) records the 18.875 ms elapsed-p95 failure. Bounded text-width/fitting caches, code-point-safe fitting and reused decoration buffers reduce repeated render work. A real resource reload verifies cache invalidation, followed by width and Unicode checks. The movement fixture waits at most 200 ticks for grounded readiness; it retains all trajectory assertions.

Previously inspected Xray scenes from the 10 November candidate (`277ee373…b69a7`): [enabled](candidate-xray-on.png), [disabled](candidate-xray-off.png). Gold/diamond textures remain visible with Xray on; the opaque stone wall and water return when it is off. These images are retained references, not screenshots of the latest JAR; the repair assertions above record the newer automated scenario.

No personal worlds or account vaults were used. This candidate has not been installed in the personal Minecraft mods folder. Server/service-dependent integrations remain deferred at the user's request.
