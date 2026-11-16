#!/usr/bin/env python3
"""Task 7.2 regression: synthetic block+item fixture and deliberate broken assets."""
import json
import struct
import tempfile
import unittest
import zlib
from pathlib import Path

from content_contract import ContentContractError, validate


def write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def put_json(root, path, obj):
    write(root / path, json.dumps(obj, indent=2) + "\n")


def png_bytes(size=16):
    def chunk(kind, payload):
        body = kind + payload
        return struct.pack(">I", len(payload)) + body + struct.pack(">I", zlib.crc32(body))
    scan = b"".join(b"\x00" + b"\x7f\x7f\x7f\xff" * size for _ in range(size))
    return (b"\x89PNG\r\n\x1a\n" +
            chunk(b"IHDR", struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0)) +
            chunk(b"IDAT", zlib.compress(scan)) + chunk(b"IEND", b""))


def sample(root):
    java = "src/main/java/com/dreamsdimensions/mod/registry/"
    res = "src/main/resources/"
    assets = res + "assets/dreamsdimensions/"
    data = res + "data/dreamsdimensions/"
    write(root / (java + "ModBlocks.java"), '''
public static final DeferredBlock<Block> MARKED_STONE = BLOCKS.registerSimpleBlock(
        "marked_stone", () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
        .requiresCorrectToolForDrops());
''')
    write(root / (java + "ModItems.java"), '''
public static final DeferredItem<Item> DREAM_DUST = ITEMS.registerItem(
        "dream_dust", Item::new, Item.Properties::new);
public static final DeferredItem<BlockItem> MARKED_STONE_ITEM = ITEMS.registerSimpleBlockItem(
        ModBlocks.MARKED_STONE);
''')
    put_json(root, assets + "blockstates/marked_stone.json",
             {"variants": {"": {"model": "dreamsdimensions:block/marked_stone"}}})
    put_json(root, assets + "models/block/marked_stone.json",
             {"parent": "minecraft:block/cube_all",
              "textures": {"all": "dreamsdimensions:block/marked_stone"}})
    put_json(root, assets + "items/marked_stone.json",
             {"model": {"type": "minecraft:model", "model": "dreamsdimensions:block/marked_stone"}})
    put_json(root, assets + "items/dream_dust.json",
             {"model": {"type": "minecraft:model", "model": "dreamsdimensions:item/dream_dust"}})
    put_json(root, assets + "models/item/dream_dust.json",
             {"parent": "minecraft:item/generated", "textures": {
                 "layer0": "dreamsdimensions:item/dream_dust"}})
    for name in ("block/marked_stone", "item/dream_dust"):
        dest = root / (assets + "textures/" + name + ".png")
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(png_bytes())
    locales = {"block.dreamsdimensions.marked_stone": "Marked Stone",
               "item.dreamsdimensions.dream_dust": "Dream Dust"}
    put_json(root, assets + "lang/en_us.json", locales)
    put_json(root, assets + "lang/pt_br.json", dict(locales))
    put_json(root, data + "loot_table/blocks/marked_stone.json",
             {"type": "minecraft:block", "pools": [{
                 "rolls": 1, "entries": [{"type": "minecraft:item",
                                          "name": "dreamsdimensions:marked_stone"}]}]})
    put_json(root, data + "recipe/marked_stone.json",
             {"type": "minecraft:crafting_shapeless",
              "ingredients": ["minecraft:stone", "dreamsdimensions:dream_dust"],
              "result": {"id": "dreamsdimensions:marked_stone"}})
    put_json(root, res + "data/minecraft/tags/block/mineable/pickaxe.json",
             {"replace": False, "values": ["dreamsdimensions:marked_stone"]})
    put_json(root, "scripts/ci/content_contract_exceptions.json",
             {"schema_version": "dreamsdimensions.content.v1",
              "legacy_blocks_without_loot": {}})


class ContentContractTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        sample(self.root)

    def test_valid_synthetic_f0_content(self):
        report = validate(self.root)
        self.assertEqual(report["status"], "PASS")
        self.assertEqual(report["evidence_type"], "STATIC_CHECK")
        self.assertEqual(report["counts"]["registered_blocks"], 1)
        self.assertEqual(report["counts"]["registered_items"], 2)

    def test_missing_block_item_registration_fails_closed(self):
        path = self.root / "src/main/java/com/dreamsdimensions/mod/registry/ModItems.java"
        source = path.read_text()
        source = source.replace(
            'public static final DeferredItem<BlockItem> MARKED_STONE_ITEM = ITEMS.registerSimpleBlockItem(\n'
            '        ModBlocks.MARKED_STONE);',
            '')
        path.write_text(source)
        with self.assertRaisesRegex(ContentContractError, "missing a BlockItem"):
            validate(self.root)

    def test_missing_blockstate_fails_closed(self):
        (self.root / "src/main/resources/assets/dreamsdimensions/blockstates/marked_stone.json").unlink()
        with self.assertRaisesRegex(ContentContractError, "Blockstate mismatch"):
            validate(self.root)

    def test_missing_model_fails_closed(self):
        (self.root / "src/main/resources/assets/dreamsdimensions/models/block/marked_stone.json").unlink()
        with self.assertRaisesRegex(ContentContractError, "missing model"):
            validate(self.root)

    def test_missing_texture_fails_closed(self):
        (self.root / "src/main/resources/assets/dreamsdimensions/textures/block/marked_stone.png").unlink()
        with self.assertRaisesRegex(ContentContractError, "missing texture"):
            validate(self.root)

    def test_invalid_png_fails_closed(self):
        (self.root / "src/main/resources/assets/dreamsdimensions/textures/block/marked_stone.png").write_bytes(b"bad")
        with self.assertRaisesRegex(ContentContractError, "Invalid PNG header"):
            validate(self.root)

    def test_missing_26_3_item_definition_fails_closed(self):
        (self.root / "src/main/resources/assets/dreamsdimensions/items/marked_stone.json").unlink()
        with self.assertRaisesRegex(ContentContractError, "Client item definition mismatch"):
            validate(self.root)

    def test_missing_language_fails_closed(self):
        pt = self.root / "src/main/resources/assets/dreamsdimensions/lang/pt_br.json"
        pt.write_text('{"item.dreamsdimensions.dream_dust": "Poeira"}')
        with self.assertRaisesRegex(ContentContractError, "pt_br: missing display names"):
            validate(self.root)

    def test_missing_block_loot_fails_closed(self):
        (self.root / "src/main/resources/data/dreamsdimensions/loot_table/blocks/marked_stone.json").unlink()
        with self.assertRaisesRegex(ContentContractError, "Missing block loot tables"):
            validate(self.root)

    def test_unknown_recipe_ingredient_fails_closed(self):
        recipe = self.root / "src/main/resources/data/dreamsdimensions/recipe/marked_stone.json"
        data = json.loads(recipe.read_text())
        data["ingredients"] = ["dreamsdimensions:does_not_exist"]
        recipe.write_text(json.dumps(data))
        with self.assertRaisesRegex(ContentContractError, "unknown registered ID"):
            validate(self.root)

    def test_missing_tool_tag_fails_closed(self):
        (self.root / "src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json").unlink()
        with self.assertRaisesRegex(ContentContractError, "requiresCorrectToolForDrops"):
            validate(self.root)

    def test_unnecessary_legacy_exception_fails_closed(self):
        config = self.root / "scripts/ci/content_contract_exceptions.json"
        obj = json.loads(config.read_text())
        obj["legacy_blocks_without_loot"]["marked_stone"] = "Should not waive a real block loot table"
        config.write_text(json.dumps(obj))
        with self.assertRaisesRegex(ContentContractError, "do not match reviewed legacy exceptions"):
            validate(self.root)


if __name__ == "__main__":
    unittest.main()
