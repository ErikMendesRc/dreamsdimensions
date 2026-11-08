#!/usr/bin/env python3
"""CI-only RCON smoke test: force chunk loading in each Dreams Dimensions dimension.

Runs on an ephemeral CI server with loopback-only access. Requires only Python stdlib.
"""
import socket
import struct
import time

HOST = "127.0.0.1"
PORT = 25575
PASSWORD = "dd-ci-local-only"
DIMENSIONS = ("dreamsdimensions:dreamscape", "dreamsdimensions:campo_onirico_azul")


def send(sock, request_id, packet_type, payload):
    body = struct.pack("<ii", request_id, packet_type) + payload.encode("utf-8") + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(body)) + body)


def receive(sock):
    def read_exact(count):
        chunks = bytearray()
        while len(chunks) < count:
            block = sock.recv(count - len(chunks))
            if not block:
                raise ConnectionError("Conexão RCON encerrada")
            chunks.extend(block)
        return bytes(chunks)

    size = struct.unpack("<i", read_exact(4))[0]
    if size < 10 or size > 4 * 1024 * 1024:
        raise ValueError(f"Tamanho de pacote RCON inválido: {size}")
    data = read_exact(size)
    request_id, packet_type = struct.unpack("<ii", data[:8])
    return request_id, packet_type, data[8:-2].decode("utf-8", "replace")


with socket.create_connection((HOST, PORT), timeout=15) as conn:
    conn.settimeout(45)
    send(conn, 11, 3, PASSWORD)
    for _ in range(4):
        request_id, packet_type, _ = receive(conn)
        if packet_type == 2:
            if request_id != 11:
                raise RuntimeError("Autenticação RCON recusada")
            break
    else:
        raise RuntimeError("Não foi recebido o pacote de autenticação RCON")

    blue = "dreamsdimensions:campo_onirico_azul"
    points = [(0, 0), (256, 256), (-320, 192), (640, -384), (-640, -576)]
    locations = [("dreamsdimensions:dreamscape", 0, 0)] + [(blue, x, z) for x, z in points]

    def checked_command(request_id, command, forbidden):
        send(conn, request_id, 2, command)
        answer_id, _, response = receive(conn)
        print(f"{command}: {response}", flush=True)
        if "positioned over" in command and not response:
            prefix = command.split(" positioned over ")[0] + " positioned over motion_blocking_no_leaves "
            for y in (48, 64, 80, 96, 112, 128, 160):
                for block_id in ("minecraft:air", "dreamsdimensions:blue_dream_stone",
                                 "dreamsdimensions:blue_dream_grass"):
                    cmd = f"execute in {blue} if block 0 {y} 0 {block_id} run forceload query 0 0"
                    send(conn, 8000 + y + len(block_id), 2, cmd)
                    _, _, observed = receive(conn)
                    if observed:
                        print(f"ABSOLUTE {cmd}: {observed}", flush=True)
            for y_offset in (-3, -2, -1, 0, 1):
                for block_id in (
                    "minecraft:air", "minecraft:water", "minecraft:bedrock",
                    "dreamsdimensions:blue_dream_grass",
                    "dreamsdimensions:blue_dream_dirt",
                    "dreamsdimensions:blue_dream_stone",
                ):
                    probe = prefix + f"if block ~ ~{y_offset} ~ {block_id} run forceload query 0 0"
                    diag_id = 1000 + (y_offset + 3) * 10 + len(block_id)
                    send(conn, diag_id, 2, probe)
                    reply_id, _, evidence = receive(conn)
                    if evidence:
                        print(f"DIAGNOSTIC {probe}: {evidence}", flush=True)
        if answer_id != request_id or not response or any(
            word in response.lower() for word in forbidden
        ):
            raise RuntimeError(f"Falha de worldgen: {command}: {response}")
        return response

    for index, (dimension, x, z) in enumerate(locations):
        checked_command(100 + index, f"execute in {dimension} run forceload add {x} {z}",
                        ("unknown", "incorrect", "error", "not found", "failed"))
    time.sleep(12)
    for index, (dimension, x, z) in enumerate(locations):
        checked_command(200 + index, f"execute in {dimension} run forceload query {x} {z}",
                        ("not", "unknown", "failed", "error"))
    # Flora can legitimately occupy an arbitrary sample column. Inspect a
    # selection of neighboring natural columns rather than demanding that the
    # one exact coordinate be vegetation-free.
    for index, (x, z) in enumerate(points):
        base = (
            f"execute in {blue} positioned {x} 0 {z} "
            "positioned over motion_blocking_no_leaves "
        )
        checked_command(300 + index, base + f"run forceload query {x} {z}",
                        ("failed", "unknown", "incorrect", "error"))
        offsets = [(0, 0), (2, 2), (4, 4), (6, 6), (8, 8),
                   (10, 10), (12, 12), (14, 14), (3, 11), (11, 3)]
        found = False
        for sample, (dx, dz) in enumerate(offsets):
            query = (
                f"execute in {blue} positioned {x + dx} 0 {z + dz} "
                "positioned over motion_blocking_no_leaves "
                "if block ~ ~-1 ~ #dreamsdimensions:blue_natural_ground "
                "if block ~ ~ ~ minecraft:air "
                "if block ~ ~1 ~ minecraft:air "
                f"run forceload query {x} {z}"
            )
            request = 400 + index * 20 + sample
            send(conn, request, 2, query)
            reply_id, _, response = receive(conn)
            if reply_id != request:
                raise RuntimeError(f"Resposta RCON inesperada: {query}: {response}")
            if "marked for force loading" in response.lower():
                print(f"Safe arrival nearby {x} {z}: +{dx},+{dz}", flush=True)
                found = True
                break
        if not found:
            raise RuntimeError(f"Sem piso natural com 2 blocos de ar perto de X={x} Z={z}")

print("Chunks e heightmaps azuis validados em cinco coordenadas.", flush=True)
