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

    for index, dimension in enumerate(DIMENSIONS):
        command = f"execute in {dimension} run forceload add 0 0"
        request_id = 100 + index
        send(conn, request_id, 2, command)
        answer_id, answer_type, response = receive(conn)
        print(f"{command}: {response}", flush=True)
        if answer_id != request_id or not response or any(
            word in response.lower() for word in ("unknown", "incorrect", "error", "not found", "failed")
        ):
            raise RuntimeError(f"Não foi possível carregar chunk em {dimension}: {response}")

    # Os dois chunks devem continuar carregados durante o processamento do servidor.
    time.sleep(8)
    for index, dimension in enumerate(DIMENSIONS):
        command = f"execute in {dimension} run forceload query 0 0"
        request_id = 200 + index
        send(conn, request_id, 2, command)
        answer_id, answer_type, response = receive(conn)
        print(f"{command}: {response}", flush=True)
        if answer_id != request_id or not response or "not" in response.lower():
            raise RuntimeError(f"Chunk não está forceloaded em {dimension}: {response}")

print("As duas dimensões aceitaram carga de chunk.", flush=True)
