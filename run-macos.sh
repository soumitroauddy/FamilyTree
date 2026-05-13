#!/usr/bin/env bash
#
# FamilyTree — one-shot macOS desktop dev runner
#
# Background (why this script exists):
# - Absorbs the iCloud symlink workaround previously in app/Makefile's run-macos target.
# - Provides a consistent entry point alongside run-ios.sh, run-android.sh, and run-web.sh.
#
# macOS + iCloud note:
# - If the repo lives under ~/Documents (iCloud), the iCloud file provider daemon stamps
#   every .app/.framework bundle directory with com.apple.FinderInfo and
#   com.apple.fileprovider.fpfs#P extended attributes that cannot be removed (the daemon
#   re-adds them immediately). codesign rejects these as "resource fork, Finder information,
#   or similar detritus not allowed".
#   Fix: symlink app/build/ → /tmp/flutter-family-tree-build (outside iCloud's reach).

set -euo pipefail

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

ensure_build_symlink() {
  if [[ ! -L "$BUILD_LINK" ]]; then
    rm -rf "$BUILD_LINK" 2>/dev/null || true
    mkdir -p "$BUILD_REAL"
    ln -s "$BUILD_REAL" "$BUILD_LINK"
    echo "Symlinked ${BUILD_LINK} -> ${BUILD_REAL} (outside iCloud)." >&2
  fi
}

main() {
  [[ "$(uname -s)" == "Darwin" ]] || die "macOS desktop target runs on macOS only."

  need_cmd flutter "Install Flutter from https://flutter.dev"

  [[ -d "$APP_DIR" ]] || die "Expected Flutter app at ${APP_DIR}"

  ensure_build_symlink

  echo "Running Flutter on macOS..." >&2
  cd "$APP_DIR"
  flutter pub get
  flutter run -d macos
}

main "$@"
