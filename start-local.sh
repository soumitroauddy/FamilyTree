#!/usr/bin/env bash
set -euo pipefail

echo "==> Building and starting Docker Compose stack..."
docker compose up --build -d

echo "==> Waiting for Postgres to be healthy..."
until docker compose exec -T postgres pg_isready -U familytree > /dev/null 2>&1; do
  sleep 1
done
echo "    Postgres is ready."

echo "==> Waiting for MinIO to be healthy..."
until curl -sf http://localhost:9000/minio/health/live > /dev/null 2>&1; do
  sleep 1
done
echo "    MinIO is ready."

echo "==> Waiting for user-management-service to start..."
until curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; do
  sleep 2
done
echo "    Backend is ready."

echo ""
echo "========================================="
echo "  FamilyTree stack is running"
echo "========================================="
echo "  Backend API:     http://localhost:8080"
echo "  Postgres:        localhost:5433  (db: familytree, user: familytree)"
echo "  MinIO API:       http://localhost:9000"
echo "  MinIO Console:   http://localhost:9001  (user: familytree / familytree)"
echo "========================================="
echo ""
echo "  Local dev auth: SUPABASE_JWT_SECRET is not set, so JWT validation"
echo "  is disabled. Use the X-Dev-User-Id header to identify yourself:"
echo "    curl -H 'X-Dev-User-Id: <userId>' http://localhost:8080/api/..."
echo "========================================="
