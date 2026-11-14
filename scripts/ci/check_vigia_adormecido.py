#!/usr/bin/env python3
"""TASK 4.5: file-level contracts; not a substitute for JDK 25 build or visual tests."""
from pathlib import Path
import json
import struct

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / 'src/main/java/com/dreamsdimensions/mod'
ASSET = ROOT / 'src/main/resources/assets/dreamsdimensions'
read = lambda path: (JAVA / path).read_text(encoding='utf-8')
entity = read('entity/VigiaAdormecidoEntity.java')
registration = read('registry/ModEntities.java')
renderer = read('client/event/VigiaRendererRegistration.java')
population = read('entity/VigiaIslandPopulation.java')
habitat = read('entity/VigiaIslandHabitat.java')
main = read('DreamsDimensions.java')
model = json.loads((ASSET / 'geckolib/models/entity/vigia_adormecido.geo.json').read_text(encoding='utf-8'))
anims = json.loads((ASSET / 'geckolib/animations/entity/vigia_adormecido.animation.json').read_text(encoding='utf-8'))['animations']
texture = (ASSET / 'textures/entity/vigia_adormecido.png').read_bytes()
source = json.loads((ROOT / 'art/blockbench/vigia_adormecido.bbmodel').read_text(encoding='utf-8'))
biome = json.loads((ROOT / 'src/main/resources/data/dreamsdimensions/worldgen/biome/dreamscape_biome.json').read_text(encoding='utf-8'))

assert 'registerEntityType("vigia_adormecido"' in registration
assert 'VigiaAdormecidoEntity.createAttributes()' in registration
assert 'VigiaIslandHabitat::maySpawn' in registration
assert 'RegisterSpawnPlacementsEvent.Operation.REPLACE' in registration
assert 'ModEntities.VIGIA_ADORMECIDO' in renderer and 'GeoEntityRenderer' in renderer
assert 'DreamMobPopulationService.register(VigiaIslandPopulation.profile())' in main
assert main.count('DreamMobPopulationService::onPlayerTick') == 1
assert 'FragmentoIslandHabitat.isIslandSurface' in habitat
assert 'feet.above(2)' in habitat
assert 'DreamMobPopulationService::annulusHeightmapCandidate' in population
assert 'VigiaIslandHabitat.habitat()' in population
assert 'class VigiaAdormecidoEntity extends PathfinderMob implements GeoEntity' in entity
assert 'GroundPathNavigation(this, level)' in entity
assert 'new AnimationController' in entity and entity.count('new AnimationController') == 1
assert 'SynchedEntityData.defineId' in entity
assert 'getNavigation().moveTo(' in entity
assert 'hasChunkAt(probe)' in entity
assert 'VigiaHomeX' in entity and 'VigiaHomeY' in entity and 'VigiaHomeZ' in entity
assert 'registerControllers(' in entity
expected = {'animation.vigia_adormecido.' + name for name in ('idle_dormant', 'awaken',
    'idle_awake', 'observe', 'walk', 'defensive', 'return_dormant', 'inspect_ground', 'alert')}
assert set(anims) == expected, set(anims) ^ expected
assert len(model['minecraft:geometry']) == 1
bones = model['minecraft:geometry'][0]['bones']
names = {bone['name'] for bone in bones}
assert {'root','head','torso','left_leg','right_leg','left_arm','right_arm'} <= names
for name, clip in anims.items():
    assert clip['animation_length'] > 0, name
    assert set(clip.get('bones',{})) <= names, name
    assert ('loop' not in clip or not clip['loop']) == any(name.endswith('.' + a) for a in (
        'awaken','defensive','return_dormant','inspect_ground','alert')), name
assert texture[:8] == b'\x89PNG\r\n\x1a\n'
assert struct.unpack('>II',texture[16:24]) == (128,128)
assert source['meta']['model_format'] == 'geckolib_model'
spawns = biome['attributes']['minecraft:gameplay/natural_mob_spawns']['argument']['spawns_by_category']['creature']
by_type = {item['type']: item for item in spawns}
assert set(by_type) == {'dreamsdimensions:fragmento_errante','dreamsdimensions:vigia_adormecido'}
assert by_type['dreamsdimensions:fragmento_errante']['weight'] == 100
assert by_type['dreamsdimensions:vigia_adormecido']['weight'] == 18
assert by_type['dreamsdimensions:vigia_adormecido']['count'] == {
    'type': 'minecraft:uniform', 'min_inclusive':1,'max_inclusive':1}
print('TASK 4.5 STATIC PASS: Vigia assets, 9 animations, server AI, habitat/spawn/GeckoLib contracts')
