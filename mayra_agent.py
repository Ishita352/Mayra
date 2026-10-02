import json
import os
import platform
import secrets
import socket
import subprocess
from datetime import datetime
from pathlib import Path

HOST = "127.0.0.1"
PORT = 8765
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
        except Exception:
            pass

    print("\n=== Mayra Windows Setup ===")
    print("প্রথম ইনস্টলেশনে একবার Setup Code দিতে হবে।")
    print("আজকের কোড: MAYRA-DDMMYYYY")
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
    if command not in ALLOWED:
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


def main():
    if not setup_if_needed():
        return
    print("\nMayra Windows Agent — MVP")
    print(f"Listening locally on {HOST}:{PORT}")
    print("Demo commands only: PING, OPEN_NOTEPAD, OPEN_CALCULATOR")
    print("Remote/network pairing is intentionally not enabled yet.\n")

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        s.bind((HOST, PORT))
        s.listen(5)
        while True:
            conn, _ = s.accept()
            with conn:
                try:
                    data = conn.recv(4096).decode("utf-8").strip()
                    if not data:
                        continue
                    request = json.loads(data)
                    result = execute(request.get("command", ""))
                    conn.sendall((json.dumps(result) + "\n").encode("utf-8"))
                except Exception as e:
                    conn.sendall((json.dumps({"ok": False, "error": str(e)}) + "\n").encode("utf-8"))


if __name__ == "__main__":
    main()
