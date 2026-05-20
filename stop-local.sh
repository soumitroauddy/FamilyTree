#!/usr/bin/env bash
set -euo pipefail

echo "==> Stopping and removing Docker Compose stack (including volumes)..."
docker compose down -v

echo "    Done. All containers and volumes removed."
