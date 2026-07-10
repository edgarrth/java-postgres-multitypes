#!/usr/bin/env bash
set -euo pipefail
docker run --rm \
  -v "$PWD":/workspace \
  -w /workspace \
  maven:3.9.11-eclipse-temurin-25 \
  mvn "$@"
