#!/usr/bin/env bash
# Клиент одним java. Сначала lab-all.jar, иначе build/server.classpath.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FAT_JAR="$ROOT/build/libs/lab-all.jar"
CP_FILE="$ROOT/build/server.classpath"

JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"
JVM_SMALL=( -Xms32m -Xmx256m -XX:ReservedCodeCacheSize=32m -XX:InitialCodeCacheSize=8m -XX:+UseSerialGC )

if [[ -f "$FAT_JAR" ]]; then
  exec "$JAVA_BIN" "${JVM_SMALL[@]}" -cp "$FAT_JAR" org.example.client.ClientMain "$@"
fi

if [[ ! -f "$CP_FILE" ]]; then
  echo "Нет $FAT_JAR и нет $CP_FILE. Соберите JAR на ПК (gradlew shadowJar) или выполните writeServerClasspath на helios." >&2
  exit 1
fi

CP="$(cat "$CP_FILE")"
exec "$JAVA_BIN" "${JVM_SMALL[@]}" -cp "$CP" org.example.client.ClientMain "$@"
