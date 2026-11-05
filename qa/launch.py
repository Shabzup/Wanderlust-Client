from pathlib import Path
import sys, os, subprocess as sp, json, shutil, zipfile
source = Path(__file__).resolve().parent
project = source.parent
root = project / "build" / "qa"
root.mkdir(parents=True, exist_ok=True)
mc = Path(os.environ.get('WANDERLUST_MINECRAFT_DIR', str(Path(os.environ['APPDATA']) / '.minecraft')))
instance = os.environ.get('WANDERLUST_FABRIC_PROFILE', 'fabric-loader-0.19.5-26.2')
ver = mc / 'versions' / instance
profile = json.loads((ver / (instance+'.json')).read_text())
memory = '--memory-write' in sys.argv or '--memory-read' in sys.argv
game = root / ('persistence' if memory else 'isolated' if '--clean' in sys.argv else 'instance')
mods = game / 'mods'
mods.mkdir(parents=True, exist_ok=True)
libs = []
for lib in profile['libraries']:
    if 'rules' in lib and not any(r.get('os',{}).get('name','windows') == 'windows' and r['action']=='allow' for r in lib['rules']): continue
    if 'artifact' in lib: p = mc / 'libraries' / lib['artifact']['path']
    else:
        g,a,v,*classifier = lib['name'].split(':')
        p = mc / 'libraries' / g.replace('.','/') / a / v / ('-'.join([a,v,*classifier])+'.jar')
    if p.exists(): libs.append(p)
libs += [ver/(instance+'.jar')]
classpath = os.pathsep.join(str(p) for p in libs)
classes = root/'classes'
classes.mkdir(exist_ok=True)
api = mc/'mods'/'fabric-api-0.161.0+26.2.jar'
shutil.copyfile(api,mods/api.name)
testcp = classpath + os.pathsep + os.pathsep.join(str(p) for p in (project/'build'/'fabric-libs').glob('*.jar'))
prod = project/'build'/'libs'/'WanderlustClient-1.0.0+26.2.jar'
baseline = '--baseline' in sys.argv
if not baseline:
    shutil.copyfile(prod, mods/prod.name)
    if not memory or '--memory-write' in sys.argv:
        (game/'config'/'wanderlust.properties').unlink(missing_ok=True)
    if '--memory-write' in sys.argv:
        (game/'config'/'wanderlust-accounts.dat').unlink(missing_ok=True)
    if '--extras' in sys.argv:
        for mod in (mc/'mods').glob('*.jar'):
            if mod.name.startswith(('meteor-client-', 'WanderlustClient-', 'fabric-api-')): continue
            shutil.copyfile(mod,mods/mod.name)
sp.run(['javac','-proc:none','-classpath',testcp,'-d',str(classes),*[str(p) for p in source.glob('*Probe.java')]],check=True)
with zipfile.ZipFile(mods/'qa-probe.jar','w') as z:
    z.writestr('fabric.mod.json',json.dumps(dict(schemaVersion=1,id='qa_probe',version='1.0',name='Isolated QA probe',environment='client',entrypoints=dict(client=['qa.UiPolishProbe' if '--polish' in sys.argv else 'qa.WorldModulesProbe' if '--world-modules' in sys.argv else 'qa.ExtendedProbe' if '--extended' in sys.argv else 'qa.CoreModulesProbe' if '--core' in sys.argv else 'qa.AllModulesProbe' if '--all-modules' in sys.argv else 'qa.RepairProbe' if '--repair' in sys.argv else 'qa.MemoryProbe' if memory else 'qa.WorkspaceProbe' if '--workspace' in sys.argv else 'qa.UiUpgradeProbe' if '--upgrade' in sys.argv else 'qa.UiProbe' if '--ui' in sys.argv else 'qa.Probe']))))
    for p in classes.rglob('*.class'): z.write(p,p.relative_to(classes).as_posix())
(game/'options.txt').write_text('pauseOnLostFocus:false\nrenderDistance:3\nsimulationDistance:5\nmaxFps:30\nenableVsync:false\nguiScale:2\nshowAutosaveIndicator:false\n')
args = ['-Dqa.combatonly='+str('--combatonly' in sys.argv).lower(),'-Dqa.visualonly='+str('--visualonly' in sys.argv).lower(),'-Dwanderlust.qa.phase='+('write' if '--memory-write' in sys.argv else 'read'),'-Xms256m','-Xmx1300m','-XX:ActiveProcessorCount=4','--sun-misc-unsafe-memory-access=allow','--enable-native-access=ALL-UNNAMED', '-cp',classpath,profile['mainClass'],'--username','WanderlustTest','--uuid','00000000000040008000000000002602','--accessToken','0','--version','26.2','--gameDir',str(game),'--assetsDir',str(mc/'assets'),'--assetIndex',profile['assets'],'--width','1920' if '--polish' in sys.argv else '1440' if '--upgrade' in sys.argv or '--workspace' in sys.argv or '--repair' in sys.argv or '--all-modules' in sys.argv or memory else '720','--height','1080' if '--polish' in sys.argv else '900' if '--upgrade' in sys.argv or '--workspace' in sys.argv or '--repair' in sys.argv or '--all-modules' in sys.argv or memory else '480','--versionType','release']
argfile = root/'launch.args'
argfile.write_text('\n'.join('"'+str(v).replace('\\','/')+'"' for v in args))
with (root/'launch.log').open('w',encoding='utf-8') as f:
    p=sp.Popen(['java','@'+str(argfile)],cwd=game,stdout=f,stderr=sp.STDOUT)
    try: rc=p.wait(timeout=240)
    except sp.TimeoutExpired: p.kill(); p.wait(); raise
if '--polish' in sys.argv:
    results=(root/'launch.log').read_text(encoding='utf-8')
    if 'POLISH COMPLETE' not in results or 'POLISH FAIL' in results:
        rc=rc or 1
print('Exit',rc)
sys.exit(rc)


