@echo off
setlocal
cd /d "%~dp0\.."
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
