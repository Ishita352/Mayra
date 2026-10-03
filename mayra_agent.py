import hashlib
import hmac
import json
import os
import platform
import secrets
import socket
import threading
import time

HOST = os.environ.get("MAYRA_AGENT_HOST", "0.0.0.0")
PORT = int(os.environ.get("MAYRA_AGENT_PORT", "8765"))
MAX_REQUEST_BYTES = 4096
PAIRING_TTL_SECONDS = 300
SESSION_TTL_SECONDS = 3600

# Remote control is deliberately capability-scoped. Never add shell/exec here.
ALLOWED = {"PING", "OPEN_NOTEPAD", "OPEN_CALCULATOR"}

_state_lock = threading.Lock()
_pairing_code = None
_pairing_expires = 0.0
_session_token = None
_session_expires = 0.0
_owner_approved_code = None
_owner_approved_code = None


def _new_pairing_code():
    return f"{secrets.randbelow(1_000_000):06d}"


def start_pairing():
    global _pairing_code, _pairing_expires, _session_token, _owner_approved_code
    with _state_lock:
        _pairing_code = _new_pairing_code()
        _pairing_expires = time.time() + PAIRING_TTL_SECONDS
        _session_token = None
        _session_expires = 0.0
        _owner_approved_code = None
        _owner_approved_code = None
        return _pairing_code


def pairing_code():
    with _state_lock:
        if _pairing_code is None or time.time() >= _pairing_expires:
            return None
        return _pairing_code


def owner_approve(code):
    global _owner_approved_code
    with _state_lock:
        if _pairing_code is None or time.time() >= _pairing_expires:
            return False
        if not hmac.compare_digest(str(code), _pairing_code):
            return False
        _owner_approved_code = _pairing_code
        return True


def owner_approve(code):
    global _owner_approved_code
    with _state_lock:
        if _pairing_code is None or time.time() >= _pairing_expires:
            return False
        if not hmac.compare_digest(str(code), _pairing_code):
            return False
        _owner_approved_code = _pairing_code
        return True


def approve_pairing(code):
    global _session_token, _session_expires, _pairing_code
    with _state_lock:
        if _pairing_code is None or time.time() >= _pairing_expires:
            return None
        if _owner_approved_code != _pairing_code:
            return None
        if not hmac.compare_digest(str(code), _pairing_code):
            return None
        _session_token = secrets.token_urlsafe(32)
        _session_expires = time.time() + SESSION_TTL_SECONDS
        _pairing_code = None
        return _session_token


def revoke_session():
    global _session_token, _session_expires, _owner_approved_code
    with _state_lock:
        _session_token = None
        _session_expires = 0.0


def authenticated(token):
    with _state_lock:
        if not token or _session_token is None or time.time() >= _session_expires:
            return False
        return hmac.compare_digest(str(token), _session_token)


def execute(command: str):
    if not isinstance(command, str) or command not in ALLOWED:
        return {"ok": False, "error": "Command not allowed"}
    if command == "PING":
        return {"ok": True, "message": "Mayra Windows Agent is online"}
    if platform.system() != "Windows":
        return {"ok": False, "error": "This action is Windows-only"}
    if command == "OPEN_NOTEPAD":
        import subprocess
        subprocess.Popen(["notepad.exe"])
        return {"ok": True, "message": "Notepad opened"}
    if command == "OPEN_CALCULATOR":
        import subprocess
        subprocess.Popen(["calc.exe"])
        return {"ok": True, "message": "Calculator opened"}
    return {"ok": False, "error": "Command not allowed"}


def handle_connection(conn):
    conn.settimeout(5)
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
                action = request.get("action", "")
                if action == "PAIR_REQUEST":
                    code = str(request.get("code", ""))
                    response = {"ok": False, "error": "Pairing rejected"}
                    if pairing_code() == code:
                        print(f"Pairing request received for code {code}. Approve locally with: PAIR {code}")
                        response = {"ok": True, "status": "OWNER_APPROVAL_REQUIRED"}
                elif action == "PAIR_APPROVE":
                    token = approve_pairing(request.get("code", ""))
                    response = {"ok": bool(token), "session_token": token}
                    if not token:
                        response["error"] = "Pairing code invalid or expired"
                elif action == "REVOKE":
                    if authenticated(request.get("session_token")):
                        revoke_session()
                        response = {"ok": True, "message": "Session revoked"}
                    else:
                        response = {"ok": False, "error": "Authentication required"}
                elif action == "COMMAND":
                    if not authenticated(request.get("session_token")):
                        response = {"ok": False, "error": "Authentication required"}
                    else:
                        response = execute(request.get("command", ""))
                elif action == "STATUS":
                    response = {
                        "ok": True,
                        "paired": authenticated(request.get("session_token")),
                        "pairing_available": pairing_code() is not None,
                        "platform": platform.system(),
                    }
                else:
                    response = {"ok": False, "error": "Action not allowed"}
            except (UnicodeDecodeError, json.JSONDecodeError, ValueError):
                response = {"ok": False, "error": "Invalid request"}
        conn.sendall((json.dumps(response) + "\n").encode("utf-8"))
    except (OSError, socket.timeout):
        try:
            conn.sendall(b'{"ok":false,"error":"Connection error"}\n')
        except OSError:
            pass


def main():
    code = start_pairing()
    print("\nMayra Windows Agent — owner-approved LAN pairing")
    print(f"Listening on {HOST}:{PORT}")
    print(f"PAIRING CODE: {code} (expires in {PAIRING_TTL_SECONDS}s)")
    print("Remote access is disabled until the PC owner approves this exact code.")
    print("Allowed remote commands:", ", ".join(sorted(ALLOWED)))
    print("To revoke the current session, restart the agent or use the REVOKE action.\n")

    def owner_console():
        while True:
            try:
                command = input().strip()
            except (EOFError, KeyboardInterrupt):
                return
            if command.startswith("PAIR ") and owner_approve(command[5:].strip()):
                print("Owner approval recorded. The paired device may now complete pairing.")
            elif command == "REVOKE":
                revoke_session()
                print("Session revoked.")

    threading.Thread(target=owner_console, daemon=True).start()

    def owner_console():
        while True:
            try:
                command = input().strip()
            except (EOFError, KeyboardInterrupt):
                return
            if command.startswith("PAIR ") and owner_approve(command[5:].strip()):
                print("Owner approval recorded. The paired device may now complete pairing.")
            elif command == "REVOKE":
                revoke_session()
                print("Session revoked.")

    threading.Thread(target=owner_console, daemon=True).start()

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
