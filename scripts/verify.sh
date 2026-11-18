#!/usr/bin/env sh
set -eu
curl -fsS http://localhost:8080/actuator/health
printf '\n'
curl -fsS http://localhost:8081/actuator/health
printf '\n'
docker compose ps
