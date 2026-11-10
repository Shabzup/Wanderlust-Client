"""Run existing behavioral suites sequentially and retain assertion-only evidence.

These suites are regression coverage, not proof of complete module parity.
"""
from pathlib import Path
import hashlib
import re
import subprocess
import sys

root=Path(__file__).resolve().parent.parent
artifact=root/'build/libs/WanderlustClient-1.0.0+26.2.jar'
candidate_hash=hashlib.sha256(artifact.read_bytes()).hexdigest()
cases=[
    ("inventory-parity",["--inventory-parity"],"INVENTORY COMPLETE failures=0"),
    ("release-modules",["--release-modules"],"RELEASE MODULES COMPLETE failures=0"),
    ("movement-parity",["--movement-parity"],"MOVEMENT COMPLETE failures=0"),
    ("core",["--core"],"CORE COMPLETE failures=0"),
    ("world",["--world-modules"],"WORLD COMPLETE failures=0"),
    ("combat-motion",["--extended"],"EXTENDED COMPLETE failures=0"),
    ("repair",["--repair"],"REPAIR COMPLETE failures=0"),
    ("all-modules",["--all-modules"],"ALL COMPLETE tested=225 failures=0"),
    ("ui",["--polish"],"POLISH COMPLETE checks=659 failures=0"),
]
selected=sys.argv[1:]
extras='--extras' in selected
if extras:selected.remove('--extras')
if selected:
    unknown=set(selected)-{x[0] for x in cases}
    if unknown:raise SystemExit(f"Unknown suites: {unknown}")
    cases=[c for c in cases if c[0] in selected]
results=[]
report=root/'build/qa/candidate-results.txt'
report.parent.mkdir(parents=True,exist_ok=True)
report.write_text(f'RUNNING JAR SHA256 {candidate_hash}\n',encoding='utf-8')
for name,flags,marker in cases:
    if hashlib.sha256(artifact.read_bytes()).hexdigest()!=candidate_hash:
        raise SystemExit('Candidate JAR changed during regression run; rebuild and restart the suites.')
    print(f"CANDIDATE START {name}",flush=True)
    process=subprocess.run([sys.executable,str(root/'qa/launch.py'),'--clean',*(['--extras'] if extras else []),*flags],cwd=root)
    log=(root/'build/qa/launch.log').read_text(encoding='utf-8',errors='replace')
    evidence=[line.split('[STDOUT]: ',1)[-1] for line in log.splitlines()
              if re.search(r'\b(?:QA|MOVEMENT|CORE|WORLD|REPAIR|POLISH|ALL|INVENTORY|RELEASE MODULES|EXTENDED|COMBAT|CHECK) (?:PASS|FAIL|RESULT|COMPLETE|PERF)',line)]
    destination=root/'qa/evidence'/f'candidate-{name}{"-extras" if extras else ""}.txt'
    mod_names=sorted(p.name for p in (root/'build/qa/isolated/mods').glob('*.jar'))
    destination.write_text(f'JAR SHA256 {candidate_hash}\nMods: {", ".join(mod_names)}\n'+'\n'.join(evidence)+'\n',encoding='utf-8')
    passed=process.returncode==0 and marker in log and not any(re.search(r'\bFAIL(?:ED)?\b',x) for x in evidence)
    results.append((name,passed))
    print(f"CANDIDATE {'PASS' if passed else 'FAIL'} {name}",flush=True)
report.write_text('\n'.join(f'{name}: {"PASS" if passed else "FAIL"}' for name,passed in results)+'\n',encoding='utf-8')
sys.exit(0 if all(passed for _,passed in results) else 1)
