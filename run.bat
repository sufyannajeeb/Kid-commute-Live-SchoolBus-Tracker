@echo off
rem One-command run for kid Commute (Windows, double-clickable).
rem Builds the app, starts the emulator if needed, installs and launches it.
setlocal enabledelayedexpansion
cd /d "%~dp0"

rem ---------------------------------------------------------------- find SDK
set "SDK=%ANDROID_HOME%"
if "%SDK%"=="" set "SDK=%ANDROID_SDK_ROOT%"
if "%SDK%"=="" (
  for /f "usebackq tokens=1,* delims==" %%a in ("local.properties") do (
    if "%%a"=="sdk.dir" set "SDK=%%b"
  )
)
if "%SDK%"=="" set "SDK=%LOCALAPPDATA%\Android\Sdk"
rem local.properties escapes as  C\:\\Users\\...  -> normalise to C:\Users\...
set "SDK=%SDK:\:=:%"
set "SDK=%SDK:\\=\%"

set "ADB=%SDK%\platform-tools\adb.exe"
set "EMU=%SDK%\emulator\emulator.exe"
if not exist "%ADB%" (
  echo ERROR: adb not found under %SDK%
  echo        Set ANDROID_HOME or fix sdk.dir in local.properties
  exit /b 1
)

rem ------------------------------------------- start emulator if none running
set "HASDEV="
for /f "skip=1 tokens=1,2" %%a in ('"%ADB%" devices') do (
  if "%%b"=="device" set "HASDEV=1"
)
if defined HASDEV (
  echo ==^> Emulator/device already connected
) else (
  set "AVD="
  for /f "usebackq delims=" %%n in (`"%EMU%" -list-avds 2^>nul`) do (
    if not defined AVD set "AVD=%%n"
  )
  if "!AVD!"=="" (
    echo ERROR: no AVD found. Create one: Android Studio ^> Tools ^> Device Manager
    exit /b 1
  )
  echo ==^> Starting emulator: !AVD!
  start "" "%EMU%" -avd "!AVD!"
  timeout /t 5 /nobreak >nul
)

rem ------------------------------------------------------- wait for boot
echo ==^> Waiting for device to finish booting...
"%ADB%" wait-for-device
set "BOOT="
:waitboot
set "BOOT="
for /f "delims=" %%b in ('"%ADB%" shell getprop sys.boot_completed 2^>nul') do set "BOOT=%%b"
if not "%BOOT:~0,1%"=="1" (
  timeout /t 2 /nobreak >nul
  goto waitboot
)
echo ==^> Device ready

rem ------------------------------------------------ build + install + launch
echo ==^> Building + installing (debug)...
call "%~dp0gradlew.bat" installDebug
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)

echo ==^> Launching kid Commute...
"%ADB%" shell am start -n com.example.kidcommute/.MainActivity

echo.
echo Done. First launch: type your server IP on the first screen, press the button.
echo Tip: watch live uploads with   adb logcat -s LocationService
endlocal
