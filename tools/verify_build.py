from pathlib import Path
import zipfile,hashlib,json
root=Path(__file__).resolve().parents[1]
version=next(l.split('=',1)[1].strip() for l in (root/'gradle.properties').read_text().splitlines() if l.startswith('mod_version='))
jar=root/'build/libs'/('interium-'+version+'.jar')
assert jar.is_file(), 'Remapped installable JAR missing'
with zipfile.ZipFile(jar) as z:
    assert z.testzip() is None
    meta=json.loads(z.read('fabric.mod.json'))
    assert meta['id']=='interium' and meta['version']==version
    assert meta['depends']['minecraft']=='1.21.11'
    nested=z.read('META-INF/jars/ViaFabricPlus-4.4.15.jar')
    assert hashlib.sha256(nested).hexdigest()=='002308aba61f85ff236f9cda391e93c158a9b197674c5a1c75db2afd185f224e'
    for n in ['interium/fabric/FreeCamera.class','interium/fabric/TntCountdownRenderer.class','interium/mixin/TntCountdownMixin.class','assets/delta/fonts/onest_regular.png','interium.mixins.json']:
        assert n in z.namelist(),n
    cfg=json.loads(z.read('interium.mixins.json'))
    for name in cfg.get('client',[])+cfg.get('mixins',[]):
        assert cfg['package'].replace('.','/')+'/'+name.replace('.','/')+'.class' in z.namelist(),name
report={'installableJar':jar.name,'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'size':jar.stat().st_size,'minecraft':'1.21.11','loaderMinimum':meta['depends']['fabricloader'],'bundledViaFabricPlus':'4.4.15','zipIntegrity':'passed','requiredClasses':'present','mixinRegistryClasses':'present','runtimeTested':False}
(root/'build/verification.json').write_text(json.dumps(report,indent=2))
(root/'build/SHA256SUMS').write_text(''.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name+'\n' for p in sorted((root/'build/libs').glob('*.jar'))))
print(json.dumps(report,indent=2))
