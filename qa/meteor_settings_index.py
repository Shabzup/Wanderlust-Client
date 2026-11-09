"""Summarize setting identifiers from a local checkout of Meteor for comparison.

This reads external source in build/meteor-reference and writes metadata only.
It does not copy or compile Meteor code into Wanderlust.
"""

from pathlib import Path
import re

project = Path(__file__).resolve().parent.parent
root = project / "build" / "meteor-reference"
modules = root / "src/main/java/meteordevelopment/meteorclient/systems/modules"
registry = (modules / "Modules.java").read_text(encoding="utf-8")
revision = __import__("subprocess").check_output(
    ["git", "-C", str(root), "rev-parse", "HEAD"], text=True
).strip()
paths = {p.stem: p for p in modules.rglob("*.java")}
groups = re.findall(
    r"private void init(Combat|Player|Movement|Render|World|Misc)\(\) \{(.*?)\n    \}",
    registry,
    re.S,
)

lines = [
    "# Meteor settings index",
    "",
    f"Source: [Meteor registry at `{revision[:12]}`](https://github.com/MeteorDevelopment/meteor-client/blob/{revision}/src/main/java/meteordevelopment/meteorclient/systems/modules/Modules.java).",
    "",
    "This is a source index of registered modules and declared setting names/defaults. A blank default means the value is supplied by the setting type or another expression. Conditional and computed controls require source review. It is a reference for independent Wanderlust implementations, not a feature-completeness claim.",
    "",
]

count = 0
for group, body in groups:
    lines += [f"## {group}", ""]
    for name in re.findall(r"add\(new (\w+)\(\)\)", body):
        count += 1
        path = paths.get(name)
        if not path:
            lines += [f"### {name}", "", "Source file missing from checkout.", ""]
            continue
        text = path.read_text(encoding="utf-8")
        url = f"https://github.com/MeteorDevelopment/meteor-client/blob/{revision}/{path.relative_to(root).as_posix()}"
        lines += [f"### [{name}]({url})", "", "| Setting | Source default |", "| --- | --- |"]
        settings = []
        for block in re.findall(r"new\s+\w+Setting\.Builder[^;]*?\.build\(\)", text, re.S):
            key = re.search(r'\.name\("([^\"]+)"\)', block)
            if not key:
                continue
            default = re.search(r"\.defaultValue\((.*?)\)\s*\.", block, re.S)
            value = " ".join(default.group(1).split()) if default else "—"
            if len(value) > 90:
                value = value[:87] + "..."
            settings.append((key.group(1), value.replace("|", "\\|")))
        if settings:
            lines += [f"| `{key}` | `{value}` |" for key, value in settings]
        else:
            lines += ["| *(No setting builder found)* | — |"]
        lines.append("")

lines.insert(6, f"Registry entries indexed: **{count}**. This file lists setting identifiers and source defaults, including controls whose behavior still needs manual review.")
lines.insert(7, "")
(project / "METEOR-SETTINGS.md").write_text("\n".join(lines), encoding="utf-8")
print(f"Indexed {count} registered modules")
