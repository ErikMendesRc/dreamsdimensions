#!/usr/bin/env python3
"""Task 7.2: versioned, read-only contract for registered content and client assets.

Static evidence only. Uses the *actual* Java registry definitions plus installed
resource paths, not an imagined catalog of items from the design documents.
Not a client-render, actual loot-roll, mining or Survival validation.
"""
from __future__ import annotations

import json
import re
import struct
from collections import Counter
from pathlib import Path
from typing import Any

SCHEMA = "dreamsdimensions.qa.v1"
MOD = "dreamsdimensions"
RESOURCES = Path("src/main/resources")
JAVA = Path("src/main/java/com/dreamsdimensions/mod/registry")
CONFIG = Path("scripts/ci/content_contract_exceptions.json")
ID = re.compile(r"^([a-z0-9_.-]+):([a-z0-9_./-]+)$")
DECL_BLOCK = re.compile(
    r"public\s+static\s+final\s+DeferredBlock<[^>]+>\s+(\w+)\s*=\s*"
    r"BLOCKS\.register(?:SimpleBlock|Block)\(\s*\"([a-z0-9_]+)\"",
    re.MULTILINE,
)
DECL_ITEM_NAMED = re.compile(
    r"public\s+static\s+final\s+DeferredItem<[^>]+>\s+(\w+)\s*=\s*"
    r"ITEMS\.registerItem\(\s*\"([a-z0-9_]+)\"",
    re.MULTILINE,
)
DECL_ITEM_BLOCK = re.compile(
    r"public\s+static\s+final\s+DeferredItem<[^>]+>\s+(\w+)\s*=\s*"
    r"ITEMS\.registerSimpleBlockItem\(\s*ModBlocks\.(\w+)",
    re.MULTILINE,
)
FIELD_BLOCK = re.compile(r"public\s+static\s+final\s+DeferredBlock<[^>]+>\s+(\w+)\s*=")
FIELD_ITEM = re.compile(r"public\s+static\s+final\s+DeferredItem<[^>]+>\s+(\w+)\s*=")


class ContentContractError(ValueError):
    pass


def require(condition: Any, message: str) -> None:
    if not condition:
        raise ContentContractError(message)


def read_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise ContentContractError(f"Invalid/missing JSON {path}: {exc}") from exc


def id_parts(value: str, context: str) -> tuple[str, str]:
    require(isinstance(value, str), f"{context}: resource location must be a string")
    match = ID.fullmatch(value)
    require(match is not None, f"{context}: invalid namespaced resource location {value!r}")
    return match.group(1), match.group(2)


def own_id(value: str, context: str, known: set[str]) -> None:
    namespace, identifier = id_parts(value, context)
    if namespace == MOD:
        require(identifier in known, f"{context}: unknown registered ID {value}")


def declared_content(root: Path) -> tuple[dict[str, str], set[str], set[str]]:
    blocks_java = (root / JAVA / "ModBlocks.java").read_text(encoding="utf-8")
    items_java = (root / JAVA / "ModItems.java").read_text(encoding="utf-8")
    blocks = dict(DECL_BLOCK.findall(blocks_java))
    fields = FIELD_BLOCK.findall(blocks_java)
    require(len(fields) == len(set(fields)) == len(blocks),
            "ModBlocks: duplicate or unsupported DeferredBlock declaration (update parser deliberately)")
    require(set(fields) == set(blocks), "ModBlocks: unsupported registry declaration")
    require(len(set(blocks.values())) == len(blocks), "Duplicate registered block ID")

    named = dict(DECL_ITEM_NAMED.findall(items_java))
    item_blocks = dict(DECL_ITEM_BLOCK.findall(items_java))
    item_fields = FIELD_ITEM.findall(items_java)
    require(set(named).isdisjoint(item_blocks), "Duplicate item field across registration styles")
    require(set(item_fields) == set(named) | set(item_blocks),
            "ModItems: unsupported DeferredItem declaration (update parser deliberately)")
    require(len(item_fields) == len(set(item_fields)), "Duplicate item registry field")
    for symbol, block_symbol in item_blocks.items():
        require(block_symbol in blocks, f"Item {symbol}: unknown ModBlocks.{block_symbol}")
    items = set(named.values()) | {blocks[symbol] for symbol in item_blocks.values()}
    require(len(items) == len(named) + len(item_blocks), "Duplicate registered item ID")
    require(set(blocks.values()) <= items,
            f"Registered blocks missing a BlockItem: {sorted(set(blocks.values()) - items)}")
    return blocks, items, set(blocks.values())


