#!/usr/bin/env bash
#
# FamilyTree — one-shot Android dev runner
#
# Background (why this script exists):
# - `flutter run -d <AVD id>` does NOT start an emulator. `-d` only matches devices that are
#   already visible to Flutter (`flutter devices`), which means the emulator must be booted.
# - `flutter emulators` lists AVD *definitions*; those ids/names are not valid `-d` targets
#   until the emulator process is running. Flutter/adb then shows them as `emulator-5554`, etc.
# - Cold boots often take 30–90+ seconds; we poll `sys.boot_completed` instead of a fixed sleep.
#
# macOS + iCloud note:
# - If the repo lives under ~/Documents (iCloud), prefer `make run-macos` for desktop builds
#   (see README). Android emulator builds are usually fine; use Makefile symlink flow if
#   codesign errors appear on Android builds too.

set -euo pipefail

AVD_NAME="${AVD_NAME:-Samsung_Galaxy_S23_Ultra}"
BOOT_WAIT_SEC="${BOOT_WAIT_SEC:-60}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="${SCRIPT_DIR}/app"

# adb lives in Android SDK platform-tools, which Flutter knows about internally but
# which is often not on the user's shell PATH. Discover it from standard locations.
_sdk_root="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-${HOME}/Library/Android/sdk}}"
_platform_tools="${_sdk_root}/platform-tools"
if [[ -d "$_platform_tools" ]] && [[ ":${PATH}:" != *":${_platform_tools}:"* ]]; then
  export PATH="${_platform_tools}:${PATH}"
fi
unset _sdk_root _platform_tools

die() {
  echo "error: $*" >&2
  exit 1
}

need_cmd() {
  local cmd="$1" hint="${2:-}"
  command -v "$cmd" >/dev/null 2>&1 || die "'${cmd}' not found on PATH.${hint:+ ${hint}}"
}

# Lines like: emulator-5554<TAB>device
list_emulator_serials() {
  adb devices 2>/dev/null | awk '/^emulator-[0-9]+[[:space:]]+device$/ { print $1 }'
}

boot_completed_for() {
  local serial="$1"
  local v
  v="$(adb -s "$serial" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  [[ "$v" == "1" ]]
}

warn_apple_silicon_avd_arch() {
  [[ "$(uname -s)" == "Darwin" ]] || return 0
  [[ "$(uname -m)" == "arm64" ]] || return 0

  local ini="${HOME}/.android/avd/${AVD_NAME}.avd/config.ini"
  [[ -f "$ini" ]] || return 0

  # Typical entries: abi.type=x86_64 / hw.cpu.arch=x86_64 — both fail on Apple Silicon without translation.
  if grep -Eiq '^(abi\.type|hw\.cpu\.arch)=.*x86' "$ini" 2>/dev/null; then
    echo "warning: AVD '${AVD_NAME}' looks x86-based. On Apple Silicon use an ARM 64 v8a system image" >&2
    echo "         or recreate the AVD in Android Studio → Device Manager → system image \"arm64-v8a\"." >&2
  fi
}

wait_for_emulator_boot() {
  local serial="$1"
  local deadline=$((SECONDS + BOOT_WAIT_SEC))
  echo "Waiting for ${serial} to finish booting (up to ${BOOT_WAIT_SEC}s)..." >&2
  while (( SECONDS < deadline )); do
    if boot_completed_for "$serial"; then
      echo "Emulator ${serial} is ready." >&2
      return 0
    fi
    printf '.' >&2
    sleep 2
  done
  echo >&2
  die "Timed out after ${BOOT_WAIT_SEC}s waiting for boot on ${serial}. Increase BOOT_WAIT_SEC or fix the AVD."
}

wait_for_any_emulator_online_after_launch() {
  local deadline=$((SECONDS + BOOT_WAIT_SEC))
  echo "Waiting for emulator to appear in adb (up to ${BOOT_WAIT_SEC}s)..." >&2
  while (( SECONDS < deadline )); do
    local s
    s="$(list_emulator_serials | head -n1)"
    if [[ -n "$s" ]]; then
      echo "Detected ${s}." >&2
      echo "$s"
      return 0
    fi
    printf '.' >&2
    sleep 2
  done
  echo >&2
  die "No emulator showed up in adb after launch. Try: emulator -avd '${AVD_NAME}' -verbose (see stderr)." \
      " On Apple Silicon, ensure the AVD uses an arm64-v8a image."
}

main() {
  need_cmd flutter "Install Flutter from https://flutter.dev"
  need_cmd adb "Install Android Studio (it includes platform-tools) or set ANDROID_SDK_ROOT."

  [[ -d "$APP_DIR" ]] || die "Expected Flutter app at ${APP_DIR}"

  warn_apple_silicon_avd_arch

  local serial=""
  serial="$(list_emulator_serials | head -n1 || true)"

  if [[ -z "$serial" ]]; then
    echo "No Android emulator running; launching AVD '${AVD_NAME}'..."
    # Flutter may exit 0 even if the emulator binary fails immediately — verify via adb.
    flutter emulators --launch "$AVD_NAME" || true
    serial="$(wait_for_any_emulator_online_after_launch)"
  fi

  wait_for_emulator_boot "$serial"

  echo "Running Flutter on device '${serial}'..." >&2
  cd "$APP_DIR"
  flutter pub get
  flutter run -d "$serial"
}

main "$@"
