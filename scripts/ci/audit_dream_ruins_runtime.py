#!/usr/bin/env python3
"""Task 5.7 read-only actual-seed /locate + chest/worldgen validation.

This is a dedicated-server test on a disposable, NEW world; unlike
/loot and /place, /locate only asks native worldgen to resolve structures.
The opt-in Java command verifies a REAL chest block entity and pending
vanilla loot-table key without rolling/opening it.
"""
import argparse
import json
import re
import time
from pathlib import Path
from check_dreamscape_homologation import Rcon

VARIANTS = (
    ("blue", "dreamsdimensions:campo_onirico_azul", "dreamsdimensions:blue_dream_ruins"),
    ("fragmented", "dreamsdimensions:dreamscape", "dreamsdimensions:fragmented_dream_ruins"),
)
AUDIT = re.compile(
    r"DREAM_RUINS_AUDIT status=OK variant=(\w+)"
    r" dimension=([a-z0-9_:]+) origin=(-?\d+),(-?\d+)"
    r" radius=(\d+) chests=(\d+) loot_matches=(\d+) stable_chests=(\d+)"
)

def coords_from_locate(text):
    # Java command returns e.g. "The nearest ... is at [x, ~, z] (... blocks away)".
    # Also support versions returning x y z as two-dimensional x/z or 3D.
    pattern = re.search(r"\[\s*(-?\d+)\s*,\s*(?:~|-?\d+)\s*,\s*(-?\d+)\s*\]", text)
    if pattern:
        return int(pattern.group(1)),int(pattern.group(2))
    pattern = re.search(r"\[\s*(-?\d+)\s*,\s*(-?\d+)\s*\]", text)
    if pattern:
        return int(pattern.group(1)),int(pattern.group(2))
    return None

def invoke(expected_seed, output):
    con = Rcon()
    details = []
    try:
        con.auth()
        seed_reply = con.command("seed")
        if expected_seed not in [int(v) for v in re.findall(r"-?\d+",seed_reply)]:
            raise AssertionError(f"Expected seed={expected_seed} got {seed_reply!r}")
        print("DREAM_RUINS_SEED_VERIFIED seed=",expected_seed,flush=True)
        for variant, dimension, structure in VARIANTS:
            command = (f"execute in {dimension} positioned 0 128 0 "
                       f"run locate structure {structure}")
            start = time.monotonic()
            result = con.command(command)
            print(f"DREAM_RUINS_LOCATE_RAW variant={variant} response={result!r}",flush=True)
            xy = coords_from_locate(result)
            if xy is None:
                details.append({"variant":variant,"dimension":dimension,
                                "structure":structure,"status":"LOCATE_FAILED",
                                "response":result})
                continue
            x,z = xy
            # Worldgen is forced only in an ephemeral CI world and the tickets
            # are removed immediately. The diagnostic itself is strictly loaded-only.
            radius = 24
            x1,z1,x2,z2 = x-radius,z-radius,x+radius,z+radius
            preload = con.command(
                f"execute in {dimension} run forceload add {x1} {z1} {x2} {z2}")
            print(f"DREAM_RUINS_PRELOAD variant={variant} {preload[:250]!r}",flush=True)
            try:
                audit_text=""
                for attempt in range(8):
                    audit_text=con.command(f"dream_ruins_audit {variant} {x} {z} {radius}")
                    if "DREAM_RUINS_AUDIT status=OK" in audit_text:
                        break
                    if "chunk_not_loaded" not in audit_text:
                        break
                    time.sleep(2)
                parsed=AUDIT.search(audit_text)
                if parsed is None:
                    details.append({"variant":variant,"dimension":dimension,
                                    "structure":structure,"x":x,"z":z,
                                    "status":"AUDIT_FAILED","response":audit_text})
                    continue
                fields=parsed.groups()
                chest_count,loot_matches,stable=map(int,fields[-3:])
                detail={"variant":variant,"dimension":dimension,
                        "structure":structure,"seed":expected_seed,"x":x,"z":z,
                        "status":"OBSERVED","chest_count":chest_count,
                        "loot_matches":loot_matches,"stable_chests":stable,
                        "locate_seconds":round(time.monotonic()-start,2),
                        "audit":parsed.group(0)}
                details.append(detail)
                print("DREAM_RUINS_RUNTIME_OBSERVED",json.dumps(detail,sort_keys=True),flush=True)
            finally:
                remove=con.command(
                    f"execute in {dimension} run forceload remove {x1} {z1} {x2} {z2}")
                print(f"DREAM_RUINS_FORCELoad_RELEASE variant={variant} {remove[:150]!r}",flush=True)
        summary={"source":"REAL_NATIVE_STRUCTURE_LOCATE_AND_LOADED_CHEST_READ",
                 "seed":expected_seed,"details":details,
                 "limitations":["locate is a structure-start search, not proof of route accessibility",
                                "no Survival player, no chest opening, no multiplayer/loot persistence test",
                                "single located candidate per dimension, not distribution per seed",
                                "chest positions and support tests logged by Java to server-smoke.log"]}
        output.parent.mkdir(parents=True,exist_ok=True)
        output.write_text(json.dumps(summary,indent=2,sort_keys=True)+"\n",encoding="utf-8")
        print("DREAM_RUINS_RUNTIME_SUMMARY",json.dumps(summary,sort_keys=True),flush=True)
        for row in details:
            if row["status"]!="OBSERVED" or row["chest_count"] != 1 or row["loot_matches"] != 1:
                raise AssertionError(f"Native ruins/chest validation failed: {row}")
            # Task 7.0: physical floor/walkability is a HARD GATE.
            # A passing /locate + chest reference is insufficient for safe recovery.
            if row["stable_chests"] != 1:
                raise AssertionError("Unsafe natural recovery ruin: a real chest exists, "
                                     "but local support/walkability failed: "
                                     f"{row}")
        return summary
    finally:
        con.close()

if __name__ == "__main__":
    a=argparse.ArgumentParser()
    a.add_argument("--expected-seed",type=int,required=True)
    a.add_argument("--output",type=Path,required=True)
    args=a.parse_args()
    invoke(args.expected_seed,args.output)
