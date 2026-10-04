import hmac
import json
import os
import platform
import secrets
import socket
import subprocess
import shutil
import threading
import time

HOST = os.environ.get("MAYRA_AGENT_HOST", "127.0.0.1")
PORT = int(os.environ.get("MAYRA_AGENT_PORT", "8765"))
ALLOW_LAN = os.environ.get("MAYRA_AGENT_ALLOW_LAN", "0") == "1"
MAX_REQUEST_BYTES = 4096
SOCKET_TIMEOUT_SECONDS = 8
PAIRING_TTL_SECONDS = 300
QUICK_LINK_TTL_SECONDS = 120

# Persistent Windows-side login/session state.
# The session remains valid until explicit REVOKE/logout. Power-off/restart
# does not clear it. Override the path in tests/deployments with MAYRA_STATE_FILE.
STATE_FILE = os.environ.get(
    "MAYRA_STATE_FILE",
    os.path.join(os.environ.get("LOCALAPPDATA", os.path.expanduser("~")), "Mayra", "windows_session.json"),
)

# Remote control is capability-scoped. Never add shell/exec here.
ALLOWED = {
    "PING", "OPEN_NOTEPAD", "OPEN_CALCULATOR", "OPEN_WINDOWS_SETTINGS",
    "OPEN_NETWORK_SETTINGS", "OPEN_DISPLAY_SETTINGS", "OPEN_SOUND_SETTINGS",
    "GET_PC_STATUS", "GET_SECURITY_STATUS", "LIST_SHARED_FILES",
    "OPEN_SHARED_FILE", "SEND_FILE_TO_PC", "RECEIVE_FILE_FROM_PC",
    "READ_CLIPBOARD", "WRITE_CLIPBOARD", "OPEN_BROWSER", "BROWSER_AUTOMATION",
    "MEDIA_PLAY_PAUSE", "MEDIA_NEXT", "MEDIA_PREVIOUS", "SET_VOLUME",
    "SCREEN_VIEW", "SCREEN_CONTROL", "PHONE_CAMERA_FRONT", "PHONE_CAMERA_BACK",
    "PHONE_MICROPHONE", "PHONE_SPEAKER", "PHONE_RECOVERY_STATUS",
    "BRIDGE_CAPABILITIES", "BRIDGE_START_SCREEN", "BRIDGE_START_FILE_SHARE",
    "REVOKE_SESSION", "REGISTER_PHONE", "PHONE_COMMAND",
}

_state_lock = threading.Lock()
_pairing_code = None
_pairing_expires = 0.0
_session_token = None
_owner_approved_code = None
_phone_endpoint = None
_quick_owner_id = None
_quick_code = None
_quick_expires = 0.0


def _state_payload():
    return {
        "session_token": _session_token,
        "phone_endpoint": list(_phone_endpoint) if _phone_endpoint else None,
    }


def _persist_state_locked():
    if _session_token is None:
        try:
            os.remove(STATE_FILE)
        except FileNotFoundError:
            pass
        except OSError:
            pass
        return
    try:
        parent = os.path.dirname(STATE_FILE)
        if parent:
            os.makedirs(parent, exist_ok=True)
        tmp = STATE_FILE + ".tmp"
        with open(tmp, "w", encoding="utf-8") as handle:
            json.dump(_state_payload(), handle)
        os.replace(tmp, STATE_FILE)
    except OSError:
        # Runtime operation remains usable even if persistence is unavailable.
        pass


def load_persistent_state():
    global _session_token, _phone_endpoint
    try:
        with open(STATE_FILE, "r", encoding="utf-8") as handle:
            data = json.load(handle)
        token = data.get("session_token")
        endpoint = data.get("phone_endpoint")
        if isinstance(token, str) and token:
            _session_token = token
        if (
            isinstance(endpoint, list)
            and len(endpoint) == 2
            and isinstance(endpoint[0], str)
            and isinstance(endpoint[1], int)
            and 1 <= endpoint[1] <= 65535
        ):
            _phone_endpoint = (endpoint[0], endpoint[1])
    except (OSError, ValueError, TypeError):
        _session_token = None
        _phone_endpoint = None


def _new_pairing_code():
    return f"{secrets.randbelow(1_000_000):06d}"


def _new_quick_code():
    return f"{secrets.randbelow(100_000_000):08d}"


def start_pairing():
    global _pairing_code, _pairing_expires, _owner_approved_code
    global _quick_owner_id, _quick_code, _quick_expires
    with _state_lock:
        _pairing_code = _new_pairing_code()
        _pairing_expires = time.time() + PAIRING_TTL_SECONDS
        _owner_approved_code = None
        _quick_owner_id = None
        _quick_code = _new_quick_code()
        _quick_expires = time.time() + QUICK_LINK_TTL_SECONDS
        return _pairing_code


def pairing_code():
    with _state_lock:
        if _pairing_code is None or time.time() >= _pairing_expires:
            return None
        return _pairing_code


