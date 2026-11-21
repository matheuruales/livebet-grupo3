#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" -lt 3 || "$#" -gt 4 ]]; then
  printf 'Uso: deploy-release.sh URL_BASE64 SHA256 RELEASE_ID [ENV_FILE]\n' >&2
  exit 2
fi

ARTIFACT_URL="$(printf '%s' "$1" | base64 -d)"
EXPECTED_SHA="$2"
RELEASE_ID="$3"
LIVEBET_ROOT="/opt/livebet"
RELEASES_ROOT="$LIVEBET_ROOT/releases"
RELEASE_DIR="$RELEASES_ROOT/$RELEASE_ID"
ARCHIVE_PATH="/tmp/livebet-$RELEASE_ID.tgz"
ENV_FILE="${4:-}"

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

if [[ -n "$ENV_FILE" && -f "$ENV_FILE" ]]; then
  install -m 600 "$ENV_FILE" "$RELEASE_DIR/.env"
elif [[ -f "$LIVEBET_ROOT/current/.env" ]]; then
  install -m 600 "$LIVEBET_ROOT/current/.env" "$RELEASE_DIR/.env"
elif [[ -f "$LIVEBET_ROOT/.env" ]]; then
  install -m 600 "$LIVEBET_ROOT/.env" "$RELEASE_DIR/.env"
else
  printf 'No se pudo obtener la configuración privada de producción\n' >&2
  exit 1
fi

if [[ -L "$LIVEBET_ROOT/current" ]]; then
  PREVIOUS_RELEASE="$(readlink -f "$LIVEBET_ROOT/current")"
else
  PREVIOUS_RELEASE="$LIVEBET_ROOT"
fi

cd "$RELEASE_DIR"
docker compose config --quiet
docker compose build
docker compose up -d --remove-orphans

print_diagnostics() {
  printf '\nEstado de los contenedores de la versión que falló:\n' >&2
  docker compose ps --all >&2 || true
  printf '\nÚltimos registros de los servicios de aplicación:\n' >&2
  docker compose logs --no-color --tail=120 \
    dashboard-backend betting-api match-generator match-simulator \
    results-engine email-worker >&2 || true
}

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
  print_diagnostics
  cd "$PREVIOUS_RELEASE"
  docker compose up -d --build --remove-orphans
  exit 1
fi

ln -sfn "$RELEASE_DIR" "$LIVEBET_ROOT/current"
printf 'Versión %s desplegada correctamente\n' "$RELEASE_ID"
