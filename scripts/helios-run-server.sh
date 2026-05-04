#!/usr/bin/env bash
# Helios: по умолчанию только java -jar (без Gradle). Gradle часто не может форкнуть демон на этом хосте.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

export PG_HOST="${PG_HOST:-pg}"
export PG_PORT="${PG_PORT:-5432}"
export PG_DATABASE="${PG_DATABASE:-studs}"
export SERVER_PORT="${SERVER_PORT:-5555}"
export AUTO_CREATE_DATABASE="${AUTO_CREATE_DATABASE:-false}"

if [[ -z "${PG_USER:-}" ]]; then
  export PG_USER="${USER:?задайте USER или PG_USER}"
fi
if [[ -z "${PG_PASSWORD:-}" ]]; then
  echo "Задайте пароль к PostgreSQL, лучше в одинарных кавычках: export PG_PASSWORD='...'" >&2
  exit 1
fi

PORT="${1:-$SERVER_PORT}"
FAT_JAR="$ROOT/build/libs/lab-all.jar"
JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"
JVM_SMALL=( -Xms32m -Xmx256m -XX:ReservedCodeCacheSize=32m -XX:InitialCodeCacheSize=8m -XX:+UseSerialGC )

if [[ -f "$FAT_JAR" ]]; then
  exec "$JAVA_BIN" "${JVM_SMALL[@]}" -jar "$FAT_JAR" "$PORT"
fi

if [[ "${USE_GRADLE:-0}" != "1" ]]; then
  echo "Нет файла: $FAT_JAR" >&2
  echo "На helios Gradle часто падает с «Gradle build daemon». Соберите JAR на своём ПК:" >&2
  echo "  bash gradlew shadowJar" >&2
  echo "  mkdir -p build/libs && scp build/libs/lab-all.jar ${USER}@helios:~/labuba7/prog_lab7/build/libs/" >&2
  echo "Повторите: bash scripts/helios-run-server.sh $PORT" >&2
  echo "(Опционально попробовать Gradle на helios: USE_GRADLE=1 bash scripts/helios-run-server.sh $PORT)" >&2
  exit 1
fi

export GRADLE_OPTS="${GRADLE_OPTS:--Xmx96m -Xms32m -XX:ReservedCodeCacheSize=24m -XX:InitialCodeCacheSize=8m -XX:+UseSerialGC}"
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:--Xmx96m -XX:ReservedCodeCacheSize=24m -XX:InitialCodeCacheSize=8m -XX:+UseSerialGC}"
unset _JAVA_OPTIONS JDK_JAVA_OPTIONS OPENJDK_JAVA_OPTIONS 2>/dev/null || true

bash "$ROOT/gradlew" --no-daemon --max-workers=1 writeServerClasspath
exec bash "$ROOT/scripts/run-server-jvm.sh" "$PORT"
