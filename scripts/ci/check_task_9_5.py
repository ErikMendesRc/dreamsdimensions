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
    # Task 9.5.7 replaces the flat cross with cutout and actual ring/cord geometry.
    assert catcher.get("render_type") is None  # 26.1+ detects cutout from the sprite alpha
    assert len(catcher["elements"])>=10
    assert catcher["textures"]["cross"]=="dreamsdimensions:block/dreamcatcher"
    assert catcher["textures"]["particle"]=="dreamsdimensions:block/dreamcatcher"
    assert all(ref.startswith("dreamsdimensions:block/") for ref in catcher["textures"].values()), "Block-model textures must be in the blocks atlas"
    block_sprite=A/"textures/block/dreamcatcher.png"
    assert (png_details(block_sprite)["width"],png_details(block_sprite)["height"])==(16,16)
    assert_png_integrity(block_sprite)
    # Task 9.5.9 regression: icon model must not reuse a cutout block model.
    # 26.3 item model definitions live under assets/<namespace>/items/;
    # the generated inventory model is separately resolved under models/item/.
    catcher_item=load(A/"items/dreamcatcher.json")
    assert catcher_item["model"]=={
        "type":"minecraft:model","model":"dreamsdimensions:item/dreamcatcher"
    }, "Dreamcatcher inventory item must use a dedicated item model"
    catcher_icon=load(A/"models/item/dreamcatcher.json")
    assert catcher_icon["parent"]=="minecraft:item/generated"
    assert catcher_icon["textures"]["layer0"]=="dreamsdimensions:item/dreamcatcher"
    assert (A/"textures/item/dreamcatcher.png").is_file()
    assert_png_integrity(A/"textures/item/dreamcatcher.png")
    # Task 9.5.11 — ceiling-only, four orientations, support and survival guards.
    variants=load(A/"blockstates/dreamcatcher.json")["variants"]
    expected={"facing=north":0,"facing=east":90,"facing=south":180,"facing=west":270}
    assert set(variants)==set(expected)
    for facing,y in expected.items():
        assert variants[facing]["model"]=="dreamsdimensions:block/dreamcatcher"
        assert variants[facing]["y"]==y
    catcher_java=java("block/DreamcatcherBlock.java")
    for snippet in (
            "context.getClickedFace() != Direction.DOWN",
            "return null;",
            "Block.canSupportCenter(level, pos.above(), Direction.DOWN)",
            "directionToNeighbour == Direction.UP",
            "Blocks.AIR.defaultBlockState()",
            "BlockStateProperties.HORIZONTAL_FACING",
            "getHorizontalDirection().getOpposite()",
            "getShape(", "rotate(", "mirror("):
        assert snippet in catcher_java, snippet
    registry=java("registry/ModBlocks.java")
    assert 'BLOCKS.registerBlock("dreamcatcher", DreamcatcherBlock::new' in registry
    assert ".noCollision().instabreak().noOcclusion()" in registry
    incubation=java("service/DreamIncubationEnvironment.java")
    assert "height <= 4" in incubation and "bed.above(height)" in incubation
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
    # Task 9.5.12: completed wake must be independent of the dream-entry roll.
    assert "CYCLES.onCompletedWake(" in sleep
    assert "ACTIVE_BEDS" in sleep
    assert "completedWakeup.canAttemptEntry()" in sleep
    cycle=java("service/DreamSleepCycle.java")
    assert "Set<UUID> completedWake" in cycle
    assert "boolean onCompletedWake(" in cycle
    # Dedicated bedside GUI: block -> persistent entity -> menu -> screen.
    bedblock=java("block/DreamBedsideBlock.java")
    assert "server.openMenu(bedside)" in bedblock
    entity=java("block/entity/DreamBedsideBlockEntity.java")
    assert "implements MenuProvider" in entity
    assert "new DreamBedsideMenu(id, inventory, this)" in entity
    assert "takeFinished(ServerPlayer player)" in entity
    assert "takePending(ServerPlayer player)" in entity
    bedside_menu=java("menu/DreamBedsideMenu.java")
    assert "new SimpleContainer(2)" in bedside_menu
    assert "boolean clickMenuButton(Player player, int buttonId)" in bedside_menu
    assert "boolean mayPickup(Player player) { return false; }" in bedside_menu
    assert "bedside.load(server, held, currentCapacity())" in bedside_menu
    assert "bedside.takeFinished(server)" in bedside_menu
    assert "bedside.takePending(server)" in bedside_menu
    assert "boolean stillValid(Player player)" in bedside_menu
    menu_registry=java("registry/ModMenuTypes.java")
    assert 'MENUS.register("dream_bedside"' in menu_registry
    client_registry=java("client/event/ClientModEvents.java")
    assert "event.register(ModMenuTypes.DREAM_BEDSIDE.get()" in client_registry
    screen=java("client/screen/DreamBedsideScreen.java")
    assert "handleInventoryButtonClick" in screen
    assert "textures/gui/container/dream_bedside_gui.png" in screen
    gui=A/"textures/gui/container/dream_bedside_gui.png"
    assert_png_integrity(gui)
    assert (png_details(gui)["width"], png_details(gui)["height"]) == (176, 166)
    for lang in ("pt_br","en_us"):
        texts=load(A/f"lang/{lang}.json")
        for key in ("container.dreamsdimensions.dream_bedside",
                    "container.dreamsdimensions.bedside.add",
                    "container.dreamsdimensions.bedside.collect",
                    "container.dreamsdimensions.bedside.cancel",
                    "container.dreamsdimensions.bedside.room.serene",
                    "container.dreamsdimensions.bedside.room.ambiguous"):
            assert key in texts
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
