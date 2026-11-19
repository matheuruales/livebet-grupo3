#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" -ne 3 ]]; then
  printf 'Uso: deploy-release.sh URL_BASE64 SHA256 RELEASE_ID\n' >&2
  exit 2
fi

ARTIFACT_URL="$(printf '%s' "$1" | base64 -d)"
EXPECTED_SHA="$2"
RELEASE_ID="$3"
LIVEBET_ROOT="/opt/livebet"
RELEASES_ROOT="$LIVEBET_ROOT/releases"
RELEASE_DIR="$RELEASES_ROOT/$RELEASE_ID"
ARCHIVE_PATH="/tmp/livebet-$RELEASE_ID.tgz"

case "$RELEASE_ID" in
  *[!a-f0-9]*|'') printf 'Identificador de versión inválido\n' >&2; exit 2 ;;
esac
case "$EXPECTED_SHA" in
  *[!a-f0-9]*|'') printf 'Checksum inválido\n' >&2; exit 2 ;;
esac

umask 077
mkdir -p "$RELEASES_ROOT"
if [[ -e "$RELEASE_DIR" ]]; then
  printf 'La versión %s ya existe; no se sobrescribirá\n' "$RELEASE_ID" >&2
  exit 1
fi

curl --fail --location --retry 4 --retry-all-errors --connect-timeout 15 \
  "$ARTIFACT_URL" --output "$ARCHIVE_PATH"
printf '%s  %s\n' "$EXPECTED_SHA" "$ARCHIVE_PATH" | sha256sum --check --status

if tar -tzf "$ARCHIVE_PATH" | grep -Eq '(^/|(^|/)\.\.(/|$))'; then
  printf 'El paquete contiene rutas no permitidas\n' >&2
  exit 1
fi

mkdir "$RELEASE_DIR"
tar -xzf "$ARCHIVE_PATH" -C "$RELEASE_DIR"

if [[ -f "$LIVEBET_ROOT/current/.env" ]]; then
  ENV_SOURCE="$LIVEBET_ROOT/current/.env"
elif [[ -f "$LIVEBET_ROOT/.env" ]]; then
  ENV_SOURCE="$LIVEBET_ROOT/.env"
else
  printf 'No existe la configuración privada /opt/livebet/.env\n' >&2
  exit 1
fi
install -m 600 "$ENV_SOURCE" "$RELEASE_DIR/.env"

if [[ -L "$LIVEBET_ROOT/current" ]]; then
  PREVIOUS_RELEASE="$(readlink -f "$LIVEBET_ROOT/current")"
else
  PREVIOUS_RELEASE="$LIVEBET_ROOT"
fi

cd "$RELEASE_DIR"
docker compose config --quiet
docker compose build
docker compose up -d --remove-orphans

healthy=false
attempts=0
while (( attempts < 36 )); do
  ((attempts += 1))
  if curl --fail --silent http://localhost:8080/actuator/health >/dev/null \
      && curl --fail --silent http://localhost:8081/actuator/health >/dev/null; then
    healthy=true
    break
  fi
  sleep 5
done

if [[ "$healthy" != "true" ]]; then
  printf 'La nueva versión no superó las comprobaciones; iniciando reversión\n' >&2
  cd "$PREVIOUS_RELEASE"
  docker compose up -d --build --remove-orphans
  exit 1
fi

ln -sfn "$RELEASE_DIR" "$LIVEBET_ROOT/current"
printf 'Versión %s desplegada correctamente\n' "$RELEASE_ID"
