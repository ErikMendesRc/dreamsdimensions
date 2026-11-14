#!/usr/bin/env python3
"""Structural-only art QA; never substitutes a Minecraft client render."""
import json
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
AS = ROOT / "src/main/resources/assets/dreamsdimensions"
ITEMS = "serene_vial dream_thread dormant_ember dreamy_herb dreamcatcher resonance_probe marked_lining dream_journal infusion_route".split()
BLOCKS = "deep_sleep_stone bedside_side bedside_top bedside_drawer bedside_niche bedside_metal trail_marker_side trail_marker_top trail_marker_brass trail_marker_gem".split()
PNGS = [(f"textures/item/{n}.png",16) for n in ITEMS] + [(f"textures/block/{n}.png",16) for n in BLOCKS] + [("textures/mob_effect/route.png",18)]

def check_png(path, size):
    buf=path.read_bytes()
    assert buf.startswith(b"\\x89PNG\\r\\n\\x1a\\n"),path
    i=8
    seen=set()
    while i<len(buf):
        assert i+12<=len(buf),path
        n=struct.unpack(">I",buf[i:i+4])[0]
        typ=buf[i+4:i+8]; payload=buf[i+8:i+8+n]
        crc=struct.unpack(">I",buf[i+8+n:i+12+n])[0]
        assert zlib.crc32(typ+payload)&0xffffffff==crc,(path,typ)
        if typ==b"IHDR":
            w,h,depth,color=struct.unpack(">IIBB",payload[:10])
            assert (w,h,depth,color)==(size,size,8,6),path
        seen.add(typ); i+=12+n
    assert b"IHDR" in seen and b"IEND" in seen and i==len(buf),path

for rel,size in PNGS:
    path=AS/rel
    assert path.is_file(),path
    check_png(path,size)

for name in ("dream_bedside","dreamcatcher","trail_marker"):
    path=AS/"models/block"/f"{name}.json"
    data=json.loads(path.read_text(encoding="utf-8"))
    assert len(data["elements"])>=10,path
    for el in data["elements"]:
        frm,to=el["from"],el["to"]
        assert len(frm)==len(to)==3
        assert all(0<=a<b<=16 for a,b in zip(frm,to)),(path,frm,to)
        assert len(el["faces"])==6
        for face in el["faces"].values():
            assert face["texture"].startswith("#")
            assert face["texture"][1:] in data["textures"],path
    for val in data["textures"].values():
        ns,tex=val.split(":",1)
        assert ns=="dreamsdimensions"
        assert (AS/"textures"/f"{tex}.png").is_file(),(path,val)

print(f"PASS: {len(PNGS)} PNGs (signature, CRC, RGBA, dimensions); 3 models; all texture references")
print("NOT RUN: Gradle build / client render / Survival gameplay / dedicated server")
