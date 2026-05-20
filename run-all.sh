#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "==> Starting backend stack..."
"$SCRIPT_DIR/start-local.sh"

echo ""
echo "==> Launching Flutter on default device..."
cd "$SCRIPT_DIR/app"
flutter run
