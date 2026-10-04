@echo off
setlocal
cd /d "%~dp0.."

echo.
echo ============================================
echo   Mayra Windows 10 - Trusted LAN Agent
echo ============================================
echo.
echo WARNING:
echo This enables Mayra on the local trusted LAN.
echo Use only on your private/trusted Wi-Fi or hotspot.
echo The transport uses authenticated session tokens,
echo but it is NOT encrypted with TLS.
echo.

set "MAYRA_AGENT_HOST=0.0.0.0"
set "MAYRA_AGENT_ALLOW_LAN=1"

where py >nul 2>nul
if %errorlevel%==0 (
  py -3 mayra_agent.py
  goto :end
)
where python >nul 2>nul
if %errorlevel%==0 (
  python mayra_agent.py
  goto :end
)

echo Python 3 was not found.
echo Install Python 3 from https://www.python.org/downloads/windows/ and enable "Add python.exe to PATH".
pause
:end
if not "%errorlevel%"=="0" pause
endlocal