def quick_pair_code():
    with _state_lock:
        if _quick_code is None or time.time() >= _quick_expires:
            return None
        return _quick_code


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
    global _session_token, _pairing_code, _owner_approved_code
    with _state_lock:
        if _pairing_code is None or time.time() >= _pairing_expires:
            return None
        if _owner_approved_code != _pairing_code:
            return None
        if not hmac.compare_digest(str(code), _pairing_code):
            return None
        _session_token = secrets.token_urlsafe(32)
        _pairing_code = None
        _owner_approved_code = None
        _persist_state_locked()
        return _session_token


def quick_pair(owner_id, code):
    global _session_token, _quick_code, _quick_expires, _quick_owner_id
    if not isinstance(owner_id, str) or not owner_id.strip():
        return None
    with _state_lock:
        if _quick_code is None or time.time() >= _quick_expires:
            return None
        if not hmac.compare_digest(str(code), _quick_code):
            return None
        _session_token = secrets.token_urlsafe(32)
        _quick_owner_id = owner_id.strip()
        _quick_code = None
        _quick_expires = 0.0
        _persist_state_locked()
        return _session_token


def register_phone(host, port, token=None):
    global _phone_endpoint
    if not authenticated(token):
        return False
    if not isinstance(host, str) or not host.strip() or not isinstance(port, int) or not (1 <= port <= 65535):
        return False
    with _state_lock:
        _phone_endpoint = (host.strip(), port)
        _persist_state_locked()
        return True


def phone_endpoint():
    with _state_lock:
        return _phone_endpoint


def _notify_phone_logout(endpoint, token):
    if endpoint is None or not token:
        return
    host, port = endpoint
    try:
        with socket.create_connection((host, port), timeout=3) as phone:
            payload = {"action": "PHONE_LOGOUT", "session_token": token}
            phone.sendall((json.dumps(payload) + "\n").encode("utf-8"))
            phone.recv(1024)
    except (OSError, ValueError):
        pass


def revoke_session():
    global _session_token, _owner_approved_code, _phone_endpoint
    with _state_lock:
        _session_token = None
        _owner_approved_code = None
        _phone_endpoint = None
        _persist_state_locked()


def authenticated(token):
    with _state_lock:
        if not token or _session_token is None:
            return False
        return hmac.compare_digest(str(token), _session_token)


def security_status():
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


def bridge_capabilities():
    """
    Discover optional bridge components without making them mandatory.
    Mayra keeps its own transport and falls back to standalone mode when a
    third-party bridge is unavailable. Discovery is intentionally read-only.
    """
    return {
        "ok": True,
        "components": {
            "scrcpy": bool(shutil.which("scrcpy") or shutil.which("scrcpy.exe")),
            "localsend": bool(shutil.which("localsend") or shutil.which("localsend.exe")),
            "adb": bool(shutil.which("adb") or shutil.which("adb.exe")),
        },
        "design": {
            "screen_control": "scrcpy-compatible",
            "file_transfer": "LocalSend-compatible",
            "device_transport": "Mayra-authenticated",
        },
    }


def start_optional_bridge(component):
    """Start an installed optional bridge without allowing arbitrary process execution."""
    if platform.system() != "Windows":
        return {"ok": False, "error": "This action is Windows-only"}
    executables = {
        "scrcpy": shutil.which("scrcpy") or shutil.which("scrcpy.exe"),
        "localsend": shutil.which("localsend") or shutil.which("localsend.exe"),
    }
    executable = executables.get(component)
    if not executable:
        return {"ok": False, "error": f"{component} is not installed or not on PATH"}
    try:
        subprocess.Popen([executable])
        return {"ok": True, "message": f"{component} bridge started"}
    except OSError:
        return {"ok": False, "error": f"{component} bridge could not be started"}


def pc_status():
    return {
        "ok": True,
        "platform": platform.system(),
        "release": platform.release(),
        "version": platform.version(),
        "hostname": socket.gethostname(),
        "mayra_session_persistent": True,
    }


