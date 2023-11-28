#!/usr/bin/env bash

# Start shared docker compose services (must be sourced, or DOCKER_COMPOSE_COMMAND won't be available)
. ../pam-docker-compose-shared/start-docker-compose.sh postgres

# Create databases
../pam-docker-compose-shared/create-database.sh "pam-eures-cv-eksport"

$DOCKER_COMPOSE_COMMAND up
