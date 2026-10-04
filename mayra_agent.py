import hashlib
import hmac
import json
import os
import platform
import secrets
import socket
import subprocess
import threading
import time

HOST = os.environ.get("MAYRA_AGENT_HOST", "0.0.0.0")
PORT = int(os.environ.get("MAYRA_AGENT_PORT", "8765"))
MAX_REQUEST_BYTES = 4096
PAIRING_TTL_SECONDS = 300
SESSION_TTL_SECONDS = 3600

# Remote control is capability-scoped. Never add shell/exec here.
ALLOWED = {
    "PING",
    "OPEN_NOTEPAD",
    "OPEN_CALCULATOR",
    "OPEN_WINDOWS_SETTINGS",
    "OPEN_NETWORK_SETTINGS",
    "OPEN_DISPLAY_SETTINGS",
    "OPEN_SOUND_SETTINGS",
    "GET_PC_STATUS",
    "GET_SECURITY_STATUS",
    "LIST_SHARED_FILES",
    "OPEN_SHARED_FILE",
    "SEND_FILE_TO_PC",
    "RECEIVE_FILE_FROM_PC",
    "READ_CLIPBOARD",
    "WRITE_CLIPBOARD",
    "OPEN_BROWSER",
    "BROWSER_AUTOMATION",
    "MEDIA_PLAY_PAUSE",
    "MEDIA_NEXT",
    "MEDIA_PREVIOUS",
    "SET_VOLUME",
    "SCREEN_VIEW",
    "SCREEN_CONTROL",
    "PHONE_CAMERA_FRONT",
    "PHONE_CAMERA_BACK",
    "PHONE_MICROPHONE",
    "PHONE_SPEAKER",
    "PHONE_RECOVERY_STATUS",
    "REVOKE_SESSION",
    "REGISTER_PHONE",
    "PHONE_COMMAND",
}

_state_lock = threading.Lock()
_pairing_code = None
_pairing_expires = 0.0
_session_token = None
_session_expires = 0.0
_owner_approved_code = None
_phone_endpoint = None\n_quick_owner_id = None\n_quick_code = None\n_quick_expires = 0.0


def _new_pairing_code():
    return f"{secrets.randbelow(1_000_000):06d}"


def start_pairing():
    global _pairing_code, _pairing_expires, _session_token, _session_expires, _owner_approved_code
    with _state_lock:
        _pairing_code = _new_pairing_code()
        _pairing_expires = time.time() + PAIRING_TTL_SECONDS
        _session_token = None
        _session_expires = 0.0
        _owner_approved_code = None
        global _phone_endpoint
        _phone_endpoint = None
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


def register_phone(host, port, token=None):
    global _phone_endpoint
    if not authenticated(token):
        return False
    if not isinstance(host, str) or not host.strip() or not isinstance(port, int) or not (1 <= port <= 65535):
        return False
    with _state_lock:
        _phone_endpoint = (host.strip(), port)
        return True


def phone_endpoint():
    with _state_lock:
        return _phone_endpoint


def revoke_session():
    global _session_token, _session_expires, _owner_approved_code, _phone_endpoint
    with _state_lock:
        _session_token = None
        _session_expires = 0.0
        _owner_approved_code = None
        _phone_endpoint = None


def authenticated(token):
    with _state_lock:
        if not token or _session_token is None or time.time() >= _session_expires:
            return False
        return hmac.compare_digest(str(token), _session_token)


