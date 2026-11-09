"""Fetch the original unmodified bundled ViaFabricPlus; verify exact bytes.
Uses Python stdlib, not curl/wget/npm. CI only needs network, no credentials.
"""
from pathlib import Path
import hashlib, urllib.request, os
ROOT=Path(__file__).resolve().parents[1]
DEST=ROOT/'vendor/ViaFabricPlus-4.4.15.jar'
URL='https://cdn.modrinth.com/data/rIC2XJV4/versions/RM6hveoK/ViaFabricPlus-4.4.15.jar'
EXPECTED='002308aba61f85ff236f9cda391e93c158a9b197674c5a1c75db2afd185f224e'
def verify(p):
    digest=hashlib.sha256(p.read_bytes()).hexdigest()
    if digest!=EXPECTED:raise SystemExit('ViaFabricPlus SHA-256 mismatch; refusing to bundle it.')
if DEST.exists():
    verify(DEST)
else:
    DEST.parent.mkdir(parents=True,exist_ok=True)
    temp=DEST.with_suffix('.download')
    try:
        request=urllib.request.Request(URL,headers={'User-Agent':'Interium-private-build/1.0'})
        with urllib.request.urlopen(request,timeout=90) as response,temp.open('wb') as output:
            while chunk:=response.read(1024*1024):output.write(chunk)
        verify(temp)
        os.replace(temp,DEST)
    finally:
        temp.unlink(missing_ok=True)
print('Verified original ViaFabricPlus 4.4.15 dependency.')