def execute(command: str):
    if not isinstance(command, str):
        return {"ok": False, "error": "Command not allowed"}
    allowed = command in ALLOWED or command.startswith("OPEN_BROWSER:")
    if not allowed:
        return {"ok": False, "error": "Command not allowed"}
    if command == "PING":
        return {"ok": True, "message": "Mayra Windows Agent is online"}
    if command == "GET_PC_STATUS":
        return pc_status()
    if command == "GET_SECURITY_STATUS":
        return security_status()
    if command == "BRIDGE_CAPABILITIES":
        return bridge_capabilities()
    if command == "BRIDGE_START_SCREEN":
        return start_optional_bridge("scrcpy")
    if command == "BRIDGE_START_FILE_SHARE":
        return start_optional_bridge("localsend")
    if command == "REVOKE_SESSION":
        revoke_session()
        return {"ok": True, "message": "Session revoked"}
    if command == "PHONE_RECOVERY_STATUS":
        return {"ok": True, "message": "Recovery status must be supplied by the authenticated Android recovery module"}
    if command == "OPEN_BROWSER":
        if platform.system() != "Windows":
            return {"ok": False, "error": "This action is Windows-only"}
        try:
            os.startfile("https://www.google.com")
            return {"ok": True, "message": "Default browser opened"}
        except OSError:
            return {"ok": False, "error": "Default browser could not be opened"}
    if command.startswith("OPEN_BROWSER:"):
        if platform.system() != "Windows":
            return {"ok": False, "error": "This action is Windows-only"}
        target = command.split(":", 1)[1].strip()
        if not (target.startswith("https://") or target.startswith("http://")):
            return {"ok": False, "error": "Browser URL must use http:// or https://"}
        try:
            os.startfile(target)
            return {"ok": True, "message": "Browser URL opened"}
        except OSError:
            return {"ok": False, "error": "Browser URL could not be opened"}
    if command in {"MEDIA_PLAY_PAUSE", "MEDIA_NEXT", "MEDIA_PREVIOUS"}:
        if platform.system() != "Windows":
            return {"ok": False, "error": "This action is Windows-only"}
        try:
            import ctypes
            media_vk = {
                "MEDIA_PLAY_PAUSE": 0xB3,
                "MEDIA_NEXT": 0xB0,
                "MEDIA_PREVIOUS": 0xB1,
            }[command]
            ctypes.windll.user32.keybd_event(media_vk, 0, 0, 0)
            ctypes.windll.user32.keybd_event(media_vk, 0, 2, 0)
            return {"ok": True, "message": "Media command sent"}
        except (AttributeError, OSError):
            return {"ok": False, "error": "Windows media control unavailable"}
    if command in {
        "LIST_SHARED_FILES", "OPEN_SHARED_FILE", "SEND_FILE_TO_PC",
        "RECEIVE_FILE_FROM_PC", "READ_CLIPBOARD", "WRITE_CLIPBOARD",
        "BROWSER_AUTOMATION", "SET_VOLUME", "SCREEN_VIEW",
        "SCREEN_CONTROL", "PHONE_CAMERA_FRONT", "PHONE_CAMERA_BACK",
        "PHONE_MICROPHONE", "PHONE_SPEAKER",
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
    conn.settimeout(SOCKET_TIMEOUT_SECONDS)
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
                if action == "QUICK_PAIR":
                    token = quick_pair(request.get("owner_id", ""), request.get("code", ""))
                    response = {"ok": bool(token), "session_token": token, "link": "quick-owner"}
                    if not token:
                        response["error"] = "Quick Owner Link invalid or expired"
                elif action == "PAIR_REQUEST":
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
                    token = request.get("session_token")
                    if authenticated(token):
                        endpoint = phone_endpoint()
                        _notify_phone_logout(endpoint, token)
                        revoke_session()
                        response = {"ok": True, "message": "Session revoked on Windows and Android when reachable"}
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
                        ok = register_phone(host, port, request.get("session_token"))
                        response = {"ok": ok, "message": "Android endpoint registered"}
                        if not ok:
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
                                with socket.create_connection((host, port), timeout=SOCKET_TIMEOUT_SECONDS) as phone:
                                    payload = {
                                        "action": "PHONE_COMMAND",
                                        "session_token": request.get("session_token"),
                                        "command": request.get("command", ""),
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
                        "quick_link_available": quick_pair_code() is not None,
                        "platform": platform.system(),
                        "persistent_login": True,
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
    if HOST not in {"127.0.0.1", "::1", "localhost"} and not ALLOW_LAN:
        raise SystemExit("LAN bind is disabled by default. Set MAYRA_AGENT_ALLOW_LAN=1 only on a trusted network.")
    load_persistent_state()
    code = start_pairing()
    print("\nMayra Windows Agent — persistent owner-approved LAN login")
    print(f"Listening on {HOST}:{PORT}")
    print("LAN mode is disabled (loopback-only)." if HOST in {"127.0.0.1", "::1", "localhost"} else "LAN mode enabled by explicit opt-in; transport is token-authenticated but not encrypted.")
    print(f"PAIRING CODE: {code} (expires in {PAIRING_TTL_SECONDS}s)")
    print(f"QUICK OWNER LINK CODE: {quick_pair_code()} (expires in {QUICK_LINK_TTL_SECONDS}s)")
    print("Existing login is preserved across Windows restart/power-off until explicit logout/revoke.")
    print("Remote access remains capability-scoped; no arbitrary shell execution.")

    def owner_console():
        while True:
            try:
                command = input().strip()
            except (EOFError, KeyboardInterrupt):
                return
            if command.startswith("PAIR ") and owner_approve(command[5:].strip()):
                print("Owner approval recorded. The paired device may now complete pairing.")
            elif command == "REVOKE":
                token = _session_token
                endpoint = phone_endpoint()
                _notify_phone_logout(endpoint, token)
                revoke_session()
                print("Session revoked; both devices must pair again.")
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
                                "command": command[6:].strip(),
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
    load_persistent_state()
    main()
