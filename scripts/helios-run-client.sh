#!/usr/bin/env bash
# Клиент на helios (к локальному серверу: SERVER_HOST=localhost).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export SERVER_HOST="${SERVER_HOST:-localhost}"
export SERVER_PORT="${SERVER_PORT:-5234}"
exec bash "$ROOT/scripts/run-client-jvm.sh"