def validate_png(path: Path) -> None:
    try:
        with path.open("rb") as file:
            header = file.read(33)
    except OSError as exc:
        raise ContentContractError(f"Unreadable PNG: {path}: {exc}") from exc
    require(len(header) >= 33 and header[:8] == b"\x89PNG\r\n\x1a\n"
            and header[12:16] == b"IHDR", f"Invalid PNG header: {path}")
    require(struct.unpack(">I", header[8:12])[0] == 13,
            f"Invalid PNG IHDR size: {path}")
    width, height = struct.unpack(">II", header[16:24])
    require(1 <= width <= 4096 and 1 <= height <= 4096,
            f"Invalid PNG dimensions: {path} {width}x{height}")
    # Asset Bible recommends 16x16 for ordinary block textures; do not
    # impose that constraint on animated textures, atlases or entity models.


class Validator:
    def __init__(self, root: Path):
        self.root = root
        self.assets = root / RESOURCES / "assets" / MOD
        self.data = root / RESOURCES / "data" / MOD
        self.blocks: dict[str, str] = {}
        self.items: set[str] = set()
        self.block_ids: set[str] = set()
        self.checked_models: set[str] = set()
        self.visiting_models: set[str] = set()
        self.checked_textures: set[str] = set()

    def model_reference(self, reference: str, context: str) -> None:
        namespace, identifier = id_parts(reference, context)
        if namespace != MOD:
            return  # Vanilla or mod dependency: runtime validates external namespace.
        require(identifier not in self.visiting_models,
                f"Cyclic model parent/reference: {reference} from {context}")
        if identifier in self.checked_models:
            return
        path = self.assets / "models" / f"{identifier}.json"
        require(path.is_file(), f"{context}: missing model {reference} at {path}")
        model = read_json(path)
        require(isinstance(model, dict), f"Invalid model JSON object: {path}")
        self.visiting_models.add(identifier)
        try:
            parent = model.get("parent")
            if parent is not None:
                self.model_reference(parent, str(path))
            for key, texture in model.get("textures", {}).items():
                require(isinstance(texture, str),
                        f"{path}: texture {key} must be a resource location or #alias")
                if not texture.startswith("#"):
                    self.texture_reference(texture, str(path))
        finally:
            self.visiting_models.remove(identifier)
        self.checked_models.add(identifier)

    def texture_reference(self, reference: str, context: str) -> None:
        namespace, identifier = id_parts(reference, context)
        if namespace != MOD or identifier in self.checked_textures:
            return
        path = self.assets / "textures" / f"{identifier}.png"
        require(path.is_file(), f"{context}: missing texture {reference} at {path}")
        validate_png(path)
        self.checked_textures.add(identifier)

    def client_model(self, node: Any, context: str) -> None:
        if isinstance(node, list):
            for value in node:
                self.client_model(value, context)
        elif isinstance(node, dict):
            if node.get("type") == "minecraft:model":
                self.model_reference(node.get("model"), context)
            for value in node.values():
                if isinstance(value, (dict, list)):
                    self.client_model(value, context)

    def blockstate(self, node: Any, context: str) -> None:
        if isinstance(node, list):
            for value in node:
                self.blockstate(value, context)
        elif isinstance(node, dict):
            if "model" in node and isinstance(node["model"], str):
                self.model_reference(node["model"], context)
            for value in node.values():
                if isinstance(value, (dict, list)):
                    self.blockstate(value, context)

    def all_json(self) -> int:
        files = sorted((self.root / RESOURCES).rglob("*.json"))
        require(files, "No resource JSON files")
        for path in files:
            read_json(path)
        return len(files)

    def check_assets(self) -> dict[str, int]:
        blockstates = {p.stem for p in (self.assets / "blockstates").glob("*.json")}
        client_items = {p.stem for p in (self.assets / "items").glob("*.json")}
        require(blockstates == self.block_ids,
                f"Blockstate mismatch missing={sorted(self.block_ids-blockstates)} "
                f"orphan={sorted(blockstates-self.block_ids)}")
        require(client_items == self.items,
                f"Client item definition mismatch missing={sorted(self.items-client_items)} "
                f"orphan={sorted(client_items-self.items)}")
        for name in sorted(blockstates):
            path = self.assets / "blockstates" / f"{name}.json"
            json_obj = read_json(path)
            require(isinstance(json_obj, dict) and
                    ("variants" in json_obj or "multipart" in json_obj or "definition" in json_obj),
                    f"{path}: missing blockstate variants/multipart/definition")
            self.blockstate(json_obj, str(path))
        for name in sorted(client_items):
            path = self.assets / "items" / f"{name}.json"
            obj = read_json(path)
            require(isinstance(obj, dict) and isinstance(obj.get("model"), dict),
                    f"{path}: missing 26.3 client item model")
            self.client_model(obj["model"], str(path))
        # Validate orphan models too; don't let missing references hide in an
        # asset that happens not to be currently selected by a blockstate.
        for model in sorted((self.assets / "models").rglob("*.json")):
            self.model_reference(f"{MOD}:{model.relative_to(self.assets/'models').with_suffix('').as_posix()}", str(model))
        for png in sorted((self.assets / "textures").rglob("*.png")):
            validate_png(png)
        return {
            "blockstates": len(blockstates), "client_items": len(client_items),
            "models": len(list((self.assets / "models").rglob("*.json"))),
            "textures": len(list((self.assets / "textures").rglob("*.png"))),
        }

    def check_localization(self) -> int:
        en = read_json(self.assets / "lang/en_us.json")
        pt = read_json(self.assets / "lang/pt_br.json")
        require(isinstance(en, dict) and isinstance(pt, dict), "Both locales must be JSON objects")
        keys = {f"block.{MOD}.{name}" for name in self.block_ids}
        keys.update(f"item.{MOD}.{name}" for name in self.items - self.block_ids)
        for language, translations in (("en_us", en), ("pt_br", pt)):
            absent = keys - translations.keys()
            require(not absent, f"{language}: missing display names for {sorted(absent)}")
            require(all(isinstance(translations[key], str) and translations[key].strip()
                        for key in keys), f"{language}: blank display name")
        return len(keys)

    def check_loot(self, exceptions: dict[str, str]) -> int:
        folder = self.data / "loot_table/blocks"
        loot_files = {p.stem for p in folder.glob("*.json")}
        missing = self.block_ids - loot_files
        require(set(exceptions) == missing,
                f"Missing block loot tables do not match reviewed legacy exceptions: "
                f"missing={sorted(missing)} exceptions={sorted(exceptions)}")
        require(all(isinstance(reason, str) and len(reason.strip()) > 12 for reason in exceptions.values()),
                "Each historical loot exception must include a meaningful reason")
        require(loot_files <= self.block_ids,
                f"Orphan block loot tables: {sorted(loot_files-self.block_ids)}")
        for name in sorted(loot_files):
            path = folder / f"{name}.json"
            loot = read_json(path)
            require(loot.get("type") == "minecraft:block", f"{path}: invalid loot table type")
            self.loot_entries(loot, str(path))
        for path in sorted((self.data / "loot_table/chests").rglob("*.json")):
            self.loot_entries(read_json(path), str(path))
        return len(loot_files)

    def loot_entries(self, node: Any, context: str) -> None:
        if isinstance(node, dict):
            if node.get("type") == "minecraft:item" and "name" in node:
                own_id(node["name"], context, self.items)
            for child in node.values():
                if isinstance(child, (dict, list)):
                    self.loot_entries(child, context)
        elif isinstance(node, list):
            for child in node:
                self.loot_entries(child, context)

    def check_tags(self) -> int:
        tags = []
        for namespace in ("minecraft", "c", MOD):
            folder = self.root / RESOURCES / "data" / namespace / "tags"
            tags.extend(folder.rglob("*.json") if folder.exists() else [])
        tag_blocks = set()
        for path in sorted(tags):
            obj = read_json(path)
            require(isinstance(obj, dict) and isinstance(obj.get("values"), list),
                    f"{path}: malformed tag")
            require(obj.get("replace") is not True,
                    f"{path}: do not replace vanilla/shared tag memberships")
            # The containing /tags/block/ or /tags/item/ is authoritative.
            fragments = path.parts
            try:
                tag_kind = fragments[fragments.index("tags") + 1]
            except (ValueError, IndexError):
                continue
            if tag_kind not in ("block", "item"):
                continue
            known = self.block_ids if tag_kind == "block" else self.items
            for row in obj["values"]:
                value = row if isinstance(row, str) else row.get("id")
                require(isinstance(value, str), f"{path}: malformed tag entry")
                if value.startswith("#"):
                    id_parts(value[1:], str(path))
                else:
                    own_id(value, str(path), known)
                    if tag_kind == "block" and "/mineable/" in path.as_posix():
                        tag_blocks.add(value.split(":", 1)[1] if value.startswith(MOD + ":") else value)
        blocks_java = (self.root / JAVA / "ModBlocks.java").read_text(encoding="utf-8")
        matches = list(DECL_BLOCK.finditer(blocks_java))
        for index, match in enumerate(matches):
            section = blocks_java[match.end():matches[index + 1].start()
                                  if index + 1 < len(matches) else len(blocks_java)]
            if ".requiresCorrectToolForDrops()" in section:
                require(match.group(2) in tag_blocks,
                        f"{match.group(2)} requiresCorrectToolForDrops but lacks a mineable tool tag")
        return len(tags)

    def ingredient(self, value: Any, context: str) -> None:
        if isinstance(value, str):
            if value.startswith("#"):
                id_parts(value[1:], context)
            else:
                own_id(value, context, self.items)
        elif isinstance(value, list):
            require(value, f"{context}: empty ingredient alternatives")
            for item in value:
                self.ingredient(item, context)
        elif isinstance(value, dict):
            if "tag" in value:
                id_parts(value["tag"], context)
            elif "item" in value:
                own_id(value["item"], context, self.items)
            elif "id" in value:
                own_id(value["id"], context, self.items)
            else:
                raise ContentContractError(f"{context}: unknown ingredient shape {value!r}")
        else:
            raise ContentContractError(f"{context}: invalid ingredient")

    def check_recipes(self) -> int:
        paths = sorted((self.data / "recipe").glob("*.json"))
        for path in paths:
            recipe = read_json(path)
            require(isinstance(recipe, dict) and isinstance(recipe.get("type"), str),
                    f"{path}: invalid recipe")
            result = recipe.get("result")
            require(isinstance(result, dict) and isinstance(result.get("id"), str),
                    f"{path}: missing result.id")
            own_id(result["id"], str(path), self.items)
            typ = recipe["type"]
            if typ == "minecraft:crafting_shaped":
                pattern, key = recipe.get("pattern"), recipe.get("key")
                require(isinstance(pattern, list) and 1 <= len(pattern) <= 3
                        and all(isinstance(x, str) and 1 <= len(x) <= 3 for x in pattern)
                        and len({len(x) for x in pattern}) == 1, f"{path}: invalid shaped pattern")
                require(isinstance(key, dict) and set(key) == set("".join(pattern)) - {" "},
                        f"{path}: invalid crafting key")
                for x in key.values():
                    self.ingredient(x, str(path))
            elif typ == "minecraft:crafting_shapeless":
                ingredients = recipe.get("ingredients")
                require(isinstance(ingredients, list) and ingredients,
                        f"{path}: missing shapeless ingredients")
                self.ingredient(ingredients, str(path))
            elif typ in ("minecraft:blasting", "minecraft:smelting",
                         "minecraft:smoking", "minecraft:campfire_cooking",
                         "minecraft:stonecutting"):
                self.ingredient(recipe.get("ingredient"), str(path))
            else:
                # Unknown recipe serializers require engine-side validation.
                raise ContentContractError(f"{path}: unreviewed recipe type {typ}")
        return len(paths)

    def run(self) -> dict[str, Any]:
        self.blocks, self.items, self.block_ids = declared_content(self.root)
        cfg = read_json(self.root / CONFIG)
        require(cfg.get("schema_version") == "dreamsdimensions.content.v1",
                "Legacy exceptions must use a versioned schema")
        exceptions = cfg.get("legacy_blocks_without_loot")
        require(isinstance(exceptions, dict), "Missing legacy_blocks_without_loot config")
        counts = {
            "registered_blocks": len(self.block_ids),
            "registered_items": len(self.items),
            "resource_json_files": self.all_json(),
            **self.check_assets(),
            "localized_content_ids": self.check_localization(),
            "block_loot_tables": self.check_loot(exceptions),
            "tags": self.check_tags(),
            "recipes": self.check_recipes(),
            "legacy_no_loot": len(exceptions),
        }
        return {
            "schema_version": SCHEMA, "test_id": "task_7_2_content_asset_contract",
            "category": "CONTENT", "evidence_type": "STATIC_CHECK", "status": "PASS",
            "counts": counts,
            "limitations": [
                "Static registry-source parsing is limited to reviewed NeoForge 26.3 registration styles",
                "Vanilla/third-party resource locations are loaded and validated by Minecraft at runtime",
                "Model and PNG file integrity does not prove rendered appearance in client",
                "Loot-table presence does not prove expected drops, Fortune/Silk Touch or Survival mining",
                "Creative tab discoverability and tool effectiveness require manual Minecraft verification",
            ],
        }


def validate(root: Path) -> dict[str, Any]:
    return Validator(root).run()
