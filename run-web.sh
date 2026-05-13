#!/usr/bin/env bash
#
# FamilyTree — one-shot Chrome (web) dev runner
#
# Background (why this script exists):
# - Provides a consistent entry point alongside run-ios.sh and run-android.sh.
# - Handles pub get and the iCloud build-symlink workaround automatically.
#
# macOS + iCloud note:
# - If the repo lives under ~/Documents (iCloud), `flutter build web` writes into
#   app/build/ which the iCloud daemon can corrupt with extended attributes.
#   Same symlink workaround as run-ios.sh: app/build/ → /tmp/flutter-family-tree-build.

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
  need_cmd flutter "Install Flutter from https://flutter.dev"

  [[ -d "$APP_DIR" ]] || die "Expected Flutter app at ${APP_DIR}"

  ensure_build_symlink

  echo "Running Flutter on Chrome..." >&2
  cd "$APP_DIR"
  flutter pub get
  flutter run -d chrome
}

main "$@"
