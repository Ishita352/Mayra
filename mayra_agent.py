import json
import os
import platform
import secrets
import socket
import subprocess
from datetime import datetime
from pathlib import Path

# Security boundary: keep this loopback-only until authenticated TLS pairing is implemented.
HOST = "127.0.0.1"
PORT = 8765
MAX_REQUEST_BYTES = 4096
ALLOWED = {"PING", "OPEN_NOTEPAD", "OPEN_CALCULATOR"}
DATA_DIR = Path(os.environ.get("APPDATA", Path.home())) / "Mayra"
STATE_FILE = DATA_DIR / "device.json"


def today_code():
    return "MAYRA-" + datetime.now().strftime("%d%m%Y")


def setup_if_needed():
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    if STATE_FILE.exists():
        try:
            state = json.loads(STATE_FILE.read_text(encoding="utf-8"))
            if state.get("installed") and state.get("device_token"):
                return True
        except (OSError, json.JSONDecodeError):
            pass

    print("\n=== Mayra Windows Setup ===")
    print("প্রথম ইনস্টলেশনে একবার Setup Code দিতে হবে।")
    print("আজকের Setup Code:", today_code())
    code = input("Setup Code: ").strip().upper()
    if code != today_code():
        print("ভুল Setup Code।")
        return False

    state = {
        "installed": True,
        "installed_at": datetime.now().isoformat(timespec="seconds"),
        "device_token": secrets.token_urlsafe(32),
    }
    STATE_FILE.write_text(json.dumps(state, indent=2), encoding="utf-8")
    print("Setup সফল। এই কম্পিউটারে পরে আর Setup Code লাগবে না।")
    return True


def execute(command: str):
    if not isinstance(command, str) or command not in ALLOWED:
        return {"ok": False, "error": "Command not allowed"}
    if command == "PING":
        return {"ok": True, "message": "Mayra Windows Agent is online"}
    if platform.system() != "Windows":
        return {"ok": False, "error": "This action is Windows-only"}
    if command == "OPEN_NOTEPAD":
        subprocess.Popen(["notepad.exe"])
        return {"ok": True, "message": "Notepad opened"}
    if command == "OPEN_CALCULATOR":
        subprocess.Popen(["calc.exe"])
        return {"ok": True, "message": "Calculator opened"}
    return {"ok": False, "error": "Command not allowed"}


def handle_connection(conn):
    conn.settimeout(3)
    try:
        data = conn.recv(MAX_REQUEST_BYTES + 1)
        if not data:
            return
        if len(data) > MAX_REQUEST_BYTES:
            response = {"ok": False, "error": "Request too large"}
        else:
            try:
                request = json.loads(data.decode("utf-8"))
                if not isinstance(request, dict):
                    raise ValueError("JSON request must be an object")
                response = execute(request.get("command", ""))
            except (UnicodeDecodeError, json.JSONDecodeError, ValueError):
                response = {"ok": False, "error": "Invalid request"}
        conn.sendall((json.dumps(response) + "\n").encode("utf-8"))
    except (OSError, socket.timeout):
        # Do not return internal exception details to the client.
        try:
            conn.sendall(b'{"ok":false,"error":"Connection error"}\n')
        except OSError:
            pass


def main():
    if not setup_if_needed():
        return
    print("\nMayra Windows Agent — local-only MVP")
    print(f"Listening locally on {HOST}:{PORT}")
    print("Demo commands only: PING, OPEN_NOTEPAD, OPEN_CALCULATOR")
    print("Phone/network pairing is not enabled; do not expose this server to the network.\n")

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((HOST, PORT))
        server.listen(5)
        while True:
            conn, _ = server.accept()
            with conn:
                handle_connection(conn)


if __name__ == "__main__":
    main()
