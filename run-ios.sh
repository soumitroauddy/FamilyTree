#!/usr/bin/env bash
#
# FamilyTree — one-shot iOS Simulator dev runner
#
# Background (why this script exists):
# - `flutter run -d <device>` only targets devices Flutter already sees (`flutter devices`).
# - The iOS Simulator must be booted before Flutter can deploy; cold boots can take a while.
# - We poll `xcrun simctl list devices --json` until the chosen simulator reports state Booted.
#
# macOS + iCloud note:
# - If the repo lives under ~/Documents (iCloud), Xcode codesign can fail on synced bundles.
#   Same workaround as `make run-macos`: symlink `app/build/` → `/tmp/flutter-family-tree-build`.

set -euo pipefail

SIMULATOR_NAME="${SIMULATOR_NAME:-iPhone 17}"
BOOT_WAIT_SEC="${BOOT_WAIT_SEC:-60}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="${SCRIPT_DIR}/app"

BUILD_LINK="${APP_DIR}/build"
BUILD_REAL="${BUILD_REAL:-/tmp/flutter-family-tree-build}"

die() {
  echo "error: $*" >&2
  exit 1
}

need_cmd() {
  local cmd="$1" hint="${2:-}"
  command -v "$cmd" >/dev/null 2>&1 || die "'${cmd}' not found on PATH.${hint:+ ${hint}}"
}

# Parse `xcrun simctl list devices available --json` and print UDID for first matching name.
# Prints "<UDID> <name>" so callers can read both fields.
find_simulator_udid() {
  local name="$1"
  need_cmd python3 "Install Xcode Command Line Tools: xcode-select --install"
  xcrun simctl list devices available --json 2>/dev/null \
    | SIMULATOR_QUERY_NAME="$name" python3 -c '
import json, os, sys

name = os.environ["SIMULATOR_QUERY_NAME"]
try:
    data = json.load(sys.stdin)
except json.JSONDecodeError as e:
    print("error: invalid JSON from simctl:", e, file=sys.stderr)
    sys.exit(1)

for _runtime, devices in data.get("devices", {}).items():
    for d in devices:
        if d.get("name") != name:
            continue
        if d.get("isAvailable") is False:
            continue
        print(d["udid"], d["name"])
        sys.exit(0)

sys.exit(1)
'
}

# Print "<UDID> <name>" for the first available iPhone simulator (any model).
find_any_iphone_udid() {
  need_cmd python3 "Install Xcode Command Line Tools: xcode-select --install"
  xcrun simctl list devices available --json 2>/dev/null \
    | python3 -c '
import json, sys

try:
    data = json.load(sys.stdin)
except json.JSONDecodeError as e:
    print("error: invalid JSON from simctl:", e, file=sys.stderr)
    sys.exit(1)

for _runtime, devices in data.get("devices", {}).items():
    for d in devices:
        if d.get("isAvailable") is False:
            continue
        if not d.get("name", "").startswith("iPhone"):
            continue
        print(d["udid"], d["name"])
        sys.exit(0)

sys.exit(1)
'
}

simulator_state_for_udid() {
  local udid="$1"
  xcrun simctl list devices --json 2>/dev/null \
    | SIMULATOR_UDID="$udid" python3 -c '
import json, os, sys

udid = os.environ["SIMULATOR_UDID"]
try:
    data = json.load(sys.stdin)
except json.JSONDecodeError:
    sys.exit(1)

for _runtime, devices in data.get("devices", {}).items():
    for d in devices:
        if d.get("udid") == udid:
            print(d.get("state", ""))
            sys.exit(0)

sys.exit(1)
'
}

booted_simulator_udid() {
  # First Booted device (any); optional helper for debugging — main uses a fixed UDID.
  xcrun simctl list devices --json 2>/dev/null \
    | python3 -c '
import json, sys

try:
    data = json.load(sys.stdin)
except json.JSONDecodeError:
    sys.exit(1)

for _runtime, devices in data.get("devices", {}).items():
    for d in devices:
        if d.get("state") == "Booted":
            print(d["udid"])
            sys.exit(0)

sys.exit(1)
'
}

simulator_is_booted() {
  local udid="$1"
  [[ "$(simulator_state_for_udid "$udid")" == "Booted" ]]
}

wait_for_simulator_boot() {
  local udid="$1"
  local deadline=$((SECONDS + BOOT_WAIT_SEC))
  echo "Waiting for simulator ${udid} to reach Booted (up to ${BOOT_WAIT_SEC}s)..." >&2
  while (( SECONDS < deadline )); do
    if simulator_is_booted "$udid"; then
      echo "Simulator is Booted." >&2
      return 0
    fi
    printf '.' >&2
    sleep 2
  done
  echo >&2
  die "Timed out after ${BOOT_WAIT_SEC}s waiting for Booted on ${udid}. Increase BOOT_WAIT_SEC or fix the simulator."
}

ensure_build_symlink() {
  if [[ ! -L "$BUILD_LINK" ]]; then
    rm -rf "$BUILD_LINK" 2>/dev/null || true
    mkdir -p "$BUILD_REAL"
    ln -s "$BUILD_REAL" "$BUILD_LINK"
    echo "Symlinked ${BUILD_LINK} -> ${BUILD_REAL} (outside iCloud)." >&2
  fi
}

main() {
  [[ "$(uname -s)" == "Darwin" ]] || die "iOS Simulator runs on macOS only."

  need_cmd flutter "Install Flutter from https://flutter.dev"
  need_cmd xcrun "Install Xcode (or Command Line Tools) so xcrun is available."

  [[ -d "$APP_DIR" ]] || die "Expected Flutter app at ${APP_DIR}"

  local udid="" resolved_name=""
  local found
  if found="$(find_simulator_udid "$SIMULATOR_NAME" 2>/dev/null)"; then
    udid="${found%% *}"
    resolved_name="${found#* }"
  else
    echo "warning: no simulator named '${SIMULATOR_NAME}'; falling back to first available iPhone." >&2
    found="$(find_any_iphone_udid)" \
      || die "No available iPhone simulators found. Try: xcrun simctl list devices available" \
             " (set SIMULATOR_NAME to an exact name from that list)."
    udid="${found%% *}"
    resolved_name="${found#* }"
    echo "Using simulator '${resolved_name}' (${udid})." >&2
  fi

  if simulator_is_booted "$udid"; then
    echo "Simulator '${resolved_name}' (${udid}) is already Booted." >&2
  else
    echo "Booting simulator '${resolved_name}' (${udid})..." >&2
    xcrun simctl boot "$udid" || die "simctl boot failed for ${udid}"
    open -a Simulator || true
  fi

  wait_for_simulator_boot "$udid"

  ensure_build_symlink

  echo "Running Flutter on simulator '${udid}'..." >&2
  cd "$APP_DIR"
  flutter pub get
  flutter run -d "$udid"
}

main "$@"
