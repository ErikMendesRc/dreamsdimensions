#!/usr/bin/env python3
"""TASK 4.7 — native MobEffect status icon and authoritative bond lifecycle."""
from pathlib import Path
import json
import struct

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/com/dreamsdimensions/mod"
ASSETS = ROOT / "src/main/resources/assets/dreamsdimensions"
read = lambda path: (JAVA / path).read_text(encoding="utf-8")

registration = read("registry/ModEffects.java")
effect = read("effect/PactoVigiliaEffect.java")
service = read("entity/behavior/DreamAnchorService.java")
mod = read("DreamsDimensions.java")
vigia = read("entity/VigiaAdormecidoEntity.java")
fragment = read("entity/FragmentoErranteEntity.java")

assert "DeferredRegister.create(Registries.MOB_EFFECT" in registration
assert 'EFFECTS.register("pacto_de_vigilia", PactoVigiliaEffect::new)' in registration
assert mod.count("ModEffects.register(modEventBus)") == 1
assert "class PactoVigiliaEffect extends MobEffect" in effect
assert "MobEffectCategory.BENEFICIAL" in effect
assert "applyEffectTick(" not in effect  # No parallel server gameplay or accidental buff

assert "ModEffects.PACTO_DE_VIGILIA" in service
assert "player.addEffect(new MobEffectInstance(" in service
assert "0, false, false, true)" in service  # 0 amp, non-ambient, no particles, show icon
assert "if (!player.addEffect(" in service
assert "ACTIVE.put(player.getUUID()" in service
assert service.index("player.addEffect(new MobEffectInstance(") < service.index("ACTIVE.put(player.getUUID()")
assert "if (bond == null) {" in service
assert "if (!player.hasEffect(ModEffects.PACTO_DE_VIGILIA))" in service
assert "clearBond(player);" in service
assert "private static void removeMarker(ServerPlayer player)" in service
assert "player.removeEffect(ModEffects.PACTO_DE_VIGILIA)" in service
for event in ("onPlayerLoggedOut", "onPlayerClone", "onChangedDimension"):
    assert f"void {event}(" in service
assert "DreamAnchorService.grant(" in vigia
assert "PactCooldown" not in fragment

for language, expected in (("pt_br","Pacto de Vigília"),("en_us","Vigil Pact")):
    names = json.loads((ASSETS / "lang" / f"{language}.json").read_text(encoding="utf-8"))
    assert names["effect.dreamsdimensions.pacto_de_vigilia"] == expected

icon = (ASSETS / "textures/mob_effect/pacto_de_vigilia.png").read_bytes()
assert icon[:8] == bytes.fromhex("89504e470d0a1a0a")
assert struct.unpack(">II", icon[16:24]) == (18, 18)
assert icon[25] == 6, "PNG must have alpha (RGBA) to blend on GUI"
assert len(icon) < 8192

print("TASK 4.7 STATIC PASS: native effect ID, custom 18x18 RGBA icon, translation and one-use cleanup")
