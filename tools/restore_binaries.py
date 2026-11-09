from pathlib import Path
import json,base64,hashlib
root=Path(__file__).resolve().parents[1]
for item in json.loads((root/'binary-transfer/manifest.json').read_text()):
    target=(root/item['path']).resolve()
    if not target.is_relative_to(root):raise ValueError('Invalid target')
    data=base64.b64decode(''.join((root/part).read_text() for part in item['parts']),validate=True)
    if hashlib.sha256(data).hexdigest()!=item['sha256']:raise ValueError('Binary checksum mismatch: '+item['path'])
    target.parent.mkdir(parents=True,exist_ok=True)
    target.write_bytes(data)
    print('Verified',item['path'])
