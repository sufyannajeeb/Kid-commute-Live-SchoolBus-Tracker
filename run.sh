#!/usr/bin/env bash
#
# One-command run for kid Commute.
#
#   ./run.sh              start emulator if needed, build, install, launch app
#   ./run.sh emulator     only boot the emulator
#   ./run.sh install      build + install, don't launch
#   ./run.sh log          show the app's logcat (LocationService, Volley, ...)
#
# Optional: AVD="My_Avd" ./run.sh
#
set -e
cd "$(dirname "$0")"

# ------------------------------------------------------------------ find SDK
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK" ] && [ -f local.properties ]; then
  SDK="$(grep -E '^sdk\.dir=' local.properties | head -1 | cut -d= -f2-)"
fi
[ -n "$SDK" ] || SDK="$HOME/AppData/Local/Android/Sdk"
# local.properties escapes as  C\:\\Users\\...  -> normalise to C:/Users/...
SDK="$(printf '%s' "$SDK" | sed -e 's/\\:/:/g' -e 's/\\/\//g')"

ADB="$SDK/platform-tools/adb"
EMU="$SDK/emulator/emulator"
[ -x "$ADB" ] || ADB="$ADB.exe"
[ -x "$EMU" ] || EMU="$EMU.exe"

if [ ! -x "$ADB" ]; then
  echo "ERROR: adb not found under $SDK"
  echo "       Set ANDROID_HOME or fix sdk.dir in local.properties"
  exit 1
fi

device_count() {
  "$ADB" devices 2>/dev/null | awk 'NR>1 && $2=="device" {c++} END {print c+0}'
}

boot_emulator() {
  if [ "$(device_count)" -gt 0 ]; then
    echo "==> Emulator/device already connected"
    return
  fi
  AVD_NAME="${AVD:-$("$EMU" -list-avds 2>/dev/null | grep -v '^INFO' | grep -v '^[[:space:]]*$' | head -n1)}"
  if [ -z "$AVD_NAME" ]; then
    echo "ERROR: no AVD found. Create one: Android Studio > Tools > Device Manager"
    exit 1
  fi
  echo "==> Starting emulator: $AVD_NAME"
  "$EMU" -avd "$AVD_NAME" >/dev/null 2>&1 &
  sleep 5
}

wait_boot() {
  echo "==> Waiting for device to finish booting..."
  "$ADB" wait-for-device
  until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    sleep 2
  done
  echo "==> Device ready"
}

case "${1:-run}" in
  emulator)
    boot_emulator
    wait_boot
    ;;

  install)
    boot_emulator
    wait_boot
    echo "==> Building + installing (debug)..."
    ./gradlew installDebug
    ;;

  log)
    "$ADB" logcat -c 2>/dev/null || true
    "$ADB" logcat -v time | grep -E --line-buffered "LocationService|Volley|AndroidRuntime|kidcommute"
    ;;

  run)
    boot_emulator
    wait_boot
    echo "==> Building + installing (debug)..."
    ./gradlew installDebug
    echo "==> Launching kid Commute..."
    "$ADB" shell am start -n com.example.kidcommute/.MainActivity
    echo
    echo "Done. First launch: type your server IP on the first screen, press the button."
    echo "Tip: ./run.sh log   -> watch LocationService uploads in real time."
    ;;

  *)
    echo "usage: ./run.sh [run|emulator|install|log]"
    exit 1
    ;;
esac
