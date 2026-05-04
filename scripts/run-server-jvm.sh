#!/usr/bin/env bash
# Сервер одним java. Сначала lab-all.jar (если есть), иначе classpath из writeServerClasspath.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FAT_JAR="$ROOT/build/libs/lab-all.jar"
CP_FILE="$ROOT/build/server.classpath"

JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"
JVM_SMALL=( -Xms32m -Xmx256m -XX:ReservedCodeCacheSize=32m -XX:InitialCodeCacheSize=8m -XX:+UseSerialGC )

if [[ -f "$FAT_JAR" ]]; then
  exec "$JAVA_BIN" "${JVM_SMALL[@]}" -jar "$FAT_JAR" "$@"
fi

if [[ ! -f "$CP_FILE" ]]; then
  echo "Нет $FAT_JAR и нет $CP_FILE." >&2
  echo "На ПК с рабочим Gradle: bash gradlew shadowJar && scp build/libs/lab-all.jar helios:~/..." >&2
  echo "Или на helios: bash gradlew --no-daemon --max-workers=1 writeServerClasspath" >&2
  exit 1
fi

CP="$(cat "$CP_FILE")"
exec "$JAVA_BIN" "${JVM_SMALL[@]}" -cp "$CP" org.example.server.ServerMain "$@"