def security_status():
    """Read-only Windows security-provider status; never changes security controls."""
    if platform.system() != "Windows":
        return {"ok": False, "error": "Windows-only security status"}
    script = r"""
$mp = Get-MpComputerStatus
$fw = (Get-NetFirewallProfile | Where-Object {$_.Enabled -eq $true}).Count -gt 0
[ordered]@{
  provider = "Microsoft Defender"
  antivirus_enabled = [bool]$mp.AntivirusEnabled
  realtime_protection_enabled = [bool]$mp.RealTimeProtectionEnabled
  definitions_up_to_date = [bool]($mp.AntivirusSignatureLastUpdated -gt (Get-Date).AddDays(-7))
  firewall_enabled = [bool]$fw
} | ConvertTo-Json -Compress
"""
    try:
        result = subprocess.run(
            ["powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script],
            capture_output=True, text=True, timeout=15, check=False,
        )
        if result.returncode != 0 or not result.stdout.strip():
            return {"ok": False, "error": "Windows security provider status unavailable"}
        return {"ok": True, "provider": "Microsoft Defender", "status": json.loads(result.stdout)}
    except (OSError, subprocess.SubprocessError, json.JSONDecodeError):
        return {"ok": False, "error": "Windows security provider status unavailable"}


def pc_status():
    return {
        "ok": True,
        "platform": platform.system(),
        "release": platform.release(),
        "version": platform.version(),
        "hostname": socket.gethostname(),
    }


def execute(command: str):
    if not isinstance(command, str) or command not in ALLOWED:
        return {"ok": False, "error": "Command not allowed"}
    if command == "PING":
        return {"ok": True, "message": "Mayra Windows Agent is online"}
    if command == "GET_PC_STATUS":
        return pc_status()
    if command == "GET_SECURITY_STATUS":
        return security_status()
    if command == "REVOKE_SESSION":
        revoke_session()
        return {"ok": True, "message": "Session revoked"}
    if command == "PHONE_RECOVERY_STATUS":
        return {"ok": True, "message": "Recovery status must be supplied by the authenticated Android recovery module"}
    if command in {
        "LIST_SHARED_FILES", "OPEN_SHARED_FILE", "SEND_FILE_TO_PC",
        "RECEIVE_FILE_FROM_PC", "READ_CLIPBOARD", "WRITE_CLIPBOARD",
        "OPEN_BROWSER", "BROWSER_AUTOMATION", "MEDIA_PLAY_PAUSE",
        "MEDIA_NEXT", "MEDIA_PREVIOUS", "SET_VOLUME", "SCREEN_VIEW",
        "SCREEN_CONTROL", "PHONE_CAMERA_FRONT", "PHONE_CAMERA_BACK",
        "PHONE_MICROPHONE", "PHONE_SPEAKER"
    }:
        return {"ok": False, "error": "Capability is published but its transport/module is not enabled yet"}
    if platform.system() != "Windows":
        return {"ok": False, "error": "This action is Windows-only"}
    settings_uris = {
        "OPEN_WINDOWS_SETTINGS": "ms-settings:",
        "OPEN_NETWORK_SETTINGS": "ms-settings:network-status",
        "OPEN_DISPLAY_SETTINGS": "ms-settings:display",
        "OPEN_SOUND_SETTINGS": "ms-settings:sound",
    }
    if command in settings_uris:
        os.startfile(settings_uris[command])
        return {"ok": True, "message": "Windows Settings opened"}
    if command == "OPEN_NOTEPAD":
        subprocess.Popen(["notepad.exe"])
        return {"ok": True, "message": "Notepad opened"}
    if command == "OPEN_CALCULATOR":
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
                if action == "QUICK_PAIR":\n                    token = quick_pair(request.get("owner_id", ""), request.get("code", ""))\n                    response = {"ok": bool(token), "session_token": token, "link": "quick-owner"}\n                    if not token: response["error"] = "Quick Owner Link invalid or expired"\n                elif action == "PAIR_REQUEST":
                    code = str(request.get("code", ""))
                    response = {"ok": False, "error": "Pairing rejected"}
                    if pairing_code() == code:
                        print(f"Pairing request received for code {code}. Approve locally with: PAIR {code}")
                        response = {"ok": True, "status": "OWNER_APPROVAL_REQUIRED"}
                elif action == "PAIR_APPROVE":
                    token = approve_pairing(request.get("code", ""))
                    response = {"ok": bool(token), "session_token": token}
                    if not token:
                        response["error"] = "Pairing code invalid, expired, or not owner-approved"
                elif action == "REVOKE":
                    if authenticated(request.get("session_token")):
                        revoke_session()
                        response = {"ok": True, "message": "Session revoked"}
                    else:
                        response = {"ok": False, "error": "Authentication required"}
                elif action == "REGISTER_PHONE":
                    if not authenticated(request.get("session_token")):
                        response = {"ok": False, "error": "Authentication required"}
                    else:
                        host = request.get("host", "")
                        try:
                            port = int(request.get("port", 8766))
                        except (TypeError, ValueError):
                            port = 0
                        response = {"ok": register_phone(host, port), "message": "Android endpoint registered"}
                        if not response["ok"]:
                            response["error"] = "Invalid Android endpoint"
                elif action == "PHONE_COMMAND":
                    if not authenticated(request.get("session_token")):
                        response = {"ok": False, "error": "Authentication required"}
                    else:
                        endpoint = phone_endpoint()
                        if endpoint is None:
                            response = {"ok": False, "error": "Android endpoint not registered"}
                        else:
                            host, port = endpoint
                            try:
                                with socket.create_connection((host, port), timeout=5) as phone:
                                    payload = {
                                        "action": "PHONE_COMMAND",
                                        "session_token": request.get("session_token"),
                                        "command": request.get("command", "")
                                    }
                                    phone.sendall((json.dumps(payload) + "\n").encode("utf-8"))
                                    line = phone.recv(MAX_REQUEST_BYTES + 1).decode("utf-8").strip()
                                    response = json.loads(line)
                            except (OSError, ValueError, json.JSONDecodeError):
                                response = {"ok": False, "error": "Android endpoint unavailable"}
                elif action == "COMMAND":
                    if not authenticated(request.get("session_token")):
                        response = {"ok": False, "error": "Authentication required"}
                    else:
                        response = execute(request.get("command", ""))
                elif action == "SECURITY_STATUS":
                    if not authenticated(request.get("session_token")):
                        response = {"ok": False, "error": "Authentication required"}
                    else:
                        response = security_status()
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
    print(f"PAIRING CODE: {code} (expires in {PAIRING_TTL_SECONDS}s)")\n    print(f"QUICK OWNER LINK CODE: {quick_pair_code()} (expires in {PAIRING_TTL_SECONDS}s)")
    print("Remote access is disabled until the PC owner approves this exact code.")
    print("Allowed remote commands:", ", ".join(sorted(ALLOWED)))
    print("SECURITY_STATUS is read-only and requires an authenticated session.")
    print("To revoke the current session, use the REVOKE action.\n")

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
            elif command.startswith("PHONE "):
                endpoint = phone_endpoint()
                if endpoint is None or not authenticated(_session_token):
                    print("Android endpoint is not registered.")
                else:
                    host, port = endpoint
                    try:
                        with socket.create_connection((host, port), timeout=5) as phone:
                            phone.sendall((json.dumps({
                                "action": "PHONE_COMMAND",
                                "session_token": _session_token,
                                "command": command[6:].strip()
                            }) + "\n").encode("utf-8"))
                            print(phone.recv(MAX_REQUEST_BYTES + 1).decode("utf-8").strip())
                    except OSError as exc:
                        print("Android connection failed:", exc)

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
