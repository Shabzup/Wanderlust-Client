# Working on WanderlustClient

The canonical project is https://github.com/Shabzup/Wanderlust-Client. Future source changes and verification notes belong in this repository. Use branches named `codex/<change>` and pull requests for subsequent changes.

GitHub Actions builds the mod with Java 25 on pushes and pull requests. Download the resulting JAR from the workflow run's artifacts. A successful build does not prove gameplay behavior: the controlled Minecraft scenarios are documented in [qa/README.md](qa/README.md), and remaining coverage is tracked in [MODULE-AUDIT.md](MODULE-AUDIT.md).

Keep account vaults, authentication data, personal Minecraft settings, worlds, downloaded game libraries, generated builds and raw local logs out of Git. Record relevant assertions in the audit without including private data. The optional Baritone dependency is supplied separately and is not vendored here.
