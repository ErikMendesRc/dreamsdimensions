#!/usr/bin/env python3
"""Task 7.5 — actual dedicated-server loot-table execution through native /loot.

This is NOT a Survival mining test: /loot mine invokes the engine's loot
resolution but does not reproduce a human block-break event or tool wear.
One disposable CI test position in a forced chunk, full cleanup in finally.
"""
import argparse
import json
import re
import time
from pathlib import Path
from check_dreamscape_homologation import Rcon

BLOCKS = ("dreamsdimensions:marked_stone", "dreamsdimensions:cut_marked_stone")
POSITION = (16, 302, 16)


def check_response(response, operation):
    bad = ("unknown command", "unknown block", "unknown loot", "incorrect argument",
           "failed to execute", "error executing", "not a valid", "not loaded")
    if any(text in response.lower() for text in bad) or not response.strip():
        raise AssertionError(f"{operation}: {response!r}")


def run(seed, output):
    client = Rcon()
    forced = False
    records = []
    x, y, z = POSITION
    try:
        client.auth()
        check_response(client.command("seed"), "seed")
        forceload = client.command("execute in minecraft:overworld run forceload add 16 16")
        check_response(forceload, "forceload add")
        forced = True
        for block in BLOCKS:
            place = client.command(
                f"execute in minecraft:overworld run setblock {x} {y} {z} {block} replace")
            check_response(place, "setblock " + block)
            match = client.command(
                f"execute in minecraft:overworld if block {x} {y} {z} {block} run seed")
            check_response(match, "if block")
            if seed not in [int(v) for v in re.findall(r"-?\d+", match)]:
                raise AssertionError(f"Native block did not persist: {block}: {match!r}")
            spawn = client.command(
                f"execute in minecraft:overworld run loot spawn {x} {y+3} {z} "
                f"mine {x} {y} {z} minecraft:stone_pickaxe")
            check_response(spawn, "/loot mine")
            # The native command's returned count must be exactly one stack.
            # It does not prove actual physical Survival block breaking.
            if not re.search(r"\b1\b", spawn):
                raise AssertionError(f"Native loot did not report one stack: {block}: {spawn!r}")
            query = (
                f"execute in minecraft:overworld positioned {x} {y+3} {z} "
                f"if entity @e[type=minecraft:item,distance=..8,"
                f'nbt={{Item:{{id:"{block}"}}}}] run seed'
            )
            actual = client.command(query)
            check_response(actual, "item entity identity")
            if seed not in [int(v) for v in re.findall(r"-?\d+", actual)]:
                raise AssertionError(f"Expected dropped item was not observed: {block}, {actual!r}")
            records.append({
                "block": block, "tool_in_loot_context": "minecraft:stone_pickaxe",
                "loot_api": "minecraft:loot_mine", "observed_item": block,
                "native_loot_response": spawn.strip(),
            })
            # Remove only test item entities at the isolated high-altitude
            # test position in a temporary CI world.
            purge = client.command(
                f"execute in minecraft:overworld positioned {x} {y+3} {z} "
                f"run kill @e[type=minecraft:item,distance=..8]")
            check_response(purge, "cleanup dropped test items")
        report = {
            "schema_version": "dreamsdimensions.qa.v1",
            "test_id": "task_7_5_f0_native_loot_context",
            "category": "CONTENT", "status": "PASS",
            "evidence_type": "RUNTIME_OBSERVED", "seed": seed,
            "source": "NATIVE_LOOT_COMMAND_ON_TEMPORARY_BLOCKS",
            "records": records,
            "limitations": [
                "Native /loot mine is a simulated block loot context, NOT physical player mining",
                "Wrong-tier tool drops, Fortune, Silk Touch and actual Survival action NOT_RUN",
                "Exactly one temporary world test position; no natural generation claim",
            ],
        }
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n",
                          encoding="utf-8")
        print("TASK_7_5_NATIVE_LOOT_PASS", json.dumps(report["records"], sort_keys=True),
              flush=True)
        return report
    finally:
        try:
            if forced:
                client.command(f"execute in minecraft:overworld run setblock "
                               f"{x} {y} {z} minecraft:air")
                client.command(
                    f"execute in minecraft:overworld positioned {x} {y+3} {z} "
                    f"run kill @e[type=minecraft:item,distance=..8]")
                client.command("execute in minecraft:overworld run forceload remove 16 16")
        finally:
            client.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--expected-seed", type=int, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    run(args.expected_seed, args.output)
