#!/bin/zsh
set -e
cd "$(dirname "$0")"
export DB_USER="${DB_USER:-root}"
export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/ResearchPublicationDB?serverTimezone=UTC}"
if [[ -z "${DB_PASSWORD:-}" ]]; then
  read -s "DB_PASSWORD?Enter MySQL password for ${DB_USER}: "
  echo
  export DB_PASSWORD
fi
mvn clean compile exec:java
