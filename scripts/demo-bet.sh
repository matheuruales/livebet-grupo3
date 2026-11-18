#!/usr/bin/env sh
set -eu
MATCH_ID="${1:?Uso: ./scripts/demo-bet.sh MATCH_ID TEAM [USER_ID] [AMOUNT]}"
TEAM="${2:?Debe indicar el equipo}"
USER_ID="${3:-user-demo}"
AMOUNT="${4:-10000}"
curl -fsS -X POST http://localhost:8081/api/bets -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$USER_ID\",\"matchId\":\"$MATCH_ID\",\"betOn\":\"$TEAM\",\"amount\":$AMOUNT}"
printf '\n'
