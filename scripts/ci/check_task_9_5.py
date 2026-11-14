#!/usr/bin/env python3
"""Task 9.5: static contracts for advanced preparation (not Survival proof)."""
from pathlib import Path
from collections import Counter
import json
from audit_visual_assets import assert_png_integrity, png_details

ROOT=Path(__file__).resolve().parents[2]
R=ROOT/"src/main/resources"
A=R/"assets/dreamsdimensions"
D=R/"data/dreamsdimensions"
J=ROOT/"src/main/java/com/dreamsdimensions/mod"

def load(path):
    return json.loads(path.read_text(encoding="utf-8"))

def java(path):
    return (J/path).read_text(encoding="utf-8")

def check():
    required=["serene_vial","dream_thread","dormant_ember","dreamy_herb",
              "dreamcatcher","resonance_probe","marked_lining",
              "dream_journal","infusion_route"]
    for name in required:
        texture=A/"textures/item"/(name+".png")
        info=png_details(texture)
        assert (info["width"], info["height"])==(16,16), name
        assert_png_integrity(texture)
    for name in ["bedside_top","bedside_side","deep_sleep_stone",
                 "trail_marker_side","trail_marker_top"]:
        texture=A/"textures/block"/(name+".png")
        info=png_details(texture)
        assert (info["width"],info["height"])==(16,16),name
        assert_png_integrity(texture)
    assert_png_integrity(A/"textures/mob_effect/route.png")
    for name in ("dream_bedside","dreamcatcher","deep_sleep_stone","trail_marker"):
        assert load(A/f"blockstates/{name}.json")["variants"],name
        assert load(A/f"items/{name}.json")["model"],name
        loot=load(D/f"loot_table/blocks/{name}.json")
        assert loot["pools"][0]["entries"][0]["name"]==f"dreamsdimensions:{name}"
    # The cabinet is a furniture model, not just an unshaded cube.
    bedside=load(A/"models/block/dream_bedside.json")
    assert len(bedside["elements"])>=5
    catcher=load(A/"models/block/dreamcatcher.json")
    assert catcher["parent"]=="minecraft:block/cross"
    # No recipe may fabricate a dream-only Reminiscence or bypass early return.
    for forbidden in ["reminiscence","deep_sleep_stone","trail_marker"]:
        assert not (D/f"recipe/{forbidden}.json").exists(),forbidden
    chamber=java("service/CoreChamberPolicy.java")
    assert "hasLowerRing" in chamber and "hasUpperRing" in chamber
    assert "ATTUNED" in chamber and "CUT_MARKED_STONE" in chamber
    assert "dx=-2; dx<=2; dx+=2" in chamber and "dz=-2; dz<=2; dz+=2" in chamber
    assert "core.offset(2,1,0)" in chamber and "core.offset(0,1,-2)" in chamber
    menu=java("menu/OneiricCoreMenu.java")
    for item in ["ONEIRIC_AWAKENER","VIGIL_TETHER","RESONANCE_PROBE",
                 "DREAM_DUST","TOASTED_VIGIL_HERB","DREAMY_HERB","REMINISCENCE"]:
        assert item in menu,item
    for snippet in ["new Assembly(5,4,1","new Assembly(6,2,1",
                    "new Assembly(1,2,1","clearContainer(player, inputs)"]:
        assert snippet in menu,snippet
    assert "private final DataSlot chamber" in menu
    bedsidejava=java("block/entity/DreamBedsideBlockEntity.java")
    for symbol in ["lastNight","owner","finishedCount","inputCount",
                   "preRemoveSideEffects","saveAdditional","loadAdditional"]:
        assert symbol in bedsidejava,symbol
    sleep=java("event/SleepTeleportHandler.java")
    assert "DreamIncubationService.afterFullNight" in sleep
    assert "DreamMineralSleepService.afterFullNight" in sleep
    inputs={
        "serene_vial":"dream_bedside","dream_thread":"dreamcatcher",
        "dormant_ember":"dream_bedside","dreamy_herb":"dream_bedside"
    }
    for name in ("dream_bedside","dreamcatcher","marked_lining","dream_journal"):
        recipe=load(D/f"recipe/{name}.json")
        assert recipe["result"]["id"]=="dreamsdimensions:"+name
    assert len(load(D/"recipe/dreamcatcher.json")["pattern"])==3
    assert len(load(D/"recipe/dream_bedside.json")["pattern"])==3
    print("TASK 9.5 STATIC_PASS: chambers, incubation, models, art, recipes and QA invariants.")
    print("TASK 9.5 LIMITS: Sleep and multiplayer not manually verified. "
          "Trail Marker recipe requires future Luzerna from Task 9.6.")

if __name__=="__main__":
    check()
